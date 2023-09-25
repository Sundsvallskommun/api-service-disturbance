package se.sundsvall.disturbance.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

/**
 * Disturbance feedback repository tests.
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
class DisturbanceFeedbackRepositoryTest {

	private static final Category CATEGORY = Category.ELECTRICITY;
	private static final String DISTURBANCE_ID_5 = "disturbance-5";
	private static final String DISTURBANCE_ID_7 = "disturbance-7";
	private static final String DISTURBANCE_ID_8 = "disturbance-8";
	private static final String PARTY_ID_1 = "eeca0a46-3b1d-11ec-8d3d-0242ac130003"; // Exists in "disturbance-7".
	private static final String PARTY_ID_2 = "eeca0c8a-3b1d-11ec-8d3d-0242ac130003"; // Exists in "disturbance-7".
	private static final String PARTY_ID_3 = "eeca0d7a-3b1d-11ec-8d3d-0242ac130003"; // Exists in "disturbance-7".

	@Autowired
	private DisturbanceFeedbackRepository disturbanceFeedbackRepository;

	@Test
	void findByCategoryAndDisturbanceId() {
		final var list = disturbanceFeedbackRepository.findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID_7);

		assertThat(list)
			.hasSize(3)
			.extracting(DisturbanceFeedbackEntity::getPartyId)
			.containsExactly(PARTY_ID_1, PARTY_ID_2, PARTY_ID_3);
	}

	@Test
	void findByCategoryAndDisturbanceIdEmptyResult() {
		final var list = disturbanceFeedbackRepository.findByCategoryAndDisturbanceId(null, DISTURBANCE_ID_5);

		assertThat(list).isEmpty();
	}

	@Test
	void findByCategoryAndDisturbanceIdAndPartyId() {
		final var result = disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyId(CATEGORY, DISTURBANCE_ID_7, PARTY_ID_2);

		assertThat(result)
			.isPresent()
			.map(DisturbanceFeedbackEntity::getPartyId)
			.hasValue(PARTY_ID_2);
	}

	@Test
	void findByCategoryAndDisturbanceIdAndPartyIdEmptyResult() {
		final var result = disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyId(null, DISTURBANCE_ID_5, PARTY_ID_2);

		assertThat(result).isNotPresent();
	}

	@Test
	void findByPartyId() {
		final var list = disturbanceFeedbackRepository.findByPartyId(PARTY_ID_1);

		assertThat(list)
			.hasSize(1)
			.extracting(DisturbanceFeedbackEntity::getDisturbanceId, DisturbanceFeedbackEntity::getCategory)
			.containsExactly(tuple(DISTURBANCE_ID_7, CATEGORY.toString()));
	}

	@Test
	void findByPartyIdEmptyResult() {
		final var list = disturbanceFeedbackRepository.findByPartyId("not a party id");

		assertThat(list).isEmpty();
	}

	@Test
	void persistWithNullValues() {
		assertThatThrownBy(() -> disturbanceFeedbackRepository.save(new DisturbanceFeedbackEntity()))
			.hasCauseInstanceOf(ConstraintViolationException.class);
	}

	@Test
	void deleteByCategoryAndDisturbanceId() {

		// Verify that we have 2 disturbanceFeedbacks for disturbance-8.
		assertThat(disturbanceFeedbackRepository.findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID_8))
			.isNotEmpty()
			.hasSize(2);

		// Delete all disturbanceFeedbacks in disturbance-8.
		final long numberOfDeletedEntities = disturbanceFeedbackRepository.deleteByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID_8);

		// Verify that we only deleted 2 and that we doesn't have any disturbanceFeedbacks left in in disturbance-8.
		assertThat(numberOfDeletedEntities).isEqualTo(2);
		assertThat(disturbanceFeedbackRepository.findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID_8))
			.isEmpty();
	}
}
