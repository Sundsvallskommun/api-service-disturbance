package se.sundsvall.disturbance.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;

@Transactional
@CircuitBreaker(name = "subscriptionRepository")
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

	SubscriptionEntity findByPartyId(String partyId);
}
