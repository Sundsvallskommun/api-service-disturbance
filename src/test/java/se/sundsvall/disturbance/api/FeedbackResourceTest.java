package se.sundsvall.disturbance.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
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

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class FeedbackResourceTest {

	@MockBean
	private FeedbackService feedbackServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createFeedback() {

		// Arrange
		final var body = FeedbackCreateRequest.create().withPartyId(UUID.randomUUID().toString());

		// Act
		webTestClient.post().uri("/feedback")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		// Assert
		verify(feedbackServiceMock).createFeedback(body);
		verifyNoMoreInteractions(feedbackServiceMock);
	}

	@Test
	void deleteFeedback() {

		// Arrange
		final var partyId = UUID.randomUUID().toString();

		// Act
		webTestClient.delete().uri("/feedback/{partyId}", partyId)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().doesNotExist(HttpHeaders.CONTENT_TYPE)
			.expectBody().isEmpty();

		// Assert
		verify(feedbackServiceMock).deleteFeedback(partyId);
		verifyNoMoreInteractions(feedbackServiceMock);
	}
}
