package se.sundsvall.disturbance.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

class SubscriptionEntityTest {

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
		Assertions.assertThat(new SubscriptionEntity()).hasAllNullFieldsOrPropertiesExcept("id");
	}

	@Test
	void testBuilders() {
		var subscriptionEntity = new SubscriptionEntity()
				.withOptOuts(new HashSet<>())
				.withPartyId("partyId");

		Assertions.assertThat(subscriptionEntity.getOptOuts()).isNotNull();
		Assertions.assertThat(subscriptionEntity.getPartyId()).isEqualTo("partyId");
	}

	@Test
	void testIdHasCorrectAnnotationsAndValues() {
		var id = FieldUtils.getDeclaredField(SubscriptionEntity.class, "id", true);
		Assertions.assertThat(id.getAnnotations()).hasSize(3);

		var idDeclaredAnnotation = id.getDeclaredAnnotation(Id.class);
		Assertions.assertThat(idDeclaredAnnotation).isNotNull();

		var generatedValue = id.getDeclaredAnnotation(GeneratedValue.class);
		Assertions.assertThat(generatedValue.strategy()).isEqualTo(GenerationType.IDENTITY);

		var column = id.getDeclaredAnnotation(Column.class);
		Assertions.assertThat(column.name()).isEqualTo("id");
	}

	@Test
	void testPartyIdHasCorrectAnnotationsAndValues() {
		var partyId = FieldUtils.getDeclaredField(SubscriptionEntity.class, "partyId", true);
		Assertions.assertThat(partyId.getAnnotations()).hasSize(1);

		var column = partyId.getDeclaredAnnotation(Column.class);
		Assertions.assertThat(column.name()).isEqualTo("party_id");
	}

	@Test
	void testOptOutsHasCorrectAnnotationsAndValues() {
		var optOuts = FieldUtils.getDeclaredField(SubscriptionEntity.class, "optOuts", true);
		Assertions.assertThat(optOuts.getAnnotations()).hasSize(1);

		var oneToMany = optOuts.getDeclaredAnnotation(OneToMany.class);
		Assertions.assertThat(oneToMany.cascade()).containsExactly(CascadeType.ALL);
		Assertions.assertThat(oneToMany.orphanRemoval()).isTrue();
	}
}
