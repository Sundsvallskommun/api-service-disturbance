package se.sundsvall.disturbance.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;

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
import se.sundsvall.disturbance.api.model.OptOutSetting;
import se.sundsvall.disturbance.api.model.SubscriptionCreateRequest;
import se.sundsvall.disturbance.api.model.SubscriptionUpdateRequest;
import se.sundsvall.disturbance.service.SubscriptionService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SubscriptionResourceFailuresTest {

	@MockBean
	private SubscriptionService subscriptionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createSubscriptionMissingBody() {

		// Act
		final var response = webTestClient.post().uri("/subscriptions")
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
			"Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.disturbance.api.SubscriptionResource.createSubscription(org.springframework.web.util.UriComponentsBuilder,se.sundsvall.disturbance.api.model.SubscriptionCreateRequest)");

		verifyNoInteractions(subscriptionServiceMock);
	}

	@Test
	void createSubscriptionEmptyBody() {

		// Act
		final var response = webTestClient.post().uri("/subscriptions")
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
			.withOptOutSettings(List.of(OptOutSetting.create()));

		// Act
		final var response = webTestClient.post().uri("/subscriptions")
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
	void getSubscriptionsByPartyIdMissingPartyId() {

		// Act
		final var response = webTestClient.get().uri("/subscriptions")
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
	void getSubscriptionsByPartyIdBadPartyId() {

		// Act
		final var response = webTestClient.get().uri("/subscriptions?partyId={partyId}", "this-is-not-an-uuid")
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
	void updateSubscriptionEmptyBody() {

		// Arrange
		final var request = SubscriptionUpdateRequest.create();

		// Act
		final var response = webTestClient.patch().uri("/subscriptions/{id}", "1234")
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		// Assert
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("optOutSettings", "must not be null"));

		verifyNoInteractions(subscriptionServiceMock);
	}
}
