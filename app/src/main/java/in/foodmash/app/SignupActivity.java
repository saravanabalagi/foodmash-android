package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Cryptography;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.utils.EmailUtils;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class SignupActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    @Bind(R.id.create) FloatingActionButton create;
    @Bind(R.id.connecting_layout) LinearLayout connectingLayout;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.terms_and_conditions) LinearLayout termsAndConditions;
    @Bind(R.id.privacy_policy) LinearLayout privacyPolicy;
    @Bind(R.id.refund_policy) LinearLayout refundPolicy;

    private Intent intent;
    private boolean fromCart;
    private LegaleseActivity.Legalese legalese;

    private EditText name;
    private EditText email;
    private EditText phone;
    private EditText password;
    private EditText passwordConfirmation;

    private ImageView nameValidate;
    private ImageView emailValidate;
    private ImageView phoneValidate;
    private ImageView passwordValidate;
    private ImageView passwordConfirmationValidate;

    private ProgressBar emailProgressBar;
    private ProgressBar phoneProgressBar;

    private boolean isEmailAvailable = false;
    private boolean isPhoneAvailable = false;
    private boolean isEmailValidationInProgress = false;
    private boolean isPhoneValidationInProgress = false;

    private JsonObjectRequest checkEmailRequest;
    private JsonObjectRequest checkPhoneRequest;
    private JsonObjectRequest registerRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        fromCart = getIntent().getBooleanExtra("from_cart", false);
        create.setOnClickListener(this);

        nameValidate = (ImageView) findViewById(R.id.name_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phoneValidate = (ImageView) findViewById(R.id.contact_validate);
        passwordValidate = (ImageView) findViewById(R.id.password_validate);
        passwordConfirmationValidate = (ImageView) findViewById(R.id.password_confirmation_validate);

        name = (EditText) findViewById(R.id.name); name.addTextChangedListener(this);
        email = (EditText) findViewById(R.id.email); email.addTextChangedListener(this);
        phone = (EditText) findViewById(R.id.contact_no); phone.addTextChangedListener(this);
        password = (EditText) findViewById(R.id.password); password.addTextChangedListener(this);
        passwordConfirmation = (EditText) findViewById(R.id.password_confirmation); passwordConfirmation.addTextChangedListener(this);

        emailProgressBar = (ProgressBar) findViewById(R.id.email_loader);
        phoneProgressBar = (ProgressBar) findViewById(R.id.phone_loader);

        termsAndConditions.setOnClickListener(this);
        refundPolicy.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.terms_and_conditions: goToLegaleseActivity(LegaleseActivity.Legalese.TERMS_AND_CONDITIONS); ;break;
            case R.id.refund_policy: goToLegaleseActivity(LegaleseActivity.Legalese.REFUND_POLICY); ;break;
            case R.id.privacy_policy: goToLegaleseActivity(LegaleseActivity.Legalese.PRIVACY_POLICY); ;break;
            case R.id.create:
                if(isEverythingValid()) makeJsonRequest();
                else Alerts.validityAlert(SignupActivity.this);
                break;
        }
    }

    private void goToLegaleseActivity(LegaleseActivity.Legalese legalese) {
        Intent intent = new Intent(this, LegaleseActivity.class);
        intent.putExtra("SignedIn", false);
        intent.putExtra("Type", legalese);
        startActivity(intent);
    }

    private JSONObject getRequestJson() {
        JSONObject jsonObject = JsonProvider.getAnonymousRequestJson(SignupActivity.this);
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("name",name.getText().toString());
        hashMap.put("email",email.getText().toString());
        hashMap.put("password",password.getText().toString());
        hashMap.put("password_confirmation",passwordConfirmation.getText().toString());
        hashMap.put("mobile_no",phone.getText().toString());
        JSONObject userJson = new JSONObject(hashMap);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("user",userJson);
            jsonObject.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return jsonObject;
    }

    private void makeJsonRequest() {
        registerRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations",getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                intent = new Intent(SignupActivity.this, LoginActivity.class);
                if(fromCart) intent = new Intent(SignupActivity.this, CartActivity.class);
                try {
                    if (response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        JSONObject userJson = dataJson.getJSONObject("user");
                        Actions.cacheUserDetails(SignupActivity.this, userJson.getString("name"), userJson.getString("email"), userJson.getString("mobile_no"));
                        String userToken = dataJson.getString("user_token");
                        String sessionToken = dataJson.getString("session_token");
                        SharedPreferences sharedPreferences = getSharedPreferences("session", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("logged_in",true);
                        editor.putString("user_token", userToken);
                        editor.putString("session_token", sessionToken);
                        editor.putString("android_token", Cryptography.getEncryptedAndroidId(SignupActivity.this, sessionToken));
                        editor.apply();
                        startActivity(intent);
                        finish();
                    } else {
                        Animations.fadeOut(connectingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        Alerts.commonErrorAlert(SignupActivity.this,"Registration Invalid", "We are unable to sign you up. Please try again!","Okay");
                        Log.e("Success False",response.getString("error"));
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
                        Swift.getInstance(SignupActivity.this).addToRequestQueue(registerRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SignupActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SignupActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(SignupActivity.this);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Animations.fadeIn(connectingLayout,500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(SignupActivity.this).addToRequestQueue(registerRequest,15000,3,1.5f);
    }

    private void setCancelOnImageView(ImageView imageView) { imageView.setColorFilter(ContextCompat.getColor(this, R.color.accent)); imageView.setImageResource(R.drawable.svg_close); }
    private void setOkayOnImageView(ImageView imageView) { imageView.setColorFilter(ContextCompat.getColor(this, R.color.okay_green)); imageView.setImageResource(R.drawable.svg_tick); }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override public void afterTextChanged(Editable s) {
        if(s==name.getEditableText()) {
            if(name.getText().toString().trim().length()<2) setCancelOnImageView(nameValidate);
            else setOkayOnImageView(nameValidate);
            if(nameValidate.getVisibility()!=View.VISIBLE) Animations.fadeIn(nameValidate, 500);
        }
        if(s==email.getEditableText()) {
            if(EmailUtils.isValidEmailAddress(s.toString())) {
                JSONObject requestJson = new JSONObject();
                try{
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("email",s.toString());
                    requestJson.put("data",dataJson);
                } catch (JSONException e) { e.printStackTrace(); }
                checkEmailRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations/checkEmail", requestJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            isEmailValidationInProgress = false;
                            if(response.getBoolean("success")) {
                                isEmailAvailable = true;
                                setOkayOnImageView(emailValidate);
                                Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
                            } else {
                                isEmailAvailable = false;
                                setCancelOnImageView(emailValidate);
                                Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
                            }
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Swift.getInstance(SignupActivity.this).addToRequestQueue(checkEmailRequest);
                            }
                        };
                        if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SignupActivity.this, onClickTryAgain);
                        else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SignupActivity.this, onClickTryAgain);
                        else Alerts.unknownErrorAlert(SignupActivity.this);
                        Log.e("Json Request","Email response error: "+error);
                        isEmailValidationInProgress = false;
                        setCancelOnImageView(emailValidate);
                        Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
                    }
                });
                isEmailValidationInProgress = true;
                Animations.fadeOut(emailValidate,500);
                Animations.fadeIn(emailProgressBar,500);
                Swift.getInstance(SignupActivity.this).addToRequestQueue(checkEmailRequest);
            }
        }
        if(s==phone.getEditableText()) {
            if(s.length()==10) {
                JSONObject requestJson = new JSONObject();
                try {
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("mobile_no", phone.getText().toString().trim());
                    requestJson.put("data",dataJson);
                } catch (JSONException e) { e.printStackTrace(); }
                checkPhoneRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations/checkMobileNo", requestJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                isPhoneAvailable = true;
                                setOkayOnImageView(phoneValidate);
                                Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
                            } else {
                                isPhoneAvailable = false;
                                setCancelOnImageView(phoneValidate);
                                Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
                            }
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Swift.getInstance(SignupActivity.this).addToRequestQueue(checkPhoneRequest);
                            }
                        };
                        if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SignupActivity.this, onClickTryAgain);
                        else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SignupActivity.this, onClickTryAgain);
                        else Alerts.unknownErrorAlert(SignupActivity.this);
                        Log.e("Json Request","Phone response error: "+error);
                        isPhoneValidationInProgress = false;
                        setCancelOnImageView(phoneValidate);
                        Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
                    }
                });
                isPhoneValidationInProgress = true;
                Animations.fadeOut(phoneValidate,500);
                Animations.fadeIn(phoneProgressBar,500);
                Swift.getInstance(SignupActivity.this).addToRequestQueue(checkPhoneRequest);
            }
        }
        if(s==password.getEditableText()) {
            if(password.getText().length()<8) setCancelOnImageView(passwordValidate);
            else setOkayOnImageView(passwordValidate);
            if(!(passwordConfirmation.getText().toString().equals(password.getText().toString()))) setCancelOnImageView(passwordConfirmationValidate);
            else setOkayOnImageView(passwordConfirmationValidate);
            if(passwordValidate.getVisibility()!=View.VISIBLE) Animations.fadeIn(passwordValidate, 500);
        }
        if(s==passwordConfirmation.getEditableText()) {
            if(!(passwordConfirmation.getText().toString().equals(password.getText().toString()))) setCancelOnImageView(passwordConfirmationValidate);
            else setOkayOnImageView(passwordConfirmationValidate);
            if(passwordConfirmationValidate.getVisibility()!=View.VISIBLE) Animations.fadeIn(passwordConfirmationValidate, 500);
        }
    }

    private boolean isEverythingValid() {
        return isEmailAvailable &&
                isPhoneAvailable &&
                password.getText().length()>=8 &&
                passwordConfirmation.getText().toString().equals(password.getText().toString()) &&
                name.getText().toString().trim().length()>=2;
    }

}
