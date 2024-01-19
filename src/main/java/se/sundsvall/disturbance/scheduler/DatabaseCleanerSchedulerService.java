package se.sundsvall.disturbance.scheduler;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;

import java.time.OffsetDateTime;
import java.util.List;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

@Component
public class DatabaseCleanerSchedulerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerSchedulerService.class);

	private static final List<Status> STATUSES_ELIGIBLE_FOR_REMOVAL = List.of(CLOSED);

	private static final String LOG_CLEANING_STARTED = "Beginning removal of obsolete entities in the database.";
	private static final String LOG_CLEANING_DELETE_RANGE = "Removing all disturbances older than '{}' and with status matching '{}'.";
	private static final String LOG_CLEANING_ENDED = "Cleaning of obsolete entities in database has ended.";

	private final DatabaseCleanerSchedulerProperties properties;
	private final DisturbanceRepository disturbanceRepository;

	public DatabaseCleanerSchedulerService(DatabaseCleanerSchedulerProperties properties, DisturbanceRepository disturbanceRepository) {
		this.properties = properties;
		this.disturbanceRepository = disturbanceRepository;
	}

	@Scheduled(cron = "${scheduler.dbcleaner.cron:-}")
	@SchedulerLock(name = "dbcleaner", lockAtMostFor = "${scheduler.dbcleaner.shedlock-lock-at-most-for}")
	public void execute() {

		LOGGER.info(LOG_CLEANING_STARTED);

		final var statusesEligibleForRemoval = STATUSES_ELIGIBLE_FOR_REMOVAL.toArray(Status[]::new);
		final var expiryDate = calculateExpiryDate();

		LOGGER.info(LOG_CLEANING_DELETE_RANGE, expiryDate, statusesEligibleForRemoval);

		disturbanceRepository.deleteByCreatedBeforeAndStatusIn(expiryDate, statusesEligibleForRemoval);

		LOGGER.info(LOG_CLEANING_ENDED);
	}

	private OffsetDateTime calculateExpiryDate() {
		return now(systemDefault()).minusMonths(properties.deleteDisturbancesOlderThanMonths());
	}
}
