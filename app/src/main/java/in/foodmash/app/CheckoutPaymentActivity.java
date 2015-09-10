package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutPaymentActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    LinearLayout address;
    LinearLayout pay;
    TextView total;
    String payableAmount;
    String paymentMethod;

    RadioGroup paymentMode;

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
        setContentView(R.layout.activity_checkout_payment);

        payableAmount = getIntent().getStringExtra("payable_amount");
        address = (LinearLayout) findViewById(R.id.address); address.setOnClickListener(this);
        pay = (LinearLayout) findViewById(R.id.pay); pay.setOnClickListener(this);
        total = (TextView) findViewById(R.id.total); total.setText(payableAmount);

        paymentMode = (RadioGroup) findViewById(R.id.payment_mode_radio_group);
        paymentMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.credit_card: paymentMethod="credit_card"; break;
                    case R.id.debit_card:  paymentMethod="debit_card"; break;
                    case R.id.netbanking:  paymentMethod="netbanking"; break;
                    case R.id.cash_on_delivery:  paymentMethod="cash_on_delivery"; break;
                }
            }
        });
        paymentMode.check(R.id.cash_on_delivery);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.address: intent = new Intent(CheckoutPaymentActivity.this,CheckoutPaymentActivity.class); intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent); break;
            case R.id.pay: if(isEverythingValid()) makePaymentRequest(); break;
        }
    }

    private JSONObject getPaymentJson() {
        JSONObject requestJson = JsonProvider.getStandartRequestJson(CheckoutPaymentActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("payment_method",paymentMethod);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makePaymentRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/purchase", getPaymentJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        String orderId = dataJson.getString("order_id");
                        intent = new Intent(CheckoutPaymentActivity.this,OrderDescriptionActivity.class);
                        intent.putExtra("order_id",orderId);
                        intent.putExtra("cart",true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else if(!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(CheckoutPaymentActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(CheckoutPaymentActivity.this);
                else Alerts.unknownErrorAlert(CheckoutPaymentActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private boolean isEverythingValid() {
        return !(paymentMethod.length()<1);
    }
}
