package se.sundsvall.disturbance.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class SubscriptionCreateRequestTest {

	@Test
	void testBean() {
		assertThat(SubscriptionCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var optOutSettings = List.of(OptOutSetting.create());
		final var partyId = UUID.randomUUID().toString();

		final var bean = SubscriptionCreateRequest.create()
			.withOptOutSettings(optOutSettings)
			.withPartyId(partyId);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getOptOutSettings()).isEqualTo(optOutSettings);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SubscriptionCreateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new SubscriptionCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
