package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ForgotPasswordOtpActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout back;
    LinearLayout proceed;
    LinearLayout otpTimeLayout;
    LinearLayout otpExpiredLayout;
    LinearLayout otpFillLayout;
    LinearLayout resendOtp;

    EditText otp;
    TextView otpTime;
    TextView otpInfo;
    String recoveryKey=null;

    Handler handler=new Handler();
    int timerMinutes=3;
    int timerSeconds=0;
    boolean otpExpired = false;

    Intent intent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signed_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: intent = new Intent(this,LoginActivity.class); startActivity(intent); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        proceed = (LinearLayout) findViewById(R.id.proceed); proceed.setOnClickListener(this);
        otpTimeLayout = (LinearLayout) findViewById(R.id.otp_time_layout);
        otpExpiredLayout = (LinearLayout) findViewById(R.id.otp_expired_layout);
        otpFillLayout = (LinearLayout) findViewById(R.id.otp_fill_layout);
        resendOtp = (LinearLayout) findViewById(R.id.resend_otp); resendOtp.setOnClickListener(this);

        otp = (EditText) findViewById(R.id.otp);
        otpTime = (TextView) findViewById(R.id.otp_time);
        otpInfo = (TextView) findViewById(R.id.otp_info);

        recoveryKey=getIntent().getStringExtra("value");
        if(getIntent().getStringExtra("type").equals("email")) otpInfo.setText("We have sent an OTP (One Time Password) to your email '"+recoveryKey+"', enter it below to reset your account password. You can resend the OTP once the timer expires.");
        else if(getIntent().getStringExtra("type").equals("phone")) otpInfo.setText("We have sent an OTP (One Time Password) to your phone "+"+91"+recoveryKey+" via a private message, enter it below to reset your account password. You can resend the OTP once the timer expires.");

        handler.removeCallbacks(initiateOtpTimer);
        handler.postDelayed(initiateOtpTimer, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resend_otp:
                otpExpired = false;
                timerMinutes = 1; timerSeconds = 0;
                Animations.fadeOutAndFadeIn(otpExpiredLayout,otpTimeLayout,500);
                Animations.fadeOutAndFadeIn(resendOtp, otpFillLayout, 500);
                handler.removeCallbacks(initiateOtpTimer);
                handler.postDelayed(initiateOtpTimer, 1000);
                break;
            case R.id.back: intent = new Intent(this, ForgotPasswordActivity.class); startActivity(intent); break;
            case R.id.proceed: if(isEverythingValid()) makeRequest(); else Alerts.validityAlert(ForgotPasswordOtpActivity.this); break;
        }
    }

    private Runnable initiateOtpTimer= new Runnable() {
        @Override
        public void run() {
            if(timerSeconds==0)
                if(timerMinutes==0) {
                    if(!otpExpired) {
                        otpExpired=true;
                        Animations.fadeOutAndFadeIn(otpTimeLayout,otpExpiredLayout,500);
                        Animations.fadeOutAndFadeIn(otpFillLayout, resendOtp, 500);
                        return;
                    } return; }
                else { timerMinutes--; timerSeconds=59; }
            else timerSeconds--;
            otpTime.setText(timerMinutes+":"+String.format("%02d",timerSeconds));
            handler.postDelayed(initiateOtpTimer, 1000);
        }
    };

    private JSONObject getRequestJson() {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("android_id", Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID));
            JSONObject dataJson = new JSONObject();
            dataJson.put("otp",otp.getText().toString().trim());
            requestJson.put("data",dataJson);
        } catch(JSONException e) {e.printStackTrace();}
        return requestJson;
    }

    private void makeRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations/checkOtp", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        intent = new Intent(ForgotPasswordOtpActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("otp_token",dataJson.getString("otp_token"));
                        intent.putExtra("forgot",true);
                        startActivity(intent);
                    } else if(!(response.getBoolean("success"))) {
                        Alerts.commonErrorAlert(ForgotPasswordOtpActivity.this, "Address Invalid", "We are unable to process your Address Details. Try Again!", "Okay");
                        System.out.println("Error: " + response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(ForgotPasswordOtpActivity.this);
                else Alerts.unknownErrorAlert(ForgotPasswordOtpActivity.this);
                System.out.println("JSON Error: " + error);
            }
        });
        Swift.getInstance(ForgotPasswordOtpActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private boolean isEverythingValid() {
        return !otpExpired &&
                otp.getText().toString().trim().length()==6;
    }

}
