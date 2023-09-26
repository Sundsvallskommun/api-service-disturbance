package se.sundsvall.disturbance.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

@Transactional
@CircuitBreaker(name = "subscriptionRepository")
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

	Optional<SubscriptionEntity> findByPartyId(String partyId);

	boolean existsByPartyId(String partyId);
}
