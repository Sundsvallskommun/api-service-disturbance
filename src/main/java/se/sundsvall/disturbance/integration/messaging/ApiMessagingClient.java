package se.sundsvall.disturbance.integration.messaging;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.disturbance.integration.messaging.configuration.ApiMessagingConfiguration.CLIENT_REGISTRATION_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageStatusResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.disturbance.integration.messaging.configuration.ApiMessagingConfiguration;

@FeignClient(name = CLIENT_REGISTRATION_ID, url = "${integration.messaging.url}", configuration = ApiMessagingConfiguration.class)
@CircuitBreaker(name = CLIENT_REGISTRATION_ID)
public interface ApiMessagingClient {

	/**
	 * Send messages as email or SMS to a list of recipients, denoted by the partyId.
	 * 
	 * @param messageRequest with a list of messages.
	 * @return a MessageStatusResponse
	 */
	@PostMapping(path = "messages/", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageStatusResponse sendMessage(MessageRequest messageRequest);
}
