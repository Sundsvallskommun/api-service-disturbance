package se.sundsvall.disturbance.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.OptOutSetting;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.service.SubscriptionService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SubscriptionResourceFailuresTest {

	private static final String PATH = "/{municipalityId}/subscriptions";
	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private SubscriptionService subscriptionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createSubscriptionMissingBody() {

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.SubscriptionResource.createSubscription(java.lang.String,se.sundsvall.disturbance.api.model.SubscriptionCreateRequest)");

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void createSubscriptionEmptyBody() {

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(SubscriptionCreateRequest.create()) // Empty body
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("partyId", "not a valid UUID"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void createSubscriptionMissingPartyId() {

		// Arrange
		final var request = SubscriptionCreateRequest.create()
			.withPartyId(null)  // missing partyId.
			.withOptOutSettings(List.of(OptOutSetting.create().withCategory(ELECTRICITY)));

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("partyId", "not a valid UUID"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void createSubscriptionInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid-municipalityId";
		final var request = SubscriptionCreateRequest.create()
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create().withCategory(ELECTRICITY)));

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("createSubscription.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void createSubscriptionMissingOptOutCategory() {

		// Arrange
		final var request = SubscriptionCreateRequest.create()
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(null))); // missing category.

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("optOutSettings[0].category", "must not be null"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void createSubscriptionBlankOptOutKey() {

		// Arrange
		final var request = SubscriptionCreateRequest.create()
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(ELECTRICITY)
				.withValues(Map.of(" ", "123456")))); // Blank key

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("optOutSettings[0].values[ ]", "must not be blank"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void createSubscriptionBlankOptOutValue() {

		// Arrange
		final var request = SubscriptionCreateRequest.create()
			.withPartyId(randomUUID().toString())
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(ELECTRICITY)
				.withValues(Map.of("facilityId", " ")))); // Blank value

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("optOutSettings[0].values[facilityId]", "must not be blank"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void getSubscriptionsByPartyIdMissingPartyId() {

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getDetail()).isEqualTo(
			"Required request parameter 'partyId' for method parameter type String is not present");

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void getSubscriptionsByPartyIdInvalidPartyId() {

		// Arrange
		final var partyId = "invalid-partyId";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("partyId", partyId)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("findSubscription.partyId", "not a valid UUID"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void getSubscriptionsByPartyIdInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid-municipalityId";
		final var partyId = randomUUID().toString();

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("partyId", partyId)
				.build(Map.of("municipalityId", municipalityId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("findSubscription.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void updateSubscriptionMissingOptOutCategory() {

		// Arrange
		final var id = "12345";
		final var request = SubscriptionUpdateRequest.create()
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(null))); // missing category.

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("optOutSettings[0].category", "must not be null"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void updateSubscriptionBlankOptOutKey() {

		// Arrange
		final var id = "12345";
		final var request = SubscriptionUpdateRequest.create()
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(ELECTRICITY)
				.withValues(Map.of(" ", "12345")))); // Blank key

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("optOutSettings[0].values[ ]", "must not be blank"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void updateSubscriptionBlankOptOutValue() {

		// Arrange
		final var id = "12345";
		final var request = SubscriptionUpdateRequest.create()
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(ELECTRICITY)
				.withValues(Map.of("facilityId", " ")))); // Blank value

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("optOutSettings[0].values[facilityId]", "must not be blank"));

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void updateSubscriptionInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid-municipalityId";
		final var id = "12345";
		final var request = SubscriptionUpdateRequest.create()
			.withOptOutSettings(List.of(OptOutSetting.create()
				.withCategory(ELECTRICITY)
				.withValues(Map.of("facilityId", "12345"))));

		// Act
		final var response = webTestClient.put()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", municipalityId, "id", id)))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("updateSubscription.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(subscriptionServiceMock);
	}
}
