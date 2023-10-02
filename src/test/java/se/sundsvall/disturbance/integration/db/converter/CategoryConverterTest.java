package se.sundsvall.disturbance.integration.db.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.disturbance.api.model.Category;

class CategoryConverterTest {

	private final CategoryConverter categoryConverter = new CategoryConverter();

	@Test
	void testConvertToDatabaseColumn() {
		final var communication = categoryConverter.convertToDatabaseColumn(Category.COMMUNICATION);
		assertThat(communication).isEqualTo(Category.COMMUNICATION.toString());
	}

	@Test
	void testConvertToDatabaseColumn_whenNullValue_shouldReturnNull() {
		final var communication = categoryConverter.convertToDatabaseColumn(null);
		assertThat(communication).isNull();
	}

	@Test
	void testConvertToEntityAttribute() {
		final var communication = categoryConverter.convertToEntityAttribute(Category.COMMUNICATION.toString());
		assertThat(communication).isEqualTo(Category.COMMUNICATION);
	}

	@Test
	void testConvertToEntityAttribute_whenMissingValue_should() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> categoryConverter.convertToEntityAttribute("noMatch"))
			.withMessage("Invalid category: Couldn't match: noMatch, to a Category");
	}
}
