package se.sundsvall.disturbance.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.disturbance.integration.db.model.SubscriptionOptOutEntity;

public interface SubscriptionOptOutRepository extends JpaRepository<SubscriptionOptOutEntity, Long> {
}
