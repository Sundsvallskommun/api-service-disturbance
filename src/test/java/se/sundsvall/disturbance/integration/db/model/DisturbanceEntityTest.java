package se.sundsvall.disturbance.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;

class DisturbanceEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now(systemDefault()).plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(DisturbanceEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var affectedEntities = List.of(AffectedEntity.create());
		final var category = Category.COMMUNICATION.toString();
		final var created = now(systemDefault());
		final var deleted = true;
		final var description = "description";
		final var disturbanceId = "disturbanceId";
		final var id = 1L;
		final var plannedStartDate = now(systemDefault());
		final var plannedStopDate = now(systemDefault());
		final var status = Status.CLOSED.toString();
		final var title = "title";
		final var updated = now(systemDefault());

		final var bean = DisturbanceEntity.create()
			.withAffectedEntities(affectedEntities)
			.withCategory(category)
			.withCreated(created)
			.withDeleted(deleted)
			.withDescription(description)
			.withDisturbanceId(disturbanceId)
			.withId(id)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(plannedStopDate)
			.withStatus(status)
			.withTitle(title)
			.withUpdated(updated);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAffectedEntities()).isEqualTo(affectedEntities);
		assertThat(bean.getCategory()).isEqualTo(category);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getDeleted()).isEqualTo(deleted);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(bean.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getTitle()).isEqualTo(title);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DisturbanceEntity.create()).hasAllNullFieldsOrPropertiesExcept("id", "deleted");
		assertThat(new DisturbanceEntity()).hasAllNullFieldsOrPropertiesExcept("id", "deleted");
	}
}
