package se.sundsvall.disturbance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;

import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.OptOutSetting;
import se.sundsvall.disturbance.api.model.Subscription;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.service.SubscriptionService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SubscriptionResourceTest {

	@MockBean
	private SubscriptionService subscriptionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@LocalServerPort
	private int port;

	@Test
	void create() {

		// Arrange
		final var id = 1L;
		final var request = SubscriptionCreateRequest.create()
			.withPartyId(UUID.randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.create(any())).thenReturn(Subscription.create().withId(id));

		// Act
		final var response = webTestClient.post().uri("/subscriptions")
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("http://localhost:".concat(String.valueOf(port)).concat(fromPath("/subscriptions/{id}").build(Map.of("id", id)).toString()))
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).create(request);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}

	@Test
	void read() {

		// Arrange
		final var id = 1L;
		final var subscription = Subscription.create()
			.withPartyId(UUID.randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.read(anyLong())).thenReturn(subscription);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path("subscriptions/{id}").build(Map.of("id", id)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Subscription.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).read(id);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}

	@Test
	void findByPartyId() {

		// Arrange
		final var partyId = UUID.randomUUID().toString();
		final var subscription = Subscription.create()
			.withPartyId(UUID.randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.findByPartyId(any())).thenReturn(subscription);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path("/subscriptions")
				.queryParam("partyId", partyId).build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Subscription.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).findByPartyId(partyId);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}

	@Test
	void update() {

		// Arrange
		final var id = 1L;
		final var subscriptionUpdateRequest = SubscriptionUpdateRequest.create()
			.withOptOutSettings(List.of(OptOutSetting.create().withCategory(ELECTRICITY)));

		final var subscription = Subscription.create()
			.withId(id)
			.withPartyId(UUID.randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.update(anyLong(), any())).thenReturn(subscription);

		// Act
		final var response = webTestClient.put().uri("/subscriptions/{id}", id)
			.contentType(APPLICATION_JSON)
			.bodyValue(subscriptionUpdateRequest)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).update(id, subscriptionUpdateRequest);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}

	@Test
	void delete() {

		// Arrange
		final var id = 1L;

		doNothing().when(subscriptionServiceMock).delete(anyLong());

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path("subscriptions/{id}").build(Map.of("id", id)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBodyList(Subscription.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).delete(id);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}
}
