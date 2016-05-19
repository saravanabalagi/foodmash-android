package in.foodmash.app.utils;

/**
 * Created by Zeke on Sep 04 2015.
 */
public class NumberUtils {

    public static String getCurrencyFormat(float f) { return String.format("%.2f",f); }
    public static String getCurrencyFormat(double d) { return String.format("%.2f",d); }
    public static String getCurrencyFormatWithoutDecimals(float d) { return String.valueOf((int)d); }
    public static String getCurrencyFormatWithoutDecimals(double d) { return String.valueOf((int)d); }
    public static boolean isInteger(String string) {
        if (string == null) { return false; }
        if (string.length() == 0) { return false; }
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if ( c < '0' || c > '9') { return false; }
        }
        return true;
    }

}
