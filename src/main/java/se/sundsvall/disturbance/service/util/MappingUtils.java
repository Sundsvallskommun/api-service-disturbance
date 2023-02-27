package se.sundsvall.disturbance.service.util;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.List;

import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

public class MappingUtils {

	private MappingUtils() {}

	/**
	 * Returns all removed AffectedEntity elements from oldDisturbanceEntity.getAffectedEntities(), when comparing with
	 * newDisturbanceEntity.getAffectedEntities().
	 *
	 * E.g.
	 *
	 * oldDisturbanceEntity.getAffectedEntities() contains: <ELEMENT-1>, <ELEMENT-2>, <ELEMENT-3>
	 * newDisturbanceEntity.getAffectedEntities() contains: <ELEMENT-1>, <ELEMENT-3>
	 *
	 * Result: This method will return [<ELEMENT-2>]
	 *
	 * @param oldDisturbanceEntity the old DisturbanceEntity.
	 * @param newDisturbanceEntity the new DisturbanceEntity.
	 * @return Returns the difference (removed elements) from oldDisturbanceEntity.
	 */
	public static List<AffectedEntity> getRemovedAffectedEntities(final DisturbanceEntity oldDisturbanceEntity, final DisturbanceEntity newDisturbanceEntity) {
		// If affectedEntities in newDisturbanceEntity isn't set (i.e. is null), just return an empty list.
		if (isNull(newDisturbanceEntity.getAffectedEntities())) {
			return emptyList();
		}
		return ofNullable(oldDisturbanceEntity.getAffectedEntities()).orElse(emptyList()).stream()
			.filter(oldEntity -> !existsInList(oldEntity, newDisturbanceEntity.getAffectedEntities()))
			.toList();
	}

	/**
	 * Returns all added AffectedEntity elements from newDisturbanceEntity.getAffectedEntities(), when comparing with
	 * oldDisturbanceEntity.getAffectedEntities().
	 *
	 * E.g.
	 *
	 * oldDisturbanceEntity.getAffectedEntities() contains: <ELEMENT-1>, <ELEMENT-2>, <ELEMENT-3>
	 * newDisturbanceEntity.getAffectedEntities() contains: <ELEMENT-1>, <ELEMENT-4>
	 *
	 * Result: This method will return [<ELEMENT-4>]
	 *
	 * @param oldDisturbanceEntity the old DisturbanceEntity.
	 * @param newDisturbanceEntity the new DisturbanceEntity.
	 * @return Returns the added elements from newDisturbanceEntity.
	 */
	public static List<AffectedEntity> getAddedAffectedEntities(final DisturbanceEntity oldDisturbanceEntity, final DisturbanceEntity newDisturbanceEntity) {
		if (isNull(oldDisturbanceEntity) || isNull(oldDisturbanceEntity.getAffectedEntities())) {
			return newDisturbanceEntity.getAffectedEntities();
		}
		// If affectedEntities in newDisturbanceEntity isn't set (i.e. is null), just return an empty list.
		if (isNull(newDisturbanceEntity) || isNull(newDisturbanceEntity.getAffectedEntities())) {
			return emptyList();
		}
		return newDisturbanceEntity.getAffectedEntities().stream()
			.filter(newEntity -> !existsInList(newEntity, oldDisturbanceEntity.getAffectedEntities()))
			.toList();
	}

	private static boolean existsInList(final AffectedEntity objectToCheck, final List<AffectedEntity> list) {
		return ofNullable(list).orElse(emptyList()).stream()
			.anyMatch(entity -> equalsIgnoreCase(entity.getPartyId(), objectToCheck.getPartyId()) &&
				equalsIgnoreCase(entity.getReference(), objectToCheck.getReference()) &&
				equalsIgnoreCase(entity.getFacilityId(), objectToCheck.getFacilityId()));
	}
}
