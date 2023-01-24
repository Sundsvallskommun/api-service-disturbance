package se.sundsvall.disturbance.integration.db;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

@Transactional
public interface FeedbackRepository extends CrudRepository<FeedbackEntity, Long> {

	Optional<FeedbackEntity> findByPartyId(String partyId);
}
