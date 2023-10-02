package se.sundsvall.disturbance.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static se.sundsvall.disturbance.api.model.Category.COMMUNICATION;
import static se.sundsvall.disturbance.api.model.Category.DISTRICT_HEATING;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.api.model.OptOutSetting;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

class SubscriptionMapperTest {

	@Test
	void toSubscriptionEntity() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionCreateRequest = SubscriptionCreateRequest.create()
			.withOptOutSettings(List.of(
				OptOutSetting.create()
					.withCategory(ELECTRICITY.toString())
					.withValues(Map.of("facilityId", "111111")),
				OptOutSetting.create()
					.withCategory(DISTRICT_HEATING.toString())
					.withValues(Map.of("facilityId", "222222"))))
			.withPartyId(partyId);

		// Act
		final var result = SubscriptionMapper.toSubscriptionEntity(subscriptionCreateRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getOptOuts())
			.extracting(OptOutSettingsEntity::getCategory, OptOutSettingsEntity::getOptOuts)
			.containsExactlyInAnyOrder(
				tuple(ELECTRICITY, Map.of("facilityId", "111111")),
				tuple(DISTRICT_HEATING, Map.of("facilityId", "222222")));
	}

	@Test
	void toSubscriptionEntityWhenInputIsNull() {

		// Act
		final var result = SubscriptionMapper.toSubscriptionEntity(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toSubscription() {

		// Arrange
		final var id = 666L;
		final var partyId = randomUUID().toString();
		final var created = now(systemDefault());
		final var updated = now(systemDefault()).plusDays(2);
		final var subscriptionEntity = SubscriptionEntity.create()
			.withCreated(created)
			.withId(id)
			.withOptOuts(Set.of(
				OptOutSettingsEntity.create().withCategory(ELECTRICITY).withOptOuts(Map.of("facilityId", "111111")),
				OptOutSettingsEntity.create().withCategory(DISTRICT_HEATING).withOptOuts(Map.of("facilityId", "222222"))))
			.withPartyId(partyId)
			.withUpdated(updated);

		// Act
		final var result = SubscriptionMapper.toSubscription(subscriptionEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getCreated()).isEqualTo(created);
		assertThat(result.getUpdated()).isEqualTo(updated);
		assertThat(result.getOptOutSettings())
			.extracting(OptOutSetting::getCategory, OptOutSetting::getValues)
			.containsExactlyInAnyOrder(
				tuple(ELECTRICITY.toString(), Map.of("facilityId", "111111")),
				tuple(DISTRICT_HEATING.toString(), Map.of("facilityId", "222222")));
	}

	@Test
	void toSubscriptionWhenInputIsNull() {

		// Act
		final var result = SubscriptionMapper.toSubscription(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toUpdatedSubscriptionEntity() {

		// Arrange
		final var id = 666L;
		final var partyId = randomUUID().toString();
		final var created = now(systemDefault());
		final var updated = now(systemDefault()).plusDays(2);
		final var oldSubscriptionEntity = SubscriptionEntity.create()
			.withCreated(created)
			.withId(id)
			.withOptOuts(Set.of(
				OptOutSettingsEntity.create().withCategory(ELECTRICITY).withOptOuts(Map.of("facilityId", "111111")),
				OptOutSettingsEntity.create().withCategory(DISTRICT_HEATING).withOptOuts(Map.of("facilityId", "222222"))))
			.withPartyId(partyId)
			.withUpdated(updated);

		final var subscriptionUpdateRequest = SubscriptionUpdateRequest.create()
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(COMMUNICATION.toString())
				.withValues(Map.of("connectionPoint", "333333"))));

		// Act
		final var result = SubscriptionMapper.toUpdatedSubscriptionEntity(oldSubscriptionEntity, subscriptionUpdateRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getCreated()).isEqualTo(created);
		assertThat(result.getUpdated()).isEqualTo(updated);
		assertThat(result.getOptOuts())
			.extracting(OptOutSettingsEntity::getCategory, OptOutSettingsEntity::getOptOuts)
			.containsExactlyInAnyOrder(
				tuple(COMMUNICATION, Map.of("connectionPoint", "333333")));
	}

	@Test
	void toUpdatedSubscriptionEntityWhenFirstInputParameterIsNull() {

		// Act
		final var result = SubscriptionMapper.toUpdatedSubscriptionEntity(null, SubscriptionUpdateRequest.create());

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toUpdatedSubscriptionEntityWhenSecondInputParameterIsNull() {

		// Arrange
		final var oldSubscriptionEntity = SubscriptionEntity.create();

		// Act
		final var result = SubscriptionMapper.toUpdatedSubscriptionEntity(oldSubscriptionEntity, null);

		// Assert
		assertThat(result).usingRecursiveComparison().isEqualTo(oldSubscriptionEntity);
	}
}
