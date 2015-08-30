package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
 * Created by Zeke on Jul 19 2015.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout register;
    LinearLayout forgotPassword;
    LinearLayout skip;
    LinearLayout login;

    TouchableImageButton clearAllFields;
    EditText email;
    EditText password;
    Switch keepLoggedIn;

    Intent intent;
    String androidId;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signed_out, menu);
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
        setContentView(R.layout.activity_login);

        androidId = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        register = (LinearLayout) findViewById(R.id.register); register.setOnClickListener(this);
        forgotPassword = (LinearLayout) findViewById(R.id.forgot_password); forgotPassword.setOnClickListener(this);
        skip = (LinearLayout) findViewById(R.id.skip); skip.setOnClickListener(this);
        login = (LinearLayout) findViewById(R.id.login); login.setOnClickListener(this);

        clearAllFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearAllFields.setOnClickListener(this);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        keepLoggedIn = (Switch) findViewById(R.id.keep_logged_in);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: email.setText(null); password.setText(null); keepLoggedIn.setChecked(false); break;
            case R.id.register: intent = new Intent(this, SignupActivity.class); startActivity(intent); break;
            case R.id.forgot_password: intent = new Intent(this, ForgotPasswordActivity.class); startActivity(intent); break;
            case R.id.skip: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.login:
                makeJsonRequest();
                break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject jsonObject = new JSONObject();
        HashMap<String,String> hashMap=  new HashMap<>();
        hashMap.put("email", email.getText().toString());
        hashMap.put("password", password.getText().toString());
        JSONObject user = new JSONObject(hashMap);
        try {
            jsonObject.put("user",user);
            jsonObject.put("android_id",androidId);
        } catch (JSONException e) { e.printStackTrace(); }
        return jsonObject;
    }

    private void makeJsonRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path)+"/sessions",getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("JSON Response: "+response);
                intent = new Intent(LoginActivity.this, MainActivity.class);
                try {
                    if (response.getBoolean("success")) {
                        String userToken = response.getString("user_token");
                        String sessionToken = response.getString("session_token");
                        String androidId = Settings.Secure.getString(LoginActivity.this.getContentResolver(),Settings.Secure.ANDROID_ID);
                        SharedPreferences sharedPreferences = getSharedPreferences("session", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("logged_in",true);
                        editor.putString("user_token", userToken);
                        editor.putString("session_token", sessionToken);
                        editor.putString("android_token", Cryptography.encrypt(androidId, sessionToken));
                        editor.apply();
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setTitle("Invalid username or password")
                                .setMessage("We are unable to log you in with the entered credentials. Please try again!")
                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                        System.out.println("Error Details: "+response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError) Alerts.showInternetConnectionError(LoginActivity.this);
                else Alerts.showUnknownError(LoginActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

}
