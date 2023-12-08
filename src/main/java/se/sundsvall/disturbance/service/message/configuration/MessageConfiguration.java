package se.sundsvall.disturbance.service.message.configuration;

import static org.apache.commons.lang3.StringUtils.lowerCase;

import org.springframework.stereotype.Component;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping.CategoryConfig;

@Component
public class MessageConfiguration {

	private final MessageConfigurationMapping messageConfigurationMapping;

	public MessageConfiguration(MessageConfigurationMapping messageConfigurationMapping) {
		this.messageConfigurationMapping = messageConfigurationMapping;
	}

	public CategoryConfig getCategoryConfig(Category category) {
		return messageConfigurationMapping.getTemplate().get(lowerCase(category.toString()));
	}
}
