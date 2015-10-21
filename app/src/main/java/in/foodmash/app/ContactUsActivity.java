package in.foodmash.app;

import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.TouchableImageButton;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    Intent intent;

    LinearLayout back;
    LinearLayout call;
    LinearLayout sendEmail;
    LinearLayout notLoggedInLayout;

    JsonObjectRequest contactUsRequest;

    ImageView issueValidate;
    ImageView descriptionValidate;
    ImageView emailValidate;
    ImageView phoneValidate;

    AutoCompleteTextView issue;
    ArrayList<String> issueList;
    EditText description;
    EditText email;
    EditText phone;

    TouchableImageButton clearFields;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(Info.isLoggedIn(ContactUsActivity.this)) getMenuInflater().inflate(R.menu.menu_main, menu);
        else getMenuInflater().inflate(R.menu.menu_signed_out, menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        TextView cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ContactUsActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_addresses) { intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_order_history) { intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_contact_us) { intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_log_out) { Actions.logout(ContactUsActivity.this); return true; }
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
        sendEmail = (LinearLayout) findViewById(R.id.send_email); sendEmail.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);
        notLoggedInLayout = (LinearLayout) findViewById(R.id.not_logged_in_layout);
        if(Info.isLoggedIn(ContactUsActivity.this)) notLoggedInLayout.setVisibility(View.GONE);

        issueValidate = (ImageView) findViewById(R.id.issue_validate);
        descriptionValidate = (ImageView) findViewById(R.id.description_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phoneValidate = (ImageView) findViewById(R.id.phone_validate);

        description = (EditText) findViewById(R.id.description); description.addTextChangedListener(this);
        email = (EditText) findViewById(R.id.email); email.addTextChangedListener(this);
        phone = (EditText) findViewById(R.id.phone); phone.addTextChangedListener(this);
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
            case R.id.send_email: if(isEverythingValid()) makeRequest(); else Alerts.validityAlert(ContactUsActivity.this); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson = JsonProvider.getStandardRequestJson(ContactUsActivity.this);
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("issue",issue.getText().toString().trim());
            hashMap.put("description",description.getText().toString().trim());
            JSONObject dataJson = new JSONObject(hashMap);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeRequest() {
        contactUsRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/contact_us", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        finish();
                    } else if(!response.getBoolean("success")) {
                        Alerts.requestUnauthorisedAlert(ContactUsActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(ContactUsActivity.this).addToRequestQueue(contactUsRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(ContactUsActivity.this, onClickTryAgain);
                if(error instanceof NoConnectionError) Alerts.timeoutErrorAlert(ContactUsActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(ContactUsActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(ContactUsActivity.this).addToRequestQueue(contactUsRequest);
    }

    private boolean isEverythingValid() {
        return (Info.isLoggedIn(ContactUsActivity.this) || (EmailValidator.getInstance().isValid(email.getText().toString()) && NumberUtils.isInteger(phone.getText().toString()) && phone.getText().toString().length() == 10)) &&
                issue.getText().length()>=2 &&
                description.getText().length()>=2;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==issue.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(issueValidate, 500); else Animations.fadeOut(issueValidate,500); }
        else if(s==description.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(descriptionValidate, 500); else Animations.fadeOut(descriptionValidate,500); }
        else if(s==email.getEditableText()) { if(!EmailValidator.getInstance().isValid(s.toString())) Animations.fadeInOnlyIfInvisible(emailValidate, 500); else Animations.fadeOut(emailValidate,500); }
        else if(s==phone.getEditableText()) { if(!(NumberUtils.isInteger(s.toString()) && s.length()==10)) Animations.fadeInOnlyIfInvisible(phoneValidate, 500); else Animations.fadeOut(phoneValidate,500); }
    }

}
