package se.sundsvall.disturbance.integration.messaging.mapper;

import static java.lang.String.valueOf;
import static java.util.Objects.isNull;

import java.util.List;

import generated.se.sundsvall.businessrules.IssueType;
import generated.se.sundsvall.messaging.Email;
import generated.se.sundsvall.messaging.Header;
import generated.se.sundsvall.messaging.Header.NameEnum;
import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.Party;
import generated.se.sundsvall.messaging.Sender;
import generated.se.sundsvall.messaging.Sms;
import se.sundsvall.disturbance.api.model.Category;

public class MessagingMapper {

	private MessagingMapper() {}

	/**
	 * Create a Message object from provided parameters.
	 * 
	 * @param sender
	 * @param party       (object with personId or organizationId)
	 * @param subject
	 * @param messageText
	 * @return A Message object.
	 */
	public static Message toMessage(List<Header> headers, Sender sender, Party party, String subject, String messageText) {
		return new Message()
			.headers(headers)
			.sender(sender)
			.party(party)
			.message(messageText)
			.subject(subject);
	}

	public static List<Header> toHeaders(Category category, String facilityId) {
		return List.of(
			new Header().name(NameEnum.TYPE).values(List.of(valueOf(IssueType.DISTURBANCE))),
			new Header().name(NameEnum.FACILITY_ID).values(List.of(facilityId)),
			new Header().name(NameEnum.CATEGORY).values(List.of(valueOf(toBusinessRulesCategory(category)))));
	}

	public static Email toEmail(String senderEmailName, String senderEmailAddress) {
		return new Email()
			.name(senderEmailName)
			.address(senderEmailAddress);
	}

	public static Sms toSms(String senderSmsName) {
		return new Sms()
			.name(senderSmsName);
	}

	public static Party toParty(String partyId) {
		return new Party()
			.partyId(partyId);
	}

	public static generated.se.sundsvall.businessrules.Category toBusinessRulesCategory(Category category) {
		if (isNull(category)) {
			return null;
		}

		return switch (category) {
			case COMMUNICATION -> generated.se.sundsvall.businessrules.Category.COMMUNICATION;
			case DISTRICT_COOLING -> generated.se.sundsvall.businessrules.Category.DISTRICT_COOLING;
			case DISTRICT_HEATING -> generated.se.sundsvall.businessrules.Category.DISTRICT_HEATING;
			case ELECTRICITY -> generated.se.sundsvall.businessrules.Category.ELECTRICITY;
			case ELECTRICITY_TRADE -> generated.se.sundsvall.businessrules.Category.ELECTRICITY_TRADE;
			case WASTE_MANAGEMENT -> generated.se.sundsvall.businessrules.Category.WASTE_MANAGEMENT;
			case WATER -> generated.se.sundsvall.businessrules.Category.WATER;
		};
	}
}
