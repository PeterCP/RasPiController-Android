package dev.petercp.raspicontroller.utils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Date parsing utility using Joda-Time library.
 */
public class DateTimeUtils {

    private static final DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final MutableDateTime EPOCH;
    static {
        EPOCH = new MutableDateTime(0);
        EPOCH.setDate(0);
        EPOCH.setTime(0);
    }

    public static DateTime fromString(String input) {
        return FMT.parseDateTime(input);
    }

    public static String toString(DateTime date) {
        return FMT.print(date);
    }

    public static int toDays(DateTime date) {
        return Days.daysBetween(EPOCH, date.withTimeAtStartOfDay()).getDays();
    }

    public static DateTime fromDays(int days) {
        return EPOCH.toDateTime().plusDays(days);
    }
}