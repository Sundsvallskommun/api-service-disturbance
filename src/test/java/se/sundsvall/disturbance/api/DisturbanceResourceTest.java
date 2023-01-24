package se.sundsvall.disturbance.api;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.service.DisturbanceFeedbackService;
import se.sundsvall.disturbance.service.DisturbanceService;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class DisturbanceResourceTest {

	@MockBean
	private DisturbanceService disturbanceServiceMock;

	@MockBean
	private DisturbanceFeedbackService disturbanceFeedbackServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@LocalServerPort
	private int port;

	@Test
	void getDisturbancesByPartyId() {

		// Parameter values.
		final var partyId = UUID.randomUUID().toString();

		webTestClient.get().uri("/disturbances/affecteds/{partyId}", partyId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		verify(disturbanceServiceMock).findByPartyIdAndCategoryAndStatus(partyId, null, null);
		verifyNoMoreInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdAndFilterParameters() {

		// Parameter values.
		final var partyId = UUID.randomUUID().toString();
		final var categoryFilter = List.of(Category.COMMUNICATION, Category.ELECTRICITY);
		final var statusFilter = List.of(se.sundsvall.disturbance.api.model.Status.PLANNED, se.sundsvall.disturbance.api.model.Status.OPEN);

		webTestClient.get().uri(uriBuilder -> uriBuilder.path("/disturbances/affecteds/{partyId}")
			.queryParam("category", categoryFilter)
			.queryParam("status", statusFilter)
			.build(partyId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		verify(disturbanceServiceMock).findByPartyIdAndCategoryAndStatus(partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbance() {

		// Parameter values.
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		when(disturbanceServiceMock.findByCategoryAndDisturbanceId(category, disturbanceId)).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		webTestClient.get().uri("/disturbances/{category}/{disturbanceId}", category, disturbanceId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody().jsonPath("$").isMap();

		verify(disturbanceServiceMock).findByCategoryAndDisturbanceId(category, disturbanceId);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void updateDisturbance() {

		// Parameter values.
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var description = "Updated description";
		final var body = DisturbanceUpdateRequest.create()
			.withDescription(description);

		when(disturbanceServiceMock.updateDisturbance(category, disturbanceId, body)).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", category, disturbanceId)
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody().jsonPath("$").isMap();

		verify(disturbanceServiceMock).updateDisturbance(category, disturbanceId, body);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void deleteDisturbance() {

		// Parameter values.
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		doNothing().when(disturbanceServiceMock).deleteDisturbance(category, disturbanceId);

		webTestClient.delete().uri("/disturbances/{category}/{disturbanceId}", category, disturbanceId)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		verify(disturbanceServiceMock).deleteDisturbance(category, disturbanceId);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbance() {

		// Parameter values.
		final var body = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("123")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("title")
			.withDescription("description");

		when(disturbanceServiceMock.createDisturbance(body)).thenReturn(Disturbance.create().withId(body.getId()).withCategory(body.getCategory()));

		webTestClient.post().uri("/disturbances/")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("http://localhost:".concat(String.valueOf(port)).concat("/disturbances/COMMUNICATION/123"))
			.expectBody().isEmpty();

		verify(disturbanceServiceMock).createDisturbance(body);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedback() {

		// Parameter values.
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var body = DisturbanceFeedbackCreateRequest.create().withPartyId(UUID.randomUUID().toString());

		webTestClient.post().uri("/disturbances/{category}/{disturbanceId}/feedback", category, disturbanceId)
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		verify(disturbanceFeedbackServiceMock).createDisturbanceFeedback(category, disturbanceId, body);
		verifyNoMoreInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesByCategoryAndStatus() {

		final var categoryFilter = List.of(Category.COMMUNICATION, Category.ELECTRICITY);
		final var statusFilter = List.of(se.sundsvall.disturbance.api.model.Status.PLANNED, se.sundsvall.disturbance.api.model.Status.OPEN);

		webTestClient.get().uri(uriBuilder -> uriBuilder.path("/disturbances")
						.queryParam("category", categoryFilter)
						.queryParam("status", statusFilter)
						.build())
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBodyList(Disturbance.class).hasSize(0);

		verify(disturbanceServiceMock).findByStatusAndCategory(statusFilter, categoryFilter);
		verifyNoMoreInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}
}
