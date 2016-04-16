package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
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
    @Bind(R.id.order_history_recycler_view) RecyclerView orderHistoryRecylerView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.toolbar) Toolbar toolbar;
    private OrderHistoryAdapter orderHistoryAdapter;

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
        orderHistoryRecylerView.hasFixedSize();
        orderHistoryRecylerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryRecylerView.setAdapter(orderHistoryAdapter);
        orderHistoryRecylerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
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
                swipeRefreshLayout.setRefreshing(false);
                try {
                    if (response.getBoolean("success")) {
                        JSONArray ordersJson = response.getJSONArray("data");
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeOrderHistoryRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
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
                setStatus(viewHolder.statusIcon, orderJson.getString("aasm_state"));
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
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

}
