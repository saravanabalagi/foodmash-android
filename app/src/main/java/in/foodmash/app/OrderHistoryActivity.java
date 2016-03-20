package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.utils.DateUtils;
import in.foodmash.app.utils.WordUtils;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class OrderHistoryActivity extends FoodmashActivity {

    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Order","history");

        makeOrderHistoryRequest();

    }

    private void setStatus (ImageView statusImageView, String status) {
        switch (status) {
            case "delivered": statusImageView.setImageResource(R.drawable.svg_tick_filled); statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.okay_green)); break;
            case "cancelled": statusImageView.setImageResource(R.drawable.svg_close_filled); statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.accent)); break;
            default: statusImageView.setImageResource(R.drawable.svg_circle_filled); statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange)); break;
        }
    }

    public void makeOrderHistoryRequest() {
        JsonObjectRequest orderHistoryRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/history", JsonProvider.getStandardRequestJson(OrderHistoryActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if (response.getBoolean("success")) {
                        JSONArray ordersJson = response.getJSONArray("data");
                        TreeMap<Date, LinearLayout> orderHistoryTreeMap = new TreeMap<>();
                        for(int i=0;i<ordersJson.length();i++) {
                            final JSONObject orderJson = ordersJson.getJSONObject(i);
                            LinearLayout orderLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_order_history_item,fillLayout,false);
                            ((TextView) orderLayout.findViewById(R.id.order_id)).setText(orderJson.getString("order_id"));
                            ((TextView) orderLayout.findViewById(R.id.date)).setText(DateUtils.railsDateStringToReadableTime(orderJson.getString("updated_at")));
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
                            orderHistoryTreeMap.put(DateUtils.railsDateStringToJavaDate(orderJson.getString("updated_at")), orderLayout);
                        }
                        for (Date key: orderHistoryTreeMap.descendingKeySet())
                            fillLayout.addView(orderHistoryTreeMap.get(key));
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(OrderHistoryActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeOrderHistoryRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(OrderHistoryActivity.this).addToRequestQueue(orderHistoryRequest);
    }


}
