package se.sundsvall.disturbance.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;

import se.sundsvall.disturbance.api.model.OptOutSetting;
import se.sundsvall.disturbance.api.model.Subscription;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

public class SubscriptionMapper {

	private SubscriptionMapper() {}

	public static SubscriptionEntity toSubscriptionEntity(SubscriptionCreateRequest subscriptionCreateRequest) {
		if (isNull(subscriptionCreateRequest)) {
			return null;
		}

		return SubscriptionEntity.create()
			.withOptOuts(toOptOuts(subscriptionCreateRequest.getOptOutSettings()))
			.withPartyId(subscriptionCreateRequest.getPartyId());
	}

	public static SubscriptionEntity toUpdatedSubscriptionEntity(SubscriptionEntity existingSubscriptionEntity, SubscriptionUpdateRequest subscriptionUpdateRequest) {
		if (isNull(existingSubscriptionEntity) || isNull(subscriptionUpdateRequest)) {
			return existingSubscriptionEntity;
		}

		return existingSubscriptionEntity
			.withOptOuts(toOptOuts(subscriptionUpdateRequest.getOptOutSettings()));
	}

	public static Subscription toSubscription(SubscriptionEntity subscriptionEntity) {
		if (isNull(subscriptionEntity)) {
			return null;
		}

		return Subscription.create()
			.withCreated(subscriptionEntity.getCreated())
			.withId(subscriptionEntity.getId())
			.withOptOutSettings(toOptOuts(subscriptionEntity.getOptOuts()))
			.withPartyId(subscriptionEntity.getPartyId())
			.withUpdated(subscriptionEntity.getUpdated());
	}

	private static Set<OptOutSettingsEntity> toOptOuts(List<OptOutSetting> optOutSettingsList) {
		if (isNull(optOutSettingsList)) {
			return emptySet();
		}

		return optOutSettingsList.stream()
			.map(optOutSetting -> OptOutSettingsEntity.create()
				.withCategory(optOutSetting.getCategory())
				.withOptOuts(optOutSetting.getValues()))
			.collect(toSet());
	}

	private static List<OptOutSetting> toOptOuts(Set<OptOutSettingsEntity> optOutSettingsEntityList) {
		if (isNull(optOutSettingsEntityList)) {
			return emptyList();
		}

		return optOutSettingsEntityList.stream()
			.map(optOutSettingsEntity -> OptOutSetting.create()
				.withCategory(optOutSettingsEntity.getCategory())
				.withValues(optOutSettingsEntity.getOptOuts()))
			.toList();
	}
}
