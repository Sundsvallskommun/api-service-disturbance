package se.sundsvall.disturbance.api;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.service.FeedbackService;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class FeedbackResourceFailuresTest {

	@MockBean
	private FeedbackService feedbackServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	/**
	 * Create feedback tests:
	 */

	@Test
	void createContinuousFeedbackMissingBody() {

		webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo(BAD_REQUEST.getReasonPhrase())
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.detail").isEqualTo(
				"Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.FeedbackResource.createFeedback(se.sundsvall.disturbance.api.model.FeedbackCreateRequest)");

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackMissingPartyId() {

		webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.bodyValue(FeedbackCreateRequest.create()) // Missing partyId
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID");

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackBadBodyFormat() {

		webTestClient.post().uri("/feedback")
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

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackBadPartyId() {

		webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.bodyValue(FeedbackCreateRequest.create().withPartyId("bad-partyId-id"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID");

		verifyNoInteractions(feedbackServiceMock);
	}

	/**
	 * Delete feedback tests:
	 */

	@Test
	void deleteContinuousFeedbackBadPartyId() {

		webTestClient.delete().uri("/feedback/{partyId}", "not-an-uuid")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
			.jsonPath("$.violations[0].field").isEqualTo("deleteFeedback.partyId")
			.jsonPath("$.violations[0].message").isEqualTo("not a valid UUID");

		verifyNoInteractions(feedbackServiceMock);
	}
}
