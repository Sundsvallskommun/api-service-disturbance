package se.sundsvall.disturbance.integration.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
