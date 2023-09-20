package se.sundsvall.disturbance.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

	@InjectMocks
	private SubscriptionService subscriptionService;

	@Test
	// TODO: Implement
	void create() {

		// Arrange
		final var subscription = SubscriptionCreateRequest.create();

		// Act
		final var result = subscriptionService.create(subscription);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	// TODO: Implement
	void createWhenAlreadyExists() {

		// Arrange
		final var id = 1L;

		// Act
		final var result = subscriptionService.read(id);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	// TODO: Implement
	void read() {

		// Arrange
		final var id = 1L;

		// Act
		subscriptionService.read(id);

	}

	@Test
	// TODO: Implement
	void readWhenNotFound() {

		// Arrange
		final var id = 1L;

		// Act
		subscriptionService.read(id);
	}

	@Test
	// TODO: Implement
	void findByPartyId() {

		// Arrange
		final var partyId = UUID.randomUUID().toString();

		// Act
		subscriptionService.findByPartyId(partyId);
	}

	@Test
	// TODO: Implement
	void findByPartyIdWhenNotFound() {

		// Arrange
		final var partyId = UUID.randomUUID().toString();

		// Act
		subscriptionService.findByPartyId(partyId);
	}

	@Test
	// TODO: Implement
	void delete() {

		// Arrange
		final var id = 1L;

		// Act
		subscriptionService.delete(id);
	}

	@Test
	// TODO: Implement
	void deleteWhenNotFound() {

		// Arrange
		final var id = 1L;

		// Act
		subscriptionService.delete(id);
	}

	@Test
	// TODO: Implement
	void update() {

		// Arrange
		final var id = 1L;
		final var subscription = SubscriptionUpdateRequest.create();

		// Act
		final var result = subscriptionService.update(id, subscription);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	// TODO: Implement
	void updateWhenNotFound() {

		// Arrange
		final var id = 1L;
		final var subscription = SubscriptionUpdateRequest.create();

		// Act
		final var result = subscriptionService.update(id, subscription);

		// Assert
		assertThat(result).isNotNull();
	}
}
