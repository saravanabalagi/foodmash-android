package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by sarav on Aug 08 2015.
 */
public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    String androidId;

    LinearLayout login;
    LinearLayout create;

    EditText name;
    EditText email;
    EditText phone;
    EditText password;
    EditText passwordConfirmation;
    Switch acceptTerms;

    boolean termsAccepted = false;

    TouchableImageButton clearFields;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signed_out, menu);
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
        setContentView(R.layout.activity_sign_up);

        androidId = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        login = (LinearLayout) findViewById(R.id.login); login.setOnClickListener(this);
        create = (LinearLayout) findViewById(R.id.create); create.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);
        acceptTerms = (Switch) findViewById(R.id.accept_terms); acceptTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) termsAccepted = true;
                else termsAccepted=false;
            }
        });

        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        password = (EditText) findViewById(R.id.password);
        passwordConfirmation = (EditText) findViewById(R.id.password_confirmation);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: name.setText(null); email.setText(null); phone.setText(null); password.setText(null); passwordConfirmation.setText(null); acceptTerms.setChecked(false); break;
            case R.id.login: intent = new Intent(this, LoginActivity.class); startActivity(intent); break;
            case R.id.create:
                if(termsAccepted) {
                    makeJsonRequest();
                } else Toast.makeText(SignupActivity.this,"Accept terms and conditions",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject jsonObject = new JSONObject();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("name",name.getText().toString());
        hashMap.put("email",email.getText().toString());
        hashMap.put("password",password.getText().toString());
        hashMap.put("password_confirmation",passwordConfirmation.getText().toString());
        hashMap.put("mobile_no",phone.getText().toString());
        JSONObject user = new JSONObject(hashMap);
        try {
            jsonObject.put("user",user);
            jsonObject.put("android_id",androidId);
        } catch (JSONException e) { e.printStackTrace(); }
        return jsonObject;
    }

    private void makeJsonRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/registrations",getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(SignupActivity.this, "Response received", Toast.LENGTH_SHORT).show();
                System.out.println("JSON Response: "+response);
                intent = new Intent(SignupActivity.this, MainActivity.class);
                try {
                    if (response.getBoolean("success")) {
                        String userToken = response.getString("user_token");
                        String mobileToken = response.getString("mobile_token");
                        intent.putExtra("logged_in",true);
                        intent.putExtra("mobile_token", mobileToken);
                        intent.putExtra("user_token", userToken);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignupActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                        System.out.println("Error Details: "+response.getString("info"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError) Toast.makeText(SignupActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
                else Toast.makeText(SignupActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}
