package se.sundsvall.disturbance.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

class SubscriptionEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now(systemDefault()).plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(SubscriptionEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SubscriptionEntity.create()).hasAllNullFieldsOrPropertiesExcept("id");
		assertThat(new SubscriptionEntity()).hasAllNullFieldsOrPropertiesExcept("id");
	}

	@Test
	void testBuilders() {

		final var id = 1L;
		final var optOutSettings = new ArrayList<OptOutSettingsEntity>();
		final var created = now(systemDefault());
		final var updated = now(systemDefault()).plusDays(2);
		final var partyId = randomUUID().toString();
		final var municipalityId = "municipalityId";
		final var bean = SubscriptionEntity.create()
			.withCreated(created)
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withOptOutSettings(optOutSettings)
			.withPartyId(partyId)
			.withUpdated(updated);

		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getOptOutSettings()).isEqualTo(optOutSettings);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testIdHasCorrectAnnotationsAndValues() {
		final var id = FieldUtils.getDeclaredField(SubscriptionEntity.class, "id", true);
		assertThat(id.getAnnotations()).hasSize(3);

		final var idDeclaredAnnotation = id.getDeclaredAnnotation(Id.class);
		assertThat(idDeclaredAnnotation).isNotNull();

		final var generatedValue = id.getDeclaredAnnotation(GeneratedValue.class);
		assertThat(generatedValue.strategy()).isEqualTo(GenerationType.IDENTITY);

		final var column = id.getDeclaredAnnotation(Column.class);
		assertThat(column.name()).isEqualTo("id");
	}

	@Test
	void testPartyIdHasCorrectAnnotationsAndValues() {
		final var partyId = FieldUtils.getDeclaredField(SubscriptionEntity.class, "partyId", true);
		assertThat(partyId.getAnnotations()).hasSize(1);

		final var column = partyId.getDeclaredAnnotation(Column.class);
		assertThat(column.name()).isEqualTo("party_id");
	}

	@Test
	void testOptOutsHasCorrectAnnotationsAndValues() {
		final var optOutSettings = FieldUtils.getDeclaredField(SubscriptionEntity.class, "optOutSettings", true);
		assertThat(optOutSettings.getAnnotations()).hasSize(2);

		final var oneToMany = optOutSettings.getDeclaredAnnotation(OneToMany.class);
		assertThat(oneToMany.cascade()).containsExactly(CascadeType.ALL);
		assertThat(oneToMany.orphanRemoval()).isTrue();
	}
}
