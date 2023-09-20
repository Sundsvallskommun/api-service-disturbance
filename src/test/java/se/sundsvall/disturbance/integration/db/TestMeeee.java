package se.sundsvall.disturbance.integration.db;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;
import se.sundsvall.disturbance.integration.db.model.SubscriptionOptOutEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
public class TestMeeee {

	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Test
	public void test() {
		SubscriptionEntity entity = new SubscriptionEntity();
		entity.setPartyId(UUID.randomUUID().toString());

		SubscriptionOptOutEntity optOutEntity = new SubscriptionOptOutEntity();
		optOutEntity.setCategory(Category.ELECTRICITY);
		optOutEntity.addOptOut("facility-1", "some-value");
		optOutEntity.addOptOut("facility-2", "another-value");


		SubscriptionOptOutEntity optOutEntity2 = new SubscriptionOptOutEntity();
		optOutEntity2.setCategory(Category.DISTRICT_HEATING);
		optOutEntity2.addOptOut("heating-1", "heat-value");
		optOutEntity2.addOptOut("heating-2", "another-heat-value");

		entity.addOptOut(optOutEntity);
		entity.addOptOut(optOutEntity2);

		System.out.println(gson.toJson(entity));

	}
}
