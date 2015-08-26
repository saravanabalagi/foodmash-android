package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.Touch;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

/**
 * Created by sarav on Aug 08 2015.
 */
public class EmailPhoneActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    EditText email;
    EditText phone;
    EditText landline;
    Switch promotionOffers;

    LinearLayout back;
    LinearLayout save;

    TouchableImageButton clearFields;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        setContentView(R.layout.activity_email_phone);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        save = (LinearLayout) findViewById(R.id.save); save.setOnClickListener(this);

        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        landline = (EditText) findViewById(R.id.landline);

        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: email.setText(null); phone.setText(null); landline.setText(null); promotionOffers.setChecked(true); break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.save: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
        }
    }
}
