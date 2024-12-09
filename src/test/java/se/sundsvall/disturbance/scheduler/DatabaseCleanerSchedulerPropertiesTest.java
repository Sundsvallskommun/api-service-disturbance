package se.sundsvall.disturbance.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.disturbance.Application;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DatabaseCleanerSchedulerPropertiesTest {

	@Autowired
	private DatabaseCleanerSchedulerProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.deleteDisturbancesOlderThanMonths()).isEqualTo(66);
		assertThat(properties.cron()).isEqualTo("-");
	}
}
