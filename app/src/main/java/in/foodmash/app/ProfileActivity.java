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

/**
 * Created by sarav on Aug 08 2015.
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    EditText firstName;
    EditText lastName;
    EditText dob;

    LinearLayout cancel;
    LinearLayout save;
    LinearLayout changePassword;

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
        setContentView(R.layout.activity_profile);

        cancel = (LinearLayout) findViewById(R.id.cancel); cancel.setOnClickListener(this);
        save = (LinearLayout) findViewById(R.id.save); save.setOnClickListener(this);
        changePassword = (LinearLayout) findViewById(R.id.change_password); changePassword.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        dob = (EditText) findViewById(R.id.dob);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: firstName.setText(null); lastName.setText(null); dob.setText(null); break;
            case R.id.change_password: intent = new Intent(this, ChangePasswordActivity.class); startActivity(intent); break;
            case R.id.cancel: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.save: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
        }
    }
}
