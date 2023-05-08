package se.sundsvall.disturbance.integration.db;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

@Transactional
@CircuitBreaker(name = "feedbackRepository")
public interface FeedbackRepository extends CrudRepository<FeedbackEntity, Long> {

	Optional<FeedbackEntity> findByPartyId(String partyId);
}
