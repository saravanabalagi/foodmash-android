package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

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

    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.empty_orders_layout) LinearLayout emptyOrdersLayout;
    @Bind(R.id.order_history_recycler_view) RecyclerView orderHistoryRecyclerView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.toolbar) Toolbar toolbar;
    private OrderHistoryAdapter orderHistoryAdapter;
    private final Handler handler = new Handler();
    private final Runnable keepRefreshing = new Runnable() {
        @Override
        public void run() {
            onResume();
            handler.postDelayed(this,30000);
        }
    };

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

        orderHistoryAdapter = new OrderHistoryAdapter();
        orderHistoryRecyclerView.hasFixedSize();
        orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryRecyclerView.setAdapter(orderHistoryAdapter);
        orderHistoryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int scrollDy = 0;
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) { super.onScrollStateChanged(recyclerView, newState); }
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollDy += dy;
                swipeRefreshLayout.setEnabled(scrollDy == 0);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
            }
        });
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(keepRefreshing);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(keepRefreshing);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        makeOrderHistoryRequest();
        handler.postDelayed(keepRefreshing,30000);
    }

    private void setStatus (View statusIndicator,
                            View divider,
                            View statusMeter,
                            View statusPurchased,
                            View statusOrdered,
                            View statusDispatched,
                            View statusDelivered,
                            ImageView statusImageView,
                            String status) {
        switch (status) {
            case "purchased":
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.warning_orange));
                statusPurchased.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusOrdered.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_disabled));
                statusDispatched.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_disabled));
                statusDelivered.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_disabled));
                statusImageView.setImageResource(R.drawable.svg_android_tick);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange));
                divider.setVisibility(View.GONE);
                statusMeter.setVisibility(View.VISIBLE);
                break;
            case "ordered":
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.warning_orange));
                statusPurchased.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusOrdered.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusDispatched.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_disabled));
                statusDelivered.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_disabled));
                statusImageView.setImageResource(R.drawable.svg_android_timer);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange));
                divider.setVisibility(View.GONE);
                statusMeter.setVisibility(View.VISIBLE);
                break;
            case "dispatched":
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.warning_orange));
                statusPurchased.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusOrdered.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusDispatched.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusDelivered.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_disabled));
                statusImageView.setImageResource(R.drawable.svg_android_timer);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange));
                divider.setVisibility(View.GONE);
                statusMeter.setVisibility(View.VISIBLE);
                break;
            case "delivered":
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusPurchased.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusOrdered.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusDispatched.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusDelivered.setBackgroundColor(ContextCompat.getColor(this, R.color.okay_green));
                statusImageView.setImageResource(R.drawable.svg_android_double_tick);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                statusMeter.setVisibility(View.GONE);
                divider.setVisibility(View.VISIBLE);
                break;
            case "cancelled":
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
                statusPurchased.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
                statusOrdered.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
                statusDispatched.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
                statusDelivered.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
                statusImageView.setImageResource(R.drawable.svg_android_close);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.accent));
                statusMeter.setVisibility(View.GONE);
                divider.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void makeOrderHistoryRequest() {
        JsonObjectRequest orderHistoryRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_get_order), JsonProvider.getStandardRequestJson(OrderHistoryActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    if (response.getBoolean("success")) {
                        JSONArray ordersJson = response.getJSONArray("data");
                        if(ordersJson.length()==0) {
                            emptyOrdersLayout.setVisibility(View.VISIBLE);
                            orderHistoryRecyclerView.setVisibility(View.GONE);
                            return;
                        }
                        emptyOrdersLayout.setVisibility(View.GONE);
                        orderHistoryRecyclerView.setVisibility(View.VISIBLE);
                        ArrayList<Pair<Date,JSONObject>> orderHistoryArrayList = new ArrayList<>();
                        for (int i=0; i<ordersJson.length(); i++) orderHistoryArrayList.add(new Pair<>(DateUtils.railsDateStringToJavaDate(ordersJson.getJSONObject(i).getString("updated_at")),ordersJson.getJSONObject(i)));
                        Collections.sort(orderHistoryArrayList, new Comparator<Pair<Date, JSONObject>>() {
                            @Override
                            public int compare(Pair<Date, JSONObject> lhs, Pair<Date, JSONObject> rhs) {
                                return rhs.first.compareTo(lhs.first);
                            }
                        });
                        JSONArray orderedJsonArray = new JSONArray();
                        for (Pair<Date,JSONObject> orderHistory: orderHistoryArrayList)
                            orderedJsonArray.put(orderHistory.second);
                        orderHistoryAdapter.setOrdersJsonArray(orderedJsonArray);
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(OrderHistoryActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeOrderHistoryRequest")).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(OrderHistoryActivity.this).addToRequestQueue(orderHistoryRequest);
    }

    class OrderHistoryAdapter extends RecyclerView.Adapter {
        JSONArray ordersJsonArray = new JSONArray();
        public void setOrdersJsonArray(JSONArray ordersJsonArray) { this.ordersJsonArray = ordersJsonArray; notifyDataSetChanged(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.order_id) TextView orderId;
            @Bind(R.id.date) TextView date;
            @Bind(R.id.status) TextView status;
            @Bind(R.id.price) TextView price;
            @Bind(R.id.status_icon) ImageView statusIcon;
            @Bind(R.id.status_indicator) View statusIndicator;
            @Bind(R.id.status_meter) View statusMeter;
            @Bind(R.id.status_purchased) View statusPurchased;
            @Bind(R.id.status_ordered) View statusOrdered;
            @Bind(R.id.status_dispatched) View statusDispatched;
            @Bind(R.id.status_delivered) View statusDelivered;
            @Bind(R.id.divider) View divider;
            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
        @Override public int getItemCount() { return ordersJsonArray.length(); }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(OrderHistoryActivity.this).inflate(R.layout.repeatable_order_history_item,parent,false)); }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                ViewHolder viewHolder = (ViewHolder) holder;
                final JSONObject orderJson = ordersJsonArray.getJSONObject(position);
                viewHolder.orderId.setText(orderJson.getString("order_id"));
                viewHolder.date.setText(DateUtils.railsDateStringToReadableTime(orderJson.getString("updated_at")));
                viewHolder.status.setText(WordUtils.titleize(orderJson.getString("aasm_state")));
                viewHolder.price.setText(orderJson.getString("total"));
                setStatus(viewHolder.statusIndicator,
                        viewHolder.divider,
                        viewHolder.statusMeter,
                        viewHolder.statusPurchased,
                        viewHolder.statusOrdered,
                        viewHolder.statusDispatched,
                        viewHolder.statusDelivered,
                        viewHolder.statusIcon,
                        orderJson.getString("aasm_state"));
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String orderId = "";
                        try { orderId = orderJson.getString("order_id");
                        } catch (JSONException e) { e.printStackTrace(); }
                        Intent intent = new Intent(OrderHistoryActivity.this, OrderDescriptionActivity.class);
                        intent.putExtra("order_id", orderId);
                        startActivity(intent);
                    }
                });
                if(viewHolder.getLayoutPosition() == this.getItemCount()-1) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(10));
                    viewHolder.itemView.setLayoutParams(layoutParams);
                } else {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(0));
                    viewHolder.itemView.setLayoutParams(layoutParams);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

}
