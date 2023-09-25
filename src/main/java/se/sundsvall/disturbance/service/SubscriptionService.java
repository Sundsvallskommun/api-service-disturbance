package se.sundsvall.disturbance.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.sundsvall.disturbance.api.model.Subscription;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.integration.db.SubscriptionRepository;
import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

import jakarta.transaction.Transactional;

@Service
public class SubscriptionService {

	private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Transactional
	public Subscription create(final SubscriptionCreateRequest request) {

		// Check that no existing feedback already exists for provided parameters.
		// if (subscriptionRepository.existsByPartyId(request.getPartyId())) {
		// throw Problem.valueOf(CONFLICT, format(ERROR_SUBSCRIPTION_ALREADY_EXISTS, request.getPartyId()));
		// }

		//Temp-implementation
		/*var subscriptionEntity = new SubscriptionEntity()
				.withPartyId(request.getPartyId())
				.withOptOuts(request.getOptOutSettings().stream()
						.map(optOutSetting -> new OptOutSettingsEntity()
								.withCategory(optOutSetting.getCategory())
								.withOptOuts(optOutSetting.getValues()))
						.collect(Collectors.toSet()));

		//Save the SubscriptionEntity
		var savedSubscriptionEntity = subscriptionRepository.save(subscriptionEntity);

		return Subscription.create().withId(savedSubscriptionEntity.getId());*/

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
