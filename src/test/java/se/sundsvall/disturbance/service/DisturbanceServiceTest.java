package se.sundsvall.disturbance.service;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbanceEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.service.mapper.DisturbanceMapper;
import se.sundsvall.disturbance.service.message.SendMessageLogic;

@ExtendWith(MockitoExtension.class)
class DisturbanceServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private DisturbanceRepository disturbanceRepositoryMock;

	@Mock
	private SendMessageLogic sendMessageLogicMock;

	@InjectMocks
	private DisturbanceService disturbanceService;

	@Captor
	private ArgumentCaptor<DisturbanceEntity> disturbanceEntityCaptor;

	@Test
	void findByMunicipalityIdAndCategoryAndDisturbanceId() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var status = Status.OPEN;

		final var disturbanceEntity = DisturbanceEntity.create()
			.withDisturbanceId(disturbanceId)
			.withCategory(category)
			.withStatus(status);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(disturbanceEntity));

		// Act
		final var disturbance = disturbanceService.findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);

		// Assert
		assertThat(disturbance).isNotNull();
		assertThat(disturbance.getCategory()).isEqualByComparingTo(Category.COMMUNICATION);
		assertThat(disturbance.getId()).isEqualTo(disturbanceId);

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void findByMunicipalityIdAndCategoryAndDisturbanceIdNotFound() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No disturbance found for category:'COMMUNICATION' and id:'12345'!");
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void createDisturbance() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(Status.OPEN)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(MUNICIPALITY_ID, disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(empty());
		when(disturbanceRepositoryMock.save(any())).thenReturn(disturbanceEntity);

		// Act
		final var disturbance = disturbanceService.createDisturbance(MUNICIPALITY_ID, disturbanceCreateRequest);

		// Assert
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verify(sendMessageLogicMock).sendCreateMessageToAllApplicableAffecteds(disturbanceEntity);

		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities())
			.hasSize(3) // Duplicates removed.
			.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference)
			.containsExactly(
				tuple("partyId-1", "reference-1"),
				tuple("partyId-2", "reference-2"),
				tuple("partyId-3", "reference-3"));
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(disturbanceCreateRequest.getCategory());
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenStatusIsClosed() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(Status.CLOSED)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(MUNICIPALITY_ID, disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(empty());
		when(disturbanceRepositoryMock.save(any())).thenReturn(disturbanceEntity);

		// Act
		final var disturbance = disturbanceService.createDisturbance(MUNICIPALITY_ID, disturbanceCreateRequest);

		// Assert
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock); // No interactions here if status is CLOSED.

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities())
			.hasSize(3) // Duplicates removed.
			.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference)
			.containsExactly(
				tuple("partyId-1", "reference-1"),
				tuple("partyId-2", "reference-2"),
				tuple("partyId-3", "reference-3"));
		assertThat(disturbanceEntityCaptorValue.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(disturbanceCreateRequest.getCategory());
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualByComparingTo(disturbanceCreateRequest.getStatus());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenStatusIsPlanned() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(Status.PLANNED)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(MUNICIPALITY_ID, disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(empty());
		when(disturbanceRepositoryMock.save(any())).thenReturn(disturbanceEntity);

		// Act
		final var disturbance = disturbanceService.createDisturbance(MUNICIPALITY_ID, disturbanceCreateRequest);

		// Assert
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());

		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock); // No interactions here if status is PLANNED.

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).hasSize(3); // Duplicates removed.
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities())
			.hasSize(3) // Duplicates removed.
			.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference)
			.containsExactly(
				tuple("partyId-1", "reference-1"),
				tuple("partyId-2", "reference-2"),
				tuple("partyId-3", "reference-3"));
		assertThat(disturbanceEntityCaptorValue.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(disturbanceCreateRequest.getCategory());
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualByComparingTo(disturbanceCreateRequest.getStatus());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenAlreadyCreated() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(Status.OPEN)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(DisturbanceEntity.create()));

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.createDisturbance(MUNICIPALITY_ID, disturbanceCreateRequest));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Conflict: A disturbance with category:'COMMUNICATION' and id:'id' already exists!");
		assertThat(throwableProblem.getStatus()).isEqualTo(CONFLICT);

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void findByPartyIdAndCategorySuccess() {

		// Arrange
		final var categoryFilter = List.of(Category.COMMUNICATION);
		final var partyId = "partyId";
		final var statusFilter = List.of(Status.OPEN);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(any(), any(), any(), any())).thenReturn(createDisturbanceEntities());

		// Act
		final var disturbances = disturbanceService.findByMunicipalityIdAndPartyIdAndCategoryAndStatus(MUNICIPALITY_ID, partyId, categoryFilter, statusFilter);

		// Assert
		assertThat(disturbances).isNotNull();
		assertThat(disturbances.get(0).getCategory()).isEqualByComparingTo(Category.COMMUNICATION);
		assertThat(disturbances.get(0).getId()).isEqualTo("disturbanceId1");
		assertThat(disturbances.get(0).getStatus()).isEqualByComparingTo(Status.OPEN);
		assertThat(disturbances.get(1).getCategory()).isEqualByComparingTo(Category.COMMUNICATION);
		assertThat(disturbances.get(1).getId()).isEqualTo("disturbanceId2");
		assertThat(disturbances.get(1).getStatus()).isEqualByComparingTo(Status.OPEN);

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void findByPartyIdAndCategoryNotFound() {

		// Arrange
		final var categoryFilter = List.of(Category.COMMUNICATION);
		final var partyId = "partyId";
		final var statusFilter = List.of(Status.OPEN);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(any(), any(), any(), any())).thenReturn(emptyList());

		// Act
		final var disturbances = disturbanceService.findByMunicipalityIdAndPartyIdAndCategoryAndStatus(MUNICIPALITY_ID, partyId, categoryFilter, statusFilter);

		// Assert
		assertThat(disturbances).isNotNull().isEmpty();

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void deleteByDisturbanceByIdAndCategory() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var status = Status.OPEN;

		final var disturbanceEntity = DisturbanceEntity.create()
			.withDisturbanceId(disturbanceId)
			.withCategory(category)
			.withStatus(status);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(disturbanceEntity));

		// Act
		disturbanceService.deleteDisturbance(MUNICIPALITY_ID, category, disturbanceId);

		// Assert
		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(sendMessageLogicMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getDeleted()).isTrue();
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualByComparingTo(category);
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualByComparingTo(status);
	}

	@Test
	void deleteByDisturbanceByIdAndCategoryNotFound() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbanceId";

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.deleteDisturbance(MUNICIPALITY_ID, category, disturbanceId));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No disturbance found for category:'COMMUNICATION' and id:'disturbanceId'!");
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void updateDisturbanceChangeStatusToClosed() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var title = "title";
		final var description = "description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 6).atOffset(now(systemDefault()).getOffset());
		final var plannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 6).atOffset(now(systemDefault()).getOffset());

		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withStatus(se.sundsvall.disturbance.api.model.Status.CLOSED);

		final var e1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1")
			.withFacilityId("facilityId-1")
			.withCoordinates("coordinate-1");

		final var e2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2")
			.withFacilityId("facilityId-2")
			.withCoordinates("coordinate-2");

		final var e3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3")
			.withFacilityId("facilityId-3")
			.withCoordinates("coordinate-3");

		final var existingDisturbanceEntity = DisturbanceEntity.create()
			.withCategory(category)
			.withDisturbanceId(disturbanceId)
			.withStatus(Status.OPEN)
			.withTitle(title)
			.withDescription(description)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(plannedStopDate)
			.withAffectedEntities(List.of(e1, e2, e3));

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any())).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCloseMessageToAllApplicableAffecteds(existingDisturbanceEntity);
		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities())
			.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
			.containsExactly(
				tuple("partyId-1", "reference-1", "facilityId-1", "coordinate-1"),
				tuple("partyId-2", "reference-2", "facilityId-2", "coordinate-2"),
				tuple("partyId-3", "reference-3", "facilityId-3", "coordinate-3"));
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualByComparingTo(category);
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(title);
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(description);
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualByComparingTo(Status.CLOSED);
	}

	@Test
	void updateDisturbanceRemoveAffectedsFromDisturbance() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;
		final var disturbanceId = "12345";
		final var title = "title";
		final var description = "description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 6).atOffset(now(systemDefault()).getOffset());
		final var plannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 6).atOffset(now(systemDefault()).getOffset());

		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withAffecteds(List.of(
				// partyId-1 removed (compared to existing entity)
				Affected.create().withPartyId("partyId-2").withReference("reference-2").withFacilityId("facilityId-2").withCoordinates("coordinate-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2").withFacilityId("facilityId-2").withCoordinates("coordinate-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3").withFacilityId("facilityId-3").withCoordinates("coordinate-3")));

		final var e1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1")
			.withFacilityId("facilityId-1")
			.withCoordinates("coordinate-1");

		final var e2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2")
			.withFacilityId("facilityId-2")
			.withCoordinates("coordinate-2");

		final var e3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3")
			.withFacilityId("facilityId-3")
			.withCoordinates("coordinate-3");

		final var existingDisturbanceEntity = DisturbanceEntity.create()
			.withCategory(category)
			.withDisturbanceId(disturbanceId)
			.withStatus(status)
			.withTitle(title)
			.withDescription(description)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(plannedStopDate)
			.withAffectedEntities(List.of(e1, e2, e3));

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any())).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCloseMessageToProvidedApplicableAffecteds(existingDisturbanceEntity, List.of(e1));
		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities())
			.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
			.containsExactly(
				tuple("partyId-2", "reference-2", "facilityId-2", "coordinate-2"),
				tuple("partyId-3", "reference-3", "facilityId-3", "coordinate-3"));
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualByComparingTo(category);
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(title);
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(description);
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualByComparingTo(Status.OPEN);
	}

	@Test
	void updateDisturbanceChangeContent() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var existingTitle = "title";
		final var newTitle = "new title";
		final var existingDescription = "description";
		final var newDescription = "new description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 0).atOffset(now(systemDefault()).getOffset());
		final var existingPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now(systemDefault()).getOffset());
		final var newPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now(systemDefault()).getOffset());
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;

		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withTitle(newTitle)
			.withDescription(newDescription)
			.withPlannedStopDate(newPlannedStopDate)
			.withAffecteds(List.of(
				// partyId-4 added (compared to existing entity)
				Affected.create().withPartyId("partyId-1").withReference("reference-1").withFacilityId("facilityId-1").withCoordinates("coordinate-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2").withFacilityId("facilityId-2").withCoordinates("coordinate-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3").withFacilityId("facilityId-3").withCoordinates("coordinate-3"),
				Affected.create().withPartyId("partyId-4").withReference("reference-4").withFacilityId("facilityId-4").withCoordinates("coordinate-4")));

		final var e1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1")
			.withFacilityId("facilityId-1")
			.withCoordinates("coordinate-1");

		final var e2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2")
			.withFacilityId("facilityId-2")
			.withCoordinates("coordinate-2");

		final var e3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3")
			.withFacilityId("facilityId-3")
			.withCoordinates("coordinate-3");

		final var e4 = AffectedEntity.create()
			.withPartyId("partyId-4")
			.withReference("reference-4")
			.withFacilityId("facilityId-4")
			.withCoordinates("coordinate-4");

		final var existingDisturbanceEntity = DisturbanceEntity.create()
			.withCategory(category)
			.withDisturbanceId(disturbanceId)
			.withStatus(status)
			.withTitle(existingTitle)
			.withDescription(existingDescription)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(existingPlannedStopDate)
			.withAffectedEntities(List.of(e1, e2, e3));

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any())).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendUpdateMessage(disturbanceEntityCaptor.capture());
		verify(sendMessageLogicMock).sendCreateMessageToProvidedApplicableAffecteds(disturbanceEntityCaptor.capture(), eq(List.of(e4)));
		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());

		// Loop through the captor values (for sendMessageLogicMock and disturbanceRepositoryMock).
		disturbanceEntityCaptor.getAllValues().stream().forEach(updatedEntity -> {
			assertThat(updatedEntity).isNotNull();
			assertThat(updatedEntity.getAffectedEntities())
				.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
				.containsExactly(
					tuple("partyId-1", "reference-1", "facilityId-1", "coordinate-1"),
					tuple("partyId-2", "reference-2", "facilityId-2", "coordinate-2"),
					tuple("partyId-3", "reference-3", "facilityId-3", "coordinate-3"),
					tuple("partyId-4", "reference-4", "facilityId-4", "coordinate-4"));
			assertThat(updatedEntity.getCategory()).isEqualByComparingTo(category);
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(newTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(newDescription);
			assertThat(updatedEntity.getStatus()).isEqualByComparingTo(status);
			assertThat(updatedEntity.getPlannedStartDate()).isEqualTo(plannedStartDate);
			assertThat(updatedEntity.getPlannedStopDate()).isEqualTo(newPlannedStopDate);
		});
	}

	@Test
	void updateDisturbanceWhenDisturbanceDoesntExist() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withStatus(se.sundsvall.disturbance.api.model.Status.CLOSED);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, disturbanceUpdateRequest));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No disturbance found for category:'COMMUNICATION' and id:'12345'!");
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void updateDisturbanceWhenStatusIsClosed() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withDescription("Test");

		final var existingDisturbanceEntity = DisturbanceEntity.create()
			.withCategory(category)
			.withDisturbanceId(disturbanceId)
			.withStatus(Status.CLOSED);

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(existingDisturbanceEntity));

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, disturbanceUpdateRequest));

		// Assert
		assertThat(throwableProblem.getMessage())
			.isEqualTo("Conflict: The disturbance with category:'COMMUNICATION' and id:'12345' is closed! No updates are allowed on closed disturbances!");
		assertThat(throwableProblem.getStatus()).isEqualTo(CONFLICT);

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void updateDisturbanceWhenStatusIsPlanned() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var existingTitle = "title";
		final var newTitle = "new title";
		final var existingDescription = "description";
		final var newDescription = "new description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 0).atOffset(now(systemDefault()).getOffset());
		final var existingPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now(systemDefault()).getOffset());
		final var newPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now(systemDefault()).getOffset());
		final var status = se.sundsvall.disturbance.api.model.Status.PLANNED;

		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withTitle(newTitle)
			.withDescription(newDescription)
			.withPlannedStopDate(newPlannedStopDate);

		final var e1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1")
			.withFacilityId("facilityId-1")
			.withCoordinates("coordinate-1");

		final var e2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2")
			.withFacilityId("facilityId-2")
			.withCoordinates("coordinate-2");

		final var e3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3")
			.withFacilityId("facilityId-3")
			.withCoordinates("coordinate-3");

		final var existingDisturbanceEntity = DisturbanceEntity.create()
			.withCategory(category)
			.withDisturbanceId(disturbanceId)
			.withStatus(status)
			.withTitle(existingTitle)
			.withDescription(existingDescription)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(existingPlannedStopDate)
			.withAffectedEntities(List.of(e1, e2, e3));

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any())).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock); // No messages sent if status is PLANNED.

		// Loop through the captor values (for sendMessageLogicMock and disturbanceRepositoryMock).
		disturbanceEntityCaptor.getAllValues().stream().forEach(updatedEntity -> {
			assertThat(updatedEntity).isNotNull();
			assertThat(updatedEntity.getAffectedEntities())
				.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
				.containsExactly(
					tuple("partyId-1", "reference-1", "facilityId-1", "coordinate-1"),
					tuple("partyId-2", "reference-2", "facilityId-2", "coordinate-2"),
					tuple("partyId-3", "reference-3", "facilityId-3", "coordinate-3"));
			assertThat(updatedEntity.getCategory()).isEqualByComparingTo(category);
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(newTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(newDescription);
			assertThat(updatedEntity.getStatus()).isEqualByComparingTo(status);
			assertThat(updatedEntity.getPlannedStartDate()).isEqualTo(plannedStartDate);
			assertThat(updatedEntity.getPlannedStopDate()).isEqualTo(newPlannedStopDate);
		});
	}

	@Test
	void updateDisturbanceWhenStatusIsChangedFromPlannedToOpen() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var existingTitle = "title";
		final var existingDescription = "description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 0).atOffset(now(systemDefault()).getOffset());
		final var existingPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now(systemDefault()).getOffset());
		final var newPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now(systemDefault()).getOffset());
		final var existingStatus = se.sundsvall.disturbance.api.model.Status.PLANNED;
		final var newStatus = se.sundsvall.disturbance.api.model.Status.OPEN;

		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withStatus(newStatus);

		final var e1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1")
			.withFacilityId("facilityId-1")
			.withCoordinates("coordinate-1");

		final var e2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2")
			.withFacilityId("facilityId-2")
			.withCoordinates("coordinate-2");

		final var e3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3")
			.withFacilityId("facilityId-3")
			.withCoordinates("coordinate-3");

		final var existingDisturbanceEntity = DisturbanceEntity.create()
			.withCategory(category)
			.withDisturbanceId(disturbanceId)
			.withStatus(existingStatus)
			.withTitle(existingTitle)
			.withDescription(existingDescription)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(existingPlannedStopDate)
			.withAffectedEntities(List.of(e1, e2, e3));

		when(disturbanceRepositoryMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any())).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCreateMessageToAllApplicableAffecteds(disturbanceEntityCaptor.capture()); // New message is sent when status goes from PLANNED -> OPEN.
		verify(disturbanceRepositoryMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);

		// Loop through the captor values (for sendMessageLogicMock and disturbanceRepositoryMock).
		disturbanceEntityCaptor.getAllValues().stream().forEach(updatedEntity -> {
			assertThat(updatedEntity).isNotNull();
			assertThat(updatedEntity.getAffectedEntities())
				.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
				.containsExactly(
					tuple("partyId-1", "reference-1", "facilityId-1", "coordinate-1"),
					tuple("partyId-2", "reference-2", "facilityId-2", "coordinate-2"),
					tuple("partyId-3", "reference-3", "facilityId-3", "coordinate-3"));
			assertThat(updatedEntity.getCategory()).isEqualByComparingTo(category);
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(existingTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(existingDescription);
			assertThat(updatedEntity.getStatus()).isEqualByComparingTo(newStatus);
			assertThat(updatedEntity.getPlannedStartDate()).isEqualTo(plannedStartDate);
			assertThat(updatedEntity.getPlannedStopDate()).isEqualTo(newPlannedStopDate);
		});
	}

	@Test
	void findByCategoryAndStatus(
		@Mock List<Category> categoryFilterMock,
		@Mock List<se.sundsvall.disturbance.api.model.Status> statusFilterMock,
		@Mock List<DisturbanceEntity> disturbanceEntitiesMock,
		@Mock List<Disturbance> disturbancesMock) {

		when(disturbanceRepositoryMock.findByMunicipalityIdAndStatusAndCategory(any(), any(), any())).thenReturn(disturbanceEntitiesMock);
		try (MockedStatic<DisturbanceMapper> disturbanceMapperMock = Mockito.mockStatic(DisturbanceMapper.class)) {
			disturbanceMapperMock.when(() -> DisturbanceMapper.toDisturbances(any())).thenReturn(disturbancesMock);

			final var result = disturbanceService.findByMunicipalityIdAndStatusAndCategory(MUNICIPALITY_ID, statusFilterMock, categoryFilterMock);

			verify(disturbanceRepositoryMock).findByMunicipalityIdAndStatusAndCategory(eq(MUNICIPALITY_ID), same(statusFilterMock), same(categoryFilterMock));
			disturbanceMapperMock.verify(() -> DisturbanceMapper.toDisturbances(same(disturbanceEntitiesMock)));
			assertThat(result).isSameAs(disturbancesMock);
		}
	}

	private List<DisturbanceEntity> createDisturbanceEntities() {
		return List.of(
			DisturbanceEntity.create()
				.withDisturbanceId("disturbanceId1")
				.withCategory(Category.COMMUNICATION)
				.withStatus(Status.OPEN),
			DisturbanceEntity.create()
				.withDisturbanceId("disturbanceId2")
				.withCategory(Category.COMMUNICATION)
				.withStatus(Status.OPEN));
	}
}
