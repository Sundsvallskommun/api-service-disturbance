package se.sundsvall.disturbance.service.util;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

class MappingUtilsTest {

	@Test
	void getRemovedAffectedEntitiesWhenElementsRemoved() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var affectedEntity3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3");

		final var affectedEntity4 = AffectedEntity.create()
			.withPartyId("partyId-4")
			.withReference("reference-4");

		final var affectedEntity5 = AffectedEntity.create()
			.withPartyId("partyId-5")
			.withReference("reference-5");

		final var affectedEntity6 = AffectedEntity.create()
			.withPartyId("partyId-6")
			.withReference("reference-6");

		final var disturbanceEntity1 = DisturbanceEntity.create()
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity2, affectedEntity3, affectedEntity4, affectedEntity5, affectedEntity6));

		final var disturbanceEntity2 = DisturbanceEntity.create()
			// The following elements are removed compared to disturbanceEntity1: affectedEntity2, affectedEntity4, affectedEntity5
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity3, affectedEntity6));

		final var result = MappingUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result)
			.isNotNull()
			.hasSize(3)
			.containsExactly(affectedEntity2, affectedEntity4, affectedEntity5);
	}

	@Test
	void getRemovedAffectedEntitiesWhenNoElementsAreRemovedButCaseDiffers() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var affectedEntity3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3");

		// Same as above, but different case.
		final var affectedEntityOtherCase1 = AffectedEntity.create()
			.withPartyId("PartyId-1")
			.withReference("Reference-1");

		final var affectedEntityOtherCase2 = AffectedEntity.create()
			.withPartyId("PARTYID-2")
			.withReference("REFERENCE-2");

		final var affectedEntityOtherCase3 = AffectedEntity.create()
			.withPartyId("partyid-3")
			.withReference("reference-3");

		final var disturbanceEntity1 = DisturbanceEntity.create()
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity2, affectedEntity3));

		final var disturbanceEntity2 = DisturbanceEntity.create()
			// All elements have different case, but is equal otherwise.
			.withAffectedEntities(List.of(affectedEntityOtherCase1, affectedEntityOtherCase2, affectedEntityOtherCase3));

		final var result = MappingUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getRemovedAffectedEntitiesWhenElementsAdded() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var affectedEntity3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3");

		final var affectedEntity4 = AffectedEntity.create()
			.withPartyId("partyId-4")
			.withReference("reference-4");

		final var affectedEntity5 = AffectedEntity.create()
			.withPartyId("partyId-5")
			.withReference("reference-5");

		final var affectedEntity6 = AffectedEntity.create()
			.withPartyId("partyId-6")
			.withReference("reference-6");

		final var disturbanceEntity1 = DisturbanceEntity.create()
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity3, affectedEntity6));

		final var disturbanceEntity2 = DisturbanceEntity.create()
			// The following elements are added compared to disturbanceEntity1: affectedEntity2, affectedEntity4, affectedEntity5
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity2, affectedEntity3, affectedEntity4, affectedEntity5, affectedEntity6));

		final var result = MappingUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getRemovedAffectedEntitiesWhenNewEntitiesIsNull() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var disturbanceEntity1 = DisturbanceEntity.create()
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity2));
		final var disturbanceEntity2 = DisturbanceEntity.create();

		final var result = MappingUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getRemovedAffectedEntitiesWhenNewEntitiesIsEmptyList() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var disturbanceEntity1 = DisturbanceEntity.create().withAffectedEntities(List.of(affectedEntity1, affectedEntity2));
		final var disturbanceEntity2 = DisturbanceEntity.create().withAffectedEntities(emptyList());

		final var result = MappingUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result)
			.isNotNull()
			.hasSize(2)
			.containsExactly(affectedEntity1, affectedEntity2);
	}

	@Test
	void getAddedAffectedEntitiesWhenElementsRemoved() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var affectedEntity3 = AffectedEntity.create()
			.withPartyId("partyId-3")
			.withReference("reference-3");

		final var affectedEntity4 = AffectedEntity.create()
			.withPartyId("partyId-4")
			.withReference("reference-4");

		final var affectedEntity5 = AffectedEntity.create()
			.withPartyId("partyId-5")
			.withReference("reference-5");

		final var affectedEntity6 = AffectedEntity.create()
			.withPartyId("partyId-6")
			.withReference("reference-6");

		final var disturbanceEntity1 = DisturbanceEntity.create()
			.withAffectedEntities(List.of(affectedEntity1, affectedEntity3, affectedEntity6));

		final var disturbanceEntity2 = DisturbanceEntity.create()
			// The following elements are added compared to disturbanceEntity1: affectedEntity2, affectedEntity4, affectedEntity5
			.withAffectedEntities(List.of(affectedEntity2, affectedEntity3, affectedEntity4, affectedEntity5, affectedEntity6));

		final var result = MappingUtils.getAddedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result)
			.isNotNull()
			.hasSize(3)
			.containsExactly(affectedEntity2, affectedEntity4, affectedEntity5);
	}

	@Test
	void getAddedAffectedEntitiesWhenNewEntitiesIsNull() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var disturbanceEntity1 = DisturbanceEntity.create().withAffectedEntities(List.of(affectedEntity1, affectedEntity2));
		final var disturbanceEntity2 = DisturbanceEntity.create();

		final var result = MappingUtils.getAddedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getAddedAffectedEntitiesWhenNewEntitiesIsEmptyList() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var disturbanceEntity1 = DisturbanceEntity.create().withAffectedEntities(List.of(affectedEntity1, affectedEntity2));
		final var disturbanceEntity2 = DisturbanceEntity.create().withAffectedEntities(emptyList());

		final var result = MappingUtils.getAddedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getAddedAffectedEntitiesWhenOldEntitiesIsNull() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var disturbanceEntity1 = DisturbanceEntity.create();
		final var disturbanceEntity2 = DisturbanceEntity.create().withAffectedEntities(List.of(affectedEntity1, affectedEntity2));

		final var result = MappingUtils.getAddedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result)
			.isNotNull()
			.hasSize(2)
			.containsExactly(affectedEntity1, affectedEntity2);
	}

	@Test
	void getAddedAffectedEntitiesWhenOldEntitiesIsEmptyList() {

		final var affectedEntity1 = AffectedEntity.create()
			.withPartyId("partyId-1")
			.withReference("reference-1");

		final var affectedEntity2 = AffectedEntity.create()
			.withPartyId("partyId-2")
			.withReference("reference-2");

		final var disturbanceEntity1 = DisturbanceEntity.create().withAffectedEntities(emptyList());
		final var disturbanceEntity2 = DisturbanceEntity.create().withAffectedEntities(List.of(affectedEntity1, affectedEntity2));

		final var result = MappingUtils.getAddedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result)
			.isNotNull()
			.hasSize(2)
			.containsExactly(affectedEntity1, affectedEntity2);
	}
}
