package se.sundsvall.disturbance.integration.db;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.List;
import java.util.Map;
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

	private static final Long ID = 1L;
	private static final String PARTY_ID = "0d64beb2-3aea-11ec-8d3d-0242ac130003";
	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Test
	void testPersistAndfindByMunicipalityIdAndPartyId() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withPartyId(partyId)
			.withOptOutSettings(List.of(
				OptOutSettingsEntity.create()
					.withCategory(Category.ELECTRICITY)
					.withOptOuts(Map.of(
						"key1", "value1",
						"key2", "value2")),
				OptOutSettingsEntity.create()
					.withCategory(Category.DISTRICT_COOLING)
					.withOptOuts(Map.of(
						"key3", "value3",
						"key4", "value4"))));

		// Act

		// Save
		subscriptionRepository.save(subscriptionEntity);

		// Read
		final var entityByPartyId = subscriptionRepository.findByMunicipalityIdAndPartyId(MUNICIPALITY_ID, partyId).orElseThrow();

		// Assert SubscriptionEntity properties
		assertThat(entityByPartyId)
			.satisfies(entity -> {
				assertThat(entity.getPartyId()).isEqualTo(partyId);
				assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
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
	void findByMunicipalityIdAndPartyId() {

		// Act
		final var result = subscriptionRepository.findByMunicipalityIdAndPartyId(MUNICIPALITY_ID, PARTY_ID);

		// Assert
		assertThat(result).isPresent();
	}

	@Test
	void findByMunicipalityIdAndPartyIdNotFound() {

		// Act
		final var result = subscriptionRepository.findByMunicipalityIdAndPartyId(MUNICIPALITY_ID, "does-not-exist");

		// Assert
		assertThat(result).isNotPresent();
	}

	@Test
	void findByMunicipalityIdAndId() {

		// Act
		final var result = subscriptionRepository.findByMunicipalityIdAndId(MUNICIPALITY_ID, ID);

		// Assert
		assertThat(result).isPresent();
	}

	@Test
	void findByMunicipalityIdAndIdNotFound() {

		// Act
		final var result = subscriptionRepository.findByMunicipalityIdAndId(MUNICIPALITY_ID, 666L);

		// Assert
		assertThat(result).isNotPresent();
	}
}
