package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses",JsonProvider.getStandartRequestJson(AddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        jsonArray = response.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
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
                                ((TextView) addressLayout.findViewById(R.id.phone)).setText(((jsonObject.getString("phone").length()==10)?"+91 ":"+91 44 ")+jsonObject.getString("phone"));
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
                                addressLayout.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        JSONObject requestJson = JsonProvider.getStandartRequestJson(AddressActivity.this);
                                        JSONObject dataJson = new JSONObject();
                                        try {
                                            dataJson.put("id", jsonObject.getString("id"));
                                            requestJson.put("data", dataJson);
                                        } catch (JSONException e) { e.printStackTrace(); }
                                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses/destroy", requestJson, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    if (response.getBoolean("success"))
                                                        fillLayout.removeView(addressLayout);
                                                    else if (response.getBoolean("success"))
                                                        Alerts.commonErrorAlert(AddressActivity.this, "Could not delete !", "The address that you want to remove could not be removed. Try again!", "Okay");
                                                } catch (JSONException e) { e.printStackTrace(); }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                if (error instanceof NoConnectionError || error instanceof TimeoutError)
                                                    Alerts.internetConnectionErrorAlert(AddressActivity.this);
                                                else Alerts.unknownErrorAlert(AddressActivity.this);
                                                System.out.println("Response Error: " + error);
                                            }
                                        });
                                        Swift.getInstance(AddressActivity.this).addToRequestQueue(jsonObjectRequest);
                                    }
                                });
                                fillLayout.addView(addressLayout);
                        }
                    } else if(!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(AddressActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(AddressActivity.this);
                else Alerts.unknownErrorAlert(AddressActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); startActivity(intent); break;
        }
    }

}
