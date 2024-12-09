package se.sundsvall.disturbance.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

@ExtendWith(MockitoExtension.class)
class DisturbanceMapperTest {

	@Test
	void toDisturbanceSuccess() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1")
			.withFacilityId("facilityId-1")
			.withCoordinates("coordinate-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2")
			.withFacilityId("facilityId-2")
			.withCoordinates("coordinate-2");

		final var disturbanceEntity = DisturbanceEntity.create()
			.withDisturbanceId("disturbanceId")
			.withCategory(Category.COMMUNICATION)
			.withDescription("description")
			.withTitle("title")
			.withStatus(Status.OPEN)
			.withPlannedStartDate(now(systemDefault()).plusDays(1))
			.withPlannedStopDate(now(systemDefault()).plusDays(2))
			.withCreated(now(systemDefault()))
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity2));

		final var disturbance = DisturbanceMapper.toDisturbance(disturbanceEntity);

		assertThat(disturbance.getCategory()).isEqualByComparingTo(Category.COMMUNICATION);
		assertThat(disturbance.getId()).isEqualTo("disturbanceId");
		assertThat(disturbance.getDescription()).isEqualTo("description");
		assertThat(disturbance.getStatus()).isEqualByComparingTo(Status.OPEN);
		assertThat(disturbance.getPlannedStartDate()).isCloseTo(now(systemDefault()).plusDays(1), within(2, SECONDS));
		assertThat(disturbance.getPlannedStopDate()).isCloseTo(now(systemDefault()).plusDays(2), within(2, SECONDS));
		assertThat(disturbance.getCreated()).isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(disturbance.getAffecteds())
			.extracting(Affected::getFacilityId, Affected::getCoordinates, Affected::getPartyId, Affected::getReference)
			.containsExactly(
				tuple("facilityId-1", "coordinate-1", "partyId-1", "reference-1"),
				tuple("facilityId-2", "coordinate-2", "partyId-2", "reference-2"));
	}

	@Test
	void toDisturbanceEntityFromDisturbanceCreateRequest() {

		final var municipalityId = "municipalityId";
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withDescription("Description")
			.withId("id")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")))
			.withPlannedStartDate(now(systemDefault()))
			.withPlannedStopDate(now(systemDefault()).plusDays(1))
			.withStatus(Status.OPEN)
			.withTitle("Title");

		final var disturbanceEntity = DisturbanceMapper.toDisturbanceEntity(municipalityId, disturbanceCreateRequest);

		assertThat(disturbanceEntity.getAffectedEntities())
			.hasSize(3) // Duplicates removed.
			.extracting(AffectedEntity::getPartyId, AffectedEntity::getReference)
			.containsExactly(
				tuple("partyId-1", "reference-1"),
				tuple("partyId-2", "reference-2"),
				tuple("partyId-3", "reference-3"));
		assertThat(disturbanceEntity.getCategory()).isEqualByComparingTo(Category.COMMUNICATION);
		assertThat(disturbanceEntity.getDescription()).isEqualTo("Description");
		assertThat(disturbanceEntity.getDisturbanceId()).isEqualTo("id");
		assertThat(disturbanceEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(disturbanceEntity.getPlannedStartDate()).isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(disturbanceEntity.getPlannedStopDate()).isCloseTo(now(systemDefault()).plusDays(1), within(2, SECONDS));
		assertThat(disturbanceEntity.getStatus()).isEqualByComparingTo(Status.OPEN);
		assertThat(disturbanceEntity.getTitle()).isEqualTo("Title");
	}

	@Test
	void toDisturbanceEntityFromDisturbanceUpdateRequest() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbanceId";
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withDescription("Description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1").withFacilityId("facility-1").withCoordinates("coordinate-1"),
				Affected.create().withPartyId("partyId-1").withReference("reference-1").withFacilityId("facility-1").withCoordinates("coordinate-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2").withFacilityId("facility-2").withCoordinates("coordinate-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2").withFacilityId("facility-2").withCoordinates("coordinate-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3").withFacilityId("facility-3").withCoordinates("coordinate-3")))
			.withPlannedStartDate(now(systemDefault()))
			.withPlannedStopDate(now(systemDefault()).plusDays(1))
			.withStatus(Status.OPEN);

		final var disturbanceEntity = DisturbanceMapper.toDisturbanceEntity(category, disturbanceId, disturbanceUpdateRequest);

		assertThat(disturbanceEntity.getAffectedEntities())
			.hasSize(3) // Duplicates removed.
			.extracting(AffectedEntity::getReference, AffectedEntity::getPartyId, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
			.containsExactly(
				tuple("reference-1", "partyId-1", "facility-1", "coordinate-1"),
				tuple("reference-2", "partyId-2", "facility-2", "coordinate-2"),
				tuple("reference-3", "partyId-3", "facility-3", "coordinate-3"));
		assertThat(disturbanceEntity.getCategory()).isEqualByComparingTo(category);
		assertThat(disturbanceEntity.getDescription()).isEqualTo("Description");
		assertThat(disturbanceEntity.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntity.getPlannedStartDate()).isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(disturbanceEntity.getPlannedStopDate()).isCloseTo(now(systemDefault()).plusDays(1), within(2, SECONDS));
		assertThat(disturbanceEntity.getStatus()).isEqualByComparingTo(Status.OPEN);
		assertThat(disturbanceEntity.getTitle()).isNull();
	}

	@Test
	void toMergedDisturbanceEntityAllNewValuesSet() {

		/**
		 * Set up old entity.
		 */

		final var oldAffected1 = AffectedEntity.create()
			.withFacilityId("oldFacility-1")
			.withCoordinates("oldCoordinate-1")
			.withPartyId("oldpartyId-1")
			.withReference("oldReference-1");

		final var oldAffected2 = AffectedEntity.create()
			.withFacilityId("oldFacility-2")
			.withCoordinates("oldCoordinate-2")
			.withPartyId("oldPartyId-2")
			.withReference("oldReference-2");

		final var oldAffected3 = AffectedEntity.create()
			.withFacilityId("oldFacility-3")
			.withCoordinates("oldCoordinate-3")
			.withPartyId("oldPartyId-3")
			.withReference("oldReference-3");

		final var oldEntity = DisturbanceEntity.create()
			.withId(1L)
			.withMunicipalityId("oldMunicipalityId")
			.withDisturbanceId("oldDisturbanceId")
			.withCategory(Category.ELECTRICITY)
			.withDescription("oldDescription")
			.withTitle("oldTitle")
			.withStatus(Status.CLOSED)
			.withPlannedStartDate(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withPlannedStopDate(now(systemDefault()).plusDays(new Random().nextInt(1, 1000)))
			.withCreated(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withUpdated(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withAffectedEntities(List.of(oldAffected1, oldAffected2, oldAffected3));

		/**
		 * Set up new entity.
		 */

		final var newAffected1 = AffectedEntity.create()
			.withFacilityId("newFacility-1")
			.withCoordinates("newCoordinate-1")
			.withPartyId("newPartyId-1")
			.withReference("newReference-1");

		final var newAffected2 = AffectedEntity.create()
			.withFacilityId("newFacility-2")
			.withCoordinates("newCoordinate-2")
			.withPartyId("newPartyId-2")
			.withReference("newReference-2");

		final var newEntity = DisturbanceEntity.create()
			.withId(0L)
			.withMunicipalityId("newMunicipalityId")
			.withDisturbanceId("newDisturbanceId")
			.withCategory(Category.WATER)
			.withDescription("newDescription")
			.withTitle("newTitle")
			.withStatus(Status.OPEN)
			.withPlannedStartDate(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withPlannedStopDate(now(systemDefault()).plusDays(new Random().nextInt(1, 1000)))
			.withCreated(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withUpdated(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withAffectedEntities(List.of(newAffected1, newAffected2));

		final var mergedDisturbanceEntity = DisturbanceMapper.toMergedDisturbanceEntity(oldEntity, newEntity);

		assertThat(mergedDisturbanceEntity).isNotNull();
		assertThat(mergedDisturbanceEntity.getAffectedEntities()).isEqualTo(List.of(newAffected1, newAffected2));
		assertThat(mergedDisturbanceEntity.getCategory()).isEqualTo(oldEntity.getCategory());
		assertThat(mergedDisturbanceEntity.getCreated()).isEqualTo(oldEntity.getCreated());
		assertThat(mergedDisturbanceEntity.getDescription()).isEqualTo(newEntity.getDescription());
		assertThat(mergedDisturbanceEntity.getDisturbanceId()).isEqualTo(oldEntity.getDisturbanceId());
		assertThat(mergedDisturbanceEntity.getId()).isEqualTo(oldEntity.getId());
		assertThat(mergedDisturbanceEntity.getMunicipalityId()).isEqualTo(oldEntity.getMunicipalityId());
		assertThat(mergedDisturbanceEntity.getPlannedStartDate()).isEqualTo(newEntity.getPlannedStartDate());
		assertThat(mergedDisturbanceEntity.getPlannedStopDate()).isEqualTo(newEntity.getPlannedStopDate());
		assertThat(mergedDisturbanceEntity.getStatus()).isEqualTo(newEntity.getStatus());
		assertThat(mergedDisturbanceEntity.getTitle()).isEqualTo(newEntity.getTitle());
		assertThat(mergedDisturbanceEntity.getUpdated()).isEqualTo(oldEntity.getUpdated());
	}

	@Test
	void toMergedDisturbanceEntityNoNewValuesSet() {

		/**
		 * Set up old entity.
		 */

		final var oldAffected1 = AffectedEntity.create()
			.withFacilityId("oldFacility-1")
			.withCoordinates("oldCoordinate-1")
			.withPartyId("oldpartyId-1")
			.withReference("oldReference-1");

		final var oldAffected2 = AffectedEntity.create()
			.withFacilityId("oldFacility-2")
			.withCoordinates("oldCoordinate-2")
			.withPartyId("oldPartyId-2")
			.withReference("oldReference-2");

		final var oldAffected3 = AffectedEntity.create()
			.withFacilityId("oldFacility-3")
			.withCoordinates("oldCoordinate-3")
			.withPartyId("oldPartyId-3")
			.withReference("oldReference-3");

		final var oldEntity = DisturbanceEntity.create()
			.withId(1L)
			.withDisturbanceId("oldDisturbanceId")
			.withCategory(Category.ELECTRICITY)
			.withDescription("oldDescription")
			.withTitle("oldTitle")
			.withStatus(Status.CLOSED)
			.withPlannedStartDate(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withPlannedStopDate(now(systemDefault()).plusDays(new Random().nextInt(1, 1000)))
			.withCreated(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withUpdated(now(systemDefault()).minusDays(new Random().nextInt(1, 1000)))
			.withAffectedEntities(List.of(oldAffected1, oldAffected2, oldAffected3));

		final var mergedDisturbanceEntity = DisturbanceMapper.toMergedDisturbanceEntity(oldEntity, new DisturbanceEntity());

		assertThat(mergedDisturbanceEntity).isEqualTo(oldEntity);
	}
}
