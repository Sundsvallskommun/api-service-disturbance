package se.sundsvall.disturbance.integration.db.converter;

import se.sundsvall.disturbance.api.model.Category;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryConverter implements AttributeConverter<Category, String> {

	@Override
	public String convertToDatabaseColumn(Category attribute) {
		if (attribute == null) {
			return null;
		} else {
			return attribute.toString();
		}
	}

	@Override
	public Category convertToEntityAttribute(String dbData) {
		return Category.valueOf(dbData);
	}
}
