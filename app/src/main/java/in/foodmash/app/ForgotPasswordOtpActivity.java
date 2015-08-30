package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    int timerMinutes=1;
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
            case R.id.menu_wallet_cash: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
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
            case R.id.proceed:
                intent = new Intent(this, ChangePasswordActivity.class);
                intent.putExtra("forgot",true); startActivity(intent);
                break;
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
}
