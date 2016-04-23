package in.foodmash.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.utils.EmailUtils;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class ContactUsActivity extends FoodmashActivity implements View.OnClickListener, TextWatcher {

    private static final int MY_PERMISSION_CALL_PHONE = 17;
    @Bind(R.id.call) FloatingActionButton call;
    @Bind(R.id.send_email) TextView sendEmail;
    @Bind(R.id.not_logged_in_layout) LinearLayout notLoggedInLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.terms_and_conditions) LinearLayout termsAndConditions;
    @Bind(R.id.privacy_policy) LinearLayout privacyPolicy;
    @Bind(R.id.refund_policy) LinearLayout refundPolicy;
    @Bind(R.id.about_us) LinearLayout aboutUs;

    Intent intent;

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
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Contact","us");

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
        issueList.add("Feature Request");
        issueList.add("Enhancement");
        issueList.add("Feedback");

        call.setOnClickListener(this);
        sendEmail.setOnClickListener(this);
        if(Info.isLoggedIn(ContactUsActivity.this)) notLoggedInLayout.setVisibility(View.GONE);

        issueValidate = (ImageView) findViewById(R.id.issue_validate);
        descriptionValidate = (ImageView) findViewById(R.id.description_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phoneValidate = (ImageView) findViewById(R.id.contact_validate);

        description = (EditText) findViewById(R.id.description); description.addTextChangedListener(this);
        email = (EditText) findViewById(R.id.email); email.addTextChangedListener(this);
        phone = (EditText) findViewById(R.id.contact_no); phone.addTextChangedListener(this);
        issue = (AutoCompleteTextView) findViewById(R.id.issue); issue.addTextChangedListener(this);
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, issueList);
        issue.setAdapter(issueAdapter);
        issue.setThreshold(2);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.terms_and_conditions: goToLegaleseActivity(LegaleseActivity.Legalese.TERMS_AND_CONDITIONS); break;
            case R.id.refund_policy: goToLegaleseActivity(LegaleseActivity.Legalese.REFUND_POLICY); break;
            case R.id.privacy_policy: goToLegaleseActivity(LegaleseActivity.Legalese.PRIVACY_POLICY); break;
            case R.id.about_us: goToLegaleseActivity(LegaleseActivity.Legalese.ABOUT_US); break;
            case R.id.send_email: if(isEverythingValid()) sendEmail(); else Snackbar.make(mainLayout,"One or more data you entered is invalid",Snackbar.LENGTH_LONG).show(); break;
            case R.id.call:
                if ( ContextCompat.checkSelfPermission( this, Manifest.permission.CALL_PHONE ) != PackageManager.PERMISSION_GRANTED )
                    ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.CALL_PHONE  },
                            MY_PERMISSION_CALL_PHONE );
                else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:+918056249612"));
                    startActivity(callIntent);
                } break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSION_CALL_PHONE) {
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:+918056249612"));
                try { startActivity(callIntent); }
                catch (Exception e) { Actions.handleIgnorableException(this,e); }
            }
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

    public void makeContactUsRequest() {
        JsonObjectRequest contactUsRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_contact_us), getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) finish();
                    else Snackbar.make(mainLayout, response.getString("error"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeContactUsRequest")).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
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

    private void sendEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"bugs@foodmash.in"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "App Error | Issue");
            intent.putExtra(Intent.EXTRA_TEXT   , getMakeErrorRequestJson().toString(4));
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (JSONException e) { Snackbar.make(mainLayout, "Json Exception occurred!", Snackbar.LENGTH_LONG).show(); }
        catch (Exception e) { Snackbar.make(mainLayout, "There are no email clients installed.", Snackbar.LENGTH_LONG).show(); }
    }

    private JSONObject getMakeErrorRequestJson() {
        JSONObject requestJson = (Info.isLoggedIn(this)) ? JsonProvider.getStandardRequestJson(this) : JsonProvider.getAnonymousRequestJson(this);
        try {
            HashMap<String, String> dataHashMap = new HashMap<>();

            Calendar calendar = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US);
            String timeNow = dateFormat.format(calendar.getTime());

            dataHashMap.put("issue",issue.getText().toString());
            dataHashMap.put("description",description.getText().toString());
            dataHashMap.put("time",timeNow);

            JSONObject dataJson = new JSONObject(dataHashMap);
            HashMap<String, String> hostHashMap = new HashMap<>();
            hostHashMap.put("release", Build.VERSION.RELEASE);
            hostHashMap.put("sdkVersion", String.valueOf(Build.VERSION.SDK_INT));
            hostHashMap.put("manufacturer", Build.MANUFACTURER);
            hostHashMap.put("model", Build.MODEL);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            String orientation = "";
            String sizeCategory = "";

            switch (getResources().getConfiguration().orientation) {
                case Configuration.ORIENTATION_LANDSCAPE: orientation = "landscape"; break;
                case Configuration.ORIENTATION_PORTRAIT: orientation = "portrait"; break;
                case Configuration.ORIENTATION_UNDEFINED: orientation = "undefined"; break;
            }

            switch ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK)) {
                case Configuration.SCREENLAYOUT_SIZE_XLARGE: sizeCategory = "xlarge"; break;
                case Configuration.SCREENLAYOUT_SIZE_LARGE: sizeCategory = "large"; break;
                case Configuration.SCREENLAYOUT_SIZE_NORMAL: sizeCategory = "normal"; break;
                case Configuration.SCREENLAYOUT_SIZE_SMALL: sizeCategory = "small"; break;
                case Configuration.SCREENLAYOUT_SIZE_UNDEFINED: sizeCategory = "undefined"; break;
            }

            hostHashMap.put("orientation", orientation);
            hostHashMap.put("sizeCategory", sizeCategory);
            hostHashMap.put("height", String.valueOf(height));
            hostHashMap.put("width", String.valueOf(width));
            JSONObject hostJson = new JSONObject(hostHashMap);
            dataJson.put("host", hostJson);

            if(Info.isLoggedIn(this)) {
                JSONObject userJson = new JSONObject();
                userJson.put("name", Info.getName(this));
                userJson.put("email", Info.getEmail(this));
                userJson.put("phone", Info.getPhone(this));
                userJson.put("area", Info.getAreaName(this));
                userJson.put("city", Info.getCityName(this));
                dataJson.put("user", userJson);
            }

            requestJson.put("data", dataJson);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        return requestJson;
    }

}
