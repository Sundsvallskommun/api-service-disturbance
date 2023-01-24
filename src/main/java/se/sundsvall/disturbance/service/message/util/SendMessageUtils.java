package se.sundsvall.disturbance.service.message.util;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.List;

import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

public class SendMessageUtils {

	private SendMessageUtils() {}

	/**
	 * Returns true if the affectedEntity matches (exists in) the list of disturbanceFeedbackEntities.
	 * 
	 * @param affectedEntity
	 * @param disturbanceFeedbackEntities the list of disturbanceFeedbackEntities
	 * @return True if there is a match, false otherwise.
	 */
	public static boolean hasDisturbanceFeedBackEntity(AffectedEntity affectedEntity, List<DisturbanceFeedbackEntity> disturbanceFeedbackEntities) {
		return ofNullable(disturbanceFeedbackEntities).orElse(emptyList()).stream()
			.anyMatch(disturbanceFeedbackEntity -> affectedEntity.getPartyId().equalsIgnoreCase(disturbanceFeedbackEntity.getPartyId()));
	}
}
