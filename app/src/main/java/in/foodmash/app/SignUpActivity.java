package in.foodmash.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Cryptography;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.models.User;
import in.foodmash.app.utils.EmailUtils;
import in.foodmash.app.volley.Swift;
import in.foodmash.app.volley.VolleyFailureFragment;
import in.foodmash.app.volley.VolleyProgressFragment;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class SignUpActivity extends FoodmashActivity implements View.OnClickListener, TextWatcher {

    @Bind(R.id.create) FloatingActionButton create;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.terms_and_conditions) LinearLayout termsAndConditions;
    @Bind(R.id.privacy_policy) LinearLayout privacyPolicy;
    @Bind(R.id.refund_policy) LinearLayout refundPolicy;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;

    private Intent intent;
    private boolean fromCart;

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
    private ObjectMapper objectMapper;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Sign","up");

        fromCart = getIntent().getBooleanExtra("from_cart", false);
        create.setOnClickListener(this);

        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

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
        snackbar = Snackbar.make(mainLayout, "", Snackbar.LENGTH_SHORT);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.terms_and_conditions: goToLegaleseActivity(LegaleseActivity.Legalese.TERMS_AND_CONDITIONS); ;break;
            case R.id.refund_policy: goToLegaleseActivity(LegaleseActivity.Legalese.REFUND_POLICY); ;break;
            case R.id.privacy_policy: goToLegaleseActivity(LegaleseActivity.Legalese.PRIVACY_POLICY); ;break;
            case R.id.create:
                if(isEverythingValid()) makeSignUpRequest();
                else Snackbar.make(mainLayout,"One or more data you entered is invalid",Snackbar.LENGTH_LONG).show();
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
        JSONObject jsonObject = JsonProvider.getAnonymousRequestJson(SignUpActivity.this);
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

    public void makeSignUpRequest() {
        JsonObjectRequest signUpRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_sign_up),getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                create.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
                intent = new Intent(SignUpActivity.this, MainActivity.class);
                if(fromCart) intent = new Intent(SignUpActivity.this, CartActivity.class);
                try {
                    if (response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        User.setInstance(objectMapper.readValue(dataJson.getJSONObject("user").toString(),User.class));
                        User user = User.getInstance();
                        Actions.cacheUserDetails(SignUpActivity.this, user.getName(), user.getEmail(), user.getMobileNo());
                        String userToken = dataJson.getString("user_token");
                        String sessionToken = dataJson.getString("session_token");
                        SharedPreferences sharedPreferences = getSharedPreferences("session", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("logged_in",true);
                        editor.putString("user_token", userToken);
                        editor.putString("session_token", sessionToken);
                        editor.putString("android_token", Cryptography.getEncryptedAndroidId(SignUpActivity.this, sessionToken));
                        editor.apply();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else Snackbar.make(mainLayout,"Unable to register your account: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { e.printStackTrace(); Actions.handleIgnorableException(SignUpActivity.this,e);}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeSignUpRequest", create)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        create.setVisibility(View.GONE);
        Swift.getInstance(SignUpActivity.this).addToRequestQueue(signUpRequest);
    }

    private void setCancelOnImageView(ImageView imageView) { imageView.setColorFilter(ContextCompat.getColor(this, R.color.accent)); imageView.setImageResource(R.drawable.svg_close_filled); }
    private void setOkayOnImageView(ImageView imageView) { imageView.setColorFilter(ContextCompat.getColor(this, R.color.okay_green)); imageView.setImageResource(R.drawable.svg_tick_filled); }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override public void afterTextChanged(Editable s) {
        if(s==name.getEditableText()) {
            if(name.getText().toString().trim().length()<2) setCancelOnImageView(nameValidate);
            else setOkayOnImageView(nameValidate);
            if(nameValidate.getVisibility()!=View.VISIBLE) Animations.fadeIn(nameValidate, 500);
        }
        if(s==email.getEditableText()) if(EmailUtils.isValidEmailAddress(s.toString())){ makeCheckEmailRequest();}else{ isEmailAvailable = false; setCancelOnImageView(emailValidate); }
        if(s==phone.getEditableText()) if(s.length()==10) makeCheckPhoneRequest();
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

    private JSONObject getCheckEmailJson() {
        JSONObject requestJson = new JSONObject();
        try{
            JSONObject dataJson = new JSONObject();
            dataJson.put("email",email.getText().toString().trim());
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private JSONObject getCheckPhoneJson() {
        JSONObject requestJson = new JSONObject();
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("mobile_no", phone.getText().toString().trim());
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeCheckEmailRequest() {
        JsonObjectRequest checkEmailRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_check_email), getCheckEmailJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    isEmailValidationInProgress = false;
                    if(response.getBoolean("success")) {
                        isEmailAvailable = true;
                        setOkayOnImageView(emailValidate);
                        if(snackbar.isShown() && isPhoneAvailable) snackbar.dismiss();
                        Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
                    } else {
                        isEmailAvailable = false;
                        setCancelOnImageView(emailValidate);
                        snackbar = Snackbar.make(mainLayout, "Email address already registered", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("Login", new View.OnClickListener() { @Override public void onClick(View v) { finish(); } });
                        snackbar.show();
                        Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                snackbar = Snackbar.make(mainLayout, "No connection",Snackbar.LENGTH_LONG);
                snackbar.setAction("Try again", new View.OnClickListener() { @Override public void onClick(View v) { makeCheckEmailRequest(); } });
                snackbar.show();
                isEmailValidationInProgress = false;
                setCancelOnImageView(emailValidate);
                Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
            }
        });
        isEmailValidationInProgress = true;
        Animations.fadeOut(emailValidate,500);
        Animations.fadeIn(emailProgressBar,500);
        Swift.getInstance(SignUpActivity.this).addToRequestQueue(checkEmailRequest);
    }

    private void makeCheckPhoneRequest() {
        JsonObjectRequest checkPhoneRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_check_mobile), getCheckPhoneJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        isPhoneAvailable = true;
                        setOkayOnImageView(phoneValidate);
                        if(snackbar.isShown() && isEmailAvailable) snackbar.dismiss();
                        Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
                    } else {
                        isPhoneAvailable = false;
                        setCancelOnImageView(phoneValidate);
                        snackbar = Snackbar.make(mainLayout, "Mobile number already registered", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("Login", new View.OnClickListener() { @Override public void onClick(View v) { finish(); } });
                        snackbar.show();
                        Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                snackbar = Snackbar.make(mainLayout, "No connection",Snackbar.LENGTH_LONG);
                snackbar.setAction("Try again", new View.OnClickListener() { @Override public void onClick(View v) { makeCheckPhoneRequest(); } });
                snackbar.show();
                isPhoneValidationInProgress = false;
                setCancelOnImageView(phoneValidate);
                Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
            }
        });
        isPhoneValidationInProgress = true;
        Animations.fadeOut(phoneValidate,500);
        Animations.fadeIn(phoneProgressBar,500);
        Swift.getInstance(SignUpActivity.this).addToRequestQueue(checkPhoneRequest);
    }

    private boolean isEverythingValid() {
        return isEmailAvailable &&
                isPhoneAvailable &&
                password.getText().length()>=8 &&
                passwordConfirmation.getText().toString().equals(password.getText().toString()) &&
                name.getText().toString().trim().length()>=2;
    }

}
