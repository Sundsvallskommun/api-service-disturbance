package se.sundsvall.disturbance.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AffectedEntityTest {

	@Test
	void testBean() {
		assertThat(AffectedEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("disturbanceEntity"),
			hasValidBeanEqualsExcluding("disturbanceEntity"),
			hasValidBeanToStringExcluding("disturbanceEntity")));
	}

	@Test
	void testBuilderMethods() {

		final var coordinates = "coordinates";
		final var disturbanceEntity = DisturbanceEntity.create();
		final var facilityId = "facilityId";
		final var id = 1L;
		final var partyId = UUID.randomUUID().toString();
		final var reference = "reference";

		final var bean = AffectedEntity.create()
			.withCoordinates(coordinates)
			.withDisturbanceEntity(disturbanceEntity)
			.withFacilityId(facilityId)
			.withId(id)
			.withPartyId(partyId)
			.withReference(reference);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCoordinates()).isEqualTo(coordinates);
		assertThat(bean.getDisturbanceEntity()).isEqualTo(disturbanceEntity);
		assertThat(bean.getFacilityId()).isEqualTo(facilityId);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getReference()).isEqualTo(reference);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AffectedEntity.create()).hasAllNullFieldsOrPropertiesExcept("id");
		assertThat(new AffectedEntity()).hasAllNullFieldsOrPropertiesExcept("id");
	}
}
