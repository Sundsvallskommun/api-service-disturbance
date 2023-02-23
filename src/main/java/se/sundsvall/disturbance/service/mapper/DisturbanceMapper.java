package se.sundsvall.disturbance.service.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static se.sundsvall.dept44.util.DateUtils.toOffsetDateTimeWithLocalOffset;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.service.util.MappingUtils;

public class DisturbanceMapper {

	private DisturbanceMapper() {}

	public static Disturbance toDisturbance(final DisturbanceEntity disturbanceEntity) {
		return Disturbance.create()
			.withCategory(isNull(disturbanceEntity.getCategory()) ? null : Category.valueOf(disturbanceEntity.getCategory()))
			.withTitle(disturbanceEntity.getTitle())
			.withDescription(disturbanceEntity.getDescription())
			.withId(disturbanceEntity.getDisturbanceId())
			.withDescription(disturbanceEntity.getDescription())
			.withAffecteds(toAffecteds(disturbanceEntity.getAffectedEntities()))
			.withStatus(isNull(disturbanceEntity.getStatus()) ? null : Status.valueOf(disturbanceEntity.getStatus()))
			.withCreated(disturbanceEntity.getCreated())
			.withPlannedStartDate(disturbanceEntity.getPlannedStartDate())
			.withPlannedStopDate(disturbanceEntity.getPlannedStopDate())
			.withUpdated(disturbanceEntity.getUpdated());
	}

	public static DisturbanceEntity toDisturbanceEntity(final DisturbanceCreateRequest disturbanceCreateRequest) {
		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.addAffectedEntities(toAffectedEntities(disturbanceCreateRequest.getAffecteds()));
		disturbanceEntity.setCategory(String.valueOf(disturbanceCreateRequest.getCategory()));
		disturbanceEntity.setDescription(disturbanceCreateRequest.getDescription());
		disturbanceEntity.setDisturbanceId(disturbanceCreateRequest.getId());
		disturbanceEntity.setPlannedStartDate(toOffsetDateTimeWithLocalOffset(disturbanceCreateRequest.getPlannedStartDate()));
		disturbanceEntity.setPlannedStopDate(toOffsetDateTimeWithLocalOffset(disturbanceCreateRequest.getPlannedStopDate()));
		disturbanceEntity.setStatus(Objects.toString(disturbanceCreateRequest.getStatus(), null));
		disturbanceEntity.setTitle(disturbanceCreateRequest.getTitle());

		return disturbanceEntity;
	}

	public static DisturbanceEntity toDisturbanceEntity(final Category category, final String disturbanceId, final DisturbanceUpdateRequest disturbanceUpdateRequest) {
		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.addAffectedEntities(toAffectedEntities(disturbanceUpdateRequest.getAffecteds()));
		disturbanceEntity.setCategory(String.valueOf(category));
		disturbanceEntity.setDescription(disturbanceUpdateRequest.getDescription());
		disturbanceEntity.setDisturbanceId(disturbanceId);
		disturbanceEntity.setPlannedStartDate(toOffsetDateTimeWithLocalOffset(disturbanceUpdateRequest.getPlannedStartDate()));
		disturbanceEntity.setPlannedStopDate(toOffsetDateTimeWithLocalOffset(disturbanceUpdateRequest.getPlannedStopDate()));
		disturbanceEntity.setStatus(Objects.toString(disturbanceUpdateRequest.getStatus(), null));
		disturbanceEntity.setTitle(disturbanceUpdateRequest.getTitle());

		return disturbanceEntity;
	}

	/**
	 * Merge all new values from "newEntity" the the "oldEntity". Values are only used (copied) if they are not null.
	 *
	 * @param oldEntity
	 * @param newEntity
	 * @return the old entity with available (non-null) values from the new entity.
	 */
	public static DisturbanceEntity toMergedDisturbanceEntity(final DisturbanceEntity oldEntity, final DisturbanceEntity newEntity) {

		Optional.ofNullable(newEntity.getAffectedEntities()).ifPresent(ae -> {
			final var removedAffectedEntities = MappingUtils.getRemovedAffectedEntities(oldEntity, newEntity);
			final var addedAffectedEntities = MappingUtils.getAddedAffectedEntities(oldEntity, newEntity);

			// Remove old affectedEntities that doesn't exist in the new affectedEntities list.
			if (nonNull(oldEntity.getAffectedEntities())) {
				oldEntity.getAffectedEntities().removeAll(removedAffectedEntities);
			}

			// Remove new affectedEntities that isn't new (i.e. added).
			newEntity.getAffectedEntities().retainAll(addedAffectedEntities);

			// Add remaining new affectedEntities to the old affectedEntities list.
			oldEntity.addAffectedEntities(newEntity.getAffectedEntities());
		});

		ofNullable(newEntity.getDescription()).ifPresent(oldEntity::setDescription);
		ofNullable(newEntity.getPlannedStartDate()).ifPresent(oldEntity::setPlannedStartDate);
		ofNullable(newEntity.getPlannedStopDate()).ifPresent(oldEntity::setPlannedStopDate);
		ofNullable(newEntity.getStatus()).ifPresent(oldEntity::setStatus);
		ofNullable(newEntity.getTitle()).ifPresent(oldEntity::setTitle);

		return oldEntity;
	}

	private static List<AffectedEntity> toAffectedEntities(final List<Affected> affecteds) {
		if (isNull(affecteds)) {
			return null;
		}

		return affecteds.stream()
			.filter(Objects::nonNull)
			.distinct() // Remove duplicates
			.map(DisturbanceMapper::toAffectedEntity)
			.toList();
	}

	private static AffectedEntity toAffectedEntity(final Affected affected) {
		final var affectedEntity = new AffectedEntity();
		affectedEntity.setFacilityId(affected.getFacilityId());
		affectedEntity.setCoordinates(affected.getCoordinates());
		affectedEntity.setPartyId(affected.getPartyId());
		affectedEntity.setReference(affected.getReference());

		return affectedEntity;
	}

	private static List<Affected> toAffecteds(final List<AffectedEntity> affectedEntityList) {
		if (isNull(affectedEntityList)) {
			return null;
		}

		return affectedEntityList.stream()
			.filter(Objects::nonNull)
			.map(DisturbanceMapper::toAffected)
			.toList();
	}

	private static Affected toAffected(final AffectedEntity affectedEntity) {
		return Affected.create()
			.withFacilityId(affectedEntity.getFacilityId())
			.withCoordinates(affectedEntity.getCoordinates())
			.withPartyId(affectedEntity.getPartyId())
			.withReference(affectedEntity.getReference());
	}

	public static List<Disturbance> toDisturbances(final List<DisturbanceEntity> disturbanceEntities) {
		return disturbanceEntities.stream()
			.filter(Objects::nonNull)
			.map(DisturbanceMapper::toDisturbance)
			.toList();
	}
}
