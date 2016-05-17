package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.models.User;
import in.foodmash.app.volley.Swift;
import in.foodmash.app.volley.VolleyFailureFragment;
import in.foodmash.app.volley.VolleyProgressFragment;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class OtpActivity extends FoodmashActivity implements View.OnClickListener{

    public static final int TIMER_MINUTES = 5;
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
    private String phoneOrEmailValue =null;
    private String recoveryMode;
    private String type;

    private Handler handler=new Handler();
    private int timerMinutes=TIMER_MINUTES;
    private int timerSeconds=0;
    private boolean otpExpired = false;

    private Intent intent;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(type.equals("verify_user")) { setResult(RESULT_CANCELED); finish(); }
            else if (type.equals("forgot_password")) finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }

        proceed.setOnClickListener(this);
        resendOtp.setOnClickListener(this);
        otp = (EditText) findViewById(R.id.otp);

        if(getIntent().getStringExtra("type").equals("forgot_password")) {
            setTitle(toolbar,"Forgot","Password");
            type = getIntent().getStringExtra("type");
            phoneOrEmailValue = getIntent().getStringExtra("value");
            recoveryMode = getIntent().getStringExtra("recovery_mode");
            if (recoveryMode.equals("email")) otpInfo.setText("We have sent an OTP (One Time Password) to your email '" + phoneOrEmailValue + "' and to the mobile no. linked with this account. Enter it below to reset your account password. You can resend the OTP once the timer expires.");
            else if (recoveryMode.equals("phone")) otpInfo.setText("We have sent an OTP (One Time Password) to your phone " + "+91" + phoneOrEmailValue + " via a private message and an email to the email address linked with this account. Enter it below to reset your account password. You can resend the OTP once the timer expires.");
        } else if(getIntent().getStringExtra("type").equals("verify_user")) {
            if(!Info.isLoggedIn(this)) { setResult(RESULT_CANCELED); finish(); }
            setTitle(toolbar,"Verify","Account");
            type = getIntent().getStringExtra("type");
            otpInfo.setText("We have sent an OTP to your phone " + Info.getPhone(this) + " via a private message, enter it below to verify your account. You can resend the OTP once the timer expires.");
            resendOtpRequest();
        }
        else { setResult(RESULT_CANCELED); finish(); }

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
        JSONObject requestJson = JsonProvider.getAnonymousRequestJson(OtpActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("otp",otp.getText().toString().trim());
            requestJson.put("data",dataJson);
        } catch(JSONException e) {e.printStackTrace();}
        return requestJson;
    }

    public void makeCheckOtpRequest() {
        String url = "";
        if(type.equals("verify_user")) url = getString(R.string.routes_verify_profile);
        else if(type.equals("forgot_password")) url = getString(R.string.routes_check_otp);
        JsonObjectRequest checkOtpRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + url, getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                proceed.setVisibility(View.VISIBLE);
                try {
                    if(response.getBoolean("success")) {
                        if(type.equals("verify_user")) {
                            User.getInstance().setVerified(true);
                            setResult(RESULT_OK);
                            finish();
                        } else if(type.equals("forgot_password")) {
                            JSONObject dataJson = response.getJSONObject("data");
                            intent = new Intent(OtpActivity.this, ChangePasswordActivity.class);
                            intent.putExtra("otp_token",dataJson.getString("otp_token"));
                            intent.putExtra("forgot", true);
                            startActivity(intent);
                            finish();
                        }
                    } else Snackbar.make(mainLayout,"OTP mismatch!",Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(OtpActivity.this,e);}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeCheckOtpRequest", proceed)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        proceed.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(OtpActivity.this).addToRequestQueue(checkOtpRequest);
    }

    private boolean isEverythingValid() { return !otpExpired && otp.getText().toString().trim().length()>=5; }
    private JSONObject getOtpRequestJson() {
        JSONObject requestJson = null;
        if(type.equals("verify_user")) requestJson = JsonProvider.getStandardRequestJson(OtpActivity.this);
        else if(type.equals("forgot_password")) {
            requestJson = JsonProvider.getAnonymousRequestJson(OtpActivity.this);
            try {
                JSONObject userJson = new JSONObject();
                if (recoveryMode.equals("phone")) userJson.put("mobile_no", phoneOrEmailValue);
                else if (recoveryMode.equals("email")) userJson.put("email", phoneOrEmailValue);
                JSONObject dataJson = new JSONObject();
                dataJson.put("user",userJson);
                requestJson.put("data",dataJson);
            } catch (JSONException e) { e.printStackTrace(); }
        }
        return requestJson;
    }

    public void resendOtpRequest() {
        String url = "";
        if(type.equals("verify_user")) url = getString(R.string.routes_get_otp_profile);
        else if(type.equals("forgot_password")) url = getString(R.string.routes_forgot_password);
        JsonObjectRequest resendOtpRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + url, getOtpRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                proceed.setVisibility(View.VISIBLE);
                try {
                    if (response.getBoolean("success")) {
                        otpExpired = false;
                        timerMinutes = TIMER_MINUTES;
                        timerSeconds = 0;
                        Animations.fadeOutAndFadeIn(otpExpiredLayout, otpTimeLayout, 500);
                        Animations.fadeIn(otpFillLayout, 500);
                        handler.removeCallbacks(setOtpTime);
                        handler.postDelayed(setOtpTime, 1000);
                    } else
                        Snackbar.make(mainLayout, "Unable to process your request: " + response.getString("error"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Actions.handleIgnorableException(OtpActivity.this, e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeLocationRequest", proceed)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        proceed.setVisibility(View.GONE);
        Swift.getInstance(OtpActivity.this).addToRequestQueue(resendOtpRequest);
    }

}
