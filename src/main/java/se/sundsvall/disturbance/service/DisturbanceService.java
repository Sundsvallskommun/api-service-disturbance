package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;
import static se.sundsvall.disturbance.api.model.Status.OPEN;
import static se.sundsvall.disturbance.api.model.Status.PLANNED;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbance;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbanceEntity;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbances;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toMergedDisturbanceEntity;
import static se.sundsvall.disturbance.service.util.MappingUtils.getAddedAffectedEntities;
import static se.sundsvall.disturbance.service.util.MappingUtils.getRemovedAffectedEntities;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.service.message.SendMessageLogic;

@Service
public class DisturbanceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisturbanceService.class);

	private final DisturbanceRepository disturbanceRepository;
	private final SendMessageLogic sendMessageLogic;

	public DisturbanceService(DisturbanceRepository disturbanceRepository, SendMessageLogic sendMessageLogic) {
		this.disturbanceRepository = disturbanceRepository;
		this.sendMessageLogic = sendMessageLogic;
	}

	@Transactional
	public Disturbance findByCategoryAndDisturbanceId(final Category category, final String disturbanceId) {
		return toDisturbance(disturbanceRepository.findByCategoryAndDisturbanceId(category, disturbanceId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId))));
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
			throw Problem.valueOf(CONFLICT, format(ERROR_DISTURBANCE_ALREADY_EXISTS, disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId()));
		}

		// Persist disturbance entity.
		final var persistedDisturbanceEntity = disturbanceRepository.save(toDisturbanceEntity(disturbanceCreateRequest));

		if (isNotEmpty(persistedDisturbanceEntity.getAffectedEntities()) &&
			!hasStatusClosed(persistedDisturbanceEntity) && hasStatusOpen(persistedDisturbanceEntity)) {

			// Send message to the created disturbance notification recipients.
			sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(persistedDisturbanceEntity);
		}

		return toDisturbance(persistedDisturbanceEntity);
	}

	@Transactional
	public Disturbance updateDisturbance(final Category category, final String disturbanceId, final DisturbanceUpdateRequest disturbanceUpdateRequest) {

		// Get existing disturbance entity.
		final var existingDisturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceId(category, disturbanceId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId)));

		// No updates allowed on closed disturbance.
		if (hasStatusClosed(existingDisturbanceEntity)) {
			throw Problem.valueOf(CONFLICT, format(ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED, category, disturbanceId));
		}

		// Get new (incoming) disturbance entity.
		final var incomingDisturbanceEntity = toDisturbanceEntity(category, disturbanceId, disturbanceUpdateRequest);

		// Get added and removed affecteds.
		final var removedAffecteds = getRemovedAffectedEntities(existingDisturbanceEntity, incomingDisturbanceEntity);
		final var addedAffecteds = getAddedAffectedEntities(existingDisturbanceEntity, incomingDisturbanceEntity);

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
			LOGGER.info("Disturbance content was changed: '{}'. Sending update messages.", mergedDisturbanceEntity);
			sendMessageLogic.sendUpdateMessage(mergedDisturbanceEntity);
		}

		final var save = disturbanceRepository.save(mergedDisturbanceEntity);
		return toDisturbance(save);
	}

	@Transactional
	public void deleteDisturbance(final Category category, final String disturbanceId) {

		final var disturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceId(category, disturbanceId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId)));

		// "Soft delete" disturbance entity.
		disturbanceEntity.setDeleted(true);
		disturbanceRepository.save(disturbanceEntity);
	}

	private boolean isChangedToStatusClosed(final DisturbanceEntity oldDisturbanceEntity, final DisturbanceEntity newDisturbanceEntity) {
		return !hasStatusClosed(oldDisturbanceEntity) && hasStatusClosed(newDisturbanceEntity);
	}

	protected static boolean hasStatusClosed(final DisturbanceEntity disturbanceEntity) {
		return CLOSED.equals(disturbanceEntity.getStatus());
	}

	protected static boolean hasStatusOpen(final DisturbanceEntity disturbanceEntity) {
		return OPEN.equals(disturbanceEntity.getStatus());
	}

	protected static boolean hasStatusPlanned(final DisturbanceEntity disturbanceEntity) {
		return PLANNED.equals(disturbanceEntity.getStatus());
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
}
