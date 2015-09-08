package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutAddressActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    JSONArray addressesJson;
    int cartId;
    int addressId;
    String payableAmount;

    LinearLayout cart;
    LinearLayout confirm;
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
        setContentView(R.layout.activity_checkout_address);
        cartId = getIntent().getIntExtra("cart_id",0);
        payableAmount = getIntent().getStringExtra("payable_amount");
        if(cartId==0 || payableAmount==null) {
            new AlertDialog.Builder(CheckoutAddressActivity.this)
                    .setCancelable(false)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle("Unable to process your cart")
                    .setMessage("Something went wrong. We are unable to process your request. Try again!")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
            return;
        }

        cart = (LinearLayout) findViewById(R.id.cart); cart.setOnClickListener(this);
        confirm = (LinearLayout) findViewById(R.id.confirm); confirm.setOnClickListener(this);
        addAddress = (LinearLayout) findViewById(R.id.add_address); addAddress.setOnClickListener(this);

        fillLayout = (LinearLayout) findViewById(R.id.fill_layout); fillLayout();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cart: finish(); break;
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); intent.putExtra("cart",true); startActivity(intent); break;
            case R.id.confirm:
                if(isEverythingValid()) makeConfirmRequest();
                else if(getSelectedAddressCount()==0) Alerts.commonErrorAlert(CheckoutAddressActivity.this,"No Address Selected","You have not selected any delivery address. Select one of the listed addresses or add new address to proceed","Okay");
                else if(getSelectedAddressCount()>1) Alerts.validityAlert(CheckoutAddressActivity.this);
                break;
        }
    }

    private JSONObject getConfirmRequestJson() {
        JSONObject requestJson = JsonProvider.getStandartRequestJson(CheckoutAddressActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("id",cartId);
            dataJson.put("delivery_address_id",addressId);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }
    private void makeConfirmRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/addAddress", getConfirmRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        intent = new Intent(CheckoutAddressActivity.this, CheckoutPaymentActivity.class);
                        intent.putExtra("payable_amount",payableAmount);
                        startActivity(intent);
                    } else if(response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(CheckoutAddressActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(CheckoutAddressActivity.this);
                else Alerts.unknownErrorAlert(CheckoutAddressActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(jsonObjectRequest);
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses",JsonProvider.getStandartRequestJson(CheckoutAddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
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
                                    JSONObject requestJson = JsonProvider.getStandartRequestJson(CheckoutAddressActivity.this);
                                    JSONObject dataJson = new JSONObject();
                                    try {
                                        dataJson.put("id", addressJson.getString("id"));
                                        requestJson.put("data", dataJson);
                                    } catch (JSONException e) { e.printStackTrace(); }
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses/destroy", requestJson, new Response.Listener<JSONObject>() {
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
                                            if (error instanceof NoConnectionError || error instanceof TimeoutError)
                                                Alerts.internetConnectionErrorAlert(CheckoutAddressActivity.this);
                                            else Alerts.unknownErrorAlert(CheckoutAddressActivity.this);
                                            System.out.println("Response Error: " + error);
                                        }
                                    });
                                    Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(jsonObjectRequest);
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
                    } else if(!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(CheckoutAddressActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(CheckoutAddressActivity.this);
                else Alerts.unknownErrorAlert(CheckoutAddressActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

}
