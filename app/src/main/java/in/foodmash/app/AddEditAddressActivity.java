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
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sarav on Aug 08 2015.
 */
public class AddEditAddressActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    LatLng latLng;

    EditText name;
    EditText addressLine1;
    EditText addressLine2;
    EditText pincode;
    EditText city;
    EditText phone;
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

        latLng = new LatLng(getIntent().getDoubleExtra("latitude", 0),getIntent().getDoubleExtra("longitude",0));
        System.out.println("Latitude: "+latLng.latitude);
        System.out.println("Longitude: "+latLng.longitude);

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
        city = (EditText) findViewById(R.id.city);
        phone = (EditText) findViewById(R.id.phone);
        primaryAddress = (Switch) findViewById(R.id.primary_address);

        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: name.setText(null); pincode.setText(null); addressLine1.setText(null); addressLine2.setText(null); area.setSelection(0); break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.save: makeJsonRequest(); break;
        }
    }

    public JSONObject getRequestJson() {

        JSONObject jsonObject = new JSONObject();
        String nameVal = name.getText().toString().trim();
        String addressLine1Temp = addressLine1.getText().toString().trim();
        if(addressLine1Temp.charAt(addressLine1Temp.length()-1)==',')
            addressLine1.setText(addressLine1Temp.substring(0,addressLine1Temp.length()-2));
        String addressVal1 = addressLine1.getText().toString().trim();
        String addressVal2 = addressLine2.getText().toString().trim();
        String areaVal = area.getSelectedItem().toString().trim();
        String cityVal = city.getText().toString().trim();
        int pincodeVal = Integer.parseInt(pincode.getText().toString().trim());
        int phoneVal = Integer.parseInt(phone.getText().toString().trim());
        boolean primaryAddressVal = primaryAddress.isChecked();
        try {

            HashMap<String,String> addressHashMap = new HashMap<>();
            addressHashMap.put("line1", addressVal1);
            addressHashMap.put("line2", addressVal2);
            addressHashMap.put("area", areaVal);
            addressHashMap.put("city", cityVal);
            JSONObject addressJson = new JSONObject(addressHashMap);
            addressJson.put("pincode",pincodeVal);

            HashMap<String,Double> geolocationHashMap = new HashMap<>();
            geolocationHashMap.put("latitude",latLng.latitude);
            geolocationHashMap.put("longitude",latLng.longitude);
            JSONObject geolocationJson = new JSONObject(geolocationHashMap);

            jsonObject.put("name", nameVal);
            jsonObject.put("address", addressJson);
            jsonObject.put("geolocation",geolocationJson);
            jsonObject.put("phone", phoneVal);
            jsonObject.put("primaryAddress", primaryAddressVal);

        } catch (JSONException e) { e.printStackTrace(); }
        return jsonObject;
    }

    private void makeJsonRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/address", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        intent = new Intent(AddEditAddressActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if(!(response.getBoolean("success"))) {
                        Toast.makeText(AddEditAddressActivity.this, "Save failed!", Toast.LENGTH_SHORT).show();
                        System.out.println("Error: "+response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError) Toast.makeText(AddEditAddressActivity.this, "Network Error. Try again!", Toast.LENGTH_SHORT).show();
                else Toast.makeText(AddEditAddressActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("JSON Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}
