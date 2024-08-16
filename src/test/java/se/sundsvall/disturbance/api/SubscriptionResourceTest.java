package se.sundsvall.disturbance.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

	private static final String PATH = "/{municipalityId}/subscriptions";
	private static final String MUNICIPALITY_ID = "2281";

	@MockBean
	private SubscriptionService subscriptionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void create() {

		// Arrange
		final var id = 1L;
		final var request = SubscriptionCreateRequest.create()
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.create(any(), any())).thenReturn(Subscription.create().withId(id));

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/subscriptions/" + id)
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).create(MUNICIPALITY_ID, request);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}

	@Test
	void read() {

		// Arrange
		final var id = 1L;
		final var subscription = Subscription.create()
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.read(any(), anyLong())).thenReturn(subscription);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Subscription.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).read(MUNICIPALITY_ID, id);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}

	@Test
	void findByPartyId() {

		// Arrange
		final var partyId = UUID.randomUUID().toString();
		final var subscription = Subscription.create()
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.findByMunicipalityIdAndPartyId(any(), any())).thenReturn(subscription);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("partyId", partyId)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Subscription.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).findByMunicipalityIdAndPartyId(MUNICIPALITY_ID, partyId);
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
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(Category.ELECTRICITY)
				.withValues(Map.of("key", "value"))));

		when(subscriptionServiceMock.update(any(), anyLong(), any())).thenReturn(subscription);

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
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
		verify(subscriptionServiceMock).update(MUNICIPALITY_ID, id, subscriptionUpdateRequest);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}

	@Test
	void delete() {

		// Arrange
		final var id = 1L;

		doNothing().when(subscriptionServiceMock).delete(any(), anyLong());

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBodyList(Subscription.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(subscriptionServiceMock).delete(MUNICIPALITY_ID, id);
		verifyNoMoreInteractions(subscriptionServiceMock);
	}
}
