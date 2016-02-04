package in.foodmash.app.commons;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import in.foodmash.app.custom.Combo;

/**
 * Created by Zeke on Sep 18 2015.
 */
public class Info {

    private static List<Combo> combos;

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

    public static String getFirstName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        return sharedPreferences.getString("firstName",null);
    }

    public static List<Combo> getCombos() { return combos; }
    public static void setCombos(List<Combo> newCombos) { combos=newCombos; }
}
