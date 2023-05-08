package se.sundsvall.disturbance.integration.db;

import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withCategory;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withCategoryFilter;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withDisturbanceId;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withPartyId;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withStatusFilter;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

@Transactional
@CircuitBreaker(name = "disturbanceRepository")
public interface DisturbanceRepository extends JpaRepository<DisturbanceEntity, Long>, JpaSpecificationExecutor<DisturbanceEntity> {

	default Optional<DisturbanceEntity> findByCategoryAndDisturbanceId(Category category, String disturbanceId) {
		return this.findOne(withCategory(category)
			.and(withDisturbanceId(disturbanceId)));
	}

	default List<DisturbanceEntity> findByAffectedEntitiesPartyIdAndCategoryInAndStatusIn(String partyId, List<Category> categoryFilter, List<Status> statusFilter) {
		return this.findAll(withPartyId(partyId)
			.and(withCategoryFilter(categoryFilter))
			.and(withStatusFilter(statusFilter)));
	}

	default List<DisturbanceEntity> findByStatusAndCategory(List<Status> statusFilter, List<Category> categoryFilter) {
		return this.findAll(withStatusFilter(statusFilter)
				.and(withCategoryFilter(categoryFilter)));
	}
}
