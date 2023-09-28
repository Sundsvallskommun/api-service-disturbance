package se.sundsvall.disturbance.integration.db.converter;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;

import se.sundsvall.disturbance.api.model.Category;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryConverter implements AttributeConverter<Category, String> {

	private static final Logger LOG = LoggerFactory.getLogger(CategoryConverter.class);

	@Override
	public String convertToDatabaseColumn(Category attribute) {
		if(attribute == null) {
			return null;
		} else {
			return attribute.name();
		}
	}

	@Override
	public Category convertToEntityAttribute(String dbData) {
		try {
			return Category.valueOf(dbData);
		} catch (IllegalArgumentException e) {
			throw Problem.builder()
					.withTitle("Invalid category")
					.withStatus(INTERNAL_SERVER_ERROR)
					.withDetail("Couldn't match: " + dbData + ", to a Category")
					.build();
		}
	}
}
