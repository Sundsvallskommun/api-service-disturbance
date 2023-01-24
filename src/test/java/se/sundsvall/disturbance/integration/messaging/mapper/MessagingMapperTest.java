package se.sundsvall.disturbance.integration.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import generated.se.sundsvall.businessrules.IssueType;
import generated.se.sundsvall.messaging.Header;
import generated.se.sundsvall.messaging.Header.NameEnum;
import generated.se.sundsvall.messaging.Sender;
import se.sundsvall.disturbance.api.model.Category;

class MessagingMapperTest {

	@Test
	void toMessage() {

		final var email = MessagingMapper.toEmail("senderEmailName", "senderEmailAddress");
		final var sms = MessagingMapper.toSms("smsName");
		final var headerCategory = Category.ELECTRICITY;
		final var headerFacilityId = "facilityId";
		final var headerIssueType = IssueType.DISTURBANCE;

		final var sender = new Sender()
			.email(email)
			.sms(sms);
		final var partyId = "partyId";
		final var subject = "subject";
		final var messageText = "message";

		final var headers = MessagingMapper.toHeaders(headerCategory, headerFacilityId);
		final var party = MessagingMapper.toParty(partyId);

		final var message = MessagingMapper.toMessage(headers, sender, party, subject, messageText);

		assertThat(message).isNotNull();
		assertThat(message.getSender()).isNotNull();
		assertThat(message.getSender().getEmail().getAddress()).isEqualTo(sender.getEmail().getAddress());
		assertThat(message.getSender().getEmail().getName()).isEqualTo(sender.getEmail().getName());
		assertThat(message.getSender().getSms().getName()).isEqualTo(sender.getSms().getName());
		assertThat(message.getParty().getPartyId()).isEqualTo(party.getPartyId());
		assertThat(message.getSubject()).isEqualTo(subject);
		assertThat(message.getMessage()).isEqualTo(messageText);
		assertThat(message.getHeaders())
			.extracting(Header::getName, Header::getValues)
			.containsExactly(
				tuple(NameEnum.TYPE, List.of(headerIssueType.toString())),
				tuple(NameEnum.FACILITY_ID, List.of(headerFacilityId)),
				tuple(NameEnum.CATEGORY, List.of(headerCategory.toString())));
	}

	@Test
	void toSms() {

		final var name = "name";

		final var sms = MessagingMapper.toSms(name);

		assertThat(sms).isNotNull();
		assertThat(sms.getName()).isEqualTo(name);
	}

	@Test
	void toEmail() {

		final var name = "name";
		final var address = "address";

		final var email = MessagingMapper.toEmail(name, address);

		assertThat(email).isNotNull();
		assertThat(email.getAddress()).isEqualTo(address);
		assertThat(email.getName()).isEqualTo(name);
	}

	@Test
	void toHeaders() {

		final var headers = MessagingMapper.toHeaders(Category.WATER, "facilityId");

		assertThat(headers)
			.extracting(Header::getName, Header::getValues)
			.containsExactly(
				tuple(NameEnum.TYPE, List.of(IssueType.DISTURBANCE.toString())),
				tuple(NameEnum.FACILITY_ID, List.of("facilityId")),
				tuple(NameEnum.CATEGORY, List.of(Category.WATER.toString())));
	}

	@ParameterizedTest
	@EnumSource(Category.class)
	void toBusinessRulesCategory(Category category) {
		assertThat(MessagingMapper.toBusinessRulesCategory(category))
			.isInstanceOf(generated.se.sundsvall.businessrules.Category.class)
			.asString().isEqualTo(category.toString());
	}

	@Test
	void toBusinessRulesCategoryWhenInputIsNull() {
		assertThat(MessagingMapper.toBusinessRulesCategory(null)).isNull();
	}
}
