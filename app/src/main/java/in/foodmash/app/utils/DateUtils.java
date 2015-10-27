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
            Date utcDate  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(railsUtcDateFormat);
            DateFormat istDate = new SimpleDateFormat("MMM dd hh:mm aa", Locale.getDefault());
            istDate.setTimeZone(TimeZone.getTimeZone("IST"));
            return istDate.format(utcDate);
        } catch (ParseException e) { e.printStackTrace(); return null; }
    }

}
