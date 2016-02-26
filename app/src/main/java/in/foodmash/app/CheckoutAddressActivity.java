package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cart;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutAddressActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.parent_layout) View parentLayout;
    @Bind(R.id.confirm) FloatingActionButton confirm;
    @Bind(R.id.add_address) TextView addAddress;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private JSONArray addressesJson;
    private int addressId;
    private JsonObjectRequest getAddressesRequest;
    private JsonObjectRequest confirmOrderRequest;
    private JsonObjectRequest deleteRequest;

    private LinearLayout fillLayout;
    private LinearLayout loadingLayout;
    private LinearLayout connectingLayout;
    private ScrollView mainLayout;

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

        confirm.setOnClickListener(this);
        addAddress.setOnClickListener(this);

        loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
        connectingLayout = (LinearLayout) findViewById(R.id.connecting_layout);
        mainLayout = (ScrollView) findViewById(R.id.main_layout);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout); fillLayout();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address:
                intent = new Intent(this, PinYourLocationActivity.class);
                intent.putExtra("cart",true);
                startActivity(intent); break;
            case R.id.confirm:
                if(isEverythingValid()) makeConfirmRequest();
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
            JSONArray cartJsonArray = Cart.getInstance().getCartOrders();
            dataJson.put("cart",cartJsonArray);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }
    private void makeConfirmRequest() {
        confirmOrderRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/addCart", getConfirmRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        intent = new Intent(CheckoutAddressActivity.this, CheckoutPaymentActivity.class);
                        intent.putExtra("payable_amount",response.getJSONObject("data").getDouble("total"));
                        startActivity(intent);
                    } else {
                        Animations.fadeOut(connectingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        Alerts.requestUnauthorisedAlert(CheckoutAddressActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOut(connectingLayout,500);
                Animations.fadeIn(mainLayout,500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(connectingLayout,500);
                        Animations.fadeOut(mainLayout, 500);
                        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(confirmOrderRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(CheckoutAddressActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(CheckoutAddressActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(CheckoutAddressActivity.this);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Animations.fadeIn(connectingLayout,500);
        Animations.fadeOut(mainLayout, 500);
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

    private void fillLayout() {
        getAddressesRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses",JsonProvider.getStandardRequestJson(CheckoutAddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Animations.fadeOut(loadingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        addressesJson = response.getJSONArray("data");
                        for (int i = 0; i < addressesJson.length(); i++) {
                            final JSONObject addressJson = addressesJson.getJSONObject(i);
                            final LinearLayout addressLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_user_address, fillLayout, false);
                            final String address = addressJson.getString("line1") + ",\n" +
                                    addressJson.getString("line2") + ",\n" +
                                    addressJson.getString("area") + ",\n" +
                                    addressJson.getString("city") + " - " +
                                    addressJson.getString("pincode");
                            ((TextView) addressLayout.findViewById(R.id.name)).setText(addressJson.getString("name"));
                            ((TextView) addressLayout.findViewById(R.id.address)).setText(address);
                            ((TextView) addressLayout.findViewById(R.id.phone)).setText(((addressJson.getString("phone").length() == 10) ? "+91 " : "+91 44 ") + addressJson.getString("phone"));
                            if (addressJson.getBoolean("primary")) {
                                addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
                                addressId = addressJson.getInt("id");
                            }
                            addressLayout.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(CheckoutAddressActivity.this, PinYourLocationActivity.class);
                                    intent.putExtra("json", addressJson.toString());
                                    intent.putExtra("edit", true);
                                    startActivity(intent);
                                }
                            });
                            addressLayout.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutAddressActivity.this);
                                    JSONObject dataJson = new JSONObject();
                                    try {
                                        dataJson.put("id", addressJson.getString("id"));
                                        requestJson.put("data", dataJson);
                                    } catch (JSONException e) { e.printStackTrace(); }
                                    deleteRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses/destroy", requestJson, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if (response.getBoolean("success")) {
                                                    fillLayout.removeView(addressLayout);
                                                    fillLayout();
                                                } else if (response.getBoolean("success"))
                                                    Alerts.commonErrorAlert(CheckoutAddressActivity.this, "Could not delete !", "The address that you want to remove could not be removed. Try again!", "Okay");
                                            } catch (JSONException e) { e.printStackTrace(); }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(deleteRequest);
                                                }
                                            };
                                            if (error instanceof TimeoutError) Alerts.timeoutErrorAlert(CheckoutAddressActivity.this, onClickTryAgain);
                                            if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(CheckoutAddressActivity.this, onClickTryAgain);
                                            else Alerts.unknownErrorAlert(CheckoutAddressActivity.this);
                                            Log.e("Json Request Failed", error.toString());
                                        }
                                    });
                                    Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(deleteRequest);
                                }
                            });
                            final int cardinalNumber = i+1;
                            addressLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        Snackbar.make(parentLayout, "Address "+cardinalNumber+" Selected", Snackbar.LENGTH_SHORT).show();
                                        addressId = addressJson.getInt("id");
                                        for(int i=0; i<fillLayout.getChildCount(); i++)
                                            fillLayout.getChildAt(i).findViewById(R.id.selected).setVisibility(View.INVISIBLE);
                                        addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
                                    }
                                    catch (JSONException e) { e.printStackTrace(); }
                                }
                            });
                            fillLayout.addView(addressLayout);
                        }
                    } else {
                        Alerts.requestUnauthorisedAlert(CheckoutAddressActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain =  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(getAddressesRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(CheckoutAddressActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(CheckoutAddressActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(CheckoutAddressActivity.this);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(getAddressesRequest);
    }

}
