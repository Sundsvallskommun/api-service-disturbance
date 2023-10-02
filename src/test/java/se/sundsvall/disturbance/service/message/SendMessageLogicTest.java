package se.sundsvall.disturbance.service.message;

import static java.time.ZoneId.systemDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.ISSUE_TYPE;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.messaging.Email;
import generated.se.sundsvall.messaging.Header;
import generated.se.sundsvall.messaging.Header.NameEnum;
import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageParty;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageSender;
import generated.se.sundsvall.messaging.Sms;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.messaging.ApiMessagingClient;
import se.sundsvall.disturbance.service.SubscriptionService;
import se.sundsvall.disturbance.service.message.configuration.MessageConfiguration;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping.CategoryConfig;

@ExtendWith(MockitoExtension.class)
class SendMessageLogicTest {

	private static final Category CATEGORY = Category.ELECTRICITY;
	private static final String DISTURBANCE_ID = "disturbanceId";
	private static final String DESCRIPTION = "Major disturbance in the central parts of town";
	private static final OffsetDateTime PLANNED_START_DATE = LocalDateTime.of(2021, 11, 1, 12, 0, 6).atZone(systemDefault()).toOffsetDateTime();
	private static final OffsetDateTime PLANNED_STOP_DATE = LocalDateTime.of(2021, 11, 10, 18, 30, 8).atZone(systemDefault()).toOffsetDateTime();
	private static final String STATUS = Status.OPEN.toString();
	private static final String TITLE = "Disturbance";

	@Captor
	private ArgumentCaptor<MessageRequest> messageRequestCaptor;

	@Mock
	private SubscriptionService subscriptionServiceMock;

	@Mock
	private MessageConfiguration messageConfigurationMock;

	@Mock
	private ApiMessagingClient apiMessagingClientMock;

	@InjectMocks
	private SendMessageLogic sendMessageLogic;

