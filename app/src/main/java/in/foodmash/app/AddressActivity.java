package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by sarav on Aug 08 2015.
 */
public class AddressActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    JSONArray jsonArray;

    LinearLayout back;
    LinearLayout addAddress;
    LinearLayout fillLayout;

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
        setContentView(R.layout.activity_addresses);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        addAddress = (LinearLayout) findViewById(R.id.add_address); addAddress.setOnClickListener(this);

        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses",getRequestJson(),new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                jsonArray = response;
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        final JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject addressJson = jsonObject.getJSONObject("address");
                        final LinearLayout addressLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.user_address, fillLayout, false);
                        String address = addressJson.getString("line1") + ",\n" +
                                addressJson.getString("line2") + ",\n" +
                                addressJson.getString("area") + ",\n" +
                                addressJson.getString("city") + " - " +
                                addressJson.getString("pincode");
                        ((TextView) addressLayout.findViewById(R.id.name)).setText(jsonObject.getString("name"));
                        ((TextView) addressLayout.findViewById(R.id.address)).setText(address);
                        ((TextView) addressLayout.findViewById(R.id.phone)).setText(jsonObject.getString("phone"));
                        final JSONObject geolocationJson = jsonObject.getJSONObject("geolocation");
                        if (jsonObject.getBoolean("primary"))
                            addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
                        addressLayout.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(AddressActivity.this, PinYourLocationActivity.class);
                                intent.putExtra("json", jsonObject.toString());
                                intent.putExtra("edit", true);
                                startActivity(intent);
                            }
                        });
                        addressLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int childCount = ((LinearLayout) fillLayout).getChildCount();
                                for (int i = 0; i < childCount; i++)
                                    fillLayout.getChildAt(i).findViewById(R.id.selected).setVisibility(View.GONE);
                                addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
                            }
                        });
                        fillLayout.addView(addressLayout);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); startActivity(intent); break;
        }
    }

    private JSONObject getRequestJson() {
        HashMap<String,String> hashMap = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("session", 0);
        hashMap.put("auth_user_token", sharedPreferences.getString("user_token",null));
        hashMap.put("auth_session_token", sharedPreferences.getString("session_token", null));
        hashMap.put("auth_android_token", sharedPreferences.getString("android_token", null));
        JSONObject requestJson = new JSONObject(hashMap);
        return requestJson;
    }
}
