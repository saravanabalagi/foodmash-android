package in.foodmash.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.utils.WordUtils;
import in.foodmash.app.volley.Swift;
import in.foodmash.app.volley.VolleyFailureFragment;
import in.foodmash.app.volley.VolleyProgressFragment;

/**
 * Created by Zeke on Feb 16, 2016.
 */
public class LegaleseActivity extends FoodmashActivity {

    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.html_content) TextView htmlContent;
    @Bind(R.id.title) TextView title;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private boolean signedIn;
    private Legalese legalese;
    public enum Legalese { TERMS_AND_CONDITIONS, PRIVACY_POLICY, REFUND_POLICY, ABOUT_US}


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legalese);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Foodmash","legalism");

        signedIn = getIntent().getBooleanExtra("SignedIn", true);
        legalese = (Legalese) getIntent().getSerializableExtra("Type");
        makeLegaleseRequest();
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

    public void makeLegaleseRequest() {
        JsonObjectRequest legaleseRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_get_legalese) + getStringFromLegalese(legalese), (signedIn)?JsonProvider.getStandardRequestJson(this):JsonProvider.getAnonymousRequestJson(this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if (response.getBoolean("success")) {
                        Animations.fadeOut(fragmentContainer,100);
                        String data = response.getString("data");
                        htmlContent.setText(Html.fromHtml(data));
                        title.setText(WordUtils.titleize(getStringFromLegalese(legalese)));
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { e.printStackTrace(); Actions.handleIgnorableException(LegaleseActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeLegaleseRequest", null)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
                Log.e("Json Request Failed", error.toString());
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(this).addToRequestQueue(legaleseRequest);
    }
}
