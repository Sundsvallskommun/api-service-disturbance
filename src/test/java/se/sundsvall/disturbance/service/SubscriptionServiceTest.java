package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.disturbance.api.model.Category.DISTRICT_COOLING;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.integration.db.SubscriptionRepository;
import se.sundsvall.disturbance.integration.db.model.OptOutSettingsEntity;
import se.sundsvall.disturbance.integration.db.model.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@InjectMocks
	private SubscriptionService subscriptionService;

	@Test
	void create() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionCreateRequest = SubscriptionCreateRequest.create().withPartyId(partyId);
		final var subscriptionEntity = SubscriptionEntity.create();

		when(subscriptionRepository.existsByPartyId(any())).thenReturn(false);
		when(subscriptionRepository.save(any())).thenReturn(subscriptionEntity);

		// Act
		final var result = subscriptionService.create(subscriptionCreateRequest);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).existsByPartyId(partyId);
		verify(subscriptionRepository).save(any(SubscriptionEntity.class));
	}

	@Test
	void createWhenAlreadyExists() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionCreateRequest = SubscriptionCreateRequest.create().withPartyId(partyId);

		when(subscriptionRepository.existsByPartyId(any())).thenReturn(true);

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.create(subscriptionCreateRequest));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo(format("Conflict: A subscription entity for partyId:'%s' already exists!", partyId));
		assertThat(throwableProblem.getStatus()).isEqualTo(CONFLICT);

		verify(subscriptionRepository).existsByPartyId(partyId);
		verify(subscriptionRepository, never()).save(any());
	}

	@Test
	void read() {

		// Arrange
		final var id = 1L;
		final var subscriptionEntity = SubscriptionEntity.create()
			.withId(id)
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSettingsEntity.create().withCategory(Category.COMMUNICATION)));

		when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.read(id);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).findById(id);
	}

	@Test
	void readWhenNotFound() {

		// Arrange
		final var id = 1L;

		when(subscriptionRepository.findById(id)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.read(id));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo(format("Not Found: No subscription entity found for id:'%s'!", id));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).findById(id);
	}

	@Test
	void findByPartyId() {

		// Arrange
		final var id = 1L;
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withId(id)
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create().withCategory(Category.COMMUNICATION)));

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.findByPartyId(partyId);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void findByPartyIdWhenNotFound() {

		// Arrange
		final var partyId = randomUUID().toString();

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.findByPartyId(partyId));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo(format("Not Found: No subscription entity found for partyId:'%s'!", partyId));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void delete() {

		// Arrange
		final var id = 1L;

		when(subscriptionRepository.existsById(id)).thenReturn(true);

		// Act
		subscriptionService.delete(id);

		// Assert
		verify(subscriptionRepository).existsById(id);
		verify(subscriptionRepository).deleteById(id);
	}

	@Test
	void deleteWhenNotFound() {

		// Arrange
		final var id = 1L;

		when(subscriptionRepository.existsById(id)).thenReturn(false);

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.delete(id));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo(format("Not Found: No subscription entity found for id:'%s'!", id));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).existsById(id);
		verify(subscriptionRepository, never()).deleteById(any());
	}

	@Test
	void update() {

		// Arrange
		final var id = 1L;
		final var subscriptionUpdateRequest = SubscriptionUpdateRequest.create();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withId(id)
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()));

		when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscriptionEntity));
		when(subscriptionRepository.save(any())).thenReturn(subscriptionEntity);

		// Act
		final var result = subscriptionService.update(id, subscriptionUpdateRequest);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).findById(id);
		verify(subscriptionRepository).save(any(SubscriptionEntity.class));
	}

	@Test
	void updateWhenNotFound() {

		// Arrange
		final var id = 1L;
		final var subscriptionUpdateRequest = SubscriptionUpdateRequest.create();

		when(subscriptionRepository.findById(id)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.update(id, subscriptionUpdateRequest));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo(format("Not Found: No subscription entity found for id:'%s'!", id));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).findById(id);
		verify(subscriptionRepository, never()).save(any());
	}

	@Test
	void hasApplicableSubscription() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId);

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, ELECTRICITY, "some-facilityId");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void hasApplicableSubscriptionWithNoMatchingCategoryOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)));

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, ELECTRICITY, "some-facilityId");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void hasApplicableSubscriptionWithNoMatchingCategoryAndFacilityIdOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)
				.withOptOuts(Map.of("facilityId", "12345"))));

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, ELECTRICITY, "some-facilityId");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void hasApplicableSubscriptionWithNoSubscription() {

		// Arrange
		final var partyId = randomUUID().toString();

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(empty());

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, DISTRICT_COOLING, "some-facilityId");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void hasApplicableSubscriptionWithMatchingCategoryOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)));

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, DISTRICT_COOLING, "some-facilityId");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void hasApplicableSubscriptionWithMatchingCategoryAndFacilityIdOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)
				.withOptOuts(Map.of("facilityId", "12345"))));

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, DISTRICT_COOLING, "12345");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void hasApplicableSubscriptionWithAllMatchingOptOutsPlusSomeMorePropertiesThatDoesNotMatch() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(
				OptOutSettingsEntity.create()
					.withCategory(DISTRICT_COOLING)
					.withOptOuts(Map.of(
						"facilityId", "12345",
						"property1", "value1",
						"property2", "value2"))));

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, DISTRICT_COOLING, "12345");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByPartyId(partyId);
	}

	@Test
	void hasApplicableSubscriptionWithOneMatchingOptOutAndOneNoMatchingOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(
				OptOutSettingsEntity.create()
					.withCategory(DISTRICT_COOLING)
					.withOptOuts(Map.of(
						"facilityId", "12345")),
				OptOutSettingsEntity.create()
					.withCategory(DISTRICT_COOLING)
					.withOptOuts(Map.of(
						"facilityId", "67890"))));

		when(subscriptionRepository.findByPartyId(partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(partyId, DISTRICT_COOLING, "12345");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByPartyId(partyId);
	}
}
