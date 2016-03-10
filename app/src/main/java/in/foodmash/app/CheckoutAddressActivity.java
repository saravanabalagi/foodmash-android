package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.custom.Address;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.City;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutAddressActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.parent_layout) View parentLayout;
    @Bind(R.id.confirm) FloatingActionButton confirm;
    @Bind(R.id.add_address) TextView addAddress;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;

    private Intent intent;
    private List<City> cities;
    private int addressId;
    private List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_address);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        if(Cart.getInstance().getCount()==0) {
            Intent intent = new Intent(CheckoutAddressActivity.this, CartActivity.class);
            intent.putExtra("empty_cart", true);
            startActivity(intent);
            finish();
        }
        confirm.setOnClickListener(this);
        addAddress.setOnClickListener(this);
        makeAddressRequest();

        if(getIntent().getBooleanExtra("total_error",false)) {
            final Snackbar totalErrorSnackbar = Snackbar.make(mainLayout, "Wrong cart value from server!", Snackbar.LENGTH_INDEFINITE);
            totalErrorSnackbar.setAction("Try Again", new View.OnClickListener() { @Override public void onClick(View v) { totalErrorSnackbar.dismiss(); } });
            totalErrorSnackbar.show();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            cities = Arrays.asList(objectMapper.readValue(Info.getCityJsonArrayString(this), City[].class));
        } catch (Exception e) { e.printStackTrace(); }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address:
                intent = new Intent(this, PinYourLocationActivity.class);
                intent.putExtra("cart",true);
                startActivity(intent); break;
            case R.id.confirm:
                if(isEverythingValid()) makeConfirmOrderRequest();
                else if(getSelectedAddressCount()==0) Alerts.commonErrorAlert(CheckoutAddressActivity.this, "No Address Selected", "You have not selected any delivery address. Select one of the listed addresses or add new address to proceed", "Okay");
                else if(getSelectedAddressCount()>1) Alerts.validityAlert(CheckoutAddressActivity.this);
                break;
        }
    }

    private JSONObject getConfirmRequestJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutAddressActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("delivery_address_id",addressId);
            dataJson.put("total",Cart.getInstance().getTotal());
            dataJson.put("vat",Cart.getInstance().getVatForTotal());
            dataJson.put("delivery_charge",Cart.getInstance().getDeliveryCharge());
            dataJson.put("grand_total",Cart.getInstance().getGrandTotal());
            JSONArray cartJsonArray = Cart.getInstance().getCartOrders();
            dataJson.put("cart",cartJsonArray);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }
    private void makeConfirmOrderRequest() {
        JsonObjectRequest confirmOrderRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/addCart", getConfirmRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Json Request", response.toString());
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        intent = new Intent(CheckoutAddressActivity.this, CheckoutPaymentActivity.class);
                        intent.putExtra("payable_amount",response.getJSONObject("data").getDouble("grand_total"));
                        intent.putExtra("order_id",response.getJSONObject("data").getString("order_id"));
                        startActivity(intent);
                    } else {
                        Alerts.requestUnauthorisedAlert(CheckoutAddressActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeConfirmOrderRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(confirmOrderRequest);
    }

    private boolean isEverythingValid() {
        return getSelectedAddressCount()==1;
    }

    private int getSelectedAddressCount() {
        int count = 0;
        for(int i=0;i<fillLayout.getChildCount();i++)
            if(fillLayout.getChildAt(i).findViewById(R.id.selected).getVisibility()==View.VISIBLE)
                count++;
        return count;
    }

    public void makeAddressRequest() {
        JsonObjectRequest addressesRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses",JsonProvider.getStandardRequestJson(CheckoutAddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        fillLayout.removeAllViews();
                        final ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        addresses = Arrays.asList(objectMapper.readValue(response.getJSONArray("data").toString(), Address[].class));
                        for (int i=0;i<addresses.size();i++) {
                            final Address address = addresses.get(i);
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
                                    Intent intent = new Intent(CheckoutAddressActivity.this, PinYourLocationActivity.class);
                                    try { intent.putExtra("json", objectMapper.writeValueAsString(address)); }
                                    catch (Exception e) { e.printStackTrace(); }
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

                                    JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutAddressActivity.this);
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
                                                else if (response.getBoolean("success"))
                                                    Alerts.commonErrorAlert(CheckoutAddressActivity.this, "Could not delete !", "The address that you want to remove could not be removed. Try again!", "Okay");
                                            } catch (JSONException e) { e.printStackTrace(); }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            couldNotDeleteSnackbar.show();
                                        }
                                    });

                                    deletingAddressSnackbar.show();
                                    Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(deleteRequest);
                                }
                            });

                            final int cardinalNumber = i+1;
                            addressLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Snackbar.make(parentLayout, "Address "+cardinalNumber+" Selected", Snackbar.LENGTH_SHORT).show();
                                    addressId = address.getId();
                                    for(int i=0; i<fillLayout.getChildCount(); i++)
                                        fillLayout.getChildAt(i).findViewById(R.id.selected).setVisibility(View.INVISIBLE);
                                    addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
                                }
                            });
                            fillLayout.addView(addressLayout);
                        }
                    } else {
                        Alerts.requestUnauthorisedAlert(CheckoutAddressActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
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
        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(addressesRequest);
    }

}
