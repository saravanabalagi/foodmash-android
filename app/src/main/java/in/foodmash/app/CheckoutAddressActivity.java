package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cart;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutAddressActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private JSONArray addressesJson;
    private int addressId;
    private JsonObjectRequest getAddressesRequest;
    private JsonObjectRequest confirmOrderRequest;
    private JsonObjectRequest deleteRequest;

    private LinearLayout cart;
    private LinearLayout confirm;
    private LinearLayout addAddress;
    private LinearLayout fillLayout;
    private LinearLayout loadingLayout;
    private LinearLayout connectingLayout;
    private ScrollView mainLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        TextView cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(CheckoutAddressActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_addresses) { intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_order_history) { intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_contact_us) { intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_log_out) { Actions.logout(CheckoutAddressActivity.this); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_address);

        cart = (LinearLayout) findViewById(R.id.cart); cart.setOnClickListener(this);
        confirm = (LinearLayout) findViewById(R.id.confirm); confirm.setOnClickListener(this);
        addAddress = (LinearLayout) findViewById(R.id.add_address); addAddress.setOnClickListener(this);

        loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
        connectingLayout = (LinearLayout) findViewById(R.id.connecting_layout);
        mainLayout = (ScrollView) findViewById(R.id.main_layout);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout); fillLayout();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cart: finish(); break;
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); intent.putExtra("cart",true); startActivity(intent); break;
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
        System.out.println(requestJson);
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
                        System.out.println(response.getString("error"));
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
                System.out.println("Response Error: " + error);
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
                            JSONObject addressDetailsJson = addressJson.getJSONObject("address");
                            final LinearLayout addressLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.user_address, fillLayout, false);
                            final String address = addressDetailsJson.getString("line1") + ",\n" +
                                    addressDetailsJson.getString("line2") + ",\n" +
                                    addressDetailsJson.getString("area") + ",\n" +
                                    addressDetailsJson.getString("city") + " - " +
                                    addressDetailsJson.getString("pincode");
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
                                            System.out.println("Response Error: " + error);
                                        }
                                    });
                                    Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(deleteRequest);
                                }
                            });
                            addressLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
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
                        System.out.println(response.getString("error"));
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
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(getAddressesRequest);
    }

}
