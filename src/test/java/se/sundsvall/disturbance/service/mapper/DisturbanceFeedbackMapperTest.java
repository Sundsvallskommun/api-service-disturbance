package se.sundsvall.disturbance.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.disturbance.service.mapper.DisturbanceFeedbackMapper.DISTURBANCE_FEEDBACK_HISTORY_STATUS_SENT;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

class DisturbanceFeedbackMapperTest {

	@Test
	void toDisturbanceFeedbackEntity() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "1337";
		final var body = DisturbanceFeedbackCreateRequest.create().withPartyId(UUID.randomUUID().toString());

		final var result = DisturbanceFeedbackMapper.toDisturbanceFeedbackEntity(category, disturbanceId, body);

		assertThat(result).isNotNull();
		assertThat(result.getCategory()).isEqualTo(String.valueOf(Category.COMMUNICATION));
		assertThat(result.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(result.getPartyId()).isEqualTo(body.getPartyId());
	}

	@Test
	void toDisturbanceFeedbackHistoryEntity() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "1337";
		final var partyId = UUID.randomUUID().toString();
		final var affectedEntity = new AffectedEntity();
		final var disturbanceEntity = new DisturbanceEntity();

		disturbanceEntity.setCategory(String.valueOf(category));
		disturbanceEntity.setDisturbanceId(disturbanceId);
		affectedEntity.setDisturbanceEntity(disturbanceEntity);
		affectedEntity.setPartyId(partyId);

		final var result = DisturbanceFeedbackMapper.toDisturbanceFeedbackHistoryEntity(affectedEntity);

		assertThat(result).isNotNull();
		assertThat(result.getCategory()).isEqualTo(String.valueOf(category));
		assertThat(result.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getStatus()).isEqualTo(DISTURBANCE_FEEDBACK_HISTORY_STATUS_SENT);
	}
}
