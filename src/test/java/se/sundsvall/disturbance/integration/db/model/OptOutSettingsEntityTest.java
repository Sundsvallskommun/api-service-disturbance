package se.sundsvall.disturbance.integration.db.model;


import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.core.AllOf.allOf;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.api.model.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

class OptOutSettingsEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(OptOutSettingsEntity.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSetters(),
				hasValidBeanHashCode(),
				hasValidBeanEquals(),
				hasValidBeanToString()));
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(new OptOutSettingsEntity()).hasAllNullFieldsOrPropertiesExcept("id");
	}

	@Test
	void testBuilders() {
		var opOutSettingsEntity = new OptOutSettingsEntity()
				.withSubscriptionEntity(new SubscriptionEntity())
				.withOptOuts(new HashMap<>())
				.withCategory(Category.ELECTRICITY);

		Assertions.assertThat(opOutSettingsEntity.getSubscriptionEntity()).isNotNull();
		Assertions.assertThat(opOutSettingsEntity.getOptOuts()).isNotNull();
		Assertions.assertThat(opOutSettingsEntity.getCategory()).isEqualByComparingTo(Category.ELECTRICITY);
	}

	@Test
	void testIdHasCorrectAnnotationsAndValues() {
		var idField = FieldUtils.getDeclaredField(SubscriptionEntity.class, "id", true);
		Assertions.assertThat(idField.getAnnotations()).hasSize(3);

		var id = idField.getDeclaredAnnotation(Id.class);
		Assertions.assertThat(id).isNotNull();

		var generatedValue = idField.getDeclaredAnnotation(GeneratedValue.class);
		Assertions.assertThat(generatedValue.strategy()).isEqualTo(GenerationType.IDENTITY);

		var column = idField.getDeclaredAnnotation(Column.class);
		Assertions.assertThat(column.name()).isEqualTo("id");
	}

	@Test
	void testCategoryHasCorrectAnnotationsAndValues() {
		var category = FieldUtils.getDeclaredField(OptOutSettingsEntity.class, "category", true);
		Assertions.assertThat(category.getAnnotations()).hasSize(2);

		var column = category.getDeclaredAnnotation(Column.class);
		Assertions.assertThat(column.name()).isEqualTo("category");

		var enumerated = category.getDeclaredAnnotation(Enumerated.class);
		Assertions.assertThat(enumerated.value()).isEqualTo(EnumType.STRING);
	}

	@Test
	void testSubscriptionEntityHasCorrectAnnotationsAndValues() {
		var subscriptionEntity = FieldUtils.getDeclaredField(OptOutSettingsEntity.class, "subscriptionEntity", true);
		Assertions.assertThat(subscriptionEntity.getAnnotations()).hasSize(2);

		var manyToOne = subscriptionEntity.getDeclaredAnnotation(ManyToOne.class);
		Assertions.assertThat(manyToOne.cascade()).containsExactly(CascadeType.PERSIST);

		var joinColumn = subscriptionEntity.getDeclaredAnnotation(JoinColumn.class);
		Assertions.assertThat(joinColumn.name()).isEqualTo("subscription_id");
		Assertions.assertThat(joinColumn.foreignKey().name()).isEqualTo("fk_opt_out_settings_subscription_id");
	}

	@Test
	void testOptOutsHasCorrectAnnotationsAndValues() {
		var optOuts = FieldUtils.getDeclaredField(OptOutSettingsEntity.class, "optOuts", true);
		Assertions.assertThat(optOuts.getAnnotations()).hasSize(2);

		var collectionTable = optOuts.getDeclaredAnnotation(CollectionTable.class);
		Assertions.assertThat(collectionTable.name()).isEqualTo("opt_out_settings_key_values");

		var joinColumns = Arrays.stream(collectionTable.joinColumns()).toList();
		Assertions.assertThat(joinColumns).hasSize(1);
		Assertions.assertThat(joinColumns.get(0).name()).isEqualTo("opt_out_settings_id");
		Assertions.assertThat(joinColumns.get(0).referencedColumnName()).isEqualTo("id");
		Assertions.assertThat(joinColumns.get(0).foreignKey().name()).isEqualTo("fk_opt_out_settings_opt_out_values");
	}
}
