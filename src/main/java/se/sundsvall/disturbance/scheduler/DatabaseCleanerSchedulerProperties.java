package se.sundsvall.disturbance.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("scheduler.dbcleaner")
public record DatabaseCleanerSchedulerProperties(int deleteDisturbancesOlderThanMonths, String cron) {
}
