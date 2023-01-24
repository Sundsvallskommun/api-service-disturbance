package se.sundsvall.disturbance.service.message.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

class SendMessageUtilsTest {

	@Test
	void hasDisturbanceFeedBackEntityIsTrue() {

		final var disturbanceFeedbackEntity1 = new DisturbanceFeedbackEntity();
		disturbanceFeedbackEntity1.setPartyId("partyId-1");

		final var disturbanceFeedbackEntity2 = new DisturbanceFeedbackEntity();
		disturbanceFeedbackEntity2.setPartyId("partyId-2");

		final var disturbanceFeedbackEntity3 = new DisturbanceFeedbackEntity();
		disturbanceFeedbackEntity3.setPartyId("partyId-3");

		final var disturbanceFeedbackEntities = List.of(disturbanceFeedbackEntity1, disturbanceFeedbackEntity2, disturbanceFeedbackEntity3);

		final var affectedEntity = new AffectedEntity();
		affectedEntity.setPartyId("partyId-2");
		affectedEntity.setFacilityId("facilityId-2");

		final var result = SendMessageUtils.hasDisturbanceFeedBackEntity(affectedEntity, disturbanceFeedbackEntities);
		assertThat(result).isTrue();
	}

	@Test
	void hasDisturbanceFeedBackEntityIsFalse() {

		final var disturbanceFeedbackEntity1 = new DisturbanceFeedbackEntity();
		disturbanceFeedbackEntity1.setPartyId("partyId-1");

		final var disturbanceFeedbackEntity2 = new DisturbanceFeedbackEntity();
		disturbanceFeedbackEntity2.setPartyId("partyId-2");

		final var disturbanceFeedbackEntity3 = new DisturbanceFeedbackEntity();
		disturbanceFeedbackEntity3.setPartyId("partyId-3");

		final var disturbanceFeedbackEntities = List.of(disturbanceFeedbackEntity1, disturbanceFeedbackEntity2, disturbanceFeedbackEntity3);

		final var affectedEntity = new AffectedEntity();
		affectedEntity.setPartyId("partyId-XXX");
		affectedEntity.setFacilityId("facilityId-XXX");

		final var result = SendMessageUtils.hasDisturbanceFeedBackEntity(affectedEntity, disturbanceFeedbackEntities);
		assertThat(result).isFalse();
	}
}
