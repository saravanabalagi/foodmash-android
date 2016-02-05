package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class SplashActivity extends AppCompatActivity {
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;

    private Intent intent;
    private JsonObjectRequest checkConnectionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

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
                Animations.fadeIn(fragmentContainer,300);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyFailureFragment()).commit();
                getSupportFragmentManager().executePendingTransactions();
                VolleyFailureFragment volleyFailureFragment = (VolleyFailureFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                volleyFailureFragment.setSetDestroyOnRetry(true);
                volleyFailureFragment.setJsonObjectRequest(checkConnectionRequest);
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
