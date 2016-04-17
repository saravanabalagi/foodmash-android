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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.utils.DateUtils;
import in.foodmash.app.utils.NumberUtils;
import in.foodmash.app.utils.WordUtils;


/**
 * Created by Zeke on Aug 08 2015.
 */
public class OrderDescriptionActivity extends FoodmashActivity {

    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.order_description_recycler_view) RecyclerView orderDescriptionRecyclerView;
    @Bind(R.id.promo_discount_layout) LinearLayout promoDiscountLayout;
    @Bind(R.id.promo_discount) TextView promoDiscount;
    @Bind(R.id.vat_percentage) TextView vatPercentage;
    @Bind(R.id.promo_code) TextView promoCode;
    @Bind(R.id.status) TextView status;
    @Bind(R.id.date) TextView date;
    @Bind(R.id.delivery_charges) TextView deliveryCharges;
    @Bind(R.id.total) TextView total;
    @Bind(R.id.vat) TextView vat;
    @Bind(R.id.payable_amount) TextView grandTotal;
    @Bind(R.id.payment_method) TextView paymentMethod;
    @Bind(R.id.status_icon) ImageView statusIcon;
    @Bind(R.id.placed) ImageView purchased;
    @Bind(R.id.aggregated) ImageView ordered;
    @Bind(R.id.dispatched) ImageView dispatched;
    @Bind(R.id.delivered) ImageView delivered;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private String orderId;
    private String aasmState;
    private boolean cart;
    private OrderDescriptionAdapter orderDescriptionAdapter;
    private final Handler refreshHandler = new Handler();
    private final Runnable keepRefreshing = new Runnable() {
        @Override
        public void run() {
            onResume();
            if(aasmState.equals("delivered") || aasmState.equals("cancelled")) refreshHandler.removeCallbacks(this);
            else refreshHandler.postDelayed(this,30000);
        }
    };

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
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Order","contents");

        cart = getIntent().getBooleanExtra("cart", false);
        orderId = getIntent().getStringExtra("order_id");

        orderDescriptionAdapter = new OrderDescriptionAdapter();
        orderDescriptionRecyclerView.hasFixedSize();
        orderDescriptionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderDescriptionRecyclerView.setAdapter(orderDescriptionAdapter);
        orderDescriptionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int scrollDy = 0;
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) { super.onScrollStateChanged(recyclerView, newState); }
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollDy += dy;
                swipeRefreshLayout.setEnabled(scrollDy==0);
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
        refreshHandler.removeCallbacks(keepRefreshing);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        makeOrderDescriptionRequest();
        refreshHandler.postDelayed(keepRefreshing, 30000);
    }

    @Override
    protected void onPause() {
        refreshHandler.removeCallbacks(keepRefreshing);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(cart) {
            Intent intent = new Intent(OrderDescriptionActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent); }
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

    public void makeOrderDescriptionRequest() {
        JsonObjectRequest orderDescriptionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/history", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    if (response.getBoolean("success")) {
                        Log.i("Json Response", response.toString());
                        JSONArray orderJsonArray = response.getJSONArray("data");
                        JSONObject orderJson = orderJsonArray.getJSONObject(0);
                        grandTotal.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("grand_total")));
                        grandTotal.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("grand_total")));
                        paymentMethod.setText(WordUtils.titleize(orderJson.getString("payment_method")));
                        if(orderJson.has("promo_discount")) {
                            if(orderJson.has("promo_code")) promoCode.setText(orderJson.getString("promo_code"));
                            promoDiscount.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("promo_discount")));
                            promoDiscountLayout.setVisibility(View.VISIBLE);
                        }
                        setStatus(statusIcon, orderJson.getString("aasm_state"));
                        date.setText(DateUtils.railsDateStringToReadableTime(orderJson.getString("updated_at")));
                        status.setText(WordUtils.titleize(orderJson.getString("aasm_state")));
                        total.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("total")));
                        if(orderJson.has("vat_percentage")) vatPercentage.setText(orderJson.getString("vat_percentage"));
                        vat.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("vat")));
                        deliveryCharges.setText(NumberUtils.getCurrencyFormat(orderJson.getDouble("delivery_charge")));
                        orderDescriptionAdapter.setJsonArray(orderJson.getJSONArray("orders"));
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { Actions.handleIgnorableException(OrderDescriptionActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeOrderDescriptionRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(OrderDescriptionActivity.this).addToRequestQueue(orderDescriptionRequest);
    }

    private void setStatus (ImageView statusImageView, String status) {
        this.aasmState = status;
        switch (status) {
            case "purchased":
                statusImageView.setImageResource(R.drawable.svg_android_tick);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange));
                purchased.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                ordered.setColorFilter(ContextCompat.getColor(this, R.color.grey_disabled));
                dispatched.setColorFilter(ContextCompat.getColor(this, R.color.grey_disabled));
                delivered.setColorFilter(ContextCompat.getColor(this, R.color.grey_disabled));
                break;
            case "ordered":
                statusImageView.setImageResource(R.drawable.svg_android_timer);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange));
                purchased.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                ordered.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                dispatched.setColorFilter(ContextCompat.getColor(this, R.color.grey_disabled));
                delivered.setColorFilter(ContextCompat.getColor(this, R.color.grey_disabled));
                break;
            case "dispatched":
                statusImageView.setImageResource(R.drawable.svg_android_timer);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange));
                purchased.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                ordered.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                dispatched.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                delivered.setColorFilter(ContextCompat.getColor(this, R.color.grey_disabled));
                break;
            case "delivered":
                statusImageView.setImageResource(R.drawable.svg_android_double_tick);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                purchased.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                ordered.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                dispatched.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                delivered.setColorFilter(ContextCompat.getColor(this, R.color.okay_green));
                break;
            case "cancelled":
                statusImageView.setImageResource(R.drawable.svg_android_close);
                statusImageView.setColorFilter(ContextCompat.getColor(this, R.color.accent));
                purchased.setColorFilter(ContextCompat.getColor(this, R.color.accent));
                ordered.setColorFilter(ContextCompat.getColor(this, R.color.accent));
                dispatched.setColorFilter(ContextCompat.getColor(this, R.color.accent));
                delivered.setColorFilter(ContextCompat.getColor(this, R.color.accent));
                break;
        }
    }

    class OrderDescriptionAdapter extends RecyclerView.Adapter {
        JSONArray subOrdersJson = new JSONArray();
        void setJsonArray(JSONArray subOrdersJson) { this.subOrdersJson = subOrdersJson; notifyDataSetChanged(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.name) TextView name;
            @Bind(R.id.amount) TextView amount;
            @Bind(R.id.dishes) TextView dishes;
            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
        @Override public int getItemCount() { return subOrdersJson.length(); }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(OrderDescriptionActivity.this).inflate(R.layout.repeatable_order_description_item,parent,false)); }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                ViewHolder viewHolder = (ViewHolder) holder;
                JSONObject subOrderJson = subOrdersJson.getJSONObject(position);
                JSONObject productJson = subOrderJson.getJSONObject("product");
                JSONArray comboDishesJson = subOrderJson.getJSONArray("order_items");
                String dishes = "";
                for(int j=0; j<comboDishesJson.length(); j++) {
                    JSONObject comboDishJson = comboDishesJson.getJSONObject(j);
                    JSONObject dishJson = comboDishJson.getJSONObject("item");
                    dishes += dishJson.getString("name") + ((j==comboDishesJson.length()-1)?"":"\n");
                }
                int quantity = Integer.parseInt(subOrderJson.getString("quantity"));
                if(quantity > 1) viewHolder.name.setText(quantity+" x "+productJson.getString("name"));
                else ((TextView) viewHolder.name.findViewById(R.id.name)).setText(productJson.getString("name"));
                viewHolder.amount.setText(subOrderJson.getString("total"));
                viewHolder.dishes.setText(dishes);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
