package se.sundsvall.disturbance.integration.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static se.sundsvall.disturbance.integration.messaging.mapper.MessagingMapper.ISSUE_TYPE;

import generated.se.sundsvall.messaging.MessageSender;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.disturbance.api.model.Category;

class MessagingMapperTest {

	@Test
	void toMessage() {

		final var email = MessagingMapper.toEmail("senderEmailName", "senderEmailAddress");
		final var sms = MessagingMapper.toSms("smsName");
		final var filterCategory = Category.ELECTRICITY;
		final var filterFacilityId = "facilityId";
		final var filterIssueType = ISSUE_TYPE;

		final var sender = new MessageSender()
			.email(email)
			.sms(sms);
		final var partyId = UUID.randomUUID().toString();
		final var subject = "subject";
		final var messageText = "message";

		final var filters = MessagingMapper.toFilters(filterCategory, filterFacilityId);
		final var party = MessagingMapper.toParty(partyId);

		final var message = MessagingMapper.toMessage(filters, sender, party, subject, messageText);

		assertThat(message).isNotNull();
		assertThat(message.getSender()).isNotNull();
		assertThat(message.getSender().getEmail().getAddress()).isEqualTo(sender.getEmail().getAddress());
		assertThat(message.getSender().getEmail().getName()).isEqualTo(sender.getEmail().getName());
		assertThat(message.getSender().getSms().getName()).isEqualTo(sender.getSms().getName());
		assertThat(message.getParty().getPartyId()).isEqualTo(party.getPartyId());
		assertThat(message.getSubject()).isEqualTo(subject);
		assertThat(message.getMessage()).isEqualTo(messageText);
		assertThat(message.getFilters())
			.containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
				entry(Filter.TYPE.toString(), List.of(filterIssueType)),
				entry(Filter.FACILITY_ID.toString(), List.of(filterFacilityId)),
				entry(Filter.CATEGORY.toString(), List.of(filterCategory.toString()))));
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
	void toFilters() {

		final var filters = MessagingMapper.toFilters(Category.WATER, "facilityId");

		assertThat(filters)
			.containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
				entry(Filter.TYPE.toString(), List.of(ISSUE_TYPE)),
				entry(Filter.FACILITY_ID.toString(), List.of("facilityId")),
				entry(Filter.CATEGORY.toString(), List.of(Category.WATER.toString()))));
	}
}
