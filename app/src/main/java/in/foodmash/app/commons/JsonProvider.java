package in.foodmash.app.commons;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Zeke on Sep 01 2015.
 */
public class JsonProvider {
    public static JSONObject getStandardRequestJson(Context context) {
        HashMap<String,String> hashMap = new HashMap<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("session", 0);
        hashMap.put("auth_user_token", sharedPreferences.getString("user_token",null));
        hashMap.put("auth_session_token", sharedPreferences.getString("session_token", null));
        hashMap.put("auth_android_token", sharedPreferences.getString("android_token", null));
        return new JSONObject(hashMap);
    }

    public static JSONObject getAnonymousRequestJson(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("android_id", androidId);
        hashMap.put("android_token",Cryptography.getEncryptedAndroidId(context, "abcdef"));
        return new JSONObject(hashMap);
    }
}
