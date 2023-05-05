package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static se.sundsvall.disturbance.service.DisturbanceService.hasStatusClosed;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_CLOSED;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_FEEDBACK_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.DisturbanceFeedbackMapper.toDisturbanceFeedbackEntity;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

@Service
public class DisturbanceFeedbackService {

	@Autowired
	private DisturbanceRepository disturbanceRepository;

	@Autowired
	private DisturbanceFeedbackRepository disturbanceFeedbackRepository;

	@Transactional
	public void createDisturbanceFeedback(final Category category, final String disturbanceId, final DisturbanceFeedbackCreateRequest request) {

		// Check that disturbance exists.
		final var disturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceId(category, disturbanceId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId)));

		// Check that disturbance is not CLOSED.
		if (hasStatusClosed(disturbanceEntity)) {
			throw Problem.valueOf(Status.CONFLICT, format(ERROR_DISTURBANCE_CLOSED, category, disturbanceId));
		}

		// Check that no existing disturbance feedback already exists for provided parameters.
		if (disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyId(category, disturbanceId, request.getPartyId()).isPresent()) {
			throw Problem.valueOf(Status.CONFLICT, format(ERROR_DISTURBANCE_FEEDBACK_ALREADY_EXISTS, category, disturbanceId, request.getPartyId()));
		}

		disturbanceFeedbackRepository.save(toDisturbanceFeedbackEntity(category, disturbanceId, request));
	}
}