	@Test
	void sendCloseMessageToAllApplicableAffecteds() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6)).thenReturn(true);
		// Let 3 of these affecteds be without an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5)).thenReturn(false);

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(messageConfigurationMock, subscriptionServiceMock, apiMessagingClientMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(3);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-2"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(2)))
				.subject("Close subject for reference-2")
				.message("Close message for reference-2"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-4"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("Close subject for reference-4")
				.message("Close message for reference-4"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(6)))
				.subject("Close subject for reference-6")
				.message("Close message for reference-6"));
	}

	@Test
	void sendCloseMessageToAllApplicableAffectedsWhenNoAffectedsHasAnApplicableSubscription() {

		// Set up disturbanceEntity with 6 affected affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(messageConfigurationMock, subscriptionServiceMock);
		verifyNoInteractions(apiMessagingClientMock);
	}

	@Test
	void sendCloseMessageToProvidedApplicableAffecteds() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 6)).thenReturn(true);

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		// AffectedEntity1. This entity has a subscription.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId(uuidFromInt(4).toString());
		affectedEntity1.setFacilityId("facilityId-4");
		affectedEntity1.setReference("reference-4");
		affectedEntity1.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity2. This entity doesn't have a subscription.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId(uuidFromInt(5).toString());
		affectedEntity2.setFacilityId("facilityId-5");
		affectedEntity2.setReference("reference-5");
		affectedEntity2.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity3. This entity has a subscription.
		final var affectedEntity3 = new AffectedEntity();
		affectedEntity3.setPartyId(uuidFromInt(4).toString()); // Same as affectedEntity1 in order to test that we can have the same partyId on another AffectedEntity.
		affectedEntity3.setFacilityId("facilityId-6");
		affectedEntity3.setReference("reference-6");
		affectedEntity3.setDisturbanceEntity(disturbanceEntity);

		// Define a AffectedEntity-override list, where two of them has a subscription (id=4, id=6).
		final var affectedEntitiesOverride = new ArrayList<>(List.of(affectedEntity1, affectedEntity2, affectedEntity3));

		sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(messageConfigurationMock, times(2)).getCategoryConfig(CATEGORY);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(messageConfigurationMock, subscriptionServiceMock, apiMessagingClientMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(2).containsExactly(
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-4"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("Close subject for reference-4")
				.message("Close message for reference-4"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("Close subject for reference-6")
				.message("Close message for reference-6"));
	}

	@Test
	void sendCloseMessageToProvidedApplicableAffectedsWhenNoAffectedsHasAnApplicableSubscription() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Define a AffectedEntity-override list with two elements.
		final var affectedEntitiesOverride = new ArrayList<AffectedEntity>();

		// AffectedEntity1.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId(uuidFromInt(1).toString());
		affectedEntity1.setFacilityId("facilityId-1");
		affectedEntity1.setReference("reference-1");
		affectedEntity1.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity2.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId(uuidFromInt(2).toString());
		affectedEntity2.setFacilityId("facilityId-2");
		affectedEntity2.setReference("reference-2");
		affectedEntity2.setDisturbanceEntity(disturbanceEntity);

		affectedEntitiesOverride.addAll(List.of(affectedEntity1, affectedEntity2));

		sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verifyNoMoreInteractions(subscriptionServiceMock);
		verifyNoInteractions(apiMessagingClientMock, messageConfigurationMock);
	}

	@Test
	void sendUpdateMessage() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6)).thenReturn(true);
		// Let 3 of these affecteds be without an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5)).thenReturn(false);

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(messageConfigurationMock, subscriptionServiceMock, apiMessagingClientMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(3);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-2"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(2)))
				.subject("Update subject for reference-2")
				.message("Update message for reference-2. Planned stop date 2021-11-10 18:30"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms().name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-4"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("Update subject for reference-4")
				.message("Update message for reference-4. Planned stop date 2021-11-10 18:30"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(6)))
				.subject("Update subject for reference-6")
				.message("Update message for reference-6. Planned stop date 2021-11-10 18:30"));
	}

	@Test
	void sendUpdateMessageWherePlannedStartAndStopDatesAreNotSet() {

		// Set up disturbanceEntity with 2 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2);
		disturbanceEntity.setPlannedStartDate(null);
		disturbanceEntity.setPlannedStopDate(null);

		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2)).thenReturn(true);

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(2)).getCategoryConfig(CATEGORY);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verifyNoMoreInteractions(messageConfigurationMock, subscriptionServiceMock, apiMessagingClientMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(2);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-1"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(1)))
				.subject("Update subject for reference-1")
				.message("Update message for reference-1. Planned stop date N/A"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-2"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(2)))
				.subject("Update subject for reference-2")
				.message("Update message for reference-2. Planned stop date N/A"));
	}

	@Test
	void sendUpdateMessageWhenNoAffectedsHasAnApplicableSubscription() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(subscriptionServiceMock);
		verifyNoInteractions(messageConfigurationMock, apiMessagingClientMock);
	}

	@Test
	void sendCreateMessageToAllApplicableAffecteds() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6)).thenReturn(true);
		// Let 3 of these affecteds be without an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5)).thenReturn(false);

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);

		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(messageConfigurationMock, subscriptionServiceMock, apiMessagingClientMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(3);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-2"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(2)))
				.subject("New subject for reference-2")
				.message("New message for reference-2"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-4"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("New subject for reference-4")
				.message("New message for reference-4"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(6)))
				.subject("New subject for reference-6")
				.message("New message for reference-6"));
	}

	@Test
	void sendCreateMessageToAllApplicableAffectedsWhenNoAffectedsHasSubscription() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(subscriptionServiceMock);
		verifyNoInteractions(apiMessagingClientMock, messageConfigurationMock);
	}

	@Test
	void sendCreateMessageToProvidedApplicableAffecteds() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 6)).thenReturn(true);

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		// AffectedEntity1. This entity has a subscription.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId(uuidFromInt(4).toString());
		affectedEntity1.setFacilityId("facilityId-4");
		affectedEntity1.setReference("reference-4");
		affectedEntity1.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity2. This entity doesn't have a subscription.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId(uuidFromInt(5).toString());
		affectedEntity2.setFacilityId("facilityId-5");
		affectedEntity2.setReference("reference-5");
		affectedEntity2.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity3. This entity has subscription.
		final var affectedEntity3 = new AffectedEntity();
		affectedEntity3.setPartyId(uuidFromInt(4).toString()); // Same as affectedEntity1 in order to test that we can have the same partyId on another AffectedEntity.
		affectedEntity3.setFacilityId("facilityId-6");
		affectedEntity3.setReference("reference-6");
		affectedEntity3.setDisturbanceEntity(disturbanceEntity);

		// Define a AffectedEntity-override list, where two of them has a subscription (id=4, id=6).
		final var affectedEntitiesOverride = new ArrayList<>(List.of(affectedEntity1, affectedEntity2, affectedEntity3));

		sendMessageLogic.sendCreateMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(messageConfigurationMock, times(2)).getCategoryConfig(CATEGORY);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoMoreInteractions(messageConfigurationMock, subscriptionServiceMock, apiMessagingClientMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(2).containsExactly(
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-4"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("New subject for reference-4")
				.message("New message for reference-4"),
			new Message()
				.sender(new MessageSender()
					.sms(new Sms()
						.name("SenderSMSName"))
					.email(new Email()
						.name("SenderEmailName")
						.address("noreply@host.se")))
				.headers(List.of(
					new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("New subject for reference-6")
				.message("New message for reference-6"));
	}

	@Test
	void sendCreateMessageToProvidedApplicableAffectedsWhenNoAffectedsHasAnApplicableSubscriptionk() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Define a AffectedEntity-override list with two elements.
		final var affectedEntitiesOverride = new ArrayList<AffectedEntity>();

		// AffectedEntity1.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId(uuidFromInt(1).toString());
		affectedEntity1.setFacilityId("facilityId-1");
		affectedEntity1.setReference("reference-1");
		affectedEntity1.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity2.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId(uuidFromInt(2).toString());
		affectedEntity2.setFacilityId("facilityId-2");
		affectedEntity2.setReference("reference-2");
		affectedEntity2.setDisturbanceEntity(disturbanceEntity);

		affectedEntitiesOverride.addAll(List.of(affectedEntity1, affectedEntity2));

		sendMessageLogic.sendCreateMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verifyNoMoreInteractions(subscriptionServiceMock);
		verifyNoInteractions(apiMessagingClientMock, messageConfigurationMock);
	}

	@Test
	void sendCreateMessageToAllApplicableAffectedsWhenConfigIsNotActive() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4)).thenReturn(true);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6)).thenReturn(true);
		// Let 3 of these affecteds be without an applicable subscription.
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3)).thenReturn(false);
		when(subscriptionServiceMock.hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5)).thenReturn(false);

		// Setup message properties mock
		final var categoryConfigMock = Mockito.mock(CategoryConfig.class);
		when(categoryConfigMock.isActive()).thenReturn(false);
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(categoryConfigMock);

		sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(1).toString(), CATEGORY, "facilityId-" + 1);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(2).toString(), CATEGORY, "facilityId-" + 2);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(3).toString(), CATEGORY, "facilityId-" + 3);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(4).toString(), CATEGORY, "facilityId-" + 4);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(5).toString(), CATEGORY, "facilityId-" + 5);
		verify(subscriptionServiceMock).hasApplicableSubscription(uuidFromInt(6).toString(), CATEGORY, "facilityId-" + 6);
		verifyNoInteractions(apiMessagingClientMock);
		verifyNoMoreInteractions(messageConfigurationMock);
	}

	private DisturbanceEntity setupDisturbanceEntity(final int... idNumbersOnAffecteds) {

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setCategory(CATEGORY.toString());
		disturbanceEntity.setDisturbanceId(DISTURBANCE_ID);
		disturbanceEntity.setDescription(DESCRIPTION);
		disturbanceEntity.setPlannedStartDate(PLANNED_START_DATE);
		disturbanceEntity.setPlannedStopDate(PLANNED_STOP_DATE);
		disturbanceEntity.setStatus(STATUS);
		disturbanceEntity.setTitle(TITLE);

		final var affectedEntities = new ArrayList<AffectedEntity>();
		for (final var idNumberOnAffected : idNumbersOnAffecteds) {
			final var affectedEntity = new AffectedEntity();
			affectedEntity.setPartyId(uuidFromInt(idNumberOnAffected).toString());
			affectedEntity.setReference("reference-" + idNumberOnAffected);
			affectedEntity.setFacilityId("facilityId-" + idNumberOnAffected);
			affectedEntity.setDisturbanceEntity(disturbanceEntity);
			affectedEntities.add(affectedEntity);
		}
		disturbanceEntity.addAffectedEntities(affectedEntities);

		return disturbanceEntity;
	}

	private CategoryConfig setupCategoryConfig() {
		final var categoryConfig = new MessageConfigurationMapping.CategoryConfig();
		categoryConfig.setActive(true);
		categoryConfig.setMessageClose("Close message for ${disturbance.affected.reference}");
		categoryConfig.setMessageNew("New message for ${disturbance.affected.reference}");
		categoryConfig.setMessageUpdate("Update message for ${disturbance.affected.reference}. Planned stop date ${disturbance.plannedStopDate}");
		categoryConfig.setSenderEmailAddress("noreply@host.se");
		categoryConfig.setSenderEmailName("SenderEmailName");
		categoryConfig.setSenderSmsName("SenderSMSName");
		categoryConfig.setSubjectClose("Close subject for ${disturbance.affected.reference}");
		categoryConfig.setSubjectNew("New subject for ${disturbance.affected.reference}");
		categoryConfig.setSubjectUpdate("Update subject for ${disturbance.affected.reference}");

		return categoryConfig;
	}

	private static final UUID uuidFromInt(final int integer) {

		final var sb = new StringBuilder();
		final String hex = Integer.toHexString(integer & 0x0000FFFF);
		sb.append("00000000".substring(hex.length()));
		sb.append(hex);
		sb.append('-');
		sb.append("0000-1000-8000-00805F9B34FB");

		return UUID.fromString(sb.toString());
	}
}
