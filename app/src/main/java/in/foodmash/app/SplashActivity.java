package in.foodmash.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class SplashActivity extends Activity {

    Intent intent;
    JsonObjectRequest checkConnectionRequest;
    JsonObjectRequest logoutRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        checkConnectionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/check_connection", JsonProvider.getAnonymousRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(isKeepMeLoggedInSet() && isLoggedIn()) {
                    intent = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if(!isKeepMeLoggedInSet() && isLoggedIn()) {
                    logoutRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/sessions/destroy", JsonProvider.getStandardRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            SharedPreferences sharedPreferences = getSharedPreferences("session",0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("logged_in",false);
                            editor.remove("user_token");
                            editor.remove("session_token");
                            editor.remove("android_token");
                            editor.commit();
                            intent = new Intent(SplashActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.timeoutErrorAlert(SplashActivity.this, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    makeRequest(logoutRequest);
                                }
                            });
                            else Alerts.unknownErrorAlert(SplashActivity.this);
                            System.out.println("Response Error: " + error);
                        }
                    });
                    Swift.getInstance(SplashActivity.this).addToRequestQueue(logoutRequest);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SplashActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makeRequest(checkConnectionRequest);
                    }
                }); else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SplashActivity.this);
                else Alerts.commonErrorAlert(SplashActivity.this,
                        "Secure connection could not be made",
                        "App cannot continue since connection cannot be securely established. App will exit now",
                        "Okay",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                System.out.println("Response Error: " + error);
            }
        });
        makeRequest(checkConnectionRequest);

    }

    private void makeRequest(JsonObjectRequest jsonObjectRequest) {
        Swift.getInstance(SplashActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private boolean isKeepMeLoggedInSet() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences",0);
        return sharedPreferences.getBoolean("keep_me_logged_in",false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        makeRequest(checkConnectionRequest);
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("session",0);
        return sharedPreferences.getBoolean("logged_in",false);
    }
}
