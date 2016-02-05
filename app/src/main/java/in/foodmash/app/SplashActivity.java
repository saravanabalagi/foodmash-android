package in.foodmash.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class SplashActivity extends Activity {

    private Intent intent;
    private JsonObjectRequest checkConnectionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkConnectionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/check_connection", JsonProvider.getAnonymousRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(Info.isKeepMeLoggedInSet(SplashActivity.this) && Info.isLoggedIn(SplashActivity.this)) {
                    intent = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if(!Info.isKeepMeLoggedInSet(SplashActivity.this) && Info.isLoggedIn(SplashActivity.this)) {
                    Actions.logout(SplashActivity.this);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("Trying again..!");
                        makeRequest(checkConnectionRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SplashActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SplashActivity.this, onClickTryAgain);
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
        Swift.getInstance(SplashActivity.this).addToRequestQueue(jsonObjectRequest, 500, 10, 2f);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        makeRequest(checkConnectionRequest);
    }


}
