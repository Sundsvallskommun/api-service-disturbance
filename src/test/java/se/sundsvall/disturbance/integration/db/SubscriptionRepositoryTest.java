package se.sundsvall.disturbance.integration.db;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class SubscriptionRepositoryTest {

	private static final String PARTY_ID = "0d64beb2-3aea-11ec-8d3d-0242ac130003"; // Exists in testdata-junit.sql;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@SuppressWarnings("serial")
	@Test
	void testPersistAndfindByPartyId() {
		// Setup
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = new SubscriptionEntity()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(new OptOutSettingsEntity()
				.withCategory(Category.ELECTRICITY)
				.withOptOuts(new HashMap<>() {
					{
						put("key1", "value1");
						put("key2", "value2");
					}
				}), new OptOutSettingsEntity()
					.withCategory(Category.DISTRICT_COOLING)
					.withOptOuts(new HashMap<>() {
						{
							put("key3", "value3");
							put("key4", "value4");
						}
					})));

		// Save
		subscriptionRepository.save(subscriptionEntity);

		// Read
		final var entityByPartyId = subscriptionRepository.findByPartyId(partyId).orElseThrow();

		// Assert SubscriptionEntity properties
		assertThat(entityByPartyId)
			.satisfies(entity -> {
				assertThat(entity.getPartyId()).isEqualTo(partyId);
				assertThat(entity.getOptOutSettings()).hasSize(2);
			});

		// Assert OptOutSettingsEntity and their optOuts
		assertThat(entityByPartyId.getOptOutSettings())
			.satisfiesExactlyInAnyOrder(
				optOut1 -> {
					assertThat(optOut1.getCategory()).isEqualByComparingTo(Category.ELECTRICITY);
					assertThat(optOut1.getOptOuts()).containsEntry("key1", "value1");
					assertThat(optOut1.getOptOuts()).containsEntry("key2", "value2");
				},
				optOut2 -> {
					assertThat(optOut2.getCategory()).isEqualByComparingTo(Category.DISTRICT_COOLING);
					assertThat(optOut2.getOptOuts()).containsEntry("key3", "value3");
					assertThat(optOut2.getOptOuts()).containsEntry("key4", "value4");
				});
	}

	@Test
	void existByPartyId() {

		// Act
		final var result = subscriptionRepository.existsByPartyId(PARTY_ID);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void existByPartyIdNotFound() {

		// Act
		final var result = subscriptionRepository.existsByPartyId("does-not-exist");

		// Assert
		assertThat(result).isFalse();
	}
}
