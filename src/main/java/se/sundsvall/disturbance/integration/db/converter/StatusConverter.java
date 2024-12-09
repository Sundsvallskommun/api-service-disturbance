package se.sundsvall.disturbance.integration.db.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.sundsvall.disturbance.api.model.Status;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {

	@Override
	public String convertToDatabaseColumn(Status attribute) {
		if (attribute == null) {
			return null;
		} else {
			return attribute.toString();
		}
	}

	@Override
	public Status convertToEntityAttribute(String dbData) {
		return Status.valueOf(dbData);
	}
}
