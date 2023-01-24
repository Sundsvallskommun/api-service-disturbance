package se.sundsvall.disturbance.integration.db;

import static org.assertj.core.api.Assertions.assertThat;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackHistoryEntity;

/**
 * Disturbance feedback history repository tests.
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@SpringBootTest
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata.sql"
})
@Transactional
class DisturbanceFeedbackHistoryRepositoryTest {

	private static final String CATEGORY = "category";
	private static final String DISTURBANCE_ID = "disturbanceId";
	private static final String PARTY_ID = "partyId";

	@Autowired
	private DisturbanceFeedbackHistoryRepository disturbanceFeedbackHistoryRepository;

	@Test
	void persist() {
		final var entity = new DisturbanceFeedbackHistoryEntity();
		entity.setCategory(CATEGORY);
		entity.setDisturbanceId(DISTURBANCE_ID);
		entity.setPartyId(PARTY_ID);

		disturbanceFeedbackHistoryRepository.save(entity);

		final var list = disturbanceFeedbackHistoryRepository.findByPartyId(PARTY_ID);

		assertThat(list)
			.hasSize(1)
			.allSatisfy(history -> {
				assertThat(history.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
				assertThat(history.getCategory()).isEqualTo(CATEGORY);
				assertThat(history.getStatus()).isEqualTo("SENT");
			});
	}
}
