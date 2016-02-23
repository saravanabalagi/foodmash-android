package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class SplashActivity extends AppCompatActivity {
    private JsonObjectRequest checkConnectionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkConnectionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/versions", JsonProvider.getAnonymousRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        int newVersion = Integer.parseInt(response.getJSONObject("data").getString("id"));
                        int currentVersion = BuildConfig.VERSION_CODE;
                        if(currentVersion != newVersion) { startActivity(new Intent(SplashActivity.this, UpdateAppActivity.class)); finish(); }
                        else startActivity(new Intent(SplashActivity.this, SelectLocationActivity.class));
                    } else Alerts.requestUnauthorisedAlert(SplashActivity.this);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Alerts.commonErrorAlert(
                        SplashActivity.this,
                        "No Internet",
                        "Sometimes Internet gets sleepy and takes a nap. Turn it on and we'll give it another go.",
                        "Exit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        },false);
                System.out.println("Response Error: " + error);
            }
        });
        makeRequest(checkConnectionRequest);
    }

    private void makeRequest(JsonObjectRequest jsonObjectRequest) { Swift.getInstance(SplashActivity.this).addToRequestQueue(jsonObjectRequest, 500, 10, 2f); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) { makeRequest(checkConnectionRequest); }


}
