package se.sundsvall.disturbance.integration.db.specification;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

public interface DisturbanceFeedbackSpecification {

	static Specification<DisturbanceFeedbackEntity> withDisturbanceId(String disturbanceId) {
		return (disturbanceFeedbackEntity, cq, cb) -> cb.like(disturbanceFeedbackEntity.get("disturbanceId"), disturbanceId);
	}

	static Specification<DisturbanceFeedbackEntity> withPartyId(String partyId) {
		return (disturbanceFeedbackEntity, cq, cb) -> cb.like(disturbanceFeedbackEntity.get("partyId"), partyId);
	}

	static Specification<DisturbanceFeedbackEntity> withCategory(Category category) {
		return (disturbanceFeedbackEntity, cq, cb) -> cb.like(disturbanceFeedbackEntity.get("category"), String.valueOf(category));
	}
}
