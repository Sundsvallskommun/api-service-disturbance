package se.sundsvall.disturbance.api;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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

	@MockBean
	private DisturbanceService disturbanceServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	/**
	 * Create disturbance tests:
	 */

	@Test
	void createDisturbanceMissingBody() {

		// Act
		final var response = webTestClient.post().uri("/disturbances")
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
			"Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.DisturbanceResource.createDisturbance(se.sundsvall.disturbance.api.model.DisturbanceCreateRequest)");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void createDisturbanceEmptyBody() {

		// Act
		final var response = webTestClient.post().uri("/disturbances")
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
		final var response = webTestClient.post().uri("/disturbances")
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
		final var response = webTestClient.post().uri("/disturbances")
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
		final var response = webTestClient.post().uri("/disturbances")
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
				.withPartyId(UUID.randomUUID().toString())
				.withReference(repeat("*", 513))));

		// Act
		final var response = webTestClient.post().uri("/disturbances")
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

	/**
	 * Get disturbance by partyId tests:
	 */

	@Test
	void getDisturbancesByPartyIdBadPartyId() {

		// Act
		final var response = webTestClient.get().uri("/disturbances/affecteds/{partyId}", "this-is-not-an-uuid")
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
	void getDisturbancesByPartyIdBadCategory() {

		// Act
		final var response = webTestClient.get().uri("/disturbances/affecteds/{partyId}?category={category}", UUID.randomUUID().toString(), "not-a-category")
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
			"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Category] for value [not-a-category]");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdIdBadStatus() {

		// Act
		final var response = webTestClient.get().uri("/disturbances/affecteds/{partyId}?status={status}", UUID.randomUUID().toString(), "not-a-status")
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
			"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Status] for value [not-a-status]");

		verifyNoInteractions(disturbanceServiceMock);
	}

	/**
	 * Update disturbance tests:
	 */

	@Test
	void updateDisturbanceMissingBody() {

		// Act
		final var response = webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", Category.COMMUNICATION, "12345")
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
			"Required request body is missing: public org.springframework.http.ResponseEntity<se.sundsvall.disturbance.api.model.Disturbance> se.sundsvall.disturbance.api.DisturbanceResource.updateDisturbance(se.sundsvall.disturbance.api.model.Category,java.lang.String,se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest)");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void updateDisturbanceContainsInvalidAffected() {

		// Arrange
		final var body = DisturbanceUpdateRequest.create() // Body with invalid partyId
			.withDescription("Description")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withAffecteds(List.of(
				Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
				Affected.create().withPartyId("invalid-party-id"), // Invalid UUID and missing reference.
				Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2")));

		// Act
		final var response = webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", Category.ELECTRICITY, "12345")
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
		final var body = DisturbanceUpdateRequest.create() // Body with to long parameters
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle(repeat("*", 256))
			.withDescription(repeat("*", 8193))
			.withAffecteds(List.of(Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference(repeat("*", 513))));

		// Act
		final var response = webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", Category.ELECTRICITY, "12345")
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
	void getDisturbancesBadCategory() {

		// Act
		final var response = webTestClient.get().uri("/disturbances?category={category}", "not-a-category")
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
			"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Category] for value [not-a-category]");

		verifyNoInteractions(disturbanceServiceMock);
	}

	@Test
	void getDisturbancesBadStatus() {

		// Act
		final var response = webTestClient.get().uri("/disturbances?status={status}", "not-a-status")
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
			"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Status] for value [not-a-status]");

		verifyNoInteractions(disturbanceServiceMock);
	}
}
