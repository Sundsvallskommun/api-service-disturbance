package se.sundsvall.disturbance.integration.db;

import static se.sundsvall.disturbance.integration.db.specification.DisturbanceFeedbackSpecification.withCategory;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceFeedbackSpecification.withDisturbanceId;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceFeedbackSpecification.withPartyId;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

@Transactional
@CircuitBreaker(name = "disturbanceFeedbackRepository")
public interface DisturbanceFeedbackRepository extends JpaRepository<DisturbanceFeedbackEntity, Long>, JpaSpecificationExecutor<DisturbanceFeedbackEntity> {

	List<DisturbanceFeedbackEntity> findByPartyId(String partyId);

	default List<DisturbanceFeedbackEntity> findByCategoryAndDisturbanceId(Category category, String disturbanceId) {
		return this.findAll(withCategory(category)
			.and(withDisturbanceId(disturbanceId)));
	}

	default Optional<DisturbanceFeedbackEntity> findByCategoryAndDisturbanceIdAndPartyId(Category category, String disturbanceId, String partyId) {
		return this.findOne(withCategory(category)
			.and(withDisturbanceId(disturbanceId))
			.and(withPartyId(partyId)));
	}

	default long deleteByCategoryAndDisturbanceId(Category category, String disturbanceId) {
		final var list = this.findAll(withCategory(category).and(withDisturbanceId(disturbanceId)));
		final var count = list.size();
		this.deleteAllInBatch(list);
		return count;
	}
}
