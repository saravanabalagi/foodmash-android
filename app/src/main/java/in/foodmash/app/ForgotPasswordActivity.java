package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout back;
    LinearLayout forgot;
    LinearLayout phoneLayout;
    LinearLayout emailLayout;

    EditText phone;
    EditText email;

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
        setContentView(R.layout.activity_forgot_password);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        forgot = (LinearLayout) findViewById(R.id.forgot); forgot.setOnClickListener(this);
        phoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
        emailLayout = (LinearLayout) findViewById(R.id.email_layout);

        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);

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

                intent = new Intent(this, ForgotPasswordOtpActivity.class);
                switch (otpMethodRadioGroup.getCheckedRadioButtonId()){
                    case R.id.phone_radio: intent.putExtra("type","phone"); intent.putExtra("value",phone.getText().toString()); break;
                    case R.id.email_radio: intent.putExtra("type","email"); intent.putExtra("value",email.getText().toString()); break;
                }
                startActivity(intent);
                break;
        }
    }

}
