package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.Touch;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;

import java.util.ArrayList;

/**
 * Created by sarav on Aug 08 2015.
 */
public class AddEditAddressActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    EditText name;
    EditText addressLine1;
    EditText addressLine2;
    EditText pincode;
    Spinner area;
    Switch primaryAddress;

    ArrayList<String> areaList;
    TouchableImageButton clearFields;

    LinearLayout back;
    LinearLayout save;

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
        setContentView(R.layout.activity_add_edit_address);

        areaList = new ArrayList<>();
        areaList.add("Area");
        areaList.add("Kottur");
        areaList.add("RA Puram");

        area = (Spinner) findViewById(R.id.area);
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,areaList);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        area.setAdapter(areaAdapter);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        save = (LinearLayout) findViewById(R.id.save); save.setOnClickListener(this);

        name = (EditText) findViewById(R.id.name);
        pincode = (EditText) findViewById(R.id.pincode);
        addressLine1 = (EditText) findViewById(R.id.address_line_1);
        addressLine2 = (EditText) findViewById(R.id.address_line_2);
        primaryAddress = (Switch) findViewById(R.id.primary_address);

        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: name.setText(null); pincode.setText(null); addressLine1.setText(null); addressLine2.setText(null); area.setSelection(0); break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.save: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
        }
    }
}
