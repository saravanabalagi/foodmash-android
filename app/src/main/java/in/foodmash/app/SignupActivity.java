package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Cryptography;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.TouchableImageButton;

/**
 * Created by sarav on Aug 08 2015.
 */
public class SignupActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    Intent intent;

    LinearLayout login;
    LinearLayout create;

    EditText name;
    EditText email;
    EditText phone;
    EditText password;
    EditText passwordConfirmation;

    ImageView nameValidate;
    ImageView emailValidate;
    ImageView phoneValidate;
    ImageView passwordValidate;
    ImageView passwordConfirmationValidate;

    ProgressBar emailProgressBar;
    ProgressBar phoneProgressBar;

    boolean isEmailAvailable = false;
    boolean isPhoneAvailable = false;
    boolean isEmailValidationInProgress = false;
    boolean isPhoneValidationInProgress = false;

    Switch acceptTerms;

    boolean termsAccepted = false;
    JsonObjectRequest checkEmailRequest;
    JsonObjectRequest checkPhoneRequest;
    JsonObjectRequest registerRequest;

    TouchableImageButton clearFields;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signed_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_log_out: intent = new Intent(this,LoginActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        login = (LinearLayout) findViewById(R.id.login); login.setOnClickListener(this);
        create = (LinearLayout) findViewById(R.id.create); create.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);
        acceptTerms = (Switch) findViewById(R.id.accept_terms); acceptTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                termsAccepted = isChecked;
            }
        });

        nameValidate = (ImageView) findViewById(R.id.name_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phoneValidate = (ImageView) findViewById(R.id.phone_validate);
        passwordValidate = (ImageView) findViewById(R.id.password_validate);
        passwordConfirmationValidate = (ImageView) findViewById(R.id.password_confirmation_validate);

        name = (EditText) findViewById(R.id.name); name.addTextChangedListener(this);
        email = (EditText) findViewById(R.id.email_or_phone); email.addTextChangedListener(this);
        phone = (EditText) findViewById(R.id.phone); phone.addTextChangedListener(this);
        password = (EditText) findViewById(R.id.password); password.addTextChangedListener(this);
        passwordConfirmation = (EditText) findViewById(R.id.password_confirmation); passwordConfirmation.addTextChangedListener(this);


        emailProgressBar = (ProgressBar) findViewById(R.id.email_loader);
        phoneProgressBar = (ProgressBar) findViewById(R.id.phone_loader);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: name.setText(null); email.setText(null); phone.setText(null); password.setText(null); passwordConfirmation.setText(null); acceptTerms.setChecked(false); break;
            case R.id.login: finish(); break;
            case R.id.create:
                if(termsAccepted) {
                    if(isEverythingValid()) makeJsonRequest();
                    else Alerts.validityAlert(SignupActivity.this);
                    break;
                } else new AlertDialog.Builder(SignupActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Accept Terms and Conditions")
                        .setMessage("You should accept all terms and conditions to sign up in Foodmash")
                        .setPositiveButton("I Understand", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                break;
        }
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
                System.out.println("JSON Response: "+response);
                intent = new Intent(SignupActivity.this, MainActivity.class);
                try {
                    if (response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        String userToken = dataJson.getString("user_token");
                        String sessionToken = dataJson.getString("session_token");
                        SharedPreferences sharedPreferences = getSharedPreferences("session", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("logged_in",true);
                        editor.putString("user_token", userToken);
                        editor.putString("session_token", sessionToken);
                        editor.putString("android_token", Cryptography.getEncrptedAndroidId(SignupActivity.this, sessionToken));
                        editor.apply();
                        startActivity(intent);
                        finish();
                    } else {
                        Alerts.commonErrorAlert(SignupActivity.this,"Registration Invalid", "We are unable to sign you up. Please try again!","Okay");
                        System.out.println("Error Details: " + response.getString("info"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SignupActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(SignupActivity.this).addToRequestQueue(registerRequest);
                    }
                });
                if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SignupActivity.this);
                else Alerts.unknownErrorAlert(SignupActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(SignupActivity.this).addToRequestQueue(registerRequest);
    }

    private void setCancelOnImageView(ImageView imageView) { imageView.setColorFilter(getResources().getColor(R.color.color_accent)); imageView.setImageResource(R.mipmap.error); }
    private void setOkayOnImageView(ImageView imageView) { imageView.setColorFilter(getResources().getColor(R.color.okay_green)); imageView.setImageResource(R.mipmap.tick); }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override public void afterTextChanged(Editable s) {
        if(s==name.getEditableText()) {
            if(name.getText().toString().trim().length()<2) setCancelOnImageView(nameValidate);
            else setOkayOnImageView(nameValidate);
            if(nameValidate.getVisibility()!=View.VISIBLE) Animations.fadeIn(nameValidate, 500);
        }
        if(s==email.getEditableText()) {
            if(EmailValidator.getInstance().isValid(s.toString())) {
                JSONObject requestJson = new JSONObject();
                try{
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("email",s.toString());
                    requestJson.put("data",dataJson);
                } catch (JSONException e) { e.printStackTrace(); }
                System.out.println("Request Json: "+requestJson);
                checkEmailRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations/checkEmail", requestJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            System.out.println("Email response: "+response);
                            isEmailValidationInProgress = false;
                            if(response.getBoolean("success")) {
                                isEmailAvailable = true;
                                setOkayOnImageView(emailValidate);
                                Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
                            } else if(!(response.getBoolean("success"))) {
                                isEmailAvailable = false;
                                setCancelOnImageView(emailValidate);
                                Animations.fadeOutAndFadeIn(emailProgressBar,emailValidate,500);
                            }
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SignupActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Swift.getInstance(SignupActivity.this).addToRequestQueue(checkEmailRequest);
                            }
                        });
                        if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SignupActivity.this);
                        else Alerts.unknownErrorAlert(SignupActivity.this);
                        System.out.println("Email response error: "+error);
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
                            System.out.println("Phone response: "+response);
                            if(response.getBoolean("success")) {
                                isPhoneAvailable = true;
                                setOkayOnImageView(phoneValidate);
                                Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
                            } else if(!(response.getBoolean("success"))) {
                                isPhoneAvailable = false;
                                setCancelOnImageView(phoneValidate);
                                Animations.fadeOutAndFadeIn(phoneProgressBar,phoneValidate,500);
                            }
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(SignupActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Swift.getInstance(SignupActivity.this).addToRequestQueue(checkPhoneRequest);
                            }
                        });
                        if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(SignupActivity.this);
                        else Alerts.unknownErrorAlert(SignupActivity.this);
                        System.out.println("Phone response error: "+error);
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
