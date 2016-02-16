package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.utils.EmailUtils;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    @Bind(R.id.call) FloatingActionButton call;
    @Bind(R.id.send_email) TextView sendEmail;
    @Bind(R.id.not_logged_in_layout) LinearLayout notLoggedInLayout;
    @Bind(R.id.connecting_layout) LinearLayout connectingLayout;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.terms_and_conditions) LinearLayout termsAndConditions;
    @Bind(R.id.privacy_policy) LinearLayout privacyPolicy;
    @Bind(R.id.refund_policy) LinearLayout refundPolicy;
    @Bind(R.id.about_us) LinearLayout aboutUs;

    Intent intent;
    JsonObjectRequest contactUsRequest;

    ImageView issueValidate;
    ImageView descriptionValidate;
    ImageView emailValidate;
    ImageView phoneValidate;

    AutoCompleteTextView issue;
    ArrayList<String> issueList;
    EditText description;
    EditText email;
    EditText phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        termsAndConditions.setOnClickListener(this);
        refundPolicy.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);
        aboutUs.setOnClickListener(this);

        issueList = new ArrayList<>();
        issueList.add("Not Delivered");
        issueList.add("Package Tampered or Damaged");
        issueList.add("Delayed Delivery");
        issueList.add("Payment Issues");
        issueList.add("Food not Hot");
        issueList.add("Issues in App");
        issueList.add("Bug Report");
        issueList.add("Feedback");

        call.setOnClickListener(this);
        sendEmail.setOnClickListener(this);
        if(Info.isLoggedIn(ContactUsActivity.this)) notLoggedInLayout.setVisibility(View.GONE);

        issueValidate = (ImageView) findViewById(R.id.issue_validate);
        descriptionValidate = (ImageView) findViewById(R.id.description_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phoneValidate = (ImageView) findViewById(R.id.phone_validate);

        description = (EditText) findViewById(R.id.description); description.addTextChangedListener(this);
        email = (EditText) findViewById(R.id.email); email.addTextChangedListener(this);
        phone = (EditText) findViewById(R.id.phone); phone.addTextChangedListener(this);
        issue = (AutoCompleteTextView) findViewById(R.id.issue); issue.addTextChangedListener(this);
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,issueList);
        issue.setAdapter(issueAdapter);
        issue.setThreshold(2);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.terms_and_conditions: goToLegaleseActivity(LegaleseActivity.Legalese.TERMS_AND_CONDITIONS); ;break;
            case R.id.refund_policy: goToLegaleseActivity(LegaleseActivity.Legalese.REFUND_POLICY); ;break;
            case R.id.privacy_policy: goToLegaleseActivity(LegaleseActivity.Legalese.PRIVACY_POLICY); ;break;
            case R.id.about_us: goToLegaleseActivity(LegaleseActivity.Legalese.ABOUT_US); ;break;
            case R.id.call: Intent callIntent = new Intent(Intent.ACTION_CALL); callIntent.setData(Uri.parse("tel:+918056249612")); try { startActivity(callIntent); } catch (SecurityException e) { e.printStackTrace(); } ; break;
            case R.id.send_email: if(isEverythingValid()) makeRequest(); else Alerts.validityAlert(ContactUsActivity.this); break;
        }
    }

    private void goToLegaleseActivity(LegaleseActivity.Legalese legalese) {
        Intent intent = new Intent(this, LegaleseActivity.class);
        intent.putExtra("Type", legalese);
        startActivity(intent);
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson = JsonProvider.getStandardRequestJson(ContactUsActivity.this);
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("issue",issue.getText().toString().trim());
            hashMap.put("description",description.getText().toString().trim());
            JSONObject dataJson = new JSONObject(hashMap);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeRequest() {
        contactUsRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/contact_us", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        finish();
                    } else {
                        Animations.fadeOut(connectingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        Alerts.requestUnauthorisedAlert(ContactUsActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOut(connectingLayout,500);
                Animations.fadeIn(mainLayout,500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(connectingLayout,500);
                        Animations.fadeOut(mainLayout, 500);
                        Swift.getInstance(ContactUsActivity.this).addToRequestQueue(contactUsRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(ContactUsActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.timeoutErrorAlert(ContactUsActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(ContactUsActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(connectingLayout,500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(ContactUsActivity.this).addToRequestQueue(contactUsRequest);
    }

    private boolean isEverythingValid() {
        return (Info.isLoggedIn(ContactUsActivity.this) || (EmailUtils.isValidEmailAddress(email.getText().toString()) && NumberUtils.isInteger(phone.getText().toString()) && phone.getText().toString().length() == 10)) &&
                issue.getText().length()>=2 &&
                description.getText().length()>=2;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==issue.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(issueValidate, 500); else Animations.fadeOut(issueValidate,500); }
        else if(s==description.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(descriptionValidate, 500); else Animations.fadeOut(descriptionValidate,500); }
        else if(s==email.getEditableText()) { if(!EmailUtils.isValidEmailAddress(s.toString())) Animations.fadeInOnlyIfInvisible(emailValidate, 500); else Animations.fadeOut(emailValidate,500); }
        else if(s==phone.getEditableText()) { if(!(NumberUtils.isInteger(s.toString()) && s.length()==10)) Animations.fadeInOnlyIfInvisible(phoneValidate, 500); else Animations.fadeOut(phoneValidate,500); }
    }

}
