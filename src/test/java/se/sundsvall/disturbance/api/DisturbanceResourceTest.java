package se.sundsvall.disturbance.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

	private static final String PATH = "/{municipalityId}/disturbances";
	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private DisturbanceService disturbanceServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getByPartyId() {

		// Arrange
		final var partyId = randomUUID().toString();

		// Act
		webTestClient.get()
			.uri(builder -> builder.path(PATH + "/affecteds/{partyId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "partyId", partyId)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		// Assert
		verify(disturbanceServiceMock).findByMunicipalityIdAndPartyIdAndCategoryAndStatus(MUNICIPALITY_ID, partyId, null, null);
		verifyNoMoreInteractions(disturbanceServiceMock);
	}

	@Test
	void getByPartyIdAndFilterParameters() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var categoryFilter = List.of(Category.COMMUNICATION, Category.ELECTRICITY);
		final var statusFilter = List.of(Status.PLANNED, Status.OPEN);

		// Act
		webTestClient.get()
			.uri(builder -> builder.path(PATH + "/affecteds/{partyId}")
				.queryParam("category", categoryFilter)
				.queryParam("status", statusFilter)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "partyId", partyId)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		// Assert
		verify(disturbanceServiceMock).findByMunicipalityIdAndPartyIdAndCategoryAndStatus(MUNICIPALITY_ID, partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceServiceMock);
	}

	@Test
	void get() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		when(disturbanceServiceMock.findByMunicipalityIdAndCategoryAndDisturbanceId(any(), any(), any())).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{category}/{disturbanceId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "category", category, "disturbanceId", disturbanceId)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Disturbance.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();

		verify(disturbanceServiceMock).findByMunicipalityIdAndCategoryAndDisturbanceId(MUNICIPALITY_ID, category, disturbanceId);
	}

	@Test
	void update() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var description = "Updated description";
		final var body = DisturbanceUpdateRequest.create()
			.withDescription(description);

		when(disturbanceServiceMock.updateDisturbance(any(), any(), any(), any())).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH + "/{category}/{disturbanceId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "category", category, "disturbanceId", disturbanceId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Disturbance.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();

		verify(disturbanceServiceMock).updateDisturbance(MUNICIPALITY_ID, category, disturbanceId, body);
	}

	@Test
	void delete() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		doNothing().when(disturbanceServiceMock).deleteDisturbance(any(), any(), any());

		// Act
		webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{category}/{disturbanceId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "category", category, "disturbanceId", disturbanceId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		// Assert
		verify(disturbanceServiceMock).deleteDisturbance(MUNICIPALITY_ID, category, disturbanceId);
	}

	@Test
	void create() {

		// Arrange
		final var body = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("123")
			.withStatus(Status.OPEN)
			.withTitle("title")
			.withDescription("description");

		when(disturbanceServiceMock.createDisturbance(any(), any()))
			.thenReturn(Disturbance.create()
				.withId(body.getId())
				.withCategory(body.getCategory()));

		// Act
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/disturbances/" + body.getCategory() + "/" + body.getId())
			.expectBody().isEmpty();

		// Assert
		verify(disturbanceServiceMock).createDisturbance(MUNICIPALITY_ID, body);
	}

	@Test
	void getByStatusAndCategory() {

		// Arrange
		final var categoryFilter = List.of(Category.COMMUNICATION, Category.ELECTRICITY);
		final var statusFilter = List.of(Status.PLANNED, Status.OPEN);

		// Act
		webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("category", categoryFilter)
				.queryParam("status", statusFilter)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Disturbance.class).hasSize(0);

		// Assert
		verify(disturbanceServiceMock).findByMunicipalityIdAndStatusAndCategory(MUNICIPALITY_ID, statusFilter, categoryFilter);
	}
}
