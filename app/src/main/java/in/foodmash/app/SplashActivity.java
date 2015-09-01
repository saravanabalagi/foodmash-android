package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class SplashActivity extends Activity {

    Intent intent;
    JsonObjectRequest jsonObjectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("android_id", Settings.Secure.getString(SplashActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID));
        JSONObject requestJson = new JSONObject(hashMap);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "check_connection", requestJson, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        if(isKeepMeLoggedInSet() && isLoggedIn()) {
                            intent = new Intent(SplashActivity.this,MainActivity.class);
                            startActivity(intent);
                        } else if(!isKeepMeLoggedInSet() && isLoggedIn()) {
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "sessions/destroy", JsonProvider.getStandartRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if(response.getBoolean("success")) {
                                            SharedPreferences sharedPreferences = getSharedPreferences("session",0);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("logged_in",false);
                                            editor.remove("user_token");
                                            editor.remove("session_token");
                                            editor.remove("android_token");
                                            editor.commit();
                                            intent = new Intent(SplashActivity.this,LoginActivity.class);
                                            startActivity(intent);
                                        } else if(!(response.getBoolean("success"))) {
                                            Alerts.showCommonErrorAlert(SplashActivity.this,"Unable to Logout","We are unable to sign you out. Try again later!","Okay");
                                            System.out.println(response.getString("error"));
                                        }
                                    } catch (JSONException e) { e.printStackTrace(); }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.showInternetConnectionError(SplashActivity.this);
                                    else Alerts.showUnknownError(SplashActivity.this);
                                    System.out.println("Response Error: " + error);
                                }
                            });
                            Swift.getInstance(SplashActivity.this).addToRequestQueue(jsonObjectRequest);
                        } else {
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    } else if(response.getBoolean("success")) {
                        Alerts.showCommonErrorAlert(SplashActivity.this,"Secure connection could not be made","App cannot continue since connection cannot be securely established. App will exit now","Okay");
                        finish();
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) {
                    Alerts.showInternetConnectionError(SplashActivity.this);
                    makeRequest(jsonObjectRequest);
                }
                else Alerts.showUnknownError(SplashActivity.this);
                System.out.println("Response Error: " + error);
                finish();
            }
        });
        makeRequest(jsonObjectRequest);



    }

    private void makeRequest(JsonObjectRequest jsonObjectRequest) {
        Swift.getInstance(SplashActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private boolean isKeepMeLoggedInSet() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences",0);
        return sharedPreferences.getBoolean("keep_me_logged_in",false);
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("session",0);
        return sharedPreferences.getBoolean("logged_in",false);
    }
}
