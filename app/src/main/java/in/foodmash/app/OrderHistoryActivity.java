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

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.utils.WordUtils;

/**
 * Created by sarav on Aug 08 2015.
 */
public class OrderHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    LinearLayout back;
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
        setContentView(R.layout.activity_order_history);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/history", JsonProvider.getStandardRequestJson(OrderHistoryActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONArray ordersJson = response.getJSONArray("data");
                        for(int i=0;i<ordersJson.length();i++) {
                            final JSONObject orderJson = ordersJson.getJSONObject(i);
                            LinearLayout orderLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.user_order,fillLayout,false);
                            ((TextView) orderLayout.findViewById(R.id.order_id)).setText(orderJson.getString("order_id"));
                            ((TextView) orderLayout.findViewById(R.id.date)).setText(orderJson.getString("updated_at"));
                            ((TextView) orderLayout.findViewById(R.id.status)).setText(WordUtils.titleize(orderJson.getString("aasm_state")));
                            ((TextView) orderLayout.findViewById(R.id.price)).setText(orderJson.getString("total"));
                            setStatus((ImageView) orderLayout.findViewById(R.id.statusIcon), orderJson.getString("aasm_state"));
                            orderLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String orderId = "";
                                    try { orderId = orderJson.getString("order_id"); }
                                    catch (JSONException e) { e.printStackTrace(); }
                                    Intent intent = new Intent(OrderHistoryActivity.this, OrderDescriptionActivity.class);
                                    intent.putExtra("order_id",orderId);
                                    startActivity(intent);
                                }
                            });
                            fillLayout.addView(orderLayout);
                        }
                    } else if (!response.getBoolean("success")) {
                        Alerts.requestUnauthorisedAlert(OrderHistoryActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError || error instanceof TimeoutError)
                    Alerts.internetConnectionErrorAlert(OrderHistoryActivity.this);
                else Alerts.unknownErrorAlert(OrderHistoryActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(OrderHistoryActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: finish(); break;
        }
    }

    private void setStatus (ImageView statusImageView, String status) {
        switch (status) {
            case "delivered": statusImageView.setImageResource(R.mipmap.tick); statusImageView.setColorFilter(getResources().getColor(R.color.okay_green)); break;
            case "cancelled": statusImageView.setImageResource(R.mipmap.cancel); statusImageView.setColorFilter(getResources().getColor(R.color.color_accent)); break;
        }
    }


}
