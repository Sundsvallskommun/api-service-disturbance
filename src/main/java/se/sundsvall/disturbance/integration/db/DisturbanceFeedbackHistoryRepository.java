package se.sundsvall.disturbance.integration.db;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackHistoryEntity;

@Transactional
@CircuitBreaker(name = "disturbanceFeedbackHistoryRepository")
public interface DisturbanceFeedbackHistoryRepository extends CrudRepository<DisturbanceFeedbackHistoryEntity, Long> {

	List<DisturbanceFeedbackHistoryEntity> findByPartyId(String partyId);
}
