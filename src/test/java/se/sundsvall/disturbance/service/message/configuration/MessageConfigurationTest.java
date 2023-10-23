package se.sundsvall.disturbance.service.message.configuration;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageConfigurationTest {

	@Autowired
	private MessageConfiguration messageConfiguration;

	@ParameterizedTest
	@EnumSource(Category.class) // Passing all categories
	void configExistsForAllCategories(Category category) {

		final var categoryConfig = messageConfiguration.getCategoryConfig(category);

		assertThat(categoryConfig)
			.describedAs("Missing one or more properties for config group: 'message.template.%s'", lowerCase(category.toString()))
			.isNotNull()
			.hasNoNullFieldsOrProperties();
	}
}
