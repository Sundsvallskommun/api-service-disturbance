package se.sundsvall.disturbance.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

	@Test
	void toMessageDateFormat() {

		final var offsetDateTime = LocalDateTime.of(2022, 11, 11, 12, 13, 14).atZone(ZoneId.systemDefault()).toOffsetDateTime();

		assertThat(DateUtils.toMessageDateFormat(offsetDateTime)).isEqualTo("2022-11-11 12:13");
		assertThat(DateUtils.toMessageDateFormat(null)).isEqualTo("N/A");
	}
}
