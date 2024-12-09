package se.sundsvall.disturbance.service.message.configuration;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "message")
public class MessageConfigurationMapping {

	private Map<String, CategoryConfig> template;

	public Map<String, CategoryConfig> getTemplate() {
		return template;
	}

	public void setTemplate(Map<String, CategoryConfig> template) {
		this.template = template;
	}

	public static class CategoryConfig {

		private boolean active;
		private String subjectClose;
		private String subjectNew;
		private String subjectUpdate;
		private String messageClose;
		private String messageNew;
		private String messageUpdate;
		private String senderEmailName;
		private String senderEmailAddress;
		private String senderSmsName;

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public String getSubjectClose() {
			return subjectClose;
		}

		public void setSubjectClose(String subjectClose) {
			this.subjectClose = subjectClose;
		}

		public String getSubjectNew() {
			return subjectNew;
		}

		public void setSubjectNew(String subjectNew) {
			this.subjectNew = subjectNew;
		}

		public String getSubjectUpdate() {
			return subjectUpdate;
		}

		public void setSubjectUpdate(String subjectUpdate) {
			this.subjectUpdate = subjectUpdate;
		}

		public String getMessageClose() {
			return messageClose;
		}

		public void setMessageClose(String messageClose) {
			this.messageClose = messageClose;
		}

		public String getMessageNew() {
			return messageNew;
		}

		public void setMessageNew(String messageNew) {
			this.messageNew = messageNew;
		}

		public String getMessageUpdate() {
			return messageUpdate;
		}

		public void setMessageUpdate(String messageUpdate) {
			this.messageUpdate = messageUpdate;
		}

		public String getSenderEmailName() {
			return senderEmailName;
		}

		public void setSenderEmailName(String senderEmailName) {
			this.senderEmailName = senderEmailName;
		}

		public String getSenderEmailAddress() {
			return senderEmailAddress;
		}

		public void setSenderEmailAddress(String senderEmailAddress) {
			this.senderEmailAddress = senderEmailAddress;
		}

		public String getSenderSmsName() {
			return senderSmsName;
		}

		public void setSenderSmsName(String senderSmsName) {
			this.senderSmsName = senderSmsName;
		}
	}
}
