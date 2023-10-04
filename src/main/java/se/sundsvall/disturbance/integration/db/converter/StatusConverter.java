package se.sundsvall.disturbance.integration.db.converter;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import org.zalando.problem.Problem;

import se.sundsvall.disturbance.api.model.Status;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {

	@Override
	public String convertToDatabaseColumn(Status attribute) {
		if(attribute == null) {
			return null;
		} else {
			return attribute.toString();
		}
	}

	@Override
	public Status convertToEntityAttribute(String dbData) {
		try {
			return Status.valueOf(dbData);
		} catch (IllegalArgumentException e) {
			throw Problem.builder()
					.withTitle("Invalid status")
					.withStatus(INTERNAL_SERVER_ERROR)
					.withDetail("Couldn't match: " + dbData + ", to a Status")
					.build();
		}
	}
}
