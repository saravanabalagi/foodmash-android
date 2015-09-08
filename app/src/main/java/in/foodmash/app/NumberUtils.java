package in.foodmash.app;

/**
 * Created by sarav on Sep 04 2015.
 */
public class NumberUtils {
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
