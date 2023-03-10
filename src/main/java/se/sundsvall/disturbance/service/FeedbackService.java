package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_FEEDBACK_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_FEEDBACK_NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.FeedbackMapper.toFeedbackEntity;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;

@Service
public class FeedbackService {

	@Autowired
	private FeedbackRepository feedbackRepository;

	@Transactional
	public void createFeedback(final FeedbackCreateRequest request) {

		// Check that no existing feedback already exists for provided parameters.
		if (feedbackRepository.findByPartyId(request.getPartyId()).isPresent()) {
			throw Problem.valueOf(Status.CONFLICT, format(ERROR_FEEDBACK_ALREADY_EXISTS, request.getPartyId()));
		}

		feedbackRepository.save(toFeedbackEntity(request));
	}

	@Transactional
	public void deleteFeedback(final String partyId) {

		final var feedbackEntity = feedbackRepository.findByPartyId(partyId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, format(ERROR_FEEDBACK_NOT_FOUND, partyId)));

		feedbackRepository.delete(feedbackEntity);
	}
}
