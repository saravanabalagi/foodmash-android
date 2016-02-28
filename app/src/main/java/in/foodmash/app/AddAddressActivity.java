package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Address;
import in.foodmash.app.custom.City;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class AddAddressActivity extends AppCompatActivity implements TextWatcher {

    @Bind(R.id.save) FloatingActionButton save;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.name_validate) ImageView nameValidate;
    @Bind(R.id.address_line_1_validate) ImageView addressLine1Validate;
    @Bind(R.id.address_line_2_validate) ImageView addressLine2Validate;
    @Bind(R.id.contact_validate) ImageView contactValidate;

    @Bind(R.id.saving_layout) LinearLayout savingLayout;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.area_city_spinner_layout) LinearLayout areaCitySpinnerLayout;

    @Bind(R.id.name) EditText name;
    @Bind(R.id.line1) EditText line1;
    @Bind(R.id.line2) EditText line2;
    @Bind(R.id.contact_no) EditText contactNo;
    @Bind(R.id.locked_area_city) EditText lockedAreaCity;

    @Bind(R.id.area) Spinner area;
    @Bind(R.id.city) Spinner city;

    private ArrayList<String> citiesArrayList = new ArrayList<>();
    private List<City> cities;
    private Address address;

    private Intent intent;
    private LatLng latLng;
    private boolean edit = false;
    private boolean cart = false;
    private JsonObjectRequest addAddressRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        save.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { if (isEverythingValid()) makeJsonRequest(); else Alerts.validityAlert(AddAddressActivity.this); } });
        cart = getIntent().getBooleanExtra("cart",false);
        latLng = new LatLng(getIntent().getDoubleExtra("latitude", 0),getIntent().getDoubleExtra("longitude",0));
        address = new Address();

        if(getIntent().getBooleanExtra("edit",false)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            try {
                address = objectMapper.readValue(getIntent().getStringExtra("json"),Address.class);
                cities = Arrays.asList(objectMapper.readValue(Info.getCityJsonArrayString(this), City[].class));
                edit = true;
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        if(!cart) {
            lockedAreaCity.setText(null);
            lockedAreaCity.setVisibility(View.GONE);

            city = (Spinner) findViewById(R.id.city);
            area = (Spinner) findViewById(R.id.area);

            for (City city : cities) citiesArrayList.add(city.getName());
            ArrayAdapter cityAdapter = new ArrayAdapter<>(
                    AddAddressActivity.this,
                    R.layout.spinner_item,
                    citiesArrayList);
            cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            city.setAdapter(cityAdapter);
            city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) { }
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ArrayAdapter areaAdapter = new ArrayAdapter<>(
                            AddAddressActivity.this,
                            R.layout.spinner_item,
                            addStringAsFirstItem(cities.get(position).getAreaStringArrayList(),"Area"));
                    areaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    area.setAdapter(areaAdapter);
                }
            });

            ArrayAdapter areaAdapter = new ArrayAdapter<>(
                    AddAddressActivity.this,
                    R.layout.spinner_item,
                    addStringAsFirstItem(cities.get(0).getAreaStringArrayList(),"Area"));
            areaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            area.setAdapter(areaAdapter);
            area.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) { }
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position==0) return;
                    int areaId = cities.get(city.getSelectedItemPosition()).getAreas().get(position-1).getId();
                    address.setAreaId(areaId);
                }
            });
        } else {
            areaCitySpinnerLayout.setVisibility(View.GONE);
            String areaCityString = Info.getAreaName(this) + ", " + Info.getCityName(this);
            lockedAreaCity.setText(areaCityString);
            if(edit) address.setAreaId(Info.getAreaId(this));
        }

        if(edit) {
            name.setText(address.getName());
            line1.setText(address.getLine1());
            line2.setText(address.getLine2());
            contactNo.setText(address.getContactNo());
            if(!cart) {
                int areaId = address.getId();
                int cityPos = -1;
                for(int i=0;i<cities.size();i++) if (cities.get(i).indexOf(areaId)!=-1) cityPos=i;
                int areaPos = cities.get(cityPos).indexOf(areaId);
                city.setSelection(cityPos, true);
                area.setSelection(areaPos, true);
            }
        } else {
            try {
                Geocoder geocoder = new Geocoder(this);
                List<android.location.Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if(addresses.size()>0 && addresses.get(0)!=null) {
                        line1.setText(addresses.get(0).getAddressLine(0));
                        line2.setText(addresses.get(0).getAddressLine(1));
                    }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public JSONObject getRequestJson() {


        String line1Temp = line1.getText().toString().trim();
        if(line1Temp.charAt(line1Temp.length()-1)==',') {
            line1.setText(line1Temp.substring(0,line1Temp.length()-1));
        }
        String line2Temp = line2.getText().toString().trim();
        if(line2Temp.charAt(line2Temp.length() - 1)==',') {
            line2.setText(line2Temp.substring(0,line2Temp.length()-1));
        }

        address.setName(name.getText().toString().trim());
        address.setLine1(line1.getText().toString().trim());
        address.setLine2(line2.getText().toString().trim());
        address.setContactNo(contactNo.getText().toString().trim());
        address.setLatitude(latLng.latitude);
        address.setLongitude(latLng.longitude);

        if(cart) address.setAreaId(Info.getAreaId(this));
        else address.setAreaId(cities.get(city.getSelectedItemPosition()).getAreas().get(area.getSelectedItemPosition()-1).getId());

        JSONObject requestJson = JsonProvider.getStandardRequestJson(AddAddressActivity.this);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            if(edit) requestJson.put("data", new JSONObject(objectMapper.writeValueAsString(address)));
            else {
                objectMapper.addMixIn(Address.class, IgnoreIdMixin.class);
                requestJson.put("data", new JSONObject(objectMapper.writeValueAsString(address)));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return requestJson;
    }



    private void makeJsonRequest() {
        addAddressRequest = new JsonObjectRequest((edit)?Request.Method.PATCH:Request.Method.POST, getString(R.string.api_root_path) + ((edit)?"/delivery_addresses":"/delivery_addresses/create"), getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        if (cart) intent = new Intent(AddAddressActivity.this, CheckoutAddressActivity.class);
                        else intent = new Intent(AddAddressActivity.this, AddressActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Animations.fadeOut(savingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        Alerts.commonErrorAlert(AddAddressActivity.this, "Address Invalid", "We are unable to process your Address Details. Try Again!", "Okay");
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOut(savingLayout,500);
                Animations.fadeIn(mainLayout,500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(savingLayout,500);
                        Animations.fadeOut(mainLayout,500);
                        Swift.getInstance(AddAddressActivity.this).addToRequestQueue(addAddressRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(AddAddressActivity.this, onClickTryAgain);
                else if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(AddAddressActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(AddAddressActivity.this);
                Log.e("Network",error.toString());
            }
        });
        Animations.fadeIn(savingLayout,500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(AddAddressActivity.this).addToRequestQueue(addAddressRequest);
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override public void afterTextChanged(Editable s) {
        if(s==name.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(nameValidate, 500); else Animations.fadeOut(nameValidate,500);}
        else if(s== line1.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(addressLine1Validate, 500); else Animations.fadeOut(addressLine1Validate,500);}
        else if(s== line2.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(addressLine2Validate, 500); else Animations.fadeOut(addressLine2Validate,500);}
        else if(s== contactNo.getEditableText()) { if(s.toString().trim().length()!=10) Animations.fadeInOnlyIfInvisible(contactValidate, 500); else Animations.fadeOut(contactValidate,500);}
    }

    private boolean isEverythingValid() {
        return
                ((!cart &&
                    (citiesArrayList.contains(((TextView) city.getSelectedView()).getText().toString()) &&
                     cities.get(city.getSelectedItemPosition()).getAreaStringArrayList().contains(((TextView) area.getSelectedView()).getText().toString().trim()))) ||
                (cart && lockedAreaCity.getText().toString().trim().length()>=2)) &&
                name.getText().toString().trim().length()>=2 &&
                line1.getText().toString().trim().length()>=2 &&
                line2.getText().toString().trim().length()>=2 &&
                (contactNo.getText().toString().trim().length()>=7 ||
                 contactNo.getText().toString().trim().length()<=12);
    }

    interface IgnoreIdMixin { @JsonIgnore int getId(); }
    private ArrayList<String> addStringAsFirstItem(ArrayList<String> arrayList, String defaultString) {
        ArrayList<String> resultArrayList = new ArrayList<>();
        resultArrayList.add(0,defaultString);
        for(String string: arrayList)
            resultArrayList.add(string);
        return  resultArrayList;
    }
}
