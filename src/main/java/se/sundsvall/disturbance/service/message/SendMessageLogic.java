package se.sundsvall.disturbance.service.message;

import static java.lang.System.lineSeparator;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toEmail;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toFilters;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toMessage;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toParty;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.toSms;
import static se.sundsvall.disturbance.service.util.DateUtils.toMessageDateFormat;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageSender;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.messaging.MessagingClient;
import se.sundsvall.disturbance.service.SubscriptionService;
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

	private final SubscriptionService subscriptionService;
	private final MessageConfiguration messageConfiguration;
	private final MessagingClient messagingClient;

	public SendMessageLogic(SubscriptionService subscriptionService, MessageConfiguration messageConfiguration, MessagingClient messagingClient) {
		this.subscriptionService = subscriptionService;
		this.messageConfiguration = messageConfiguration;
		this.messagingClient = messagingClient;
	}

	/**
	 * Send a "closed disturbance" message to all affected persons/organizations in a disturbance with an existing
	 * subscription. The affectedEntities will get a message if a subscription with no matching opt-outs exists.
	 *
	 * @param disturbanceEntity the DisturbanceEntity.
	 */
	@Transactional
	public void sendCloseMessageToAllApplicableAffecteds(final DisturbanceEntity disturbanceEntity) {
		sendCloseMessage(disturbanceEntity, disturbanceEntity.getAffectedEntities());
	}

	/**
	 * Send a "closed disturbance" message to all affected persons/organizations with an existing subscription, in
	 * the provided affectedEntities list. This will override the existing affectedEntities in the DisturbanceEntity.
	 *
	 * The affectedEntities will get a message if a subscription exists for this disturbance.
	 *
	 * @param disturbanceEntity The entity that is closed.
	 * @param affectedEntities  The affectedEntities that will get a message (if a subscription with no matching opt-outs
	 *                          exists).
	 */
	@Transactional
	public void sendCloseMessageToProvidedApplicableAffecteds(final DisturbanceEntity disturbanceEntity, final List<AffectedEntity> affectedEntities) {
		sendCloseMessage(disturbanceEntity, affectedEntities);
	}

	/**
	 * Send a "new disturbance" message to all affected persons/organizations with an existing subscription (without
	 * matching opt-outs) in a
	 * disturbance.
	 *
	 * @param disturbanceEntity the DisturbanceEntity.
	 */
	@Transactional
	public void sendCreateMessageToAllApplicableAffecteds(final DisturbanceEntity disturbanceEntity) {
		sendCreateMessage(disturbanceEntity, disturbanceEntity.getAffectedEntities());
	}

	/**
	 * Send a "new disturbance" message to all affected persons/organizations with an existing subscription, in the
	 * provided affectedEntities list. This will override the existing affectedEntities in the DisturbanceEntity.
	 *
	 * The affectedEntities will get a message if a subscription with no matching opt-outs exists.
	 *
	 * @param disturbanceEntity The entity.
	 * @param affectedEntities  The affectedEntities that will get a message (if a subscription with no matching opt-outs
	 *                          exists).
	 */
	@Transactional
	public void sendCreateMessageToProvidedApplicableAffecteds(final DisturbanceEntity disturbanceEntity, final List<AffectedEntity> affectedEntities) {
		sendCreateMessage(disturbanceEntity, affectedEntities);
	}

	/**
	 * Send a "updated disturbance" message to all affected persons/organizations with an existing subscription in a
	 * disturbance.
	 *
	 * @param updatedDisturbanceEntity the updated DisturbanceEntity.
	 */
	@Transactional
	public void sendUpdateMessage(final DisturbanceEntity updatedDisturbanceEntity) {

		// Create messages
		final var municipalityId = updatedDisturbanceEntity.getMunicipalityId();
		final var messages = updatedDisturbanceEntity.getAffectedEntities().stream()
			.filter(this::hasApplicableSubscription)
			.map(affectedEntity -> mapToUpdateMessage(updatedDisturbanceEntity, affectedEntity))
			.filter(Objects::nonNull)
			.toList();

		// Send messages.
		sendMessages(municipalityId, messages);
	}

	private void sendCreateMessage(final DisturbanceEntity createdDisturbanceEntity, final List<AffectedEntity> affectedEntities) {

		// Create messages
		final var municipalityId = createdDisturbanceEntity.getMunicipalityId();
		final var messages = affectedEntities.stream()
			.filter(this::hasApplicableSubscription)
			.map(affectedEntity -> mapToNewMessage(createdDisturbanceEntity, affectedEntity))
			.filter(Objects::nonNull)
			.toList();

		// Send messages.
		sendMessages(municipalityId, messages);
	}

	private void sendCloseMessage(final DisturbanceEntity disturbanceEntity, final List<AffectedEntity> affectedEntities) {

		// Create messages
		final var municipalityId = disturbanceEntity.getMunicipalityId();
		final var messages = affectedEntities.stream()
			.filter(this::hasApplicableSubscription)
			.map(affectedEntity -> mapToCloseMessage(disturbanceEntity, affectedEntity))
			.filter(Objects::nonNull)
			.toList();

		// Send messages.
		sendMessages(municipalityId, messages);
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

		final var filters = toFilters(disturbanceEntity.getCategory(), affectedEntity.getFacilityId());
		final var subject = propertyResolver.replace(messageConfig.getSubjectUpdate());
		final var message = propertyResolver.replace(messageConfig.getMessageUpdate());

		return toMessage(filters, sender, toParty(affectedEntity.getPartyId()), subject, message);
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

		final var filters = toFilters(disturbanceEntity.getCategory(), affectedEntity.getFacilityId());
		final var subject = propertyResolver.replace(messageConfig.getSubjectNew());
		final var message = propertyResolver.replace(messageConfig.getMessageNew());

		return toMessage(filters, sender, toParty(affectedEntity.getPartyId()), subject, message);
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
		final var filters = toFilters(disturbanceEntity.getCategory(), affectedEntity.getFacilityId());
		final var subject = propertyResolver.replace(messageConfig.getSubjectClose());
		final var message = propertyResolver.replace(messageConfig.getMessageClose());
		final var party = toParty(affectedEntity.getPartyId());

		return toMessage(filters, sender, party, subject, message);
	}

	private void sendMessages(final String municipalityId, final List<Message> messages) {

		LOGGER.debug("Messages to send to api-messaging-service: '{}'", messages);

		// Send messageRequest to api-messaging-service service (if it contains messages).
		if (isNotEmpty(messages)) {
			LOGGER.info("apiMessagingClient: Sending '{}' messages to api-messaging-service...", messages.size());
			messagingClient.sendMessage(municipalityId, new MessageRequest().messages(messages));
			LOGGER.info("apiMessagingClient: Messages sent!");
		}
	}

	private Optional<CategoryConfig> getMessageConfigByCategory(final Category category) {
		return Optional.ofNullable(messageConfiguration.getCategoryConfig(category));
	}

	/**
	 * Returns true if the person/organization that belongs to the affectedEntity has an applicable subscription without
	 * matching opt-out.
	 *
	 * @param  affectedEntity the affectedEntity to check.
	 * @return                true if there is a match, false otherwise.
	 */
	private boolean hasApplicableSubscription(final AffectedEntity affectedEntity) {
		final var category = affectedEntity.getDisturbanceEntity().getCategory();
		final var partyId = affectedEntity.getPartyId();
		final var municipalityId = affectedEntity.getDisturbanceEntity().getMunicipalityId();
		final var facilityId = affectedEntity.getFacilityId();

		return subscriptionService.hasApplicableSubscription(municipalityId, partyId, category, facilityId);
	}
}
