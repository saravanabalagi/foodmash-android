package in.foodmash.app.commons;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sarav on Sep 18 2015.
 */
public class Info {
    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("session",0);
        return sharedPreferences.getBoolean("logged_in",false);
    }

    public static boolean isKeepMeLoggedInSet(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferences", 0);
        return sharedPreferences.getBoolean("keep_me_logged_in",false);
    }

    public static String getEmail(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        return sharedPreferences.getString("email", null);
    }

    public static String getPhone(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        return sharedPreferences.getString("phone",null);
    }
}
