package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    android.os.Handler handler=new android.os.Handler();

    LinearLayout back;
    LinearLayout buy;
    LinearLayout fillLayout;
    LinearLayout emptyCartLayout;
    int cartId;

    TextView total;
    TouchableImageButton clearCart;

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
        setContentView(R.layout.activity_cart);

        total = (TextView) findViewById(R.id.total);
        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        buy = (LinearLayout) findViewById(R.id.buy); buy.setOnClickListener(this);
        clearCart = (TouchableImageButton) findViewById(R.id.clear_cart); clearCart.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        emptyCartLayout = (LinearLayout) findViewById(R.id.empty_cart_layout);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts", JsonProvider.getStandartRequestJson(CartActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        cartId = dataJson.getInt("id");
                        total.setText(dataJson.getString("total"));
                        JSONArray subOrdersJson = dataJson.getJSONArray("orders");
                        if(subOrdersJson.length()>0) emptyCartLayout.setVisibility(View.GONE);
                        for(int i=0;i<subOrdersJson.length();i++){
                            final JSONObject subOrderJson = subOrdersJson.getJSONObject(i);
                            JSONArray comboDishesJson = subOrderJson.getJSONArray("order_items");
                            String dishes = "";
                            for(int j=0; j<comboDishesJson.length(); j++) {
                                JSONObject comboDishJson = comboDishesJson.getJSONObject(j);
                                JSONObject dishJson = comboDishJson.getJSONObject("item");
                                dishes += dishJson.getString("name") + ((j==comboDishesJson.length()-1)?"":",  ");
                            }
                            JSONObject productJson = subOrderJson.getJSONObject("product");
                            final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.cart_combo, fillLayout, false);
                            ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                            ((TextView) comboLayout.findViewById(R.id.name)).setText(productJson.getString("name"));
                            ((TextView) comboLayout.findViewById(R.id.dishes)).setText(dishes);
                            ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText(subOrderJson.getString("quantity"));
                            final TextView price = (TextView) comboLayout.findViewById(R.id.price); price.setText(productJson.getString("price"));
                            final EditText quantity = (EditText) comboLayout.findViewById(R.id.quantity); quantity.setText(subOrderJson.getString("quantity")); quantity.addTextChangedListener(new TextWatcher() {
                                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(s.toString().equals("0")) quantity.setText("");
                                    if (s.length() > 0 && Calculations.isInteger(s.toString())) {
                                        JSONObject requestJson = JsonProvider.getStandartRequestJson(CartActivity.this);
                                        try {
                                            JSONObject dataJson = new JSONObject();
                                            JSONObject orderJson = new JSONObject();
                                            orderJson.put("id",subOrderJson.getInt("id"));
                                            orderJson.put("quantity",Integer.parseInt(s.toString()));
                                            dataJson.put("order",orderJson);
                                            requestJson.put("data",dataJson);
                                        } catch (JSONException e) { e.printStackTrace(); }
                                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, getString(R.string.api_root_path) + "/orders", requestJson, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    if (response.getBoolean("success")) {
                                                        JSONObject dataJson = response.getJSONObject("data");
                                                        total.setText(dataJson.getString("total"));
                                                        JSONObject orderJson = dataJson.getJSONObject("order");
                                                        ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText(orderJson.getString("quantity"));
                                                        ((TextView) comboLayout.findViewById(R.id.amount)).setText(orderJson.getString("total"));
                                                    } else if (response.getBoolean("success")) {
                                                        Alerts.unableToProcessResponseAlert(CartActivity.this);
                                                        System.out.println(response.getString("error"));
                                                    }
                                                } catch (JSONException e) { e.printStackTrace(); }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                if (error instanceof NoConnectionError || error instanceof TimeoutError)
                                                    Alerts.internetConnectionErrorAlert(CartActivity.this);
                                                else Alerts.unknownErrorAlert(CartActivity.this);
                                                System.out.println("Response Error: " + error);
                                            }
                                        });
                                        Swift.getInstance(CartActivity.this).addToRequestQueue(jsonObjectRequest);
                                    }
                                }
                            });
                            quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if(!hasFocus)
                                        if(quantity.getText().length()<1) {
                                            Alerts.commonErrorAlert(CartActivity.this,"Empty Quantity","Quantity field cannot be empty","Okay");
                                            quantity.setText(((TextView)comboLayout.findViewById(R.id.quantity_display)).getText().toString());
                                            quantity.requestFocus();
                                        }
                                }
                            });
                            ((TextView) comboLayout.findViewById(R.id.amount)).setText(subOrderJson.getString("total"));
                            comboLayout.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    JSONObject requestJson = JsonProvider.getStandartRequestJson(CartActivity.this);
                                    try {
                                        JSONObject dataJson = new JSONObject();
                                        JSONObject orderJson = new JSONObject();
                                        orderJson.put("id", subOrderJson.getInt("id"));
                                        dataJson.put("order", orderJson);
                                        requestJson.put("data", dataJson);
                                    } catch (JSONException e) { e.printStackTrace(); }
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/orders/destroy", requestJson, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if(response.getBoolean("success")) {
                                                    JSONObject dataJson = response.getJSONObject("data");
                                                    total.setText(dataJson.getString("total"));
                                                    fillLayout.removeView(comboLayout);
                                                    if(fillLayout.getChildCount()==0)
                                                        Animations.fadeIn(emptyCartLayout,500);
                                                } else if(response.getBoolean("success")) {
                                                    Alerts.unableToProcessResponseAlert(CartActivity.this);
                                                    System.out.println(response.getString("error"));
                                                }
                                            } catch (JSONException e) { e.printStackTrace(); }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(CartActivity.this);
                                            else Alerts.unknownErrorAlert(CartActivity.this);
                                            System.out.println("Response Error: " + error);
                                        }
                                    });
                                    Swift.getInstance(CartActivity.this).addToRequestQueue(jsonObjectRequest);
                                }
                            });

                            fillLayout.addView(comboLayout);
                        }
                    } else if(response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(CartActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(CartActivity.this);
                else Alerts.unknownErrorAlert(CartActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(CartActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_cart:
                new AlertDialog.Builder(CartActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Remove all from cart ?")
                        .setMessage("Do you want to remove all combos added to the cart?")
                        .setPositiveButton("Remove All", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/destroy", JsonProvider.getStandartRequestJson(CartActivity.this),new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            if(response.getBoolean("success")) {
                                                for (int i = 0; i < fillLayout.getChildCount(); i++) {
                                                    handler.postDelayed(new Runnable() {
                                                        @Override public void run() {
                                                            fillLayout.removeViewAt(0); } }, i*500);
                                                }
                                                total.setText("0");
                                                Animations.fadeIn(emptyCartLayout,500);
                                            } else if(response.getBoolean("success")) {
                                                Alerts.unableToProcessResponseAlert(CartActivity.this);
                                                System.out.println(response.getString("error"));
                                            }
                                        } catch (JSONException e) { e.printStackTrace(); }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(CartActivity.this);
                                        else Alerts.unknownErrorAlert(CartActivity.this);
                                        System.out.println("Response Error: " + error);
                                    }
                                });
                                Swift.getInstance(CartActivity.this).addToRequestQueue(jsonObjectRequest);
                            }
                        }).setNegativeButton("No, don't remove", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { }
                }).show(); break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.buy:
                if(isCartEmpty()) Alerts.commonErrorAlert(CartActivity.this,"Empty Cart","Your cart is empty. Add some combos and we'll proceed!","Okay");
                else if(isEverythingValid()) {
                    intent = new Intent(this, CheckoutAddressActivity.class);
                    intent.putExtra("cart_id",cartId);
                    intent.putExtra("payable_amount", String.format("%.2f",Float.parseFloat(total.getText().toString())));
                    startActivity(intent);
                } else {
                    Alerts.validityAlert(CartActivity.this);
                    for(int i=0;i<fillLayout.getChildCount();i++) {
                        LinearLayout linearLayout = (LinearLayout) fillLayout.getChildAt(i);
                        ((EditText) linearLayout.findViewById(R.id.quantity)).setText(((TextView)linearLayout.findViewById(R.id.quantity_display)).getText().toString());
                    }
                }
                break;
        }
    }

    private boolean isCartEmpty() { return fillLayout.getChildCount()==0; }
    private boolean isEverythingValid() {
        boolean valid = true;
        for(int i=0;i<fillLayout.getChildCount();i++) {
            LinearLayout linearLayout = (LinearLayout) fillLayout.getChildAt(i);
            if(!(((EditText) linearLayout.findViewById(R.id.quantity)).getText().toString().equals(((TextView) linearLayout.findViewById(R.id.quantity_display)).getText().toString()))) valid=false;
        }
        return valid;
    }

}
