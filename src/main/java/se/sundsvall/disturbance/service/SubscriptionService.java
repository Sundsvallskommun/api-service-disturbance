package se.sundsvall.disturbance.service;

import static io.micrometer.common.util.StringUtils.isBlank;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_SUBSCRIPTION_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_SUBSCRIPTION_NOT_FOUND_BY_ID;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_SUBSCRIPTION_NOT_FOUND_BY_PARTY_ID;
import static se.sundsvall.disturbance.service.mapper.SubscriptionMapper.toSubscription;
import static se.sundsvall.disturbance.service.mapper.SubscriptionMapper.toSubscriptionEntity;
import static se.sundsvall.disturbance.service.mapper.SubscriptionMapper.toUpdatedSubscriptionEntity;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Subscription;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.integration.db.SubscriptionRepository;
import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;

@Service
public class SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;

	public SubscriptionService(SubscriptionRepository subscriptionRepository) {
		this.subscriptionRepository = subscriptionRepository;
	}

	@Transactional
	public Subscription create(final String municipalityId, final SubscriptionCreateRequest request) {

		if (subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, request.getPartyId()).isPresent()) {
			throw Problem.valueOf(CONFLICT, ERROR_SUBSCRIPTION_ALREADY_EXISTS.formatted(request.getPartyId()));
		}

		return toSubscription(subscriptionRepository.save(toSubscriptionEntity(municipalityId, request)));
	}

	@Transactional
	public Subscription read(final String municipalityId, final long id) {

		final var subscriptionEntity = subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_SUBSCRIPTION_NOT_FOUND_BY_ID.formatted(id)));

		return toSubscription(subscriptionEntity);
	}

	@Transactional
	public Subscription findByMunicipalityIdAndPartyId(final String municipalityId, final String partyId) {

		final var subscriptionEntity = subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_SUBSCRIPTION_NOT_FOUND_BY_PARTY_ID.formatted(partyId)));

		return toSubscription(subscriptionEntity);
	}

	@Transactional
	public Subscription update(final String municipalityId, final long id, final SubscriptionUpdateRequest request) {

		final var subscriptionEntity = subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_SUBSCRIPTION_NOT_FOUND_BY_ID.formatted(id)));

		return toSubscription(subscriptionRepository.save(toUpdatedSubscriptionEntity(subscriptionEntity, request).withUpdated(now(systemDefault()))));
	}

	@Transactional
	public void delete(final String municipalityId, final long id) {

		final var subscriptionEntity = subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_SUBSCRIPTION_NOT_FOUND_BY_ID.formatted(id)));

		subscriptionRepository.delete(subscriptionEntity);
	}

	public boolean hasApplicableSubscription(final String municipalityId, String partyId, Category category, String facilityId) {
		if (isBlank(partyId)) {
			return false;
		}

		final var subscriptionEntity = subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId);

		if (subscriptionEntity.isPresent()) {
			// Check if parameters matches any current optOutValues on the subscription.
			final var hasMatchingOptOut = Optional.ofNullable(subscriptionEntity.get().getOptOutSettings()).orElse(emptyList()).stream()
				.anyMatch(optOutSetting -> hasMatchingOptOut(optOutSetting, category, facilityId));

			if (!hasMatchingOptOut) {
				// Person/organization has active subscription without matching opt-outs.
				return true;
			}
		}

		// Subscription is missing or has matching opt-outs.
		return false;
	}

	private boolean hasMatchingOptOut(OptOutSettingsEntity optOutSetting, Category category, String facilityId) {
		if (isNull(optOutSetting)) {
			return false;
		}

		final var categoryMatch = optOutSetting.getCategory().equals(category);
		if (isEmpty(optOutSetting.getOptOuts())) {
			return categoryMatch;
		}

		final var optOutValueMatch = matches(optOutSetting.getOptOuts(), Map.of("facilityId", facilityId));

		return categoryMatch && optOutValueMatch;
	}

	private <K, V> boolean matches(Map<K, V> optOutSettingValues, Map<K, V> actualValues) {
		if (isEmpty(optOutSettingValues) || isEmpty(actualValues)) {
			return false;
		}

		return optOutSettingValues.entrySet().stream()
			.allMatch(it -> actualValues.entrySet().contains(it));
	}
}
