package se.sundsvall.disturbance.api;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.service.DisturbanceService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DisturbanceResourceFailuresTest {

	private static final String PATH = "/{municipalityId}/disturbances";
	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private DisturbanceService disturbanceServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	/**
	 * Create disturbance tests:
	 */

	@Test
	void createDisturbanceMissingBody() {

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
			"Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.DisturbanceResource.createDisturbance(java.lang.String,se.sundsvall.disturbance.api.model.DisturbanceCreateRequest)");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void createDisturbanceEmptyBody() {

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(DisturbanceCreateRequest.create()) // Empty body
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
			.containsExactly(
				tuple("category", "must not be null"),
				tuple("description", "must not be null"),
				tuple("id", "must not be null"),
				tuple("status", "must not be null"),
				tuple("title", "must not be null"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void createDisturbanceMissingId() {

		// Arrange
		final var body = DisturbanceCreateRequest.create() // Body with missing id.
			.withCategory(Category.COMMUNICATION)
			.withTitle("Title")
			.withDescription("Description")
			.withStatus(Status.OPEN);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(tuple("id", "must not be null"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void createDisturbanceMissingCategory() {

		// Arrange
		final var body = DisturbanceCreateRequest.create() // Body with missing category.
			.withId("id")
			.withStatus(Status.OPEN)
			.withTitle("Title")
			.withDescription("Description");

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(tuple("category", "must not be null"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void createDisturbanceContainsInvalidAffected() {

		// Arrange
		final var body = DisturbanceCreateRequest.create() // Body with invalid partyId
			.withId("12345")
			.withCategory(Category.ELECTRICITY)
			.withTitle("Title")
			.withDescription("Description")
			.withStatus(Status.OPEN)
			.withAffecteds(List.of(
				Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
				Affected.create().withPartyId("invalid-party-id"), // Invalid UUID and missing reference.
				Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2")));

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(
				tuple("affecteds[1].partyId", "not a valid UUID"),
				tuple("affecteds[1].reference", "must not be null"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void createDisturbanceToLongParameters() {

		// Arrange
		final var body = DisturbanceCreateRequest.create() // Body with to long parameters.
			.withId(repeat("*", 256))
			.withStatus(Status.OPEN)
			.withCategory(Category.ELECTRICITY)
			.withTitle(repeat("*", 256))
			.withDescription(repeat("*", 8193))
			.withAffecteds(List.of(Affected.create()
				.withPartyId(randomUUID().toString())
				.withReference(repeat("*", 513))));

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(
				tuple("affecteds[0].reference", "size must be between 0 and 512"),
				tuple("description", "size must be between 0 and 8192"),
				tuple("id", "size must be between 0 and 255"),
				tuple("title", "size must be between 0 and 255"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void createDisturbanceInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid-municipalityId";
		final var body = DisturbanceCreateRequest.create()
			.withId("12345")
			.withCategory(Category.COMMUNICATION)
			.withTitle("Title")
			.withDescription("Description")
			.withStatus(Status.OPEN);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(tuple("createDisturbance.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	/**
	 * Get disturbance by partyId tests:
	 */

	@Test
	void getDisturbancesByPartyIdInvalidPartyId() {

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/affecteds/{partyId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "partyId", "invalid-uuid")))
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
			.containsExactly(tuple("getDisturbancesByPartyId.partyId", "not a valid UUID"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdInvalidCategory() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var category = "invalid-category";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/affecteds/{partyId}").queryParam("category", category).build(Map.of("municipalityId", MUNICIPALITY_ID, "partyId", partyId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Method parameter 'category': Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Category] for value [invalid-category]");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdIdInvalidStatus() {

		// Arrange
		final var partyId = randomUUID().toString();
		final var status = "invalid-status";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/affecteds/{partyId}").queryParam("status", status).build(Map.of("municipalityId", MUNICIPALITY_ID, "partyId", partyId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Method parameter 'status': Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Status] for value [invalid-status]");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid-municipalityId";
		final var partyId = randomUUID().toString();

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/affecteds/{partyId}").build(Map.of("municipalityId", municipalityId, "partyId", partyId)))
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
			.containsExactly(tuple("getDisturbancesByPartyId.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	/**
	 * Update disturbance tests:
	 */

	@Test
	void updateDisturbanceMissingBody() {

		// Arrange
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH + "/{category}/{disturbanceId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "category", category, "disturbanceId", disturbanceId)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Required request body is missing: org.springframework.http.ResponseEntity<se.sundsvall.disturbance.api.model.Disturbance> se.sundsvall.disturbance.api.DisturbanceResource.updateDisturbance(java.lang.String,se.sundsvall.disturbance.api.model.Category,java.lang.String,se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest)");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void updateDisturbanceContainsInvalidAffected() {

		// Arrange
		final var category = Category.ELECTRICITY;
		final var disturbanceId = "12345";
		final var body = DisturbanceUpdateRequest.create() // Body with invalid partyId
			.withDescription("Description")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withAffecteds(List.of(
				Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
				Affected.create().withPartyId("invalid-party-id"), // Invalid UUID and missing reference.
				Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2")));

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH + "/{category}/{disturbanceId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "category", category, "disturbanceId", disturbanceId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(
				tuple("affecteds[1].partyId", "not a valid UUID"),
				tuple("affecteds[1].reference", "must not be null"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void updateDisturbanceToLongParameters() {

		// Arrange
		final var category = Category.ELECTRICITY;
		final var disturbanceId = "12345";
		final var body = DisturbanceUpdateRequest.create() // Body with to long parameters
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle(repeat("*", 256))
			.withDescription(repeat("*", 8193))
			.withAffecteds(List.of(Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference(repeat("*", 513))));

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH + "/{category}/{disturbanceId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "category", category, "disturbanceId", disturbanceId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(
				tuple("affecteds[0].reference", "size must be between 0 and 512"),
				tuple("description", "size must be between 0 and 8192"),
				tuple("title", "size must be between 0 and 255"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void updateDisturbanceInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid-municipalityId";
		final var category = Category.ELECTRICITY;
		final var disturbanceId = "12345";
		final var body = DisturbanceUpdateRequest.create()
			.withDescription("Description")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withAffecteds(List.of(
				Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
				Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2")));

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH + "/{category}/{disturbanceId}").build(Map.of("municipalityId", municipalityId, "category", category, "disturbanceId", disturbanceId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
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
			.containsExactly(
				tuple("updateDisturbance.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesInvalidCategory() {

		// Arrange
		final var category = "invalid-category";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("category", category).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Method parameter 'category': Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Category] for value [invalid-category]");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesInvalidStatus() {

		// Arrange
		final var status = "invalid-status";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("status", status).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Method parameter 'status': Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Status] for value [invalid-status]");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid-municipalityId";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
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
			.containsExactly(
				tuple("getDisturbances.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(disturbanceServiceMock);
	}
}
