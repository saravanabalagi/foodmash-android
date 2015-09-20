package in.foodmash.app.commons;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import in.foodmash.app.LoginActivity;
import in.foodmash.app.R;

/**
 * Created by sarav on Sep 20 2015.
 */
public class Actions {

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
        editor.putBoolean("keep_me_logged_in", bool);
        editor.apply();
    }

    public static void logout(final Context context) {
        Swift.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST, context.getString(R.string.api_root_path) + "/sessions/destroy", JsonProvider.getStandardRequestJson(context), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) { performLogout(context); }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { performLogout(context); }
        }));
    }

    private static void performLogout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("session", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged_in", false);
        editor.remove("user_token");
        editor.remove("session_token");
        editor.remove("android_token");
        editor.apply();
        Intent intent = new Intent(context,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
