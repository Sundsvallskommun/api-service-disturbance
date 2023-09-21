package se.sundsvall.disturbance.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
}
