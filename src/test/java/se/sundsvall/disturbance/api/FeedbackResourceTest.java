package se.sundsvall.disturbance.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.service.FeedbackService;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class FeedbackResourceTest {

	@MockBean
	private FeedbackService feedbackServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createFeedback() {

		// Parameter values.
		final var body = FeedbackCreateRequest.create().withPartyId(UUID.randomUUID().toString());

		webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		verify(feedbackServiceMock).createFeedback(body);
		verifyNoMoreInteractions(feedbackServiceMock);
	}

	@Test
	void deleteFeedback() {

		// Parameter values.
		final var partyId = UUID.randomUUID().toString();

		webTestClient.delete().uri("/feedback/{partyId}", partyId)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		verify(feedbackServiceMock).deleteFeedback(partyId);
		verifyNoMoreInteractions(feedbackServiceMock);
	}
}
