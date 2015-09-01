package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    LinearLayout back;
    LinearLayout call;
    LinearLayout email;

    AutoCompleteTextView issue;
    ArrayList<String> issueList;
    EditText description;

    TouchableImageButton clearFields;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true; }
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
        setContentView(R.layout.activity_contact_us);

        issueList = new ArrayList<>();
        issueList.add("Not Delivered");
        issueList.add("Package Tampered or Damaged");
        issueList.add("Delayed Delivery");
        issueList.add("Payment Issues");
        issueList.add("Food not Hot");
        issueList.add("Issues in App");
        issueList.add("Bug Report");
        issueList.add("Feedback");

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        call = (LinearLayout) findViewById(R.id.call); call.setOnClickListener(this);
        email = (LinearLayout) findViewById(R.id.email); email.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);

        description = (EditText) findViewById(R.id.description);
        issue = (AutoCompleteTextView) findViewById(R.id.issue);
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,issueList);
        issue.setAdapter(issueAdapter);
        issue.setThreshold(2);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: issue.setSelection(0); description.setText(null); break;
            case R.id.call: Intent callIntent = new Intent(Intent.ACTION_CALL); callIntent.setData(Uri.parse("tel:+918056249612")); startActivity(callIntent); break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.email: break;
        }
    }

}
