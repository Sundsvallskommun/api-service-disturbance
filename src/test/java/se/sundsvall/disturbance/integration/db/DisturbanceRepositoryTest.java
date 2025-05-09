package se.sundsvall.disturbance.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.disturbance.api.model.Category.COMMUNICATION;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;
import static se.sundsvall.disturbance.api.model.Status.OPEN;
import static se.sundsvall.disturbance.api.model.Status.PLANNED;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

/**
 * Disturbance repository tests.
 *
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class DisturbanceRepositoryTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String DISTURBANCE_ID_2 = "disturbance-2";
	private static final String PARTY_ID_1 = "0d64beb2-3aea-11ec-8d3d-0242ac130003"; // Exists in "disturbance-2".
	private static final String PARTY_ID_2 = "0d64c132-3aea-11ec-8d3d-0242ac130003"; // Exists in "disturbance-2".
	private static final String PARTY_ID_3 = "0d64c42a-3aea-11ec-8d3d-0242ac130003"; // Exists in "disturbance-2".

	@Autowired
	private DisturbanceRepository disturbanceRepository;

	@Test
	void findByMunicipalityIdAndCategoryAndDisturbanceId() {
		final var disturbanceOptional = disturbanceRepository.findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, COMMUNICATION, DISTURBANCE_ID_2);

		assertThat(disturbanceOptional).isPresent();
		assertAsDisturbanceEntity2(disturbanceOptional.get());
	}

	@Test
	void persistAndFetch() {

		final var disturbanceEntity = setupNewDisturbanceEntity("persistAndFetch-disturbanceId");

		final var disturbance = disturbanceRepository.save(disturbanceEntity);
		assertThat(disturbance.getId()).isPositive();
		assertThat(disturbanceEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(disturbance.getAffectedEntities()).hasSize(1);
		assertThat(disturbance.getAffectedEntities()).extracting(AffectedEntity::getPartyId, AffectedEntity::getFacilityId, AffectedEntity::getCoordinates)
			.containsExactly(tuple("partyId-1", "facility-1", "coordinates-1"));
		assertThat(disturbance.getDisturbanceId()).isEqualTo("persistAndFetch-disturbanceId");
		assertThat(disturbance.getCategory()).isEqualTo(COMMUNICATION);
		assertThat(disturbance.getCreated()).isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(disturbance.getDeleted()).isFalse();
		assertThat(disturbance.getDescription()).isEqualTo("description");
		assertThat(disturbance.getPlannedStartDate()).isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(disturbance.getPlannedStopDate()).isCloseTo(now(systemDefault()).plusDays(6), within(2, SECONDS));
		assertThat(disturbance.getStatus()).isEqualByComparingTo(OPEN);
	}

	@Test
	void persistAndFetchReplaceAffected() {

		// Create new entity.
		var disturbanceEntity = setupNewDisturbanceEntity("persistAndFetchReplaceAffected-disturbanceId");
		disturbanceEntity = disturbanceRepository.save(disturbanceEntity);

		// Assert affected.
		assertThat(disturbanceEntity.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1");

		// Replace with new affectedEntities.
		final var affectedNew1 = new AffectedEntity();
		affectedNew1.setPartyId("new-partyId-1");
		final var affectedNew2 = new AffectedEntity();
		affectedNew2.setPartyId("new-partyId-2");

		disturbanceEntity.getAffectedEntities().clear();
		disturbanceEntity.addAffectedEntities(Arrays.asList(affectedNew1, affectedNew2));
		disturbanceEntity = disturbanceRepository.save(disturbanceEntity);

		// Assert that everything was persisted correct.
		assertThat(disturbanceEntity.getAffectedEntities())
			.hasSize(2)
			.extracting(AffectedEntity::getPartyId)
			.containsExactlyInAnyOrder("new-partyId-1", "new-partyId-2");
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithCategoryFilterAndStatusFilter() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, PARTY_ID_1, List.of(COMMUNICATION), List.of(OPEN));
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithEmptyCategoryAndEmptyStatus() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, PARTY_ID_2, emptyList(), emptyList());
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithMultipleCategoriesAndMultipleStatuses() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, PARTY_ID_2, List.of(COMMUNICATION, ELECTRICITY), List.of(OPEN,
			CLOSED));
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithCategoryFilter() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, PARTY_ID_1, List.of(COMMUNICATION), null);
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithStatusFilter() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, PARTY_ID_2, null, List.of(OPEN));
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithNoStatusFilterAndNoCategoryFilter() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndAffectedEntitiesPartyIdAndCategoryInAndStatusIn(MUNICIPALITY_ID, PARTY_ID_2, null, null);
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findAllFilteredByCategory() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndStatusAndCategory(MUNICIPALITY_ID, null, List.of(COMMUNICATION));

		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(3)
			.extracting(DisturbanceEntity::getId, DisturbanceEntity::getCategory)
			.containsExactly(
				tuple(2L, COMMUNICATION),
				tuple(4L, COMMUNICATION),
				tuple(11L, COMMUNICATION));
	}

	@Test
	void findAllFilteredByStatus() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndStatusAndCategory(MUNICIPALITY_ID, List.of(CLOSED), null);

		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(3)
			.extracting(DisturbanceEntity::getId, DisturbanceEntity::getStatus)
			.containsExactly(
				tuple(4L, CLOSED),
				tuple(10L, CLOSED),
				tuple(15L, CLOSED));
	}

	@Test
	void findAllFilteredByStatusAndCategory() {
		final var disturbances = disturbanceRepository.findByMunicipalityIdAndStatusAndCategory(MUNICIPALITY_ID, List.of(CLOSED, PLANNED), List.of(ELECTRICITY, COMMUNICATION));

		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(3)
			.extracting(DisturbanceEntity::getId, DisturbanceEntity::getCategory, DisturbanceEntity::getStatus)
			.containsExactly(
				tuple(4L, COMMUNICATION, CLOSED),
				tuple(10L, ELECTRICITY, CLOSED),
				tuple(12L, ELECTRICITY, PLANNED));
	}

	@Test
	void deleteByCreatedBeforeAndStatusIn() {

		// Arrange
		final var deleteOlderThanMonths = 24;
		final var expiryDate = now(systemDefault()).minusMonths(deleteOlderThanMonths);
		final var disturbanceNotEligibleForDelete = disturbanceRepository.findById(14L).get();
		final var disturbanceEligibleForDelete = disturbanceRepository.findById(15L).get();

		// Assert that both is older than 24 months.
		assertThat(disturbanceEligibleForDelete.getCreated()).isBefore(expiryDate);
		assertThat(disturbanceNotEligibleForDelete.getCreated()).isBefore(expiryDate);

		// Act
		disturbanceRepository.deleteByCreatedBeforeAndStatusIn(expiryDate, CLOSED);

		// Assert
		assertThat(disturbanceRepository.findById(disturbanceEligibleForDelete.getId())).isNotPresent();
		assertThat(disturbanceRepository.findById(disturbanceNotEligibleForDelete.getId())).isPresent();
	}

	private void assertAsDisturbanceEntity2(final DisturbanceEntity disturbanceEntity) {

		assertThat(disturbanceEntity.getId()).isEqualTo(2);
		assertThat(disturbanceEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(disturbanceEntity.getCategory()).isEqualTo(COMMUNICATION);
		assertThat(disturbanceEntity.getCreated()).isEqualTo(getOffsetDateTime(2021, 9, 23, 9, 5, 48, 198000000));
		assertThat(disturbanceEntity.getUpdated()).isEqualTo(getOffsetDateTime(2021, 9, 24, 9, 5, 48, 298000000));
		assertThat(disturbanceEntity.getDescription()).isEqualTo("Description");
		assertThat(disturbanceEntity.getDisturbanceId()).isEqualTo("disturbance-2");
		assertThat(disturbanceEntity.getPlannedStartDate()).isEqualTo(getOffsetDateTime(2021, 12, 31, 11, 30, 45, 0));
		assertThat(disturbanceEntity.getPlannedStopDate()).isEqualTo(getOffsetDateTime(2022, 1, 11, 11, 30, 45, 0));

		assertThat(disturbanceEntity.getStatus()).isEqualByComparingTo(OPEN);
		assertThat(disturbanceEntity.getTitle()).isEqualTo("Title");
		assertThat(disturbanceEntity.getAffectedEntities()).hasSize(3);
		// Affected 1
		assertThat(disturbanceEntity.getAffectedEntities().get(0).getPartyId()).isEqualTo(PARTY_ID_1);
		assertThat(disturbanceEntity.getAffectedEntities().get(0).getReference()).isEqualTo("Streetname 11");
		assertThat(disturbanceEntity.getAffectedEntities().get(0).getFacilityId()).isEqualTo("facility-11");
		assertThat(disturbanceEntity.getAffectedEntities().get(0).getCoordinates()).isEqualTo("coordinate-11");
		// Affected 2
		assertThat(disturbanceEntity.getAffectedEntities().get(1).getPartyId()).isEqualTo(PARTY_ID_2);
		assertThat(disturbanceEntity.getAffectedEntities().get(1).getReference()).isEqualTo("Streetname 22");
		assertThat(disturbanceEntity.getAffectedEntities().get(1).getFacilityId()).isEqualTo("facility-22");
		assertThat(disturbanceEntity.getAffectedEntities().get(1).getCoordinates()).isEqualTo("coordinate-22");
		// Affected 3
		assertThat(disturbanceEntity.getAffectedEntities().get(2).getPartyId()).isEqualTo(PARTY_ID_3);
		assertThat(disturbanceEntity.getAffectedEntities().get(2).getReference()).isEqualTo("Streetname 33");
		assertThat(disturbanceEntity.getAffectedEntities().get(2).getFacilityId()).isEqualTo("facility-33");
		assertThat(disturbanceEntity.getAffectedEntities().get(2).getCoordinates()).isEqualTo("coordinate-33");
	}

	private static OffsetDateTime getOffsetDateTime(final int year, final int month, final int day, final int hour, final int minute, final int second, final int nanoOfSecond) {
		final var utcOffsetDateTime = OffsetDateTime.of(year, month, day, hour, minute, second, nanoOfSecond, ZoneOffset.UTC);
		final ZoneOffset currentOffset = ZoneId.systemDefault().getRules().getOffset(utcOffsetDateTime.toInstant());

		return OffsetDateTime.of(year, month, day, hour, minute, second, nanoOfSecond, currentOffset);
	}

	private DisturbanceEntity setupNewDisturbanceEntity(final String disturbanceId) {
		return DisturbanceEntity.create()
			.withDisturbanceId(disturbanceId)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withCategory(COMMUNICATION)
			.withTitle("title")
			.withDescription("description")
			.withStatus(OPEN)
			.withPlannedStartDate(now(systemDefault()))
			.withPlannedStopDate(now(systemDefault()).plusDays(6))
			.addAffectedEntities(List.of(AffectedEntity.create()
				.withPartyId("partyId-1")
				.withCoordinates("coordinates-1")
				.withFacilityId("facility-1")));
	}
}
