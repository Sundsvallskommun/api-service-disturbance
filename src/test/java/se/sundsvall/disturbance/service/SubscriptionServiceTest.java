package se.sundsvall.disturbance.service;

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
		final var municipalityId = "2281";
		final var partyId = randomUUID().toString();
		final var subscriptionCreateRequest = SubscriptionCreateRequest.create().withPartyId(partyId);
		final var subscriptionEntity = SubscriptionEntity.create();

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(any(), any())).thenReturn(empty());
		when(subscriptionRepository.save(any())).thenReturn(subscriptionEntity);

		// Act
		final var result = subscriptionService.create(municipalityId, subscriptionCreateRequest);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
		verify(subscriptionRepository).save(any(SubscriptionEntity.class));
	}

	@Test
	void createWhenAlreadyExists() {

		// Arrange
		final var municipalityId = "2281";
		final var partyId = randomUUID().toString();
		final var subscriptionCreateRequest = SubscriptionCreateRequest.create().withPartyId(partyId);

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(any(), any())).thenReturn(Optional.of(SubscriptionEntity.create()));

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.create(municipalityId, subscriptionCreateRequest));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo("Conflict: A subscription entity for partyId:'%s' already exists!".formatted(partyId));
		assertThat(throwableProblem.getStatus()).isEqualTo(CONFLICT);

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
		verify(subscriptionRepository, never()).save(any());
	}

	@Test
	void read() {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";
		final var subscriptionEntity = SubscriptionEntity.create()
			.withId(id)
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSettingsEntity.create().withCategory(Category.COMMUNICATION)));

		when(subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.read(municipalityId, id);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).findByMunicipalityIdAndId(municipalityId, id);
	}

	@Test
	void readWhenNotFound() {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";

		when(subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.read(municipalityId, id));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No subscription entity found for id:'%s'!".formatted(id));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).findByMunicipalityIdAndId(municipalityId, id);
	}

	@Test
	void findByMunicipalityIdAndPartyId() {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";
		final var partyId = randomUUID().toString();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withId(id)
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create().withCategory(Category.COMMUNICATION)));

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.findByMunicipalityIdAndPartyId(municipalityId, partyId);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void findByMunicipalityIdAndPartyIdNotFound() {

		// Arrange
		final var municipalityId = "2281";
		final var partyId = randomUUID().toString();

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.findByMunicipalityIdAndPartyId(municipalityId, partyId));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No subscription entity found for partyId:'%s'!".formatted(partyId));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void delete() {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";
		final var entity = SubscriptionEntity.create();

		when(subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.of(entity));

		// Act
		subscriptionService.delete(municipalityId, id);

		// Assert
		verify(subscriptionRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(subscriptionRepository).delete(entity);
	}

	@Test
	void deleteWhenNotFound() {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";

		when(subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.delete(municipalityId, id));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No subscription entity found for id:'%s'!".formatted(id));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(subscriptionRepository, never()).delete(any());
	}

	@Test
	void update() {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";
		final var subscriptionUpdateRequest = SubscriptionUpdateRequest.create();
		final var subscriptionEntity = SubscriptionEntity.create()
			.withId(id)
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()));

		when(subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.of(subscriptionEntity));
		when(subscriptionRepository.save(any())).thenReturn(subscriptionEntity);

		// Act
		final var result = subscriptionService.update(municipalityId, id, subscriptionUpdateRequest);

		// Assert
		assertThat(result).isNotNull();

		verify(subscriptionRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(subscriptionRepository).save(any(SubscriptionEntity.class));
	}

	@Test
	void updateWhenNotFound() {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";
		final var subscriptionUpdateRequest = SubscriptionUpdateRequest.create();

		when(subscriptionRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(empty());

		// Act
		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> subscriptionService.update(municipalityId, id, subscriptionUpdateRequest));

		// Assert
		assertThat(throwableProblem).isNotNull();
		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No subscription entity found for id:'%s'!".formatted(id));
		assertThat(throwableProblem.getStatus()).isEqualTo(NOT_FOUND);

		verify(subscriptionRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(subscriptionRepository, never()).save(any());
	}

	@Test
	void hasApplicableSubscription() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId);

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, ELECTRICITY, "some-facilityId");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void hasApplicableSubscriptionWithNoMatchingCategoryOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)));

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, ELECTRICITY, "some-facilityId");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void hasApplicableSubscriptionWithNoMatchingCategoryAndFacilityIdOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)
				.withOptOuts(Map.of("facilityId", "12345"))));

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, ELECTRICITY, "some-facilityId");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void hasApplicableSubscriptionWithNoSubscription() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(empty());

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, DISTRICT_COOLING, "some-facilityId");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void hasApplicableSubscriptionWithMatchingCategoryOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)));

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, DISTRICT_COOLING, "some-facilityId");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void hasApplicableSubscriptionWithMatchingCategoryAndFacilityIdOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(OptOutSettingsEntity.create()
				.withCategory(DISTRICT_COOLING)
				.withOptOuts(Map.of("facilityId", "12345"))));

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, DISTRICT_COOLING, "12345");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void hasApplicableSubscriptionWithAllMatchingOptOutsPlusSomeMorePropertiesThatDoesNotMatch() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";
		final var subscriptionEntity = SubscriptionEntity.create()
			.withPartyId(partyId)
			.withOptOutSettings(List.of(
				OptOutSettingsEntity.create()
					.withCategory(DISTRICT_COOLING)
					.withOptOuts(Map.of(
						"facilityId", "12345",
						"property1", "value1",
						"property2", "value2"))));

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, DISTRICT_COOLING, "12345");

		// Assert
		assertThat(result).isTrue();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}

	@Test
	void hasApplicableSubscriptionWithOneMatchingOptOutAndOneNoMatchingOptOut() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var municipalityId = "2281";
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

		when(subscriptionRepository.findByMunicipalityIdAndPartyId(municipalityId, partyId)).thenReturn(Optional.of(subscriptionEntity));

		// Act
		final var result = subscriptionService.hasApplicableSubscription(municipalityId, partyId, DISTRICT_COOLING, "12345");

		// Assert
		assertThat(result).isFalse();

		verify(subscriptionRepository).findByMunicipalityIdAndPartyId(municipalityId, partyId);
	}
}
