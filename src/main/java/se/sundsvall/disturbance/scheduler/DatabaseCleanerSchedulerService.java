package se.sundsvall.disturbance.scheduler;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private DatabaseCleanerSchedulerProperties properties;

	@Autowired
	private DisturbanceRepository disturbanceRepository;

	@Scheduled(cron = "${scheduler.dbcleaner.cron:-}")
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
