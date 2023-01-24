package se.sundsvall.disturbance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

	@Mock
	private FeedbackRepository feedbackRepositoryMock;

	@InjectMocks
	private FeedbackService feedbackService;

	@Captor
	private ArgumentCaptor<FeedbackEntity> feedbackEntityCaptor;

	@Test
	void createFeedback() {

		final var partyId = UUID.randomUUID().toString();

		when(feedbackRepositoryMock.findByPartyId(any(String.class))).thenReturn(Optional.empty());

		feedbackService.createFeedback(FeedbackCreateRequest.create().withPartyId(partyId));

		verify(feedbackRepositoryMock).findByPartyId(partyId);
		verify(feedbackRepositoryMock).save(feedbackEntityCaptor.capture());
		verifyNoMoreInteractions(feedbackRepositoryMock);

		final var feedbackEntityCaptorValue = feedbackEntityCaptor.getValue();
		assertThat(feedbackEntityCaptorValue).isNotNull();
		assertThat(feedbackEntityCaptorValue.getPartyId()).isEqualTo(partyId);
	}

	@Test
	void createFeedbackWhenAlreadyCreated() {

		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";
		final var request = FeedbackCreateRequest.create().withPartyId(partyId);

		when(feedbackRepositoryMock.findByPartyId(any())).thenReturn(Optional.of(new FeedbackEntity()));

		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> feedbackService.createFeedback(request));

		assertThat(throwableProblem.getMessage()).isEqualTo("Conflict: A feedback entity for partyId:'81471222-5798-11e9-ae24-57fa13b361e1' already exists!");
		assertThat(throwableProblem.getStatus()).isEqualTo(Status.CONFLICT);

		verify(feedbackRepositoryMock).findByPartyId(partyId);
		verifyNoMoreInteractions(feedbackRepositoryMock);
	}

	@Test
	void deleteFeedback() {

		final var partyId = UUID.randomUUID().toString();
		final var feedbackEntity = new FeedbackEntity();
		feedbackEntity.setPartyId(partyId);

		when(feedbackRepositoryMock.findByPartyId(any())).thenReturn(Optional.of(feedbackEntity));

		feedbackService.deleteFeedback(partyId);

		verify(feedbackRepositoryMock).findByPartyId(partyId);
		verify(feedbackRepositoryMock).delete(feedbackEntityCaptor.capture());
		verifyNoMoreInteractions(feedbackRepositoryMock);

		final var feedbackEntityCaptorValue = feedbackEntityCaptor.getValue();
		assertThat(feedbackEntityCaptorValue).isNotNull();
		assertThat(feedbackEntityCaptorValue.getPartyId()).isEqualTo(partyId);
	}

	@Test
	void deleteFeedbackNotFound() {

		final var partyId = "61e4f268-c5db-494c-86a0-4cc9a5cf411f";

		when(feedbackRepositoryMock.findByPartyId(any())).thenReturn(Optional.empty());

		final var throwableProblem = assertThrows(ThrowableProblem.class, () -> feedbackService.deleteFeedback(partyId));

		assertThat(throwableProblem.getMessage()).isEqualTo("Not Found: No feedback entity found for partyId:'61e4f268-c5db-494c-86a0-4cc9a5cf411f'!");
		assertThat(throwableProblem.getStatus()).isEqualTo(Status.NOT_FOUND);

		verify(feedbackRepositoryMock).findByPartyId(partyId);
		verifyNoMoreInteractions(feedbackRepositoryMock);
	}
}
