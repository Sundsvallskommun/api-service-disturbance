package se.sundsvall.disturbance.api;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.service.DisturbanceFeedbackService;
import se.sundsvall.disturbance.service.DisturbanceService;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class DisturbanceResourceFailuresTest {

	@MockBean
	private DisturbanceFeedbackService disturbanceFeedbackServiceMock;

	@MockBean
	private DisturbanceService disturbanceServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	/**
	 * Create disturbance tests:
	 */

	@Test
	void createDisturbanceMissingBody() {

		webTestClient.post().uri("/disturbances/")
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.detail").isEqualTo(
				"Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.DisturbanceResource.createDisturbance(org.springframework.web.util.UriComponentsBuilder,se.sundsvall.disturbance.api.model.DisturbanceCreateRequest)");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceEmptyBody() {

		webTestClient.post().uri("/disturbances/")
			.contentType(APPLICATION_JSON)
			.bodyValue(DisturbanceCreateRequest.create()) // Empty body
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("category")
			.jsonPath("$.violations[0].message").isEqualTo("must not be null")
			.jsonPath("$.violations[1].field").isEqualTo("description")
			.jsonPath("$.violations[1].message").isEqualTo("must not be null")
			.jsonPath("$.violations[2].field").isEqualTo("id")
			.jsonPath("$.violations[2].message").isEqualTo("must not be null")
			.jsonPath("$.violations[3].field").isEqualTo("status")
			.jsonPath("$.violations[3].message").isEqualTo("must not be null")
			.jsonPath("$.violations[4].field").isEqualTo("title")
			.jsonPath("$.violations[4].message").isEqualTo("must not be null");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceMissingId() {

		// Parameter values.
		final var body = DisturbanceCreateRequest.create() // Body with missing id.
			.withCategory(Category.COMMUNICATION)
			.withTitle("Title")
			.withDescription("Description")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN);

		webTestClient.post().uri("/disturbances/")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("id")
			.jsonPath("$.violations[0].message").isEqualTo("must not be null");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceMissingCategory() {

		// Parameter values.
		final var body = DisturbanceCreateRequest.create() // Body with missing category.
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("Title")
			.withDescription("Description");

		webTestClient.post().uri("/disturbances/")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("category")
			.jsonPath("$.violations[0].message").isEqualTo("must not be null");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceContainsInvalidAffected() {

		// Parameter values.
		final var body = DisturbanceCreateRequest.create() // Body with invalid partyId
			.withId("12345")
			.withCategory(Category.ELECTRICITY)
			.withTitle("Title")
			.withDescription("Description")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withAffecteds(List.of(
				Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
				Affected.create().withPartyId("invalid-party-id"), // Invalid UUID and missing reference.
				Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2")));

		webTestClient.post().uri("/disturbances/")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("affecteds[1].partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID")
			.jsonPath("$.violations[1].field").isEqualTo("affecteds[1].reference")
			.jsonPath("$.violations[1].message").isEqualTo("must not be null");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceToLongParameters() {

		// Parameter values.
		final var body = DisturbanceCreateRequest.create() // Body with to long parameters.
			.withId(repeat("*", 256))
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withCategory(Category.ELECTRICITY)
			.withTitle(repeat("*", 256))
			.withDescription(repeat("*", 8193))
			.withAffecteds(List.of(Affected.create()
				.withPartyId(UUID.randomUUID().toString())
				.withReference(repeat("*", 513))));

		webTestClient.post().uri("/disturbances/")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("affecteds[0].reference")
			.jsonPath("$.violations[0].message").isEqualTo("size must be between 0 and 512")
			.jsonPath("$.violations[1].field").isEqualTo("description")
			.jsonPath("$.violations[1].message").isEqualTo("size must be between 0 and 8192")
			.jsonPath("$.violations[2].field").isEqualTo("id")
			.jsonPath("$.violations[2].message").isEqualTo("size must be between 0 and 255")
			.jsonPath("$.violations[3].field").isEqualTo("title")
			.jsonPath("$.violations[3].message").isEqualTo("size must be between 0 and 255");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Get disturbance by partyId tests:
	 */

	@Test
	void getDisturbancesByPartyIdBadPartyId() {

		webTestClient.get().uri("/disturbances/affecteds/{partyId}", "this-is-not-an-uuid")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("getDisturbancesByPartyId.partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdBadCategory() {

		webTestClient.get().uri("/disturbances/affecteds/{partyId}?category={category}", UUID.randomUUID().toString(), "not-a-category")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.detail").isEqualTo(
				"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Category] for value 'not-a-category'; nested exception is java.lang.IllegalArgumentException: No enum constant se.sundsvall.disturbance.api.model.Category.not-a-category");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdIdBadStatus() {

		webTestClient.get().uri("/disturbances/affecteds/{partyId}?status={status}", UUID.randomUUID().toString(), "not-a-status")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.detail").isEqualTo(
				"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Status] for value 'not-a-status'; nested exception is java.lang.IllegalArgumentException: No enum constant se.sundsvall.disturbance.api.model.Status.not-a-status");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Update disturbance tests:
	 */

	@Test
	void updateDisturbanceMissingBody() {

		webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", Category.COMMUNICATION, "12345")
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.detail").isEqualTo(
				"Required request body is missing: public org.springframework.http.ResponseEntity<se.sundsvall.disturbance.api.model.Disturbance> se.sundsvall.disturbance.api.DisturbanceResource.updateDisturbance(se.sundsvall.disturbance.api.model.Category,java.lang.String,se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest)");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void updateDisturbanceContainsInvalidAffected() {

		// Parameter values.
		final var body = DisturbanceUpdateRequest.create() // Body with invalid partyId
			.withDescription("Description")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withAffecteds(List.of(
				Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
				Affected.create().withPartyId("invalid-party-id"), // Invalid UUID and missing reference.
				Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2")));

		webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", Category.ELECTRICITY, "12345")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("affecteds[1].partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID")
			.jsonPath("$.violations[1].field").isEqualTo("affecteds[1].reference")
			.jsonPath("$.violations[1].message").isEqualTo("must not be null");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void updateDisturbanceToLongParameters() {

		// Parameter values.
		final var body = DisturbanceUpdateRequest.create() // Body with to long parameters
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle(repeat("*", 256))
			.withDescription(repeat("*", 8193))
			.withAffecteds(List.of(Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference(repeat("*", 513))));

		webTestClient.patch().uri("/disturbances/{category}/{disturbanceId}", Category.ELECTRICITY, "12345")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("affecteds[0].reference")
			.jsonPath("$.violations[0].message").isEqualTo("size must be between 0 and 512")
			.jsonPath("$.violations[1].field").isEqualTo("description")
			.jsonPath("$.violations[1].message").isEqualTo("size must be between 0 and 8192")
			.jsonPath("$.violations[2].field").isEqualTo("title")
			.jsonPath("$.violations[2].message").isEqualTo("size must be between 0 and 255");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Create disturbance feedback tests:
	 */

	@Test
	void createDisturbanceFeedbackMissingBody() {

		webTestClient.post().uri("/disturbances/{category}/{disturbanceId}/feedback", Category.ELECTRICITY, "12345")
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.detail").isEqualTo(
				"Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.DisturbanceResource.createDisturbanceFeedback(se.sundsvall.disturbance.api.model.Category,java.lang.String,se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest)");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedbackMissingPartyId() {

		webTestClient.post().uri("/disturbances/{category}/{disturbanceId}/feedback", Category.ELECTRICITY, "12345")
			.contentType(APPLICATION_JSON)
			.bodyValue(DisturbanceFeedbackCreateRequest.create().withPartyId(null)) // Missing partyId
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedbackBadBodyFormat() {

		webTestClient.post().uri("/disturbances/{category}/{disturbanceId}/feedback", Category.ELECTRICITY, "12345")
			.contentType(APPLICATION_JSON)
			.bodyValue("badformat")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.detail").isEqualTo(
				"JSON parse error: Unrecognized token 'badformat': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false'); nested exception is com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'badformat': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); line: 1, column: 10]");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedbackBadPartyId() {

		webTestClient.post().uri("/disturbances/{category}/{disturbanceId}/feedback", Category.ELECTRICITY, "12345")
			.contentType(APPLICATION_JSON)
			.bodyValue(DisturbanceFeedbackCreateRequest.create().withPartyId("bad-party-id"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesBadCategory() {

		webTestClient.get().uri("/disturbances?category={category}", "not-a-category")
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
				.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
				.jsonPath("$.detail").isEqualTo(
						"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Category] for value 'not-a-category'; nested exception is java.lang.IllegalArgumentException: No enum constant se.sundsvall.disturbance.api.model.Category.not-a-category");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesBadStatus() {

		webTestClient.get().uri("/disturbances?status={status}", "not-a-status")
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
				.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
				.jsonPath("$.detail").isEqualTo(
						"Failed to convert value of type 'java.lang.String' to required type 'java.util.List'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@io.swagger.v3.oas.annotations.Parameter @org.springframework.web.bind.annotation.RequestParam se.sundsvall.disturbance.api.model.Status] for value 'not-a-status'; nested exception is java.lang.IllegalArgumentException: No enum constant se.sundsvall.disturbance.api.model.Status.not-a-status");

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}
}
