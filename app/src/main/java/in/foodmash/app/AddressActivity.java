package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Address;
import in.foodmash.app.custom.City;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class AddressActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.add_address) FloatingActionButton addAddress;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.connecting_layout) LinearLayout connectingLayout;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private List<Address> addresses;
    private List<City> cities;
    private JSONArray jsonArray;
    private JsonObjectRequest deleteRequest;
    private JsonObjectRequest getAddressesRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        addAddress.setOnClickListener(this);
        fillLayout();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); startActivity(intent); break;
        }
    }

    private void fillLayout() {
        fillLayout.removeAllViews();
        getAddressesRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses", JsonProvider.getStandardRequestJson(AddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Animations.fadeOut(loadingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        final ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        addresses = Arrays.asList(objectMapper.readValue(response.getJSONArray("data").toString(), Address[].class));
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
                                    catch (Exception e) { e.printStackTrace(); }
                                    intent.putExtra("edit", true);
                                    startActivity(intent);
                                }
                            });

                            addressLayout.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    JSONObject requestJson = JsonProvider.getStandardRequestJson(AddressActivity.this);
                                    JSONObject dataJson = new JSONObject();
                                    try {
                                        dataJson.put("id", address.getId());
                                        requestJson.put("data", dataJson);
                                    } catch (JSONException e) { e.printStackTrace(); }
                                    deleteRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses/destroy", requestJson, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if (response.getBoolean("success")) fillLayout.removeView(addressLayout);
                                                else if (response.getBoolean("success"))
                                                    Alerts.commonErrorAlert(AddressActivity.this, "Could not delete !", "The address that you want to remove could not be removed. Try again!", "Okay");
                                            } catch (JSONException e) { e.printStackTrace(); }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Swift.getInstance(AddressActivity.this).addToRequestQueue(deleteRequest);
                                                }
                                            };
                                            if (error instanceof TimeoutError) Alerts.timeoutErrorAlert(AddressActivity.this, onClickTryAgain);
                                            if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(AddressActivity.this, onClickTryAgain);
                                            else Alerts.unknownErrorAlert(AddressActivity.this);
                                            Log.e("Json Request Failed", error.toString());
                                        }
                                    });
                                    Swift.getInstance(AddressActivity.this).addToRequestQueue(deleteRequest);
                                }
                            });
                            fillLayout.addView(addressLayout);
                        }
                    } else {
                        Alerts.requestUnauthorisedAlert(AddressActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(AddressActivity.this).addToRequestQueue(getAddressesRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(AddressActivity.this, onClickTryAgain);
                if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(AddressActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(AddressActivity.this);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(AddressActivity.this).addToRequestQueue(getAddressesRequest);
    }

}
