package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONException;
import org.json.JSONObject;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.TouchableImageButton;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    LinearLayout back;
    LinearLayout forgot;
    LinearLayout phoneLayout;
    LinearLayout emailLayout;

    JsonObjectRequest forgotRequest;

    EditText phone;
    EditText email;

    ImageView phoneValidate;
    ImageView emailValidate;

    RadioGroup otpMethodRadioGroup;
    TouchableImageButton clearAllFields;
    Intent intent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signed_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        forgot = (LinearLayout) findViewById(R.id.forgot); forgot.setOnClickListener(this);
        phoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
        emailLayout = (LinearLayout) findViewById(R.id.email_layout);

        phoneValidate = (ImageView) findViewById(R.id.phone_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phone = (EditText) findViewById(R.id.phone); phone.setText(Info.getPhone(ForgotPasswordActivity.this)); phone.addTextChangedListener(this);
        email = (EditText) findViewById(R.id.email_or_phone); email.setText(Info.getEmail(ForgotPasswordActivity.this)); email.addTextChangedListener(this);

        clearAllFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearAllFields.setOnClickListener(this);

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
            case R.id.clear_fields: email.setText(null); phone.setText(null); break;
            case R.id.back: finish(); break;
            case R.id.forgot: if(isEverythingValid()) makeRequest(); else Alerts.validityAlert(ForgotPasswordActivity.this); break;
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

    private void makeRequest() {
        forgotRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations/forgotPassword", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        System.out.println(response.getString("otp"));
                        intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordOtpActivity.class);
                        intent.putExtra("type",(otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.phone_radio)?"phone":"email");
                        intent.putExtra("value",(otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.phone_radio)?phone.getText().toString().trim():email.getText().toString().trim());
                        startActivity(intent);
                    } else if(!(response.getBoolean("success"))) {
                        Alerts.commonErrorAlert(ForgotPasswordActivity.this, "Could not send OTP", "We are unable to send you OTP as the details you entered are invalid. Try Again!", "Okay");
                        System.out.println("Error: " + response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(ForgotPasswordActivity.this).addToRequestQueue(forgotRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(ForgotPasswordActivity.this, onClickTryAgain);
                if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(ForgotPasswordActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(ForgotPasswordActivity.this);
                System.out.println("JSON Error: " + error);
            }
        });
        Swift.getInstance(ForgotPasswordActivity.this).addToRequestQueue(forgotRequest);
    }

    private boolean isEverythingValid() {
         return (otpMethodRadioGroup.getCheckedRadioButtonId()==R.id.phone_radio)
                 ?phone.getText().toString().trim().length()==10
                 : EmailValidator.getInstance().isValid(email.getText().toString().trim());
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==phone.getEditableText()) { if(s.toString().trim().length()!=10) Animations.fadeInOnlyIfInvisible(phoneValidate, 500); else Animations.fadeOut(phoneValidate,500); }
        else if(s==email.getEditableText()) { if(!EmailValidator.getInstance().isValid(s.toString())) Animations.fadeInOnlyIfInvisible(emailValidate, 500); else Animations.fadeOut(emailValidate,500); }
    }
}
