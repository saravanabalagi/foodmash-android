package in.foodmash.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.utils.WordUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Zeke on Feb 16, 2016.
 */
public class LegaleseActivity extends AppCompatActivity {

    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.html_content) TextView htmlContent;
    @Bind(R.id.title) TextView title;
    @Bind(R.id.toolbar) Toolbar toolbar;

    public enum Legalese { TERMS_AND_CONDITIONS, PRIVACY_POLICY, REFUND_POLICY, ABOUT_US};
    private JsonObjectRequest legaleseRequest;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legalese);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        final boolean signedIn = getIntent().getBooleanExtra("SignedIn", true);
        final Legalese legalese = (Legalese) getIntent().getSerializableExtra("Type");
        legaleseRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/legalese/" + getStringFromLegalese(legalese), (signedIn)?JsonProvider.getStandardRequestJson(this):JsonProvider.getAnonymousRequestJson(this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Animations.fadeOut(fragmentContainer,100);
                        String data = response.getString("data");
                        htmlContent.setText(Html.fromHtml(data));
                        title.setText(WordUtils.titleize(getStringFromLegalese(legalese)));
                    } else {
                        Alerts.requestUnauthorisedAlert(LegaleseActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyFailureFragment()).commit();
                getSupportFragmentManager().executePendingTransactions();
                ((VolleyFailureFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                        .setJsonObjectRequest(legaleseRequest);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Animations.fadeIn(fragmentContainer, 300);
        Swift.getInstance(this).addToRequestQueue(legaleseRequest);
    }

    private String getStringFromLegalese(Legalese legalese) {
        switch (legalese) {
            case TERMS_AND_CONDITIONS: return "termsAndConditions";
            case PRIVACY_POLICY: return "privacyPolicy";
            case REFUND_POLICY: return "refundPolicy";
            case ABOUT_US: return "aboutUs";
            default: return null;
        }
    }
}
