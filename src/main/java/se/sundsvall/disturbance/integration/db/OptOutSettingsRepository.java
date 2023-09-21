package se.sundsvall.disturbance.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;

public interface OptOutSettingsRepository extends JpaRepository<OptOutSettingsEntity, Long> {
}
