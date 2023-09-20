package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;
import static se.sundsvall.disturbance.api.model.Status.OPEN;
import static se.sundsvall.disturbance.api.model.Status.PLANNED;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.DisturbanceFeedbackMapper.toDisturbanceFeedbackEntity;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbance;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbanceEntity;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbances;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toMergedDisturbanceEntity;
import static se.sundsvall.disturbance.service.util.MappingUtils.getAddedAffectedEntities;
import static se.sundsvall.disturbance.service.util.MappingUtils.getRemovedAffectedEntities;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import jakarta.transaction.Transactional;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.service.message.SendMessageLogic;

@Service
public class DisturbanceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisturbanceService.class);

	@Autowired
	private DisturbanceRepository disturbanceRepository;

	@Autowired
	private FeedbackRepository feedbackRepository;

	@Autowired
	private DisturbanceFeedbackRepository disturbanceFeedbackRepository;

	@Autowired
	private SendMessageLogic sendMessageLogic;

	@Transactional
	public Disturbance findByCategoryAndDisturbanceId(final Category category, final String disturbanceId) {
		return toDisturbance(disturbanceRepository.findByCategoryAndDisturbanceId(category, disturbanceId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId))));
	}

	@Transactional
	public List<Disturbance> findByPartyIdAndCategoryAndStatus(final String partyId, final List<Category> categoryFilter, final List<se.sundsvall.disturbance.api.model.Status> statusFilter) {
		return toDisturbances(disturbanceRepository.findByAffectedEntitiesPartyIdAndCategoryInAndStatusIn(partyId, categoryFilter, statusFilter));
	}

	@Transactional
	public List<Disturbance> findByStatusAndCategory(final List<se.sundsvall.disturbance.api.model.Status> statusFilter, final List<Category> categoryFilter) {
		return toDisturbances(disturbanceRepository.findByStatusAndCategory(statusFilter, categoryFilter));
	}

	@Transactional
	public Disturbance createDisturbance(final DisturbanceCreateRequest disturbanceCreateRequest) {

		// Check if disturbance already exists.
		if (disturbanceRepository.findByCategoryAndDisturbanceId(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId()).isPresent()) {
			throw Problem.valueOf(Status.CONFLICT, format(ERROR_DISTURBANCE_ALREADY_EXISTS, disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId()));
		}

		// Persist disturbance entity.
		final var persistedDisturbanceEntity = disturbanceRepository.save(toDisturbanceEntity(disturbanceCreateRequest));

		if (isNotEmpty(persistedDisturbanceEntity.getAffectedEntities()) && !hasStatusClosed(persistedDisturbanceEntity)) {

			// Add disturbance feedback, if not yet created.
			addDisturbanceFeedbackForAffectedEntities(persistedDisturbanceEntity.getAffectedEntities());

			// Send message to the created disturbance feedback recipients.
			if (hasStatusOpen(persistedDisturbanceEntity)) {
				sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(persistedDisturbanceEntity);
			}
		}

		return toDisturbance(persistedDisturbanceEntity);
	}

	@Transactional
	public Disturbance updateDisturbance(final Category category, final String disturbanceId, final DisturbanceUpdateRequest disturbanceUpdateRequest) {

		// Get existing disturbance entity.
		final var existingDisturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceId(category, disturbanceId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId)));

		// No updates allowed on closed disturbance.
		if (hasStatusClosed(existingDisturbanceEntity)) {
			throw Problem.valueOf(Status.CONFLICT, format(ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED, category, disturbanceId));
		}

		// Get new (incoming) disturbance entity.
		final var incomingDisturbanceEntity = toDisturbanceEntity(category, disturbanceId, disturbanceUpdateRequest);

		// Get added and removed affecteds.
		final var removedAffecteds = getRemovedAffectedEntities(existingDisturbanceEntity, incomingDisturbanceEntity);
		final var addedAffecteds = getAddedAffectedEntities(existingDisturbanceEntity, incomingDisturbanceEntity);

		// Add disturbance feedback for new affecteds, if not yet created.
		addDisturbanceFeedbackForAffectedEntities(addedAffecteds);

		// Send "close" message if status is changed to CLOSED.
		if (isChangedToStatusClosed(existingDisturbanceEntity, incomingDisturbanceEntity)) {
			LOGGER.info("Disturbance status was changed to CLOSED: '{}'. Sending close messages.", incomingDisturbanceEntity);
			sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(existingDisturbanceEntity);

			// Return since there is no need to continue after this.
			return toDisturbance(disturbanceRepository.save(toMergedDisturbanceEntity(existingDisturbanceEntity, incomingDisturbanceEntity)));
		}
		// Send "close" message to affecteds that was removed from the disturbance (but not if status is PLANNED).
		if (isNotEmpty(removedAffecteds) && !hasStatusPlanned(existingDisturbanceEntity)) {
			LOGGER.info("Removed affecteds was discovered: '{}'. Sending close messages.", removedAffecteds);
			sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(existingDisturbanceEntity, removedAffecteds);
		}
		// Send "create" message to affecteds that was added to the disturbance (but not if status is PLANNED).
		if (isNotEmpty(addedAffecteds) && !hasStatusPlanned(existingDisturbanceEntity)) {
			LOGGER.info("Added affecteds was discovered: '{}'. Sending create messages.", addedAffecteds);
			sendMessageLogic.sendCreateMessageToProvidedApplicableAffecteds(existingDisturbanceEntity, addedAffecteds);
		}

		/**
		 * Perform attribute value checks. These checks must be performed before the toMergedDisturbanceEntity-call, since the
		 * old disturbance entity will be modified with the new values.
		 */
		final var disturbanceContentIsChanged = contentIsChanged(existingDisturbanceEntity, incomingDisturbanceEntity);
		final var disturbanceStatusIsChangedFromPlannedToOpen = hasStatusPlanned(existingDisturbanceEntity) && hasStatusOpen(incomingDisturbanceEntity);

		// Merge new and old entities.
		final var mergedDisturbanceEntity = toMergedDisturbanceEntity(existingDisturbanceEntity, incomingDisturbanceEntity);

		// Send "create" message to all affecteds, if the disturbance status is changed from PLANNED TO OPEN.
		if (disturbanceStatusIsChangedFromPlannedToOpen) {
			LOGGER.info("Disturbance status changed from PLANNED to OPEN: '{}'. Sending create messages.", mergedDisturbanceEntity);
			sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(mergedDisturbanceEntity);
		}
		// Send "update" message to all affecteds, if the disturbance content is updated (but not for status PLANNED).
		else if (disturbanceContentIsChanged && !hasStatusPlanned(mergedDisturbanceEntity)) {
			LOGGER.info("Disturbance status changed from PLANNED to OPEN: '{}'. Sending update messages.", mergedDisturbanceEntity);
			sendMessageLogic.sendUpdateMessage(mergedDisturbanceEntity);
		}

		return toDisturbance(disturbanceRepository.save(mergedDisturbanceEntity));
	}

	@Transactional
	public void deleteDisturbance(final Category category, final String disturbanceId) {

		final var disturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceId(category, disturbanceId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId)));

		// Delete all related disturbanceFeedback-entities.
		disturbanceFeedbackRepository.deleteByCategoryAndDisturbanceId(category, disturbanceId);

		// "Soft delete" disturbance entity.
		disturbanceEntity.setDeleted(true);
		disturbanceRepository.save(disturbanceEntity);
	}

	private boolean isChangedToStatusClosed(final DisturbanceEntity oldDisturbanceEntity, final DisturbanceEntity newDisturbanceEntity) {
		return !hasStatusClosed(oldDisturbanceEntity) && hasStatusClosed(newDisturbanceEntity);
	}

	protected static boolean hasStatusClosed(final DisturbanceEntity disturbanceEntity) {
		return CLOSED.toString().equals(disturbanceEntity.getStatus());
	}

	protected static boolean hasStatusOpen(final DisturbanceEntity disturbanceEntity) {
		return OPEN.toString().equals(disturbanceEntity.getStatus());
	}

	protected static boolean hasStatusPlanned(final DisturbanceEntity disturbanceEntity) {
		return PLANNED.toString().equals(disturbanceEntity.getStatus());
	}

	/**
	 * Check if parameters in the newEntity are not null (i.e. they are set in the PATCH request). If set (i.e. not null):
	 * Check if the values differs from the existing ones that are stored in the oldEntity.
	 *
	 * The attributes that are checked are: description, title, plannedStartDate and plannedStopDate (and also if status is
	 * changed from PLANNED to OPEN).
	 *
	 * @param  oldEntity the old entity
	 * @param  newEntity the new (changed) entity
	 * @return           true if the content is changed, false otherwise.
	 */
	private boolean contentIsChanged(final DisturbanceEntity oldEntity, final DisturbanceEntity newEntity) {
		final var contentIsChanged = (nonNull(newEntity.getDescription()) && !equalsIgnoreCase(oldEntity.getDescription(), newEntity.getDescription())) ||
			(nonNull(newEntity.getTitle()) && !equalsIgnoreCase(oldEntity.getTitle(), newEntity.getTitle())) ||
			(nonNull(newEntity.getPlannedStartDate()) && !equalsIgnoreCase(String.valueOf(oldEntity.getPlannedStartDate()), String.valueOf(newEntity.getPlannedStartDate()))) ||
			(nonNull(newEntity.getPlannedStopDate()) && !equalsIgnoreCase(String.valueOf(oldEntity.getPlannedStopDate()), String.valueOf(newEntity.getPlannedStopDate()))) ||
			(hasStatusOpen(newEntity) && hasStatusPlanned(oldEntity));

		if (contentIsChanged) {
			LOGGER.debug("Disturbance content update was discovered. Old:'{}' New:'{}'", oldEntity, newEntity);
		}

		return contentIsChanged;
	}

	/**
	 * Adds a disturbanceFeedback for the provided affectedEntities if these conditions are met:
	 *
	 * <pre>
	 * - The person/organization doesn't already have a disturbanceFeedback for this disturbance.
	 * - A feedback (i.e. permanent subscription option) exists for this person/organization.
	 * </pre>
	 *
	 * @param affectedEntities the affectedEntites to create disturbanceFeedback for.
	 */
	private void addDisturbanceFeedbackForAffectedEntities(final List<AffectedEntity> affectedEntities) {
		Optional.ofNullable(affectedEntities).orElse(emptyList()).stream()
			// Only process affectedEntities with no existing disturbanceFeedback-entry in DB.
			.filter(affected -> disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyId(
				Category.valueOf(affected.getDisturbanceEntity().getCategory()),
				affected.getDisturbanceEntity().getDisturbanceId(),
				affected.getPartyId()).isEmpty())
			// Only process affectedEntities with an existing feedback-entry in DB.
			.filter(affected -> feedbackRepository.findByPartyId(affected.getPartyId()).isPresent())
			.forEach(affected -> disturbanceFeedbackRepository.save(toDisturbanceFeedbackEntity(
				Category.valueOf(affected.getDisturbanceEntity().getCategory()),
				affected.getDisturbanceEntity().getDisturbanceId(),
				DisturbanceFeedbackCreateRequest.create().withPartyId(affected.getPartyId()))));
	}
}
