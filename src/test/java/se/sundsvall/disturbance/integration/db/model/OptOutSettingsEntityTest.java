package se.sundsvall.disturbance.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import se.sundsvall.disturbance.api.model.Category;

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
		assertThat(new OptOutSettingsEntity()).hasAllNullFieldsOrPropertiesExcept("id");
	}

	@Test
	void testBuilders() {
		final var optOuts = new HashMap<String, String>();
		final var category = ELECTRICITY;
		final var opOutSettingsEntity = new OptOutSettingsEntity()
			.withOptOuts(optOuts)
			.withCategory(category);

		assertThat(opOutSettingsEntity.getOptOuts()).isEqualTo(optOuts);
		assertThat(opOutSettingsEntity.getCategory()).isEqualByComparingTo(Category.ELECTRICITY);
	}

	@Test
	void testIdHasCorrectAnnotationsAndValues() {
		final var idField = FieldUtils.getDeclaredField(SubscriptionEntity.class, "id", true);
		assertThat(idField.getAnnotations()).hasSize(3);

		final var id = idField.getDeclaredAnnotation(Id.class);
		assertThat(id).isNotNull();

		final var generatedValue = idField.getDeclaredAnnotation(GeneratedValue.class);
		assertThat(generatedValue.strategy()).isEqualTo(GenerationType.IDENTITY);

		final var column = idField.getDeclaredAnnotation(Column.class);
		assertThat(column.name()).isEqualTo("id");
	}

	@Test
	void testCategoryHasCorrectAnnotationsAndValues() {
		final var category = FieldUtils.getDeclaredField(OptOutSettingsEntity.class, "category", true);
		assertThat(category.getAnnotations()).hasSize(1);

		final var column = category.getDeclaredAnnotation(Column.class);
		assertThat(column.name()).isEqualTo("category");
	}

	@Test
	void testOptOutsHasCorrectAnnotationsAndValues() {
		final var optOuts = FieldUtils.getDeclaredField(OptOutSettingsEntity.class, "optOuts", true);
		assertThat(optOuts.getAnnotations()).hasSize(2);

		final var collectionTable = optOuts.getDeclaredAnnotation(CollectionTable.class);
		assertThat(collectionTable.name()).isEqualTo("opt_out_settings_key_values");

		final var joinColumns = Arrays.stream(collectionTable.joinColumns()).toList();
		assertThat(joinColumns).hasSize(1);
		assertThat(joinColumns.get(0).name()).isEqualTo("opt_out_settings_id");
		assertThat(joinColumns.get(0).referencedColumnName()).isEqualTo("id");
		assertThat(joinColumns.get(0).foreignKey().name()).isEqualTo("fk_opt_out_settings_key_values_opt_out_settings_id");
	}
}
