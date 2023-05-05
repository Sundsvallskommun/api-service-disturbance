package se.sundsvall.disturbance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

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
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.service.FeedbackService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
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

		// Act
		final var response = webTestClient.post().uri("/feedback")
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
			"Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.FeedbackResource.createFeedback(se.sundsvall.disturbance.api.model.FeedbackCreateRequest)");

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackMissingPartyId() {

		// Act
		final var response = webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.bodyValue(FeedbackCreateRequest.create()) // Missing partyId
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

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackBadBodyFormat() {

		// Act
		final var response = webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.bodyValue("badformat")
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
			"JSON parse error: Unrecognized token 'badformat': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')");

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackBadPartyId() {

		// Act
		final var response = webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.bodyValue(FeedbackCreateRequest.create().withPartyId("bad-partyId-id"))
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

		verifyNoInteractions(feedbackServiceMock);
	}

	/**
	 * Delete feedback tests:
	 */

	@Test
	void deleteContinuousFeedbackBadPartyId() {

		// Act
		final var response = webTestClient.delete().uri("/feedback/{partyId}", "not-an-uuid")
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
			.containsExactly(tuple("deleteFeedback.partyId", "not a valid UUID"));

		verifyNoInteractions(feedbackServiceMock);
	}
}
