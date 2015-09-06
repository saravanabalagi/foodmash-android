package in.foodmash.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    Intent intent;

    LinearLayout back;
    LinearLayout call;
    LinearLayout email;

    ImageView issueValidate;
    ImageView descriptionValidate;

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
        if (id == R.id.menu_profile) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_addresses) { intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_order_history) { intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_contact_us) { intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_log_out) { intent = new Intent(this,LoginActivity.class); startActivity(intent); finish(); return true; }
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
        email = (LinearLayout) findViewById(R.id.email_or_phone); email.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);

        issueValidate = (ImageView) findViewById(R.id.issue_validate);
        descriptionValidate = (ImageView) findViewById(R.id.description_validate);

        description = (EditText) findViewById(R.id.description); description.addTextChangedListener(this);
        issue = (AutoCompleteTextView) findViewById(R.id.issue); issue.addTextChangedListener(this);
        ArrayAdapter<String> issueAdapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,issueList);
        issue.setAdapter(issueAdapter);
        issue.setThreshold(2);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: issue.setText(null); description.setText(null); break;
            case R.id.call: Intent callIntent = new Intent(Intent.ACTION_CALL); callIntent.setData(Uri.parse("tel:+918056249612")); startActivity(callIntent); break;
            case R.id.back: finish(); break;
            case R.id.email_or_phone: if(isEverythingValid()) makeRequset(); else Alerts.validityAlert(ContactUsActivity.this); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson = JsonProvider.getStandartRequestJson(ContactUsActivity.this);
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("issue",issue.getText().toString().trim());
            hashMap.put("description",description.getText().toString().trim());
            JSONObject dataJson = new JSONObject(hashMap);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeRequset() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/contact_us", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        finish();
                    } else if(!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(ContactUsActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(ContactUsActivity.this);
                else Alerts.unknownErrorAlert(ContactUsActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(ContactUsActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private boolean isEverythingValid() {
        return issue.getText().length()>=2 &&
                description.getText().length()>=2;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==issue.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(issueValidate, 500); else Animations.fadeOut(issueValidate,500); }
        else if(s==description.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(descriptionValidate, 500); else Animations.fadeOut(descriptionValidate,500); }
    }
}
