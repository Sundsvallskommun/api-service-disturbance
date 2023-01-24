package se.sundsvall.disturbance.service.mapper;

import static java.lang.String.valueOf;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackHistoryEntity;

public class DisturbanceFeedbackMapper {

	static final String DISTURBANCE_FEEDBACK_HISTORY_STATUS_SENT = "SENT";

	private DisturbanceFeedbackMapper() {}

	public static DisturbanceFeedbackEntity toDisturbanceFeedbackEntity(Category category, String disturbanceId, DisturbanceFeedbackCreateRequest request) {
		final var entity = new DisturbanceFeedbackEntity();
		entity.setCategory(valueOf(category));
		entity.setDisturbanceId(disturbanceId);
		entity.setPartyId(request.getPartyId());
		return entity;
	}

	public static DisturbanceFeedbackHistoryEntity toDisturbanceFeedbackHistoryEntity(AffectedEntity affectedEntity) {
		final var disturbanceFeedbackHistoryEntity = new DisturbanceFeedbackHistoryEntity();
		disturbanceFeedbackHistoryEntity.setPartyId(affectedEntity.getPartyId());
		disturbanceFeedbackHistoryEntity.setDisturbanceId(affectedEntity.getDisturbanceEntity().getDisturbanceId());
		disturbanceFeedbackHistoryEntity.setCategory(affectedEntity.getDisturbanceEntity().getCategory());
		disturbanceFeedbackHistoryEntity.setStatus(DISTURBANCE_FEEDBACK_HISTORY_STATUS_SENT);
		return disturbanceFeedbackHistoryEntity;
	}
}
