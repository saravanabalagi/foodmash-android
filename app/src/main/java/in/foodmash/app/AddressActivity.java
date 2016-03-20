package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.custom.Address;
import in.foodmash.app.custom.City;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class AddressActivity extends FoodmashActivity implements View.OnClickListener {

    @Bind(R.id.add_address) FloatingActionButton addAddress;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.empty_address_layout) LinearLayout emptyAddressLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private List<Address> addresses;
    private ObjectMapper objectMapper;
    private List<City> cities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Mash","addresses");

        try {
            objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            cities = Arrays.asList(objectMapper.readValue(Info.getCityJsonArrayString(this), City[].class));
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        addAddress.setOnClickListener(this);
        makeAddressRequest();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); startActivity(intent); break;
        }
    }

    public void makeAddressRequest() {
        JsonObjectRequest getAddressesRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses", JsonProvider.getStandardRequestJson(AddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        fillLayout.removeAllViews();
                        final ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        addresses = Arrays.asList(objectMapper.readValue(response.getJSONArray("data").toString(), Address[].class));
                        if(addresses.size()==0) { emptyAddressLayout.setVisibility(View.VISIBLE); fillLayout.setVisibility(View.GONE); return; }
                        else { fillLayout.setVisibility(View.VISIBLE); emptyAddressLayout.setVisibility(View.GONE); }
                        for (final Address address: addresses) {
                            final LinearLayout addressLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_user_address, fillLayout, false);
                            ((TextView) addressLayout.findViewById(R.id.name)).setText(address.getName());
                            ((TextView) addressLayout.findViewById(R.id.line1)).setText(address.getLine1());
                            ((TextView) addressLayout.findViewById(R.id.line2)).setText(address.getLine2());
                            ((TextView) addressLayout.findViewById(R.id.contact_no)).setText(address.getContactNo());

                            int areaId = address.getAreaId();
                            int cityPos = -1;
                            for(int j=0;j<cities.size();j++) if (cities.get(j).indexOf(areaId)!=-1) cityPos = j;
                            int areaPos = cities.get(cityPos).indexOf(areaId);
                            String city = cities.get(cityPos).getName();
                            String area = cities.get(cityPos).getAreas().get(areaPos).getName();
                            String areaCity = area + ", " + city;
                            ((TextView) addressLayout.findViewById(R.id.area_city)).setText(areaCity);

                            addressLayout.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(AddressActivity.this, PinYourLocationActivity.class);
                                    try { intent.putExtra("json", objectMapper.writeValueAsString(address)); }
                                    catch (Exception e) { Actions.handleIgnorableException(AddressActivity.this,e); }
                                    intent.putExtra("edit", true);
                                    startActivity(intent);
                                }
                            });

                            addressLayout.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Snackbar deletingAddressSnackbar = Snackbar.make(mainLayout,"Deleting address...",Snackbar.LENGTH_INDEFINITE);
                                    final Snackbar couldNotDeleteSnackbar = Snackbar.make(mainLayout,"Could not delete",Snackbar.LENGTH_INDEFINITE);
                                    couldNotDeleteSnackbar.setAction("Try Again", new View.OnClickListener() { @Override public void onClick(View v) { couldNotDeleteSnackbar.dismiss(); } });

                                    JSONObject requestJson = JsonProvider.getStandardRequestJson(AddressActivity.this);
                                    JSONObject dataJson = new JSONObject();
                                    try {
                                        dataJson.put("id", address.getId());
                                        requestJson.put("data", dataJson);
                                    } catch (JSONException e) { e.printStackTrace(); }
                                    JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses/destroy", requestJson, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            if(deletingAddressSnackbar.isShown()) deletingAddressSnackbar.dismiss();
                                            try {
                                                if (response.getBoolean("success")) fillLayout.removeView(addressLayout);
                                                else if (response.getBoolean("success")) Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                                            } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(AddressActivity.this,e); }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            couldNotDeleteSnackbar.show();
                                        }
                                    });

                                    deletingAddressSnackbar.show();
                                    Swift.getInstance(AddressActivity.this).addToRequestQueue(deleteRequest);
                                }
                            });
                            fillLayout.addView(addressLayout);
                        }
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { Actions.handleIgnorableException(AddressActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeAddressRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(AddressActivity.this).addToRequestQueue(getAddressesRequest);
    }

}
