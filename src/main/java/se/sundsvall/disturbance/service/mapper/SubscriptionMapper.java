package se.sundsvall.disturbance.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import java.util.List;
import se.sundsvall.disturbance.api.model.OptOutSetting;
import se.sundsvall.disturbance.api.model.Subscription;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

public final class SubscriptionMapper {

	private SubscriptionMapper() {}

	public static SubscriptionEntity toSubscriptionEntity(String municipalityId, SubscriptionCreateRequest subscriptionCreateRequest) {
		if (isNull(subscriptionCreateRequest)) {
			return null;
		}

		return SubscriptionEntity.create()
			.withMunicipalityId(municipalityId)
			.withOptOutSettings(toOptOutSettingsEntities(subscriptionCreateRequest.getOptOutSettings()))
			.withPartyId(subscriptionCreateRequest.getPartyId());
	}

	public static SubscriptionEntity toUpdatedSubscriptionEntity(SubscriptionEntity existingSubscriptionEntity, SubscriptionUpdateRequest subscriptionUpdateRequest) {
		if (isNull(existingSubscriptionEntity) || isNull(subscriptionUpdateRequest)) {
			return existingSubscriptionEntity;
		}

		return existingSubscriptionEntity
			.withOptOutSettings(toOptOutSettingsEntities(subscriptionUpdateRequest.getOptOutSettings()));
	}

	public static Subscription toSubscription(SubscriptionEntity subscriptionEntity) {
		if (isNull(subscriptionEntity)) {
			return null;
		}

		return Subscription.create()
			.withCreated(subscriptionEntity.getCreated())
			.withId(subscriptionEntity.getId())
			.withMunicipalityId(subscriptionEntity.getMunicipalityId())
			.withOptOutSettings(toOptOutSettings(subscriptionEntity.getOptOutSettings()))
			.withPartyId(subscriptionEntity.getPartyId())
			.withUpdated(subscriptionEntity.getUpdated());
	}

	private static List<OptOutSettingsEntity> toOptOutSettingsEntities(List<OptOutSetting> optOutSettings) {
		if (isNull(optOutSettings)) {
			return emptyList();
		}

		return optOutSettings.stream()
			.map(optOutSetting -> OptOutSettingsEntity.create()
				.withCategory(optOutSetting.getCategory())
				.withOptOuts(optOutSetting.getValues()))
			.distinct()
			.toList();
	}

	private static List<OptOutSetting> toOptOutSettings(List<OptOutSettingsEntity> optOutSettingsEntities) {
		if (isNull(optOutSettingsEntities)) {
			return emptyList();
		}

		return optOutSettingsEntities.stream()
			.map(optOutSettingsEntity -> OptOutSetting.create()
				.withCategory(optOutSettingsEntity.getCategory())
				.withValues(optOutSettingsEntity.getOptOuts()))
			.distinct()
			.toList();
	}
}
