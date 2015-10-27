package in.foodmash.app.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by sarav on Oct 27 2015.
 */
public class DateUtils {

    public static String railsDateToLocalTime(String railsUtcDateFormat) {
        try {
            DateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date  = utcDateFormat.parse(railsUtcDateFormat);
            DateFormat istDateFormat = new SimpleDateFormat("MMM dd hh:mm aa", Locale.getDefault());
            istDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            return istDateFormat.format(date);
        } catch (ParseException e) { e.printStackTrace(); return null; }
    }

}
