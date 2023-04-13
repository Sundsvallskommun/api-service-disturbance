package se.sundsvall.disturbance.service.message;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

import generated.se.sundsvall.businessrules.IssueType;
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
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackHistoryRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackHistoryEntity;
import se.sundsvall.disturbance.integration.messaging.ApiMessagingClient;
import se.sundsvall.disturbance.service.message.configuration.MessageConfiguration;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping.CategoryConfig;

@ExtendWith(MockitoExtension.class)
class SendMessageLogicTest {

	private static final Category CATEGORY = Category.ELECTRICITY;
	private static final String DISTURBANCE_ID = "disturbanceId";
	private static final String DESCRIPTION = "Major disturbance in the central parts of town";
	private static final OffsetDateTime PLANNED_START_DATE = LocalDateTime.of(2021, 11, 1, 12, 0, 6).atZone(ZoneId.systemDefault()).toOffsetDateTime();
	private static final OffsetDateTime PLANNED_STOP_DATE = LocalDateTime.of(2021, 11, 10, 18, 30, 8).atZone(ZoneId.systemDefault()).toOffsetDateTime();
	private static final String STATUS = Status.OPEN.toString();
	private static final String TITLE = "Disturbance";

	@Captor
	private ArgumentCaptor<MessageRequest> messageRequestCaptor;

	@Captor
	private ArgumentCaptor<DisturbanceFeedbackHistoryEntity> disturbanceFeedbackHistoryEntityCaptor;

	@Mock
	private DisturbanceFeedbackRepository disturbanceFeedBackRepositoryMock;

