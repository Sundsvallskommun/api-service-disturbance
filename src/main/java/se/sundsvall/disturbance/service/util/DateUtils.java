package se.sundsvall.disturbance.service.util;

import static java.util.Objects.nonNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

	private static final DateTimeFormatter MESSAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private DateUtils() {}

	/**
	 * Formats a date on the format "yyyy-MM-dd HH:mm" to be used in the Message to send. If the date is null, "N/A" is
	 * returned.
	 *
	 * @param date
	 * @return A formatted date on format "yyyy-MM-dd HH:mm".
	 */
	public static String toMessageDateFormat(final OffsetDateTime date) {
		return nonNull(date) ? MESSAGE_DATE_FORMAT.format(OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())) : "N/A";
	}
}
