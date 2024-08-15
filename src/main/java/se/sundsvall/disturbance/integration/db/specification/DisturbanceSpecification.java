package se.sundsvall.disturbance.integration.db.specification;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity_;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity_;

public interface DisturbanceSpecification {

	static Specification<DisturbanceEntity> withCategoryFilter(List<Category> categoryList) {
		return (disturbanceEntity, cq, cb) -> {
			if (isNotEmpty(categoryList)) {
				return disturbanceEntity.get(DisturbanceEntity_.CATEGORY).in(categoryList);
			}
			// always-true predicate, means that no filtering would be applied
			return cb.and();
		};
	}

	static Specification<DisturbanceEntity> withStatusFilter(List<Status> statusList) {
		return (disturbanceEntity, cq, cb) -> {
			if (isNotEmpty(statusList)) {
				return disturbanceEntity.get(DisturbanceEntity_.STATUS).in(statusList);
			}
			// always-true predicate, means that no filtering would be applied
			return cb.and();
		};
	}

	static Specification<DisturbanceEntity> withPartyId(String partyId) {
		return (disturbanceEntity, cq, cb) -> cb.like(disturbanceEntity.join(DisturbanceEntity_.AFFECTED_ENTITIES).get(AffectedEntity_.PARTY_ID), partyId);
	}

	static Specification<DisturbanceEntity> withMunicipalityId(String municipalityId) {
		return (disturbanceEntity, cq, cb) -> cb.like(disturbanceEntity.get(DisturbanceEntity_.MUNICIPALITY_ID), municipalityId);
	}

	static Specification<DisturbanceEntity> withDisturbanceId(String disturbanceId) {
		return (disturbanceEntity, cq, cb) -> cb.like(disturbanceEntity.get(DisturbanceEntity_.DISTURBANCE_ID), disturbanceId);
	}

	static Specification<DisturbanceEntity> withCategory(Category category) {
		return (disturbanceEntity, cq, cb) -> cb.equal(disturbanceEntity.get(DisturbanceEntity_.CATEGORY), category);
	}
}