	@Mock
	private DisturbanceFeedbackHistoryRepository disturbanceFeedBackHistoryRepositoryMock;

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

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(setupDisturbanceFeedbackEntityList(2, 4, 6));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(3)).save(disturbanceFeedbackHistoryEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(6)))
				.subject("Close subject for reference-6")
				.message("Close message for reference-6"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackHistoryEntityCaptor.getAllValues())
			.extracting(DisturbanceFeedbackHistoryEntity::getCategory, DisturbanceFeedbackHistoryEntity::getDisturbanceId, DisturbanceFeedbackHistoryEntity::getPartyId)
			.containsExactly(
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(2).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(4).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(6).toString()));
	}

	@Test
	void sendCloseMessageToAllApplicableAffectedsWhenNoAffectedsHasDisturbanceFeedback() {

		// Set up disturbanceEntity with 6 affected affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);
	}

	@Test
	void sendCloseMessageToProvidedApplicableAffecteds() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an disturbanceFeedbackEntity.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(setupDisturbanceFeedbackEntityList(2, 4, 6));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		// AffectedEntity1. This entity has a disturbanceFeedbackEntity.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId(uuidFromInt(4).toString());
		affectedEntity1.setFacilityId("facilityId-4");
		affectedEntity1.setReference("reference-4");
		affectedEntity1.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity2. This entity doesn't have a disturbanceFeedbackEntity.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId(uuidFromInt(5).toString());
		affectedEntity2.setFacilityId("facilityId-5");
		affectedEntity2.setReference("reference-5");
		affectedEntity2.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity3. This entity has a disturbanceFeedbackEntity.
		final var affectedEntity3 = new AffectedEntity();
		affectedEntity3.setPartyId(uuidFromInt(4).toString()); // Same as affectedEntity1 in order to test that we can have the same partyId on another AffectedEntity.
		affectedEntity3.setFacilityId("facilityId-6");
		affectedEntity3.setReference("reference-6");
		affectedEntity3.setDisturbanceEntity(disturbanceEntity);

		// Define a AffectedEntity-override list, where two of them has a disturbanceFeedbackEntity (id=4, id=6).
		final var affectedEntitiesOverride = new ArrayList<>(List.of(affectedEntity1, affectedEntity2, affectedEntity3));

		sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(messageConfigurationMock, times(2)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(2)).save(disturbanceFeedbackHistoryEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("Close subject for reference-6")
				.message("Close message for reference-6"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackHistoryEntityCaptor.getAllValues())
			.extracting(DisturbanceFeedbackHistoryEntity::getCategory, DisturbanceFeedbackHistoryEntity::getDisturbanceId, DisturbanceFeedbackHistoryEntity::getPartyId)
			.containsExactly(
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(4).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(4).toString()));
	}

	@Test
	void sendCloseMessageToProvidedApplicableAffectedsWhenNoAffectedsHasDisturbanceFeedback() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		// Define a AffectedEntity-override list with two elements.
		final var affectedEntitiesOverride = new ArrayList<AffectedEntity>();

		// AffectedEntity1.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");
		affectedEntity1.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity2.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");
		affectedEntity2.setDisturbanceEntity(disturbanceEntity);

		affectedEntitiesOverride.addAll(List.of(affectedEntity1, affectedEntity2));

		sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock, messageConfigurationMock);
	}

	@Test
	void sendUpdateMessage() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any()))
			.thenReturn(setupDisturbanceFeedbackEntityList(2, 4, 6));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(3)).save(disturbanceFeedbackHistoryEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(6)))
				.subject("Update subject for reference-6")
				.message("Update message for reference-6. Planned stop date 2021-11-10 18:30"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackHistoryEntityCaptor.getAllValues())
			.extracting(DisturbanceFeedbackHistoryEntity::getCategory, DisturbanceFeedbackHistoryEntity::getDisturbanceId, DisturbanceFeedbackHistoryEntity::getPartyId)
			.containsExactly(
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(2).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(4).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(6).toString()));
	}

	@Test
	void sendUpdateMessageWherePlannedStartAndStopDatesAreNotSet() {

		// Set up disturbanceEntity with 2 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2);
		disturbanceEntity.setPlannedStartDate(null);
		disturbanceEntity.setPlannedStopDate(null);

		// Let all of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(setupDisturbanceFeedbackEntityList(1, 2));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(2)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(2)).save(disturbanceFeedbackHistoryEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-2"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(2)))
				.subject("Update subject for reference-2")
				.message("Update message for reference-2. Planned stop date N/A"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackHistoryEntityCaptor.getAllValues())
			.extracting(DisturbanceFeedbackHistoryEntity::getCategory, DisturbanceFeedbackHistoryEntity::getDisturbanceId, DisturbanceFeedbackHistoryEntity::getPartyId)
			.containsExactly(
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(1).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(2).toString()));
	}

	@Test
	void sendUpdateMessageWhenNoAffectedsHasDisturbanceFeedback() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(messageConfigurationMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);
	}

	@Test
	void sendCreateMessageToAllApplicableAffecteds() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any()))
			.thenReturn(setupDisturbanceFeedbackEntityList(2, 4, 6));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(3)).save(disturbanceFeedbackHistoryEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(6)))
				.subject("New subject for reference-6")
				.message("New message for reference-6"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackHistoryEntityCaptor.getAllValues())
			.extracting(DisturbanceFeedbackHistoryEntity::getCategory, DisturbanceFeedbackHistoryEntity::getDisturbanceId, DisturbanceFeedbackHistoryEntity::getPartyId)
			.containsExactly(
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(2).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(4).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(6).toString()));
	}

	@Test
	void sendCreateMessageToAllApplicableAffectedsWhenNoAffectedsHasDisturbanceFeedback() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock, messageConfigurationMock);
	}

	@Test
	void sendCreateMessageToProvidedApplicableAffecteds() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an disturbanceFeedbackEntity.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(setupDisturbanceFeedbackEntityList(2, 4, 6));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		// AffectedEntity1. This entity has a disturbanceFeedbackEntity.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId(uuidFromInt(4).toString());
		affectedEntity1.setFacilityId("facilityId-4");
		affectedEntity1.setReference("reference-4");
		affectedEntity1.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity2. This entity doesn't have a disturbanceFeedbackEntity.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId(uuidFromInt(5).toString());
		affectedEntity2.setFacilityId("facilityId-5");
		affectedEntity2.setReference("reference-5");
		affectedEntity2.setDisturbanceEntity(disturbanceEntity);

		// AffectedEntity3. This entity has disturbanceFeedbackEntity.
		final var affectedEntity3 = new AffectedEntity();
		affectedEntity3.setPartyId(uuidFromInt(4).toString()); // Same as affectedEntity1 in order to test that we can have the same partyId on another AffectedEntity.
		affectedEntity3.setFacilityId("facilityId-6");
		affectedEntity3.setReference("reference-6");
		affectedEntity3.setDisturbanceEntity(disturbanceEntity);

		// Define a AffectedEntity-override list, where two of them has a disturbanceFeedbackEntity (id=4, id=6).
		final var affectedEntitiesOverride = new ArrayList<>(List.of(affectedEntity1, affectedEntity2, affectedEntity3));

		sendMessageLogic.sendCreateMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(messageConfigurationMock, times(2)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(2)).save(disturbanceFeedbackHistoryEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
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
					new Header().name(NameEnum.TYPE).values(List.of(String.valueOf(IssueType.DISTURBANCE))),
					new Header().name(NameEnum.FACILITY_ID).values(List.of(String.valueOf("facilityId-6"))),
					new Header().name(NameEnum.CATEGORY).values(List.of(String.valueOf(CATEGORY)))))
				.party(new MessageParty().partyId(uuidFromInt(4)))
				.subject("New subject for reference-6")
				.message("New message for reference-6"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackHistoryEntityCaptor.getAllValues())
			.extracting(DisturbanceFeedbackHistoryEntity::getCategory, DisturbanceFeedbackHistoryEntity::getDisturbanceId, DisturbanceFeedbackHistoryEntity::getPartyId)
			.containsExactly(
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(4).toString()),
				tuple(CATEGORY.toString(), DISTURBANCE_ID, uuidFromInt(4).toString()));
	}

	@Test
	void sendCreateMessageToProvidedApplicableAffectedsWhenNoAffectedsHasDisturbanceFeedback() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		// Define a AffectedEntity-override list with two elements.
		final var affectedEntitiesOverride = new ArrayList<AffectedEntity>();

		// AffectedEntity1.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		// AffectedEntity2.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		affectedEntitiesOverride.addAll(List.of(affectedEntity1, affectedEntity2));

		sendMessageLogic.sendCreateMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock, messageConfigurationMock);
	}

	@Test
	void sendCreateMessageToAllApplicableAffectedsWhenConfigIsNotActive() {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity(1, 2, 3, 4, 5, 6);

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any()))
			.thenReturn(setupDisturbanceFeedbackEntityList(2, 4, 6));

		// Setup message properties mock
		final var categoryConfigMock = Mockito.mock(CategoryConfig.class);
		when(categoryConfigMock.isActive()).thenReturn(false);
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(categoryConfigMock);

		sendMessageLogic.sendCreateMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock);
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

	private List<DisturbanceFeedbackEntity> setupDisturbanceFeedbackEntityList(final int... idNumbersOnAffecteds) {

		final var disturbanceFeedbackEntityList = new ArrayList<DisturbanceFeedbackEntity>();
		for (final var idNumberOnAffected : idNumbersOnAffecteds) {
			final var disturbanceFeedbackEntity = new DisturbanceFeedbackEntity();
			disturbanceFeedbackEntity.setCategory(CATEGORY.toString());
			disturbanceFeedbackEntity.setDisturbanceId(DISTURBANCE_ID);
			disturbanceFeedbackEntity.setPartyId(uuidFromInt(idNumberOnAffected).toString());

			disturbanceFeedbackEntityList.add(disturbanceFeedbackEntity);
		}

		return disturbanceFeedbackEntityList;
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
