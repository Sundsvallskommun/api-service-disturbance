package se.sundsvall.disturbance.service;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.sundsvall.disturbance.api.model.Subscription;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.integration.db.OptOutSettingsRepository;
import se.sundsvall.disturbance.integration.db.SubscriptionRepository;

import jakarta.transaction.Transactional;

@Service
public class SubscriptionService {

	private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private OptOutSettingsRepository optOutSettingsRepository;

	@Transactional
	public Subscription create(final SubscriptionCreateRequest request) {

		// Check that no existing feedback already exists for provided parameters.
		// if (subscriptionRepository.existsByPartyId(request.getPartyId())) {
		// throw Problem.valueOf(CONFLICT, format(ERROR_SUBSCRIPTION_ALREADY_EXISTS, request.getPartyId()));
		// }

		//Temp-implementation
		/*Set<OptOutSettingsEntity> optOutSettingsEntities = new HashSet<>();
		request.getOptOutSettings().forEach(optOutSetting -> {
			OptOutSettingsEntity optOutSettingsEntity = new OptOutSettingsEntity();
			optOutSettingsEntity.setCategory(optOutSetting.getCategory());
			optOutSettingsEntity.setOptOuts(optOutSetting.getValues());
			optOutSettingsEntities.add(optOutSettingsEntity);
		});

		var subscriptionEntity = new SubscriptionEntity()
				.withPartyId(request.getPartyId())
				.withOptOuts(optOutSettingsEntities);
		//Save the optOutSettings
		optOutSettingsEntities.forEach(optOutSettingsEntity -> {
			optOutSettingsEntity.setSubscriptionEntity(subscriptionEntity);
			optOutSettingsRepository.save(optOutSettingsEntity);
		});

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
