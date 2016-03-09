package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
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
import in.foodmash.app.utils.DateUtils;
import in.foodmash.app.utils.NumberUtils;
import in.foodmash.app.utils.WordUtils;


/**
 * Created by Zeke on Aug 08 2015.
 */
public class OrderDescriptionActivity extends AppCompatActivity {

    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.status) TextView status;
    @Bind(R.id.date) TextView date;
    @Bind(R.id.delivery_charges) TextView deliveryCharges;
    @Bind(R.id.total) TextView total;
    @Bind(R.id.vat) TextView vat;
    @Bind(R.id.payable_amount) TextView grandTotal;
    @Bind(R.id.payment_method) TextView paymentMethod;
    @Bind(R.id.status_icon) ImageView statusIcon;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private String orderId;
    private boolean cart;

    private JsonObjectRequest orderDescriptionRequest;
    private ImageLoader imageLoader;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_description);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        imageLoader = Swift.getInstance(OrderDescriptionActivity.this).getImageLoader();
        cart = getIntent().getBooleanExtra("cart", false);
        orderId = getIntent().getStringExtra("order_id");

        orderDescriptionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/history", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Animations.fadeOut(loadingLayout, 500);
                        Animations.fadeIn(mainLayout, 500);
                        Log.i("Json Response", response.toString());
                        JSONArray orderJsonArray = response.getJSONArray("data");
                        JSONObject orderJson = orderJsonArray.getJSONObject(0);
                        grandTotal.setText(String.format("%.2f", Float.parseFloat(orderJson.getString("grand_total"))));
                        paymentMethod.setText(WordUtils.titleize(orderJson.getString("payment_method")));
                        setStatus(statusIcon, orderJson.getString("aasm_state"));
                        date.setText(DateUtils.railsDateToLocalTime(orderJson.getString("updated_at")));
                        status.setText(WordUtils.titleize(orderJson.getString("aasm_state")));
                        total.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("total")));
                        vat.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("vat")));
                        deliveryCharges.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("delivery_charge")));
                        JSONArray subOrdersJson = orderJson.getJSONArray("orders");
                        for(int i=0; i<subOrdersJson.length(); i++) {
                            JSONObject subOrderJson = subOrdersJson.getJSONObject(i);
                            JSONObject productJson = subOrderJson.getJSONObject("product");
                            JSONArray comboDishesJson = subOrderJson.getJSONArray("order_items");
                            String dishes = "";
                            for(int j=0; j<comboDishesJson.length(); j++) {
                                JSONObject comboDishJson = comboDishesJson.getJSONObject(j);
                                JSONObject dishJson = comboDishJson.getJSONObject("item");
                                dishes += dishJson.getString("name") + ((j==comboDishesJson.length()-1)?"":"\n");
                            }
                            LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_order_description_item, fillLayout, false);
                            int quantity = Integer.parseInt(subOrderJson.getString("quantity"));
                            if(quantity > 1) ((TextView) comboLayout.findViewById(R.id.name)).setText(quantity+" x "+productJson.getString("name"));
                            else ((TextView) comboLayout.findViewById(R.id.name)).setText(productJson.getString("name"));
                            ((TextView) comboLayout.findViewById(R.id.amount)).setText(subOrderJson.getString("total"));
                            ((TextView) comboLayout.findViewById(R.id.dishes)).setText(dishes);
                            fillLayout.addView(comboLayout);
                        }
                    } else {
                        Alerts.requestUnauthorisedAlert(OrderDescriptionActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (Exception e) { e.printStackTrace(); Snackbar.make(mainLayout, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(OrderDescriptionActivity.this).addToRequestQueue(orderDescriptionRequest);
                    }
                };
                if (error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(OrderDescriptionActivity.this, onClickTryAgain);
                else if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(OrderDescriptionActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(OrderDescriptionActivity.this);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(OrderDescriptionActivity.this).addToRequestQueue(orderDescriptionRequest);

    }

    @Override
    public void onBackPressed() {
        if(cart) { intent = new Intent(OrderDescriptionActivity.this, MainActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent); }
        else { finish(); }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(OrderDescriptionActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("order_id",orderId);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        Log.i("Json Request", requestJson.toString());
        return requestJson;
    }

    private void setStatus (ImageView statusImageView, String status) {
        switch (status) {
            case "delivered": statusImageView.setImageResource(R.drawable.svg_tick); statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.okay_green)); break;
            case "cancelled": statusImageView.setImageResource(R.drawable.svg_close); statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.accent)); break;
        }
    }
}
