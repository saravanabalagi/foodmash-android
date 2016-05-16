package in.foodmash.app.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on Oct 27 2015.
 */
public class DateUtils {

    public static String railsDateStringToReadableTime(String railsUtcDateFormat) {
        try {
            DateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date  = utcDateFormat.parse(railsUtcDateFormat);
            DateFormat istDateFormat = new SimpleDateFormat("MMM dd hh:mm aa", Locale.getDefault());
            istDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            return istDateFormat.format(date);
        } catch (ParseException e) { e.printStackTrace(); return null; }
    }

    public static Date railsDateStringToJavaDate(String railsUtcDateFormat) {
        try {
            DateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return utcDateFormat.parse(railsUtcDateFormat);
        } catch (ParseException e) { e.printStackTrace(); return null; }
    }
    public static Date ddmmyyslashDateStringToJavaDate(String ddmmyyyyslashUtcDateFormat) {
        try {
            DateFormat utcDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return utcDateFormat.parse(ddmmyyyyslashUtcDateFormat);
        } catch (ParseException e) { e.printStackTrace(); return null; }
    }

    public static long howOldInHours(Date date) { return TimeUnit.MILLISECONDS.toHours(new Date().getTime() - date.getTime()); }
    public static long howOldInDays(Date date) { return TimeUnit.MILLISECONDS.toDays(new Date().getTime() - date.getTime()); }
    public static String javaDateToRailsDateString(Date date) {
        DateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcDateFormat.format(date);
    }

}
