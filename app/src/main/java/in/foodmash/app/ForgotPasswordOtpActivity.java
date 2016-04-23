package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class ForgotPasswordOtpActivity extends FoodmashActivity implements View.OnClickListener{

    @Bind(R.id.proceed) FloatingActionButton proceed;
    @Bind(R.id.otp_time_layout) LinearLayout otpTimeLayout;
    @Bind(R.id.otp_expired_layout) LinearLayout otpExpiredLayout;
    @Bind(R.id.otp_fill_layout) LinearLayout otpFillLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.resend_otp) TextView resendOtp;
    @Bind(R.id.otp_time) TextView otpTime;
    @Bind(R.id.otp_info) TextView otpInfo;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private EditText otp;
    private String recoveryKey=null;
    private String type;

    private Handler handler=new Handler();
    private int timerMinutes=3;
    private int timerSeconds=0;
    private boolean otpExpired = false;
    private JsonObjectRequest checkOtpRequest;
    private JsonObjectRequest resendOtpRequest;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Forgot","Password");

        proceed.setOnClickListener(this);
        resendOtp.setOnClickListener(this);
        otp = (EditText) findViewById(R.id.otp);

        recoveryKey=getIntent().getStringExtra("value");
        type = getIntent().getStringExtra("type");
        if(type.equals("email")) otpInfo.setText("We have sent an OTP (One Time Password) to your email '"+recoveryKey+"', enter it below to reset your account password. You can resend the OTP once the timer expires.");
        else if(type.equals("phone")) otpInfo.setText("We have sent an OTP (One Time Password) to your phone "+"+91"+recoveryKey+" via a private message, enter it below to reset your account password. You can resend the OTP once the timer expires.");

        handler.removeCallbacks(setOtpTime);
        handler.postDelayed(setOtpTime, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resend_otp: if(otpExpired) resendOtpRequest(); break;
            case R.id.proceed: if(isEverythingValid()) makeCheckOtpRequest(); else Snackbar.make(mainLayout,"One or more data you entered is invalid",Snackbar.LENGTH_LONG).show(); break;
        }
    }

    private Runnable setOtpTime = new Runnable() {
        @Override
        public void run() {
            if(timerSeconds==0)
                if(timerMinutes==0) {
                    if(!otpExpired) {
                        otpExpired=true;
                        Animations.fadeOutAndFadeIn(otpTimeLayout, otpExpiredLayout, 500);
                        Animations.fadeOut(otpFillLayout, 500);
                        return;
                    } return; }
                else { timerMinutes--; timerSeconds=59; }
            else timerSeconds--;
            String otpTimeString = timerMinutes+":"+String.format("%02d",timerSeconds);
            otpTime.setText(otpTimeString);
            handler.postDelayed(setOtpTime, 1000);
        }
    };

    private JSONObject getRequestJson() {
        JSONObject requestJson = JsonProvider.getAnonymousRequestJson(ForgotPasswordOtpActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("otp",otp.getText().toString().trim());
            requestJson.put("data",dataJson);
        } catch(JSONException e) {e.printStackTrace();}
        return requestJson;
    }

    public void makeCheckOtpRequest() {
        JsonObjectRequest checkOtpRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        intent = new Intent(ForgotPasswordOtpActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("otp_token",dataJson.getString("otp_token"));
                        intent.putExtra("forgot",true);
                        startActivity(intent);
                        finish();
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(ForgotPasswordOtpActivity.this,e);}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeCheckOtpRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(ForgotPasswordOtpActivity.this).addToRequestQueue(checkOtpRequest);
    }

    private boolean isEverythingValid() {
        return !otpExpired &&
                otp.getText().toString().trim().length()>=5;
    }

    private JSONObject getOtpRequestJson() {
        JSONObject requestJson = JsonProvider.getAnonymousRequestJson(ForgotPasswordOtpActivity.this);
        try {
            JSONObject userJson = new JSONObject();
            if(type.equals("phone")) userJson.put("mobile_no",recoveryKey);
            else if(type.equals("email")) userJson.put("email",recoveryKey);
            JSONObject dataJson = new JSONObject();
            dataJson.put("user",userJson);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    public void resendOtpRequest() {
        resendOtpRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + getString(R.string.forgot_password), getOtpRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        otpExpired = false;
                        timerMinutes = 3; timerSeconds = 0;
                        Animations.fadeOutAndFadeIn(otpExpiredLayout, otpTimeLayout, 500);
                        Animations.fadeIn(otpFillLayout, 500);
                        handler.removeCallbacks(setOtpTime);
                        handler.postDelayed(setOtpTime, 1000);
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(ForgotPasswordOtpActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeLocationRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(ForgotPasswordOtpActivity.this).addToRequestQueue(resendOtpRequest);
    }

}
