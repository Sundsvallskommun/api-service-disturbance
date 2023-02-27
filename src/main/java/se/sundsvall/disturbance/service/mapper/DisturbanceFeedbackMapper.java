package se.sundsvall.disturbance.service.mapper;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackHistoryEntity;

public class DisturbanceFeedbackMapper {

	static final String DISTURBANCE_FEEDBACK_HISTORY_STATUS_SENT = "SENT";

	private DisturbanceFeedbackMapper() {}

	public static DisturbanceFeedbackEntity toDisturbanceFeedbackEntity(final Category category, final String disturbanceId, final DisturbanceFeedbackCreateRequest request) {
		final var entity = new DisturbanceFeedbackEntity();
		entity.setCategory(String.valueOf(category));
		entity.setDisturbanceId(disturbanceId);
		entity.setPartyId(request.getPartyId());
		return entity;
	}

	public static DisturbanceFeedbackHistoryEntity toDisturbanceFeedbackHistoryEntity(final AffectedEntity affectedEntity) {
		final var disturbanceFeedbackHistoryEntity = new DisturbanceFeedbackHistoryEntity();
		disturbanceFeedbackHistoryEntity.setPartyId(affectedEntity.getPartyId());
		disturbanceFeedbackHistoryEntity.setDisturbanceId(affectedEntity.getDisturbanceEntity().getDisturbanceId());
		disturbanceFeedbackHistoryEntity.setCategory(affectedEntity.getDisturbanceEntity().getCategory());
		disturbanceFeedbackHistoryEntity.setStatus(DISTURBANCE_FEEDBACK_HISTORY_STATUS_SENT);
		return disturbanceFeedbackHistoryEntity;
	}
}
