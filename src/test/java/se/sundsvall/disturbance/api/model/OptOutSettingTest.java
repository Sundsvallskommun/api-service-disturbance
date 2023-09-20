package se.sundsvall.disturbance.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class OptOutSettingTest {

	@Test
	void testBean() {
		assertThat(OptOutSetting.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var category = Category.ELECTRICITY;
		final var values = List.of(OptOutValue.create());

		final var bean = OptOutSetting.create()
			.withCategory(category)
			.withValues(values);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCategory()).isEqualTo(category);
		assertThat(bean.getValues()).isEqualTo(values);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(OptOutValue.create()).hasAllNullFieldsOrProperties();
		assertThat(new OptOutValue()).hasAllNullFieldsOrProperties();
	}
}
