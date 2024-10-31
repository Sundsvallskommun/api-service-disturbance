package se.sundsvall.disturbance.integration.db.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import se.sundsvall.disturbance.api.model.Status;

class StatusConverterTest {

	private final StatusConverter statusConverter = new StatusConverter();

	@ParameterizedTest
	@EnumSource(value = Status.class, names = {
		"OPEN", "CLOSED", "PLANNED"
	})
	void testConvertToDatabaseColumn(Status status) {
		final var communication = statusConverter.convertToDatabaseColumn(status);
		assertThat(communication).isNotNull();
	}

	@Test
	void testConvertToDatabaseColumn_whenNullValue_shouldReturnNull() {
		final var communication = statusConverter.convertToDatabaseColumn(null);
		assertThat(communication).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"OPEN", "CLOSED", "PLANNED"
	})
	void testConvertToEntityAttribute(String status) {
		final var communication = statusConverter.convertToEntityAttribute(status);
		assertThat(communication).isNotNull();
	}

	@Test
	void testConvertToEntityAttribute_whenMissingValue_should() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> statusConverter.convertToEntityAttribute("noMatch"))
			.withMessage("No enum constant se.sundsvall.disturbance.api.model.Status.noMatch");
	}
}
