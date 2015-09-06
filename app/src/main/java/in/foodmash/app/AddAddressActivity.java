package in.foodmash.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.RadioGroup;
import android.widget.Switch;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sarav on Aug 08 2015.
 */
public class AddAddressActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    Intent intent;
    LatLng latLng;
    boolean edit = false;
    boolean cart = false;
    JSONObject jsonObject;

    RadioGroup phoneRadioGroup;

    EditText name;
    EditText addressLine1;
    EditText addressLine2;
    EditText pincode;
    EditText city;
    EditText phone;
    EditText landline;
    AutoCompleteTextView area;
    Switch primaryAddress;
    int id;

    ImageView nameValidate;
    ImageView addressLine1Validate;
    ImageView addressLine2Validate;
    ImageView pincodeValidate;
    ImageView areaValidate;
    ImageView phoneValidate;
    ImageView landlineValidate;

    ArrayList<String> areaList;
    TouchableImageButton clearFields;

    LinearLayout mobileLayout;
    LinearLayout landlineLayout;
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
        setContentView(R.layout.activity_add_address);

        cart = getIntent().getBooleanExtra("cart",false);
        if(getIntent().getBooleanExtra("edit",false)) {
            try { jsonObject = new JSONObject(getIntent().getStringExtra("json")); edit = true; }
            catch (JSONException e) { e.printStackTrace(); }
        }

        latLng = new LatLng(getIntent().getDoubleExtra("latitude", 0),getIntent().getDoubleExtra("longitude",0));
        System.out.println("Latitude: " + latLng.latitude);
        System.out.println("Longitude: " + latLng.longitude);

        areaList = new ArrayList<>();
        areaList.add("Kotturpuram");
        areaList.add("RA Puram");
        areaList.add("Nandanam");

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        save = (LinearLayout) findViewById(R.id.save); save.setOnClickListener(this);
        mobileLayout = (LinearLayout) findViewById(R.id.mobile_layout);
        landlineLayout = (LinearLayout) findViewById(R.id.landline_layout);

        nameValidate = (ImageView) findViewById(R.id.name_validate);
        addressLine1Validate = (ImageView) findViewById(R.id.address_line_1_validate);
        addressLine2Validate = (ImageView) findViewById(R.id.address_line_2_validate);
        pincodeValidate = (ImageView) findViewById(R.id.pincode_validate);
        areaValidate = (ImageView) findViewById(R.id.area_validate);
        phoneValidate = (ImageView) findViewById(R.id.phone_validate);
        landlineValidate = (ImageView) findViewById(R.id.landline_validate);

        name = (EditText) findViewById(R.id.name); name.addTextChangedListener(this);
        pincode = (EditText) findViewById(R.id.pincode); pincode.addTextChangedListener(this);
        addressLine1 = (EditText) findViewById(R.id.address_line_1); addressLine1.addTextChangedListener(this);
        addressLine2 = (EditText) findViewById(R.id.address_line_2); addressLine2.addTextChangedListener(this);
        city = (EditText) findViewById(R.id.city);
        phone = (EditText) findViewById(R.id.phone); if(!edit) phone.setText(getPhone());  phone.addTextChangedListener(this);
        landline = (EditText) findViewById(R.id.landline); landline.addTextChangedListener(this);
        primaryAddress = (Switch) findViewById(R.id.primary_address);
        area = (AutoCompleteTextView) findViewById(R.id.area);
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,android.R.layout.select_dialog_item,areaList);
        area.setAdapter(areaAdapter);
        area.setThreshold(1);
        area.addTextChangedListener(this);


        phoneRadioGroup = (RadioGroup) findViewById(R.id.phone_radio_group); phoneRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.mobile_radio: Animations.fadeOutAndFadeIn(landlineLayout,mobileLayout,500); break;
                    case R.id.landline_radio: Animations.fadeOutAndFadeIn(mobileLayout,landlineLayout,500); break;
                }
            }
        });

        if(edit) {
            try {
                id = jsonObject.getInt("id");
                JSONObject addressJson = jsonObject.getJSONObject("address");
                name.setText(jsonObject.getString("name"));
                pincode.setText(addressJson.getString("pincode"));
                addressLine1.setText(addressJson.getString("line1"));
                addressLine2.setText(addressJson.getString("line2"));
                city.setText(addressJson.getString("city"));
                area.setText(addressJson.getString("area"));
                primaryAddress.setChecked(jsonObject.getBoolean("primary"));
                if(jsonObject.getString("phone").length()==10) phone.setText(jsonObject.getString("phone"));
                else { landline.setText(jsonObject.getString("phone")); phoneRadioGroup.check(R.id.landline_radio); Animations.fadeOutAndFadeIn(mobileLayout,landlineLayout,0); }
            } catch (JSONException e) { e.printStackTrace(); }
        } else {
            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if(addresses.get(0)!=null)
                    if(addresses.get(0).getPostalCode().length()==6) {
                        pincode.setText(addresses.get(0).getPostalCode());
                        addressLine2.setText(addresses.get(0).getAddressLine(0));
                    }
            } catch (Exception e) { e.printStackTrace(); }
        }

        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: name.setText(null); pincode.setText(null); addressLine1.setText(null); addressLine2.setText(null); area.setText(null);  break;
            case R.id.back: finish(); break;
            case R.id.save: if(isEverythingValid()) makeJsonRequest(); else Alerts.validityAlert(AddAddressActivity.this); break;
        }
    }

    public JSONObject getRequestJson() {

        JSONObject requestJson = new JSONObject();
        String nameVal = name.getText().toString().trim();
        String addressLine1Temp = addressLine1.getText().toString().trim();
        if(addressLine1Temp.charAt(addressLine1Temp.length()-1)==',')
            addressLine1.setText(addressLine1Temp.substring(0,addressLine1Temp.length()-1));
        String addressVal1 = addressLine1.getText().toString().trim();
        String addressLine2Temp = addressLine2.getText().toString().trim();
        if(addressLine2Temp.charAt(addressLine2Temp.length() - 1)==',')
            addressLine2.setText(addressLine2Temp.substring(0,addressLine2Temp.length()-1));
        String addressVal2 = addressLine2.getText().toString().trim();
        String areaVal = area.getText().toString().trim();
        String cityVal = city.getText().toString().trim();
        String pincodeVal = pincode.getText().toString().trim();
        String phoneVal = (phoneRadioGroup.getCheckedRadioButtonId()==R.id.mobile_radio)?phone.getText().toString().trim():landline.getText().toString().trim();
        boolean primaryAddressVal = primaryAddress.isChecked();
        try {

            HashMap<String,String> addressHashMap = new HashMap<>();
            addressHashMap.put("line1", addressVal1);
            addressHashMap.put("line2", addressVal2);
            addressHashMap.put("area", areaVal);
            addressHashMap.put("city", cityVal);
            addressHashMap.put("pincode", pincodeVal);
            JSONObject addressJson = new JSONObject(addressHashMap);

            HashMap<String,Double> geolocationHashMap = new HashMap<>();
            geolocationHashMap.put("latitude",latLng.latitude);
            geolocationHashMap.put("longitude",latLng.longitude);
            JSONObject geolocationJson = new JSONObject(geolocationHashMap);

            JSONObject dataJson = new JSONObject();
            dataJson.put("name", nameVal);
            dataJson.put("address", addressJson);
            dataJson.put("geolocation", geolocationJson);
            dataJson.put("phone", phoneVal);
            dataJson.put("primary", primaryAddressVal);
            if(edit) dataJson.put("id",id);

            requestJson = JsonProvider.getStandartRequestJson(AddAddressActivity.this);
            requestJson.put("data",dataJson);

        } catch (JSONException e) { e.printStackTrace(); }
        System.out.println(requestJson);
        return requestJson;
    }

    private void makeJsonRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest((edit)?Request.Method.PATCH:Request.Method.POST, getString(R.string.api_root_path) + ((edit)?"/delivery_addresses":"/delivery_addresses/create"), getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        if (cart) intent = new Intent(AddAddressActivity.this, CheckoutPaymentActivity.class);
                        else intent = new Intent(AddAddressActivity.this, AddressActivity.class);
                        startActivity(intent);
                        finish();
                    } else if(!(response.getBoolean("success"))) {
                        Alerts.commonErrorAlert(AddAddressActivity.this, "Address Invalid", "We are unable to process your Address Details. Try Again!", "Okay");
                        System.out.println("Error: " + response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(AddAddressActivity.this);
                else Alerts.unknownErrorAlert(AddAddressActivity.this);
                System.out.println("JSON Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override public void afterTextChanged(Editable s) {
        if(s==name.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(nameValidate, 500); else Animations.fadeOut(nameValidate,500);}
        else if(s==addressLine1.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(addressLine1Validate, 500); else Animations.fadeOut(addressLine1Validate,500);}
        else if(s==addressLine2.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(addressLine2Validate, 500); else Animations.fadeOut(addressLine2Validate,500);}
        else if(s==pincode.getEditableText()) { if(s.toString().trim().length()!=6) Animations.fadeInOnlyIfInvisible(pincodeValidate, 500); else Animations.fadeOut(pincodeValidate,500);}
        else if(s==phone.getEditableText()) { if(s.toString().trim().length()!=10) Animations.fadeInOnlyIfInvisible(phoneValidate, 500); else Animations.fadeOut(phoneValidate,500);}
        else if(s==landline.getEditableText()) { if(s.toString().trim().length()<7) Animations.fadeInOnlyIfInvisible(landlineValidate, 500); else Animations.fadeOut(landlineValidate,500);}
        else if(s==area.getEditableText()) { if(!(areaList.contains(s.toString()))) Animations.fadeInOnlyIfInvisible(areaValidate, 500); else Animations.fadeOut(areaValidate,500); }

    }

    private boolean isEverythingValid() {
        return
                areaList.contains(area.getText().toString().trim()) &&
                name.getText().toString().trim().length()>=2 &&
                addressLine1.getText().toString().trim().length()>=2 &&
                addressLine2.getText().toString().trim().length()>=2 &&
                pincode.getText().toString().trim().length()==6 &&
                (phoneRadioGroup.getCheckedRadioButtonId()==R.id.mobile_radio)?phone.getText().toString().trim().length()==10:landline.getText().toString().trim().length()>=7;
    }

    private String getPhone() {
        SharedPreferences sharedPreferences = getSharedPreferences("cache",0);
        return sharedPreferences.getString("phone",null);
    }
}
