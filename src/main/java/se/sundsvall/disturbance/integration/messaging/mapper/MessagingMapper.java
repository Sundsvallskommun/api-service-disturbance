package se.sundsvall.disturbance.integration.messaging.mapper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import generated.se.sundsvall.messaging.Email;
import generated.se.sundsvall.messaging.Header;
import generated.se.sundsvall.messaging.Header.NameEnum;
import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageParty;
import generated.se.sundsvall.messaging.MessageSender;
import generated.se.sundsvall.messaging.Sms;
import se.sundsvall.disturbance.api.model.Category;

public class MessagingMapper {

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
	public static Message toMessage(final List<Header> headers, final MessageSender sender, final MessageParty party, final String subject, final String messageText) {
		return new Message()
			.headers(headers)
			.sender(sender)
			.party(party)
			.message(messageText)
			.subject(subject);
	}

	public static List<Header> toHeaders(final Category category, final String facilityId) {
		return List.of(
			new Header().name(NameEnum.TYPE).values(List.of(ISSUE_TYPE)),
			new Header().name(NameEnum.FACILITY_ID).values(List.of(facilityId)),
			new Header().name(NameEnum.CATEGORY).values(List.of(Objects.toString(category, null))));
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
			.partyId(UUID.fromString(partyId));
	}
}
