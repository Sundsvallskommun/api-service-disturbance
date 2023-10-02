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
import java.util.ArrayList;
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
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.service.mapper.DisturbanceMapper;
import se.sundsvall.disturbance.service.message.SendMessageLogic;

@ExtendWith(MockitoExtension.class)
class DisturbanceServiceTest {

	@Mock
	private DisturbanceRepository disturbanceRepositoryMock;

	@Mock
	private SendMessageLogic sendMessageLogicMock;

	@InjectMocks
	private DisturbanceService disturbanceService;

	@Captor
	private ArgumentCaptor<DisturbanceEntity> disturbanceEntityCaptor;

	@Test
	void findByDisturbanceIdAndCategorySuccess() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setDisturbanceId(disturbanceId);
		disturbanceEntity.setCategory(category);
		disturbanceEntity.setStatus(status.toString());

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(category, disturbanceId)).thenReturn(Optional.of(disturbanceEntity));

		// Act
		final var disturbance = disturbanceService.findByCategoryAndDisturbanceId(category, disturbanceId);

		// Assert
		assertThat(disturbance).isNotNull();
		assertThat(disturbance.getCategory()).isEqualTo(Category.COMMUNICATION.toString());
		assertThat(disturbance.getId()).isEqualTo(disturbanceId);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void findByDisturbanceIdAndCategoryNotFound() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(category, disturbanceId)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.findByCategoryAndDisturbanceId(category, disturbanceId));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No disturbance found for category:'COMMUNICATION' and id:'12345'!");
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void createDisturbance() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION.toString())
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(empty());
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(disturbanceEntity);

		// Act
		final var disturbance = disturbanceService.createDisturbance(disturbanceCreateRequest);

		// Assert
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(Category.valueOf(disturbanceCreateRequest.getCategory()), disturbanceCreateRequest.getId());
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
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(Category.valueOf(disturbanceCreateRequest.getCategory()));
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus().toString());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenStatusIsClosed() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION.toString())
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.CLOSED)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(empty());
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(disturbanceEntity);

		// Act
		final var disturbance = disturbanceService.createDisturbance(disturbanceCreateRequest);

		// Assert
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(Category.valueOf(disturbanceCreateRequest.getCategory()), disturbanceCreateRequest.getId());
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
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(Category.valueOf(disturbanceCreateRequest.getCategory()));
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus().toString());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenStatusIsPlanned() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION.toString())
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.PLANNED)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(empty());
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(disturbanceEntity);

		// Act
		final var disturbance = disturbanceService.createDisturbance(disturbanceCreateRequest);

		// Assert
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(Category.valueOf(disturbanceCreateRequest.getCategory()), disturbanceCreateRequest.getId());
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
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(Category.valueOf(disturbanceCreateRequest.getCategory()));
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus().toString());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenAlreadyCreated() {

		// Arrange
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION.toString())
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(Optional.of(new DisturbanceEntity()));

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.createDisturbance(disturbanceCreateRequest));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Conflict: A disturbance with category:'COMMUNICATION' and id:'id' already exists!");
		assertThat(throwableProblem.getStatus()).isEqualTo(CONFLICT);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(Category.valueOf(disturbanceCreateRequest.getCategory()), disturbanceCreateRequest.getId());
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void findByPartyIdAndCategorySuccess() {

		// Arrange
		final var categoryFilter = List.of(Category.COMMUNICATION);
		final var partyId = "partyId";
		final var statusFilter = List.of(se.sundsvall.disturbance.api.model.Status.OPEN);

		when(disturbanceRepositoryMock.findByAffectedEntitiesPartyIdAndCategoryInAndStatusIn(partyId, categoryFilter, statusFilter)).thenReturn(createDisturbanceEntities());

		// Act
		final var disturbances = disturbanceService.findByPartyIdAndCategoryAndStatus(partyId, categoryFilter, statusFilter);

		// Assert
		assertThat(disturbances).isNotNull();
		assertThat(disturbances.get(0).getCategory()).isEqualTo(Category.COMMUNICATION.toString());
		assertThat(disturbances.get(0).getId()).isEqualTo("disturbanceId1");
		assertThat(disturbances.get(0).getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN);
		assertThat(disturbances.get(1).getCategory()).isEqualTo(Category.COMMUNICATION.toString());
		assertThat(disturbances.get(1).getId()).isEqualTo("disturbanceId2");
		assertThat(disturbances.get(1).getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN);

		verify(disturbanceRepositoryMock).findByAffectedEntitiesPartyIdAndCategoryInAndStatusIn(partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void findByPartyIdAndCategoryNotFound() {

		// Arrange
		final var categoryFilter = List.of(Category.COMMUNICATION);
		final var partyId = "partyId";
		final var statusFilter = List.of(se.sundsvall.disturbance.api.model.Status.OPEN);

		when(disturbanceRepositoryMock.findByAffectedEntitiesPartyIdAndCategoryInAndStatusIn(partyId, categoryFilter, statusFilter)).thenReturn(emptyList());

		// Act
		final var disturbances = disturbanceService.findByPartyIdAndCategoryAndStatus(partyId, categoryFilter, statusFilter);

		// Assert
		assertThat(disturbances).isNotNull().isEmpty();

		verify(disturbanceRepositoryMock).findByAffectedEntitiesPartyIdAndCategoryInAndStatusIn(partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock);
	}

	@Test
	void deleteByDisturbanceByIdAndCategory() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setDisturbanceId(disturbanceId);
		disturbanceEntity.setCategory(category);
		disturbanceEntity.setStatus(status.toString());

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(category, disturbanceId)).thenReturn(Optional.of(disturbanceEntity));

		// Act
		disturbanceService.deleteDisturbance(category, disturbanceId);

		final var updatedDisturbanceEntity = disturbanceEntity;
		updatedDisturbanceEntity.setDeleted(true);

		// Assert
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(sendMessageLogicMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getDeleted()).isTrue();
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(category);
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(status.toString());
	}

	@Test
	void deleteByDisturbanceByIdAndCategoryNotFound() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbanceId";

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(category, disturbanceId)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.deleteDisturbance(category, disturbanceId));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No disturbance found for category:'COMMUNICATION' and id:'disturbanceId'!");
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
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

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");
		e1.setFacilityId("facilityId-1");
		e1.setCoordinates("coordinate-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");
		e2.setFacilityId("facilityId-2");
		e2.setCoordinates("coordinate-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");
		e3.setFacilityId("facilityId-3");
		e3.setCoordinates("coordinate-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category);
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(se.sundsvall.disturbance.api.model.Status.OPEN.toString());
		existingDisturbanceEntity.setTitle(title);
		existingDisturbanceEntity.setDescription(description);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(plannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(List.of(e1, e2, e3));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCloseMessageToAllApplicableAffecteds(existingDisturbanceEntity);
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
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
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(category);
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(title);
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(description);
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.CLOSED.toString());
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

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");
		e1.setFacilityId("facilityId-1");
		e1.setCoordinates("coordinate-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");
		e2.setFacilityId("facilityId-2");
		e2.setCoordinates("coordinate-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");
		e3.setFacilityId("facilityId-3");
		e3.setCoordinates("coordinate-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category);
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(status.toString());
		existingDisturbanceEntity.setTitle(title);
		existingDisturbanceEntity.setDescription(description);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(plannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCloseMessageToProvidedApplicableAffecteds(existingDisturbanceEntity, List.of(e1));
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
		verify(disturbanceRepositoryMock).save(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities())
			.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
			.containsExactly(
				tuple("partyId-2", "reference-2", "facilityId-2", "coordinate-2"),
				tuple("partyId-3", "reference-3", "facilityId-3", "coordinate-3"));
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(category);
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(title);
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(description);
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN.toString());
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

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");
		e1.setFacilityId("facilityId-1");
		e1.setCoordinates("coordinate-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");
		e2.setFacilityId("facilityId-2");
		e2.setCoordinates("coordinate-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");
		e3.setFacilityId("facilityId-3");
		e3.setCoordinates("coordinate-3");

		final var e4 = new AffectedEntity();
		e4.setPartyId("partyId-4");
		e4.setReference("reference-4");
		e4.setFacilityId("facilityId-4");
		e4.setCoordinates("coordinate-4");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category);
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(status.toString());
		existingDisturbanceEntity.setTitle(existingTitle);
		existingDisturbanceEntity.setDescription(existingDescription);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(existingPlannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendUpdateMessage(disturbanceEntityCaptor.capture());
		verify(sendMessageLogicMock).sendCreateMessageToProvidedApplicableAffecteds(disturbanceEntityCaptor.capture(), eq(List.of(e4)));
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
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
			assertThat(updatedEntity.getCategory()).isEqualTo(category);
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(newTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(newDescription);
			assertThat(updatedEntity.getStatus()).isEqualTo(status.toString());
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

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest));

		// Assert
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No disturbance found for category:'COMMUNICATION' and id:'12345'!");
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
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

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category);
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(se.sundsvall.disturbance.api.model.Status.CLOSED.toString());

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest));

		// Assert
		assertThat(throwableProblem.getMessage())
			.isEqualTo("Conflict: The disturbance with category:'COMMUNICATION' and id:'12345' is closed! No updates are allowed on closed disturbances!");
		assertThat(throwableProblem.getStatus()).isEqualTo(CONFLICT);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
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

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");
		e1.setFacilityId("facilityId-1");
		e1.setCoordinates("coordinate-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");
		e2.setFacilityId("facilityId-2");
		e2.setCoordinates("coordinate-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");
		e3.setFacilityId("facilityId-3");
		e3.setCoordinates("coordinate-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category);
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(status.toString());
		existingDisturbanceEntity.setTitle(existingTitle);
		existingDisturbanceEntity.setDescription(existingDescription);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(existingPlannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
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
			assertThat(updatedEntity.getCategory()).isEqualTo(category);
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(newTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(newDescription);
			assertThat(updatedEntity.getStatus()).isEqualTo(status.toString());
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

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");
		e1.setFacilityId("facilityId-1");
		e1.setCoordinates("coordinate-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");
		e2.setFacilityId("facilityId-2");
		e2.setCoordinates("coordinate-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");
		e3.setFacilityId("facilityId-3");
		e3.setCoordinates("coordinate-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category);
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(existingStatus.toString());
		existingDisturbanceEntity.setTitle(existingTitle);
		existingDisturbanceEntity.setDescription(existingDescription);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(existingPlannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceId(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.save(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		// Act
		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		// Assert
		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCreateMessageToAllApplicableAffecteds(disturbanceEntityCaptor.capture()); // New message is sent when status goes from PLANNED -> OPEN.
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceId(category, disturbanceId);
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
			assertThat(updatedEntity.getCategory()).isEqualTo(category);
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(existingTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(existingDescription);
			assertThat(updatedEntity.getStatus()).isEqualTo(newStatus.toString());
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

		when(disturbanceRepositoryMock.findByStatusAndCategory(any(), any())).thenReturn(disturbanceEntitiesMock);
		try (MockedStatic<DisturbanceMapper> disturbanceMapperMock = Mockito.mockStatic(DisturbanceMapper.class)) {
			disturbanceMapperMock.when(() -> DisturbanceMapper.toDisturbances(any())).thenReturn(disturbancesMock);

			final var result = disturbanceService.findByStatusAndCategory(statusFilterMock, categoryFilterMock);

			verify(disturbanceRepositoryMock).findByStatusAndCategory(same(statusFilterMock), same(categoryFilterMock));
			disturbanceMapperMock.verify(() -> DisturbanceMapper.toDisturbances(same(disturbanceEntitiesMock)));
			assertThat(result).isSameAs(disturbancesMock);
		}
	}

	private List<DisturbanceEntity> createDisturbanceEntities() {
		final var disturbanceEntity1 = new DisturbanceEntity();
		disturbanceEntity1.setDisturbanceId("disturbanceId1");
		disturbanceEntity1.setCategory(Category.COMMUNICATION);
		disturbanceEntity1.setStatus(se.sundsvall.disturbance.api.model.Status.OPEN.toString());

		final var disturbanceEntity2 = new DisturbanceEntity();
		disturbanceEntity2.setDisturbanceId("disturbanceId2");
		disturbanceEntity2.setCategory(Category.COMMUNICATION);
		disturbanceEntity2.setStatus(se.sundsvall.disturbance.api.model.Status.OPEN.toString());

		return List.of(disturbanceEntity1, disturbanceEntity2);
	}
}
