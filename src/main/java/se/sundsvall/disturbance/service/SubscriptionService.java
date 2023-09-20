package se.sundsvall.disturbance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import se.sundsvall.disturbance.api.model.Subscription;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;

@Service
public class SubscriptionService {

	// @Autowired
	// private SubscriptionRepository subscriptionRepository;

	@Transactional
	public Subscription create(final SubscriptionCreateRequest request) {

		// Check that no existing feedback already exists for provided parameters.
		// if (subscriptionRepository.existsByPartyId(request.getPartyId())) {
		// throw Problem.valueOf(CONFLICT, format(ERROR_SUBSCRIPTION_ALREADY_EXISTS, request.getPartyId()));
		// }

		// TODO: map and save
		return Subscription.create().withId(1L);
	}

	@Transactional
	public Subscription read(final long id) {

		// subscriptionRepository.findById(id)
		// .orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_SUBSCRIPTION_NOT_FOUND, id)));

		// TODO: find and map.
		return Subscription.create();
	}

	@Transactional
	public List<Subscription> findByPartyId(final String partyId) {

		// subscriptionRepository.findById(id)
		// .orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_SUBSCRIPTION_NOT_FOUND, id)));

		// TODO: find and map.
		return List.of(Subscription.create());
	}

	@Transactional
	public Subscription update(final long id, final SubscriptionUpdateRequest request) {

		// subscriptionRepository.findById(id)
		// .orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_SUBSCRIPTION_NOT_FOUND, id)));

		// TODO: find and map.
		return Subscription.create();
	}

	@Transactional
	public void delete(final long id) {

		// final var subscriptionEntity = subscriptionRepository.findById(id)
		// .orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_SUBSCRIPTION_NOT_FOUND, id)));

		// TODO: call delete
	}

}
