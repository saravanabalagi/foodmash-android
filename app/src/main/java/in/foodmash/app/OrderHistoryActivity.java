package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.utils.DateUtils;
import in.foodmash.app.utils.WordUtils;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class OrderHistoryActivity extends AppCompatActivity {

    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private JsonObjectRequest orderHistoryRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        orderHistoryRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/history", JsonProvider.getStandardRequestJson(OrderHistoryActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Animations.fadeOut(loadingLayout,500);
                        Animations.fadeIn(mainLayout, 500);
                        JSONArray ordersJson = response.getJSONArray("data");
                        for(int i=0;i<ordersJson.length();i++) {
                            final JSONObject orderJson = ordersJson.getJSONObject(i);
                            LinearLayout orderLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_order_history_item,fillLayout,false);
                            ((TextView) orderLayout.findViewById(R.id.order_id)).setText(orderJson.getString("order_id"));
                            ((TextView) orderLayout.findViewById(R.id.date)).setText(DateUtils.railsDateToLocalTime(orderJson.getString("updated_at")));
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
                    } else {
                        Alerts.requestUnauthorisedAlert(OrderHistoryActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(OrderHistoryActivity.this).addToRequestQueue(orderHistoryRequest);
                    }
                };
                if (error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(OrderHistoryActivity.this, onClickTryAgain);
                else if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(OrderHistoryActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(OrderHistoryActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(OrderHistoryActivity.this).addToRequestQueue(orderHistoryRequest);

    }

    private void setStatus (ImageView statusImageView, String status) {
        switch (status) {
            case "delivered": statusImageView.setImageResource(R.drawable.svg_tick); statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.okay_green)); break;
            case "cancelled": statusImageView.setImageResource(R.drawable.svg_close); statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.accent)); break;
        }
    }


}
