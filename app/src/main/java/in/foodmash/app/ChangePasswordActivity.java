package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.util.HashMap;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    EditText oldPassword;
    EditText newPassword;
    EditText confirmPassword;

    boolean forgot = false;
    String otpHash;

    ImageView oldPasswordValidate;
    ImageView newPasswordValidate;
    ImageView confirmPasswordValidate;

    LinearLayout back;
    LinearLayout change;

    Intent intent;
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
        if (id == R.id.menu_contact_us) { intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_log_out) { intent = new Intent(this,LoginActivity.class); startActivity(intent); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        if(getIntent().getBooleanExtra("forgot",false)) { forgot = true; otpHash = getIntent().getStringExtra("opt_hash"); }

        oldPasswordValidate = (ImageView) findViewById(R.id.old_password_validate);
        newPasswordValidate = (ImageView) findViewById(R.id.new_password_validate);
        confirmPasswordValidate = (ImageView) findViewById(R.id.confirm_password_validate);

        oldPassword = (EditText) findViewById(R.id.old_password); if(forgot) oldPassword.setVisibility(View.GONE); else oldPassword.addTextChangedListener(this);
        newPassword = (EditText) findViewById(R.id.new_password); newPassword.addTextChangedListener(this);
        confirmPassword = (EditText) findViewById(R.id.confirm_password); confirmPassword.addTextChangedListener(this);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        change = (LinearLayout) findViewById(R.id.change); change.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);

        if(getIntent().getBooleanExtra("forgot", false)) {
            oldPassword.setVisibility(View.GONE);
            back.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: oldPassword.setText(null); newPassword.setText(null); confirmPassword.setText(null); break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.forgot: if (isEverythingValid()) makeRequest(); else Alerts.validityAlert(ChangePasswordActivity.this); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = new JSONObject();
        try {
            HashMap<String, String> hashMap = new HashMap<>();
            if(forgot) hashMap.put("otp_token", otpHash);
            else hashMap.put("old_password", oldPassword.getText().toString());
            hashMap.put("password", newPassword.getText().toString());
            hashMap.put("password_confirmation", confirmPassword.getText().toString());
            JSONObject userJson = new JSONObject(hashMap);
            requestJson = JsonProvider.getStandartRequestJson(ChangePasswordActivity.this);
            requestJson.put("user", userJson);
        } catch (Exception e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/change_password", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        intent = new Intent(ChangePasswordActivity.this,ProfileActivity.class);
                        startActivity(intent);
                    } else if(!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(ChangePasswordActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(ChangePasswordActivity.this);
                else Alerts.unknownErrorAlert(ChangePasswordActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(ChangePasswordActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private boolean isEverythingValid() {
        return oldPassword.getText().length()>=8 &&
                newPassword.getText().length()>=8 &&
                confirmPassword.getText().length()>=8;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==oldPassword.getEditableText()) { if(oldPassword.getText().toString().length()<8) Animations.fadeInOnlyIfInvisible(oldPasswordValidate,500); else Animations.fadeOut(oldPasswordValidate,500); }
        else if(s==newPassword.getEditableText()) { if(newPassword.getText().toString().length()<8) Animations.fadeInOnlyIfInvisible(newPasswordValidate,500); else Animations.fadeOut(newPasswordValidate,500); }
        else if(s==confirmPassword.getEditableText()) { if(confirmPassword.getText().toString().length()<8) Animations.fadeInOnlyIfInvisible(confirmPasswordValidate,500); else Animations.fadeOut(confirmPasswordValidate,500); }
    }
}
