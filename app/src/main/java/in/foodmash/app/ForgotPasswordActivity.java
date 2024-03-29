package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

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

/**
 * Created by Zeke on Aug 08 2015.
 */
public class ForgotPasswordActivity extends FoodmashActivity implements View.OnClickListener, TextWatcher {

    @Bind(R.id.forgot) FloatingActionButton forgot;
    @Bind(R.id.phone_layout) LinearLayout phoneLayout;
    @Bind(R.id.email_layout) LinearLayout emailLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private EditText phone;
    private EditText email;
    private ImageView phoneValidate;
    private ImageView emailValidate;

    private RadioGroup otpMethodRadioGroup;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Forgot","Password");
        forgot.setOnClickListener(this);

        phoneValidate = (ImageView) findViewById(R.id.contact_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phone = (EditText) findViewById(R.id.contact_no); phone.setText(Info.getPhone(ForgotPasswordActivity.this)); phone.addTextChangedListener(this);
        email = (EditText) findViewById(R.id.email_or_phone); email.setText(Info.getEmail(ForgotPasswordActivity.this)); email.addTextChangedListener(this);

        otpMethodRadioGroup = (RadioGroup) findViewById(R.id.otp_method_radio_group); otpMethodRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.phone_radio: Animations.fadeOutAndFadeIn(emailLayout, phoneLayout, 500); break;
                    case R.id.email_radio: Animations.fadeOutAndFadeIn(phoneLayout,emailLayout,500); break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot: if(isEverythingValid()) makeForgotRequest(); else Snackbar.make(mainLayout,"One or more data you entered is invalid",Snackbar.LENGTH_LONG).show(); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = JsonProvider.getAnonymousRequestJson(ForgotPasswordActivity.this);
        try {
            JSONObject userJson = new JSONObject();
            if(otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.phone_radio) userJson.put("mobile_no",phone.getText().toString().trim());
            else if(otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.email_radio) userJson.put("email",email.getText().toString().trim());
            JSONObject dataJson = new JSONObject();
            dataJson.put("user",userJson);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    public void makeForgotRequest() {
        JsonObjectRequest forgotRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations/forgotPassword", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        Log.i("Security",response.getString("otp"));
                        intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordOtpActivity.class);
                        intent.putExtra("type",(otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.phone_radio)?"phone":"email");
                        intent.putExtra("value",(otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.phone_radio)?phone.getText().toString().trim():email.getText().toString().trim());
                        startActivity(intent);
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(ForgotPasswordActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeForgotRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(ForgotPasswordActivity.this).addToRequestQueue(forgotRequest);
    }

    private boolean isEverythingValid() {
         return (otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.phone_radio)
                 ?phone.getText().toString().trim().length()==10
                 : EmailUtils.isValidEmailAddress(email.getText().toString().trim());
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==phone.getEditableText()) { if(s.toString().trim().length()!=10) Animations.fadeInOnlyIfInvisible(phoneValidate, 500); else Animations.fadeOut(phoneValidate,500); }
        else if(s==email.getEditableText()) { if(!EmailUtils.isValidEmailAddress(s.toString())) Animations.fadeInOnlyIfInvisible(emailValidate, 500); else Animations.fadeOut(emailValidate,500); }
    }
}
