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
public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout back;
    LinearLayout forgot;
    LinearLayout chooseMethodLayout;
    LinearLayout otpLayout;
    LinearLayout otpInfoLayout;
    LinearLayout phoneLayout;
    LinearLayout emailLayout;
    LinearLayout otpTimeLayout;
    LinearLayout otpExpiredLayout;

    EditText otp;
    EditText phone;
    EditText email;
    TextView otpTime;

    Handler handler=new Handler();
    int timerMinutes=3;
    int timerSeconds=0;
    boolean otpExpired = false;

    RadioGroup otpMethodRadioGroup;
    boolean forgotClicked = false;

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
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_email_phone: intent = new Intent(this,EmailPhoneActivity.class); startActivity(intent); return true;
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
        setContentView(R.layout.activity_forgot_password);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        forgot = (LinearLayout) findViewById(R.id.forgot); forgot.setOnClickListener(this);
        chooseMethodLayout = (LinearLayout) findViewById(R.id.choose_method_layout);
        otpInfoLayout = (LinearLayout) findViewById(R.id.otp_info_layout);
        otpLayout = (LinearLayout) findViewById(R.id.otp_layout);
        phoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
        emailLayout = (LinearLayout) findViewById(R.id.email_layout);
        otpTimeLayout = (LinearLayout) findViewById(R.id.otp_time_layout);
        otpExpiredLayout = (LinearLayout) findViewById(R.id.otp_expired_layout);

        otp = (EditText) findViewById(R.id.otp);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        otpTime = (TextView) findViewById(R.id.otp_time);

        clearAllFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearAllFields.setOnClickListener(this);

        otpMethodRadioGroup = (RadioGroup) findViewById(R.id.otp_method_radio_group); otpMethodRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.phone_radio: Animations.fadeOutAndFadeIn(emailLayout,phoneLayout,500); break;
                    case R.id.email_radio: Animations.fadeOutAndFadeIn(phoneLayout,emailLayout,500); break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: email.setText(null); phone.setText(null); break;
            case R.id.back: intent = new Intent(this, LoginActivity.class); startActivity(intent); break;
            case R.id.forgot:
                if(!forgotClicked) {
                    setOtpLayout();
                    forgotClicked = true;
                } else {
                    intent = new Intent(this, ChangePasswordActivity.class);
                    intent.putExtra("forgot",true); startActivity(intent);
                }
                break;
        }
    }

    private void setOtpLayout() {
        ((TextView) forgot.findViewById(R.id.done)).setText("proceed");
        Animations.fadeOutAndFadeIn(chooseMethodLayout,otpInfoLayout,500);
        Animations.fadeIn(otpLayout, 500);
        handler.removeCallbacks(initiateOtpTimer);
        handler.postDelayed(initiateOtpTimer, 1000);
    }

    private Runnable initiateOtpTimer= new Runnable() {
        @Override
        public void run() {
            if(timerSeconds==0)
                if(timerMinutes==0) {
                    if(!otpExpired) {
                        otpExpired=true;
                        Animations.fadeOutAndFadeIn(otpTimeLayout,otpExpiredLayout,500);
                        return;
                    } return; }
                else { timerMinutes--; timerSeconds=59; }
            else timerSeconds--;
            otpTime.setText(timerMinutes+":"+String.format("%02d",timerSeconds));
            handler.postDelayed(initiateOtpTimer, 1000);
        }
    };
}
