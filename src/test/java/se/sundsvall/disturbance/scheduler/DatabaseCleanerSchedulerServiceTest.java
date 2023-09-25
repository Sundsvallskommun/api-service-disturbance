package se.sundsvall.disturbance.scheduler;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanerSchedulerServiceTest {

	@Mock
	private DatabaseCleanerSchedulerProperties properties;

	@Mock
	private DisturbanceRepository disturbanceRepository;

	@Captor
	private ArgumentCaptor<OffsetDateTime> expiryDateCaptor;

	@InjectMocks
	private DatabaseCleanerSchedulerService databaseCleanerSchedulerService;

	@Test
	void execute() {

		// Arrange
		final var deleteDisturbancesOlderThanMonths = 12;
		when(properties.deleteDisturbancesOlderThanMonths()).thenReturn(deleteDisturbancesOlderThanMonths);

		// Act
		databaseCleanerSchedulerService.execute();

		// Assert
		verify(disturbanceRepository).deleteByCreatedBeforeAndStatusIn(expiryDateCaptor.capture(), eq(CLOSED.toString()));

		final var expiryDate = expiryDateCaptor.getValue();
		assertThat(expiryDate).isCloseTo(now(systemDefault()).minusMonths(deleteDisturbancesOlderThanMonths), within(2, SECONDS));
	}
}
