package se.sundsvall.disturbance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
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
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.service.DisturbanceService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DisturbanceResourceTest {

	@MockBean
	private DisturbanceService disturbanceServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@LocalServerPort
	private int port;

	@Test
	void getDisturbancesByPartyId() {

		// Arrange
		final var partyId = UUID.randomUUID().toString();

		// Act
		webTestClient.get().uri("/disturbances/affecteds/{partyId}", partyId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		// Assert
		verify(disturbanceServiceMock).findByPartyIdAndCategoryAndStatus(partyId, null, null);
		verifyNoMoreInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdAndFilterParameters() {

		// Arrange
		final var partyId = UUID.randomUUID().toString();
		final var categoryFilter = List.of(Category.COMMUNICATION, Category.ELECTRICITY);
		final var statusFilter = List.of(Status.PLANNED, Status.OPEN);

		// Act
		webTestClient.get().uri(uriBuilder -> uriBuilder.path("/disturbances/affecteds/{partyId}")
			.queryParam("category", categoryFilter)
			.queryParam("status", statusFilter)
			.build(partyId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		// Assert
		verify(disturbanceServiceMock).findByPartyIdAndCategoryAndStatus(partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbance() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		when(disturbanceServiceMock.findByCategoryAndDisturbanceId(category, disturbanceId)).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		// Act
		final var response = webTestClient.get().uri("/disturbances/{category}/{disturbanceId}", category, disturbanceId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Disturbance.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();

		verify(disturbanceServiceMock).findByCategoryAndDisturbanceId(category, disturbanceId);
	}

	@Test
	void updateDisturbance() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var description = "Updated description";
		final var body = DisturbanceUpdateRequest.create()
			.withDescription(description);

		when(disturbanceServiceMock.updateDisturbance(category, disturbanceId, body)).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		// Act
		final var response = webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", category, disturbanceId)
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Disturbance.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();

		verify(disturbanceServiceMock).updateDisturbance(category, disturbanceId, body);
	}

	@Test
	void deleteDisturbance() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		doNothing().when(disturbanceServiceMock).deleteDisturbance(category, disturbanceId);

		// Act
		webTestClient.delete().uri("/disturbances/{category}/{disturbanceId}", category, disturbanceId)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		// Assert
		verify(disturbanceServiceMock).deleteDisturbance(category, disturbanceId);
	}

	@Test
	void createDisturbance() {

		// Arrange
		final var body = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("123")
			.withStatus(Status.OPEN)
			.withTitle("title")
			.withDescription("description");

		when(disturbanceServiceMock.createDisturbance(body))
				.thenReturn(Disturbance.create()
						.withId(body.getId())
						.withCategory(body.getCategory()));

		// Act
		webTestClient.post().uri("/disturbances")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("http://localhost:".concat(String.valueOf(port)).concat("/disturbances/COMMUNICATION/123"))
			.expectBody().isEmpty();

		// Assert
		verify(disturbanceServiceMock).createDisturbance(body);
	}

	@Test
	void getDisturbancesByCategoryAndStatus() {

		// Arrange
		final var categoryFilter = List.of(Category.COMMUNICATION, Category.ELECTRICITY);
		final var statusFilter = List.of(Status.PLANNED, Status.OPEN);

		// Act
		webTestClient.get().uri(uriBuilder -> uriBuilder.path("/disturbances")
			.queryParam("category", categoryFilter)
			.queryParam("status", statusFilter)
			.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		// Assert
		verify(disturbanceServiceMock).findByStatusAndCategory(statusFilter, categoryFilter);
	}
}
