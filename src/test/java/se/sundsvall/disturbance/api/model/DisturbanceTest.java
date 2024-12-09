package se.sundsvall.disturbance.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DisturbanceTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now(systemDefault()).plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(Disturbance.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var category = Category.COMMUNICATION;
		final var created = now(systemDefault());
		final var updated = now(systemDefault()).plusHours(5);
		final var description = "some description";
		final var id = "1234567890";
		final var municipalityId = "municipalityId";
		final var status = Status.CLOSED;
		final var title = "Title";
		final var affecteds = List.of(Affected.create());
		final var plannedStartDate = now(systemDefault());
		final var plannedStopDate = now(systemDefault());

		final var bean = Disturbance.create()
			.withCategory(category)
			.withCreated(created)
			.withDescription(description)
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withAffecteds(affecteds)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(plannedStopDate)
			.withStatus(status)
			.withTitle(title)
			.withUpdated(updated);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAffecteds()).isEqualTo(affecteds);
		assertThat(bean.getCategory()).isEqualByComparingTo(category);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(bean.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(bean.getStatus()).isEqualByComparingTo(status);
		assertThat(bean.getTitle()).isEqualTo(title);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Disturbance.create()).hasAllNullFieldsOrProperties();
		assertThat(new Disturbance()).hasAllNullFieldsOrProperties();
	}
}
