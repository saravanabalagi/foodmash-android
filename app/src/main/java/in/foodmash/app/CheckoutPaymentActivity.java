package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutPaymentActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    LinearLayout address;
    LinearLayout pay;

    RadioGroup paymentMode;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_email_phone) { intent = new Intent(this,EmailPhoneActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_addresses) { intent = new Intent(this,AddressActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_order_history) { intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_wallet_cash) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_contact_us) { intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_log_out) { intent = new Intent(this,LoginActivity.class); startActivity(intent); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_payment);

        address = (LinearLayout) findViewById(R.id.address); address.setOnClickListener(this);
        pay = (LinearLayout) findViewById(R.id.pay); pay.setOnClickListener(this);

        paymentMode = (RadioGroup) findViewById(R.id.payment_mode_radio_group);
        paymentMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.credit_card: break;
                    case R.id.debit_card: break;
                    case R.id.netbanking: break;
                    case R.id.paytm_wallet: break;
                }
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.address: intent = new Intent(this, CheckoutAddressActivity.class); startActivity(intent); break;
            case R.id.pay: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
        }
    }
}
