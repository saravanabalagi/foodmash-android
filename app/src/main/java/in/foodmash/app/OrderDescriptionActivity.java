package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
 * Created by sarav on Aug 08 2015.
 */
public class OrderDescriptionActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    String orderId;
    boolean cart;

    TextView status;
    TextView date;
    TextView total;
    TextView paymentMethod;
    ImageView statusIcon;
    LinearLayout orderHistory;
    LinearLayout fillLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_log_out: intent = new Intent(this,LoginActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_description);

        cart = getIntent().getBooleanExtra("cart", false);
        orderId = getIntent().getStringExtra("order_id");
        orderHistory = (LinearLayout) findViewById(R.id.order_history); orderHistory.setOnClickListener(this);
        status = (TextView) findViewById(R.id.status);
        date = (TextView) findViewById(R.id.date);
        total = (TextView) findViewById(R.id.total);
        paymentMethod = (TextView) findViewById(R.id.payment_method);
        statusIcon = (ImageView) findViewById(R.id.status_icon);

        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/show", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONObject orderJson = response.getJSONObject("data");
                        total.setText(orderJson.getString("total"));
                        paymentMethod.setText(orderJson.getString("payment_method"));
                        JSONArray subOrdersJson = orderJson.getJSONArray("orders");
                        for(int i=0; i<subOrdersJson.length(); i++) {
                            JSONObject subOrderJson = subOrdersJson.getJSONObject(i);
                            JSONObject productJson = subOrderJson.getJSONObject("product");
                            JSONArray comboDishesJson = subOrderJson.getJSONArray("order_items");
                            String dishes = "";
                            for(int j=0; j<comboDishesJson.length(); j++) {
                                JSONObject comboDishJson = comboDishesJson.getJSONObject(j);
                                JSONObject dishJson = comboDishJson.getJSONObject("item");
                                dishes += dishJson.getString("name") + ((j==comboDishesJson.length()-1)?"":",  ");
                            }
                            LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.order_combo, fillLayout, false);
                            ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                            ((TextView) comboLayout.findViewById(R.id.name)).setText(productJson.getString("name"));
                            TextView price = (TextView) comboLayout.findViewById(R.id.price); price.setText(productJson.getString("price"));
                            TextView quantity = (TextView) comboLayout.findViewById(R.id.quantity); quantity.setText(subOrderJson.getString("quantity"));
                            ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText(subOrderJson.getString("quantity"));
                            ((TextView) comboLayout.findViewById(R.id.amount)).setText(subOrderJson.getString("total"));
                            ((TextView) comboLayout.findViewById(R.id.dishes)).setText(dishes);
                            fillLayout.addView(comboLayout);
                        }
                    } else if (!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(OrderDescriptionActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError || error instanceof TimeoutError)
                    Alerts.internetConnectionErrorAlert(OrderDescriptionActivity.this);
                else Alerts.unknownErrorAlert(OrderDescriptionActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(OrderDescriptionActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    @Override
    public void onBackPressed() {
        if(cart) { intent = new Intent(OrderDescriptionActivity.this,MainActivity.class); startActivity(intent); }
        else super.onBackPressed();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order_history: if(cart) { intent = new Intent(OrderDescriptionActivity.this,OrderHistoryActivity.class); startActivity(intent); } else finish(); break;
            case R.id.home: intent = new Intent(OrderDescriptionActivity.this,MainActivity.class); startActivity(intent); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = JsonProvider.getStandartRequestJson(OrderDescriptionActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("order_id",orderId);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

}
