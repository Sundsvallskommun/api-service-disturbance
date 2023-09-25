package se.sundsvall.disturbance.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SubscriptionTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now(systemDefault()).plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(Subscription.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var created = now(systemDefault());
		final var id = 1L;
		final var optOutSettings = List.of(OptOutSetting.create());
		final var partyId = UUID.randomUUID().toString();
		final var updated = now(systemDefault()).plusHours(5);

		final var bean = Subscription.create()
			.withCreated(created)
			.withId(id)
			.withOptOutSettings(optOutSettings)
			.withPartyId(partyId)
			.withUpdated(updated);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getOptOutSettings()).isEqualTo(optOutSettings);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Subscription.create()).hasAllNullFieldsOrProperties();
		assertThat(new Subscription()).hasAllNullFieldsOrProperties();
	}
}
