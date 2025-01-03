package se.sundsvall.disturbance.integration.messaging.mapper;

import static java.util.Map.entry;

import generated.se.sundsvall.messaging.Email;
import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageParty;
import generated.se.sundsvall.messaging.MessageSender;
import generated.se.sundsvall.messaging.Sms;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import se.sundsvall.disturbance.api.model.Category;

public final class MessagingMapper {

	public static final String ISSUE_TYPE = "DISTURBANCE";

	private MessagingMapper() {}

	/**
	 * Create a Message object from provided parameters.
	 *
	 * @param  sender      the mesage sender
	 * @param  party       the message party (an object with partyId and/or externalReferences)
	 * @param  subject     the message subject
	 * @param  messageText the message text.
	 * @return             A Message object.
	 */
	public static Message toMessage(final Map<String, List<String>> filters, final MessageSender sender, final MessageParty party, final String subject, final String messageText) {
		return new Message()
			.filters(filters)
			.sender(sender)
			.party(party)
			.message(messageText)
			.subject(subject);
	}

	public static Map<String, List<String>> toFilters(final Category category, final String facilityId) {
		return Map.ofEntries(
			entry(Filter.TYPE.toString(), List.of(ISSUE_TYPE)),
			entry(Filter.FACILITY_ID.toString(), List.of(facilityId)),
			entry(Filter.CATEGORY.toString(), List.of(Objects.toString(category, null))));
	}

	public static Email toEmail(final String senderEmailName, final String senderEmailAddress) {
		return new Email()
			.name(senderEmailName)
			.address(senderEmailAddress);
	}

	public static Sms toSms(final String senderSmsName) {
		return new Sms()
			.name(senderSmsName);
	}

	public static MessageParty toParty(final String partyId) {
		return new MessageParty()
			.partyId(UUID.fromString(partyId))
			// To keep collection instantiated and not suddenly change to null if openAPI decides to change the implementation.
			.externalReferences(List.of());
	}
}
