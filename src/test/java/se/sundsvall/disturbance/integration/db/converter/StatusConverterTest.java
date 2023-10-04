package se.sundsvall.disturbance.integration.db.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.disturbance.api.model.Status;

class StatusConverterTest {

	private final StatusConverter statusConverter = new StatusConverter();

	@Test
	void testConvertToDatabaseColumn() {
		final var communication = statusConverter.convertToDatabaseColumn(Status.OPEN);
		assertThat(communication).isEqualTo(Status.OPEN.toString());
	}

	@Test
	void testConvertToDatabaseColumn_whenNullValue_shouldReturnNull() {
		final var communication = statusConverter.convertToDatabaseColumn(null);
		assertThat(communication).isNull();
	}

	@Test
	void testConvertToEntityAttribute() {
		final var communication = statusConverter.convertToEntityAttribute(Status.OPEN.toString());
		assertThat(communication).isEqualByComparingTo(Status.OPEN);
	}

	@Test
	void testConvertToEntityAttribute_whenMissingValue_should() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> statusConverter.convertToEntityAttribute("noMatch"))
			.withMessage("Invalid status: Couldn't match: noMatch, to a Status");
	}
}
