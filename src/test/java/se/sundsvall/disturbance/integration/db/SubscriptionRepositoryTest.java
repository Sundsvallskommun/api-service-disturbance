package se.sundsvall.disturbance.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.HashMap;
import java.util.Set;

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
		"/db/scripts/testdata.sql"
})
class SubscriptionRepositoryTest {

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	private static final String PARTY_ID = "partyId";

	@Test
	void testPersistAndfindByPartyId() {
		//Setup
		var subscriptionEntity = new SubscriptionEntity()
				.withPartyId(PARTY_ID)
				.withOptOuts(Set.of(new OptOutSettingsEntity()
						.withCategory(Category.ELECTRICITY)
						.withOptOuts(new HashMap<>(){{
							put("key1", "value1");
							put("key2", "value2");
						}}),new OptOutSettingsEntity()
						.withCategory(Category.DISTRICT_COOLING)
						.withOptOuts(new HashMap<>(){{
							put("key3", "value3");
							put("key4", "value4");
						}})
				));

		//Save
		subscriptionRepository.save(subscriptionEntity);

		//Read
		var entityByPartyId = subscriptionRepository.findByPartyId(PARTY_ID);

		//Assert SubscriptionEntity properties
		assertThat(entityByPartyId)
				.satisfies(entity -> {
					assertThat(entity.getPartyId()).isEqualTo(PARTY_ID);
					assertThat(entity.getOptOuts()).hasSize(2);
				});

		//Assert OptOutSettingsEntity and their optOuts
		assertThat(entityByPartyId.getOptOuts())
				.satisfiesExactlyInAnyOrder(
						optOut1 -> {
							assertThat(optOut1.getCategory()).isEqualTo(Category.ELECTRICITY);
							assertThat(optOut1.getOptOuts()).containsEntry("key1", "value1");
							assertThat(optOut1.getOptOuts()).containsEntry("key2", "value2");
						},
						optOut2 -> {
							assertThat(optOut2.getCategory()).isEqualTo(Category.DISTRICT_COOLING);
							assertThat(optOut2.getOptOuts()).containsEntry("key3", "value3");
							assertThat(optOut2.getOptOuts()).containsEntry("key4", "value4");
						}
				);
	}
}