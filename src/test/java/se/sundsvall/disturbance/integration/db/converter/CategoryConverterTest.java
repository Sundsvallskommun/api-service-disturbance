package se.sundsvall.disturbance.integration.db.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import se.sundsvall.disturbance.api.model.Category;

class CategoryConverterTest {

	private final CategoryConverter categoryConverter = new CategoryConverter();

	@ParameterizedTest
	@EnumSource(value = Category.class, names = {"COMMUNICATION", "DISTRICT_COOLING", "DISTRICT_HEATING", "ELECTRICITY", "ELECTRICITY_TRADE", "WASTE_MANAGEMENT", "WATER"})
	void testConvertToDatabaseColumn(Category category) {
		final var communication = categoryConverter.convertToDatabaseColumn(category);
		assertThat(communication).isNotNull();
	}

	@Test
	void testConvertToDatabaseColumn_whenNullValue_shouldReturnNull() {
		final var communication = categoryConverter.convertToDatabaseColumn(null);
		assertThat(communication).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {"COMMUNICATION", "DISTRICT_COOLING", "DISTRICT_HEATING", "ELECTRICITY", "ELECTRICITY_TRADE", "WASTE_MANAGEMENT", "WATER"})
	void testConvertToEntityAttribute(String category) {
		final var communication = categoryConverter.convertToEntityAttribute(category);
		assertThat(communication).isNotNull();
	}

	@Test
	void testConvertToEntityAttribute_whenMissingValue_should() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> categoryConverter.convertToEntityAttribute("noMatch"))
			.withMessage("No enum constant se.sundsvall.disturbance.api.model.Category.noMatch");
	}
}
