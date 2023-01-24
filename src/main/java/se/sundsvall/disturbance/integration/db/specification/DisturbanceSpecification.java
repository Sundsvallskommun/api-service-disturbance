package se.sundsvall.disturbance.integration.db.specification;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

public interface DisturbanceSpecification {

	static Specification<DisturbanceEntity> withCategoryFilter(List<Category> categoryList) {
		return (disturbanceEntity, cq, cb) -> {
			if (isNotEmpty(categoryList)) {
				return disturbanceEntity.get("category").in(toStringList(categoryList));
			}
			// always-true predicate, means that no filtering would be applied
			return cb.and();
		};
	}

	static Specification<DisturbanceEntity> withStatusFilter(List<Status> statusList) {
		return (disturbanceEntity, cq, cb) -> {
			if (isNotEmpty(statusList)) {
				return disturbanceEntity.get("status").in(toStringList(statusList));
			}
			// always-true predicate, means that no filtering would be applied
			return cb.and();
		};
	}

	static Specification<DisturbanceEntity> withPartyId(String partyId) {
		return (disturbanceEntity, cq, cb) -> cb.like(disturbanceEntity.join("affectedEntities").get("partyId"), partyId);
	}

	static Specification<DisturbanceEntity> withDisturbanceId(String disturbanceId) {
		return (disturbanceEntity, cq, cb) -> cb.like(disturbanceEntity.get("disturbanceId"), disturbanceId);
	}

	static Specification<DisturbanceEntity> withCategory(Category category) {
		return (disturbanceEntity, cq, cb) -> cb.like(disturbanceEntity.get("category"), String.valueOf(category));
	}

	private static List<String> toStringList(List<? extends Enum<?>> enumList) {
		return ofNullable(enumList).orElse(emptyList()).stream().map(Enum::name).toList();
	}
}
