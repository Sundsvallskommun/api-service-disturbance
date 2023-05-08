package se.sundsvall.disturbance.service.message;

import static java.lang.System.lineSeparator;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toEmail;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toHeaders;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toMessage;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toParty;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toSms;
import static se.sundsvall.disturbance.service.mapper.DisturbanceFeedbackMapper.toDisturbanceFeedbackHistoryEntity;
import static se.sundsvall.disturbance.service.message.util.SendMessageUtils.hasDisturbanceFeedBackEntity;
import static se.sundsvall.disturbance.service.util.DateUtils.toMessageDateFormat;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageSender;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackHistoryRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.messaging.ApiMessagingClient;
import se.sundsvall.disturbance.service.message.configuration.MessageConfiguration;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping.CategoryConfig;

@Component
public class SendMessageLogic {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageLogic.class);

	// Message template variable name definitions.
	private static final String MSG_TITLE = "disturbance.title";
	private static final String MSG_NEWLINE = "newline";
	private static final String MSG_DESCRIPTION = "disturbance.description";
	private static final String MSG_PLANNED_START_DATE = "disturbance.plannedStartDate";
	private static final String MSG_PLANNED_STOP_DATE = "disturbance.plannedStopDate";
	private static final String MSG_AFFECTED_REFERENCE = "disturbance.affected.reference";

	@Autowired
	private DisturbanceFeedbackRepository disturbanceFeedBackRepository;

	@Autowired
	private DisturbanceFeedbackHistoryRepository disturbanceFeedBackHistoryRepository;

	@Autowired
	private MessageConfiguration messageConfiguration;

	@Autowired
	private ApiMessagingClient apiMessagingClient;

	/**
	 * Send a "closed disturbance" message to all affected persons/organizations in a disturbance with an existing
	 * disturbanceFeedback. The affectedEntities will get a message if a disturbanceFeedback exists for this disturbance.
	 *
	 * @param disturbanceEntity the DisturbanceEntity.
	 */
	@Transactional
	public void sendCloseMessageToAllApplicableAffecteds(final DisturbanceEntity disturbanceEntity) {
		sendCloseMessage(disturbanceEntity, disturbanceEntity.getAffectedEntities());
	}

	/**
	 * Send a "closed disturbance" message to all affected persons/organizations with an existing disturbanceFeedback, in
	 * the provided affectedEntities list. This will override the existing affectedEntities in the DisturbanceEntity.
	 *
	 * The affectedEntities will get a message if a disturbanceFeedback exists for this disturbance.
	 *
	 * @param disturbanceEntity The entity that is closed.
	 * @param affectedEntities  The affectedEntities that will get a message (if a disturbanceFeedback exists)
	 */
	@Transactional
	public void sendCloseMessageToProvidedApplicableAffecteds(final DisturbanceEntity disturbanceEntity, final List<AffectedEntity> affectedEntities) {
		sendCloseMessage(disturbanceEntity, affectedEntities);
	}

	/**
	 * Send a "new disturbance" message to all affected persons/organizations with an existing disturbanceFeedback in a
	 * disturbance.
	 *
	 * @param disturbanceEntity the DisturbanceEntity.
	 */
	@Transactional
	public void sendCreateMessageToAllApplicableAffecteds(final DisturbanceEntity disturbanceEntity) {
		sendCreateMessage(disturbanceEntity, disturbanceEntity.getAffectedEntities());
	}

	/**
	 * Send a "new disturbance" message to all affected persons/organizations with an existing disturbanceFeedback, in the
	 * provided affectedEntities list. This will override the existing affectedEntities in the DisturbanceEntity.
	 *
	 * The affectedEntities will get a message if a disturbanceFeedback exists for this disturbance.
	 *
	 * @param disturbanceEntity The entity.
	 * @param affectedEntities  The affectedEntities that will get a message (if a disturbanceFeedback exists)
	 */
	@Transactional
	public void sendCreateMessageToProvidedApplicableAffecteds(final DisturbanceEntity disturbanceEntity, final List<AffectedEntity> affectedEntities) {
		sendCreateMessage(disturbanceEntity, affectedEntities);
	}

	/**
	 * Send a "updated disturbance" message to all affected persons/organizations with an existing disturbanceFeedback in a
	 * disturbance.
	 *
	 * @param updatedDisturbanceEntity the updated DisturbanceEntity.
	 */
	@Transactional
	public void sendUpdateMessage(final DisturbanceEntity updatedDisturbanceEntity) {

		// Fetch all feedbackEntities for this disturbance.
		final var disturbanceFeedbackEntities = disturbanceFeedBackRepository.findByCategoryAndDisturbanceId(
			Category.valueOf(updatedDisturbanceEntity.getCategory()), updatedDisturbanceEntity.getDisturbanceId());

		// Create messages
		final var messages = updatedDisturbanceEntity.getAffectedEntities().stream()
			.filter(affectedEntity -> hasDisturbanceFeedBackEntity(affectedEntity, disturbanceFeedbackEntities))
			.map(affectedEntity -> mapToUpdateMessage(updatedDisturbanceEntity, affectedEntity))
			.filter(Objects::nonNull)
			.toList();

		// Send messages.
		sendMessages(messages);
	}

	private void sendCreateMessage(final DisturbanceEntity createdDisturbanceEntity, final List<AffectedEntity> affectedEntities) {

		// Fetch all feedbackEntities for this disturbance.
		final var disturbanceFeedbackEntities = disturbanceFeedBackRepository.findByCategoryAndDisturbanceId(
			Category.valueOf(createdDisturbanceEntity.getCategory()), createdDisturbanceEntity.getDisturbanceId());

		// Create messages
		final var messages = affectedEntities.stream()
			.filter(affectedEntity -> hasDisturbanceFeedBackEntity(affectedEntity, disturbanceFeedbackEntities))
			.map(affectedEntity -> mapToNewMessage(createdDisturbanceEntity, affectedEntity))
			.filter(Objects::nonNull)
			.toList();

		// Send messages.
		sendMessages(messages);
	}

	private void sendCloseMessage(final DisturbanceEntity disturbanceEntity, final List<AffectedEntity> affectedEntities) {

		// Fetch all feedbackEntities for this disturbance.
		final var disturbanceFeedbackEntities = disturbanceFeedBackRepository.findByCategoryAndDisturbanceId(
			Category.valueOf(disturbanceEntity.getCategory()), disturbanceEntity.getDisturbanceId());

		// Create messages
		final var messages = affectedEntities.stream()
			.filter(affectedEntity -> hasDisturbanceFeedBackEntity(affectedEntity, disturbanceFeedbackEntities))
			.map(affectedEntity -> mapToCloseMessage(disturbanceEntity, affectedEntity))
			.filter(Objects::nonNull)
			.toList();

		// Send messages.
		sendMessages(messages);
	}

	private void persistFeedbackHistory(final AffectedEntity affectedEntity) {
		disturbanceFeedBackHistoryRepository.save(toDisturbanceFeedbackHistoryEntity(affectedEntity));
	}

	private Message mapToUpdateMessage(final DisturbanceEntity disturbanceEntity, final AffectedEntity affectedEntity) {

		// Fetch message properties by category.
		final var messageConfig = getMessageConfigByCategory(disturbanceEntity.getCategory()).orElse(new CategoryConfig());
		if (!messageConfig.isActive()) {
			return null;
		}

		final var propertyResolver = new StringSubstitutor(Map.of(
			MSG_NEWLINE, lineSeparator(),
			MSG_TITLE, disturbanceEntity.getTitle(),
			MSG_DESCRIPTION, disturbanceEntity.getDescription(),
			MSG_PLANNED_START_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStartDate()),
			MSG_PLANNED_STOP_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStopDate()),
			MSG_AFFECTED_REFERENCE, affectedEntity.getReference()));

		// Assemble message and subject based on the properties.
		final var sender = new MessageSender()
			.email(toEmail(messageConfig.getSenderEmailName(), messageConfig.getSenderEmailAddress()))
			.sms(toSms(messageConfig.getSenderSmsName()));

		final var headers = toHeaders(Category.valueOf(disturbanceEntity.getCategory()), affectedEntity.getFacilityId());
		final var subject = propertyResolver.replace(messageConfig.getSubjectUpdate());
		final var message = propertyResolver.replace(messageConfig.getMessageUpdate());

		// Store feedback history.
		persistFeedbackHistory(affectedEntity);

		return toMessage(headers, sender, toParty(affectedEntity.getPartyId()), subject, message);
	}

	private Message mapToNewMessage(final DisturbanceEntity disturbanceEntity, final AffectedEntity affectedEntity) {

		// Fetch message properties by category.
		final var messageConfig = getMessageConfigByCategory(disturbanceEntity.getCategory()).orElse(new CategoryConfig());
		if (!messageConfig.isActive()) {
			return null;
		}

		final var propertyResolver = new StringSubstitutor(Map.of(
			MSG_NEWLINE, lineSeparator(),
			MSG_TITLE, disturbanceEntity.getTitle(),
			MSG_DESCRIPTION, disturbanceEntity.getDescription(),
			MSG_PLANNED_START_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStartDate()),
			MSG_PLANNED_STOP_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStopDate()),
			MSG_AFFECTED_REFERENCE, affectedEntity.getReference()));

		// Assemble message and subject based on the properties.
		final var sender = new MessageSender()
			.email(toEmail(messageConfig.getSenderEmailName(), messageConfig.getSenderEmailAddress()))
			.sms(toSms(messageConfig.getSenderSmsName()));

		final var headers = toHeaders(Category.valueOf(disturbanceEntity.getCategory()), affectedEntity.getFacilityId());
		final var subject = propertyResolver.replace(messageConfig.getSubjectNew());
		final var message = propertyResolver.replace(messageConfig.getMessageNew());

		// Store feedback history.
		persistFeedbackHistory(affectedEntity);

		return toMessage(headers, sender, toParty(affectedEntity.getPartyId()), subject, message);
	}

	private Message mapToCloseMessage(final DisturbanceEntity disturbanceEntity, final AffectedEntity affectedEntity) {

		// Fetch message properties by category.
		final var messageConfig = getMessageConfigByCategory(disturbanceEntity.getCategory()).orElse(new CategoryConfig());
		if (!messageConfig.isActive()) {
			return null;
		}

		final var propertyResolver = new StringSubstitutor(Map.of(
			MSG_NEWLINE, lineSeparator(),
			MSG_TITLE, disturbanceEntity.getTitle(),
			MSG_DESCRIPTION, disturbanceEntity.getDescription(),
			MSG_PLANNED_START_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStartDate()),
			MSG_PLANNED_STOP_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStopDate()),
			MSG_AFFECTED_REFERENCE, affectedEntity.getReference()));

		// Assemble message and subject based on the properties.
		final var sender = new MessageSender()
			.email(toEmail(messageConfig.getSenderEmailName(), messageConfig.getSenderEmailAddress()))
			.sms(toSms(messageConfig.getSenderSmsName()));

		final var headers = toHeaders(Category.valueOf(disturbanceEntity.getCategory()), affectedEntity.getFacilityId());
		final var subject = propertyResolver.replace(messageConfig.getSubjectClose());
		final var message = propertyResolver.replace(messageConfig.getMessageClose());

		// Store feedback history.
		persistFeedbackHistory(affectedEntity);

		return toMessage(headers, sender, toParty(affectedEntity.getPartyId()), subject, message);
	}

	private void sendMessages(final List<Message> messages) {

		LOGGER.debug("Messages to send to api-messaging-service: '{}'", messages);

		// Send messageRequest to api-messaging-service service (if it contains messages).
		if (isNotEmpty(messages)) {
			LOGGER.info("apiMessagingClient: Sending '{}' messages to api-messaging-service...", messages.size());
			apiMessagingClient.sendMessage(new MessageRequest().messages(messages));
			LOGGER.info("apiMessagingClient: Messages sent!");
		}
	}

	private Optional<CategoryConfig> getMessageConfigByCategory(final String category) {
		return Optional.ofNullable(messageConfiguration.getCategoryConfig(Category.valueOf(category)));
	}
}
