package se.sundsvall.disturbance.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

/**
 * Feedback repository tests.
 *
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata.sql"
})
class FeedbackRepositoryTest {

	private static final String PARTY_ID = "49a974ea-9137-419b-bcb9-ad74c81a1d7f";

	@Autowired
	private FeedbackRepository feedbackRepository;

	@Test
	void findByPartyId() {
		final var optionalFeedback = feedbackRepository.findByPartyId(PARTY_ID);

		assertThat(optionalFeedback).isPresent();
		assertThat(optionalFeedback.get().getPartyId()).isEqualTo(PARTY_ID);
		assertThat(optionalFeedback.get().getId()).isPositive();
		assertThat(optionalFeedback.get().getCreated().toString()).hasToString("2021-12-28T12:20:41.298+01:00");
	}

	@Test
	void findByPartyIdEmptyResult() {
		final var optionalFeedback = feedbackRepository.findByPartyId("not a party id");

		assertThat(optionalFeedback).isNotPresent();
	}

	@Test()
	void persistWithNullValues() {
		assertThatThrownBy(() -> feedbackRepository.save(new FeedbackEntity())).hasCauseInstanceOf(ConstraintViolationException.class);
	}
}
