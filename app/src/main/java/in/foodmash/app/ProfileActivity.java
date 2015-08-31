package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    EditText name;
    EditText dob;
    EditText email;
    EditText phone;
    EditText landline;
    Switch promotionOffers;

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

        name = (EditText) findViewById(R.id.name);
        dob = (EditText) findViewById(R.id.dob);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        landline = (EditText) findViewById(R.id.landline);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: name.setText(null); dob.setText(null); email.setText(null); phone.setText(null); landline.setText(null); promotionOffers.setChecked(true); break;
            case R.id.change_password: intent = new Intent(this, ChangePasswordActivity.class); startActivity(intent); break;
            case R.id.cancel: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.save: makeJsonRequest(); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = null;
        try {
            HashMap<String, String> profileHashMap = new HashMap<>();
            profileHashMap.put("name", name.getText().toString().trim());
            profileHashMap.put("dob", dob.getText().toString().trim());
            profileHashMap.put("email", email.getText().toString().trim());
            profileHashMap.put("phone", phone.getText().toString().trim());
            profileHashMap.put("landline", landline.getText().toString().trim());
            JSONObject dataJson = new JSONObject(profileHashMap);
            dataJson.put("offers", promotionOffers.isChecked());

            HashMap<String,String> tokensHashMap = new HashMap<>();
            SharedPreferences sharedPreferences = getSharedPreferences("session",0);
            tokensHashMap.put("auth_user_token", sharedPreferences.getString("user_token", null));
            tokensHashMap.put("auth_session_token", sharedPreferences.getString("session_token", null));
            tokensHashMap.put("auth_android_token", sharedPreferences.getString("android_token", null));
            requestJson = new JSONObject(tokensHashMap);
            requestJson.put("data",dataJson);

        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeJsonRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/profile", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        intent = new Intent(ProfileActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if(!(response.getBoolean("success"))) {
                        Alerts.showCommonErrorAlert(ProfileActivity.this,
                                "Invalid Details",
                                "We are unable to save your profile details as they are invalid. Try again later!",
                                "Okay");
                        System.out.println("Response error: "+response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError) Alerts.showInternetConnectionError(ProfileActivity.this);
                else Alerts.showUnknownError(ProfileActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
    }



}
