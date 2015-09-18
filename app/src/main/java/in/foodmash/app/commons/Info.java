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

    public static void cacheEmailAndPhone(Context context, String email, String phone) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email",email);
        editor.putString("phone",phone);
        editor.apply();
    }

    public static void setKeepMeLoggedIn(Context context,boolean bool) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferences", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("keep_me_logged_in",bool);
        editor.apply();
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
