package se.sundsvall.disturbance.integration.messaging;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.disturbance.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageResult;
import se.sundsvall.disturbance.integration.messaging.configuration.MessagingConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.url}", configuration = MessagingConfiguration.class)
public interface MessagingClient {

	/**
	 * Send messages as email or SMS to a list of recipients, denoted by the partyId.
	 *
	 * @param  messageRequest with a list of messages.
	 * @return                a MessageResult
	 */
	@PostMapping(path = "/messages?async=true", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendMessage(MessageRequest messageRequest);
}
