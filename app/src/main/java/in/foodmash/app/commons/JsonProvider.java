package in.foodmash.app.commons;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by sarav on Sep 01 2015.
 */
public class JsonProvider {
    public static JSONObject getStandartRequestJson(Context context) {
        HashMap<String,String> hashMap = new HashMap<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("session", 0);
        hashMap.put("auth_user_token", sharedPreferences.getString("user_token",null));
        hashMap.put("auth_session_token", sharedPreferences.getString("session_token", null));
        hashMap.put("auth_android_token", sharedPreferences.getString("android_token", null));
        return new JSONObject(hashMap);
    }
}
