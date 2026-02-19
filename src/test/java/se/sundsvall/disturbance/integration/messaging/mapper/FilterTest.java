package se.sundsvall.disturbance.integration.messaging.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilterTest {

	@Test
	void testFilterToStringIsTranslatedToCamelCase() {
		assertThat(Filter.CATEGORY).hasToString("category");
		assertThat(Filter.FACILITY_ID).hasToString("facilityId");
		assertThat(Filter.TYPE).hasToString("type");
	}

	@Test
	void testFilterHasCorrectValue() {
		assertThat(Filter.values()).containsExactly(Filter.CATEGORY, Filter.FACILITY_ID, Filter.TYPE);
	}
}
