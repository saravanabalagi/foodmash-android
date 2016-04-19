package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.models.Address;
import in.foodmash.app.models.Cart;
import in.foodmash.app.models.City;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutAddressActivity extends FoodmashActivity implements View.OnClickListener {

    @Bind(R.id.confirm) FloatingActionButton confirm;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.add_address) TextView addAddress;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.address_recycler_view) RecyclerView addressRecyclerView;
    @Bind(R.id.choose_address) LinearLayout chooseAddressLayout;
    @Bind(R.id.address_progress) LinearLayout addressProgress;
    @Bind(R.id.empty_address_layout) LinearLayout emptyAddressLayout;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;

    private Intent intent;
    private List<City> cities;
    private int addressId = -1;
    private List<Address> addresses = new ArrayList<>();
    private AddressAdapter addressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_address);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Select","address");

        confirm.setOnClickListener(this);
        addAddress.setOnClickListener(this);
        addressAdapter = new AddressAdapter();
        addressRecyclerView.hasFixedSize();
        addressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressRecyclerView.setAdapter(addressAdapter);
        addressRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        swipeRefreshLayout.setEnabled(false);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            cities = Arrays.asList(objectMapper.readValue(Info.getCityJsonArrayString(this), City[].class));
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }

    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);

        if(Cart.getInstance().getCount()==0) {
            Intent intent = new Intent(CheckoutAddressActivity.this, CartActivity.class);
            intent.putExtra("empty_cart", true);
            startActivity(intent);
            finish();
        }
        makeAddressRequest();

        if(getIntent().getBooleanExtra("total_error",false)) {
            final Snackbar totalErrorSnackbar = Snackbar.make(mainLayout, "Wrong cart value from server!", Snackbar.LENGTH_INDEFINITE);
            totalErrorSnackbar.setAction("Try Again", new View.OnClickListener() { @Override public void onClick(View v) { totalErrorSnackbar.dismiss(); } });
            totalErrorSnackbar.show();
        }

        if(getIntent().getBooleanExtra("order_id_error",false)) {
            Intent intent = new Intent(CheckoutAddressActivity.this, CartActivity.class);
            intent.putExtra("order_id_error", true);
            startActivity(intent);
            finish();
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address:
                intent = new Intent(this, PinYourLocationActivity.class);
                intent.putExtra("cart",true);
                startActivity(intent); break;
            case R.id.confirm:
                if(isEverythingValid()) makeConfirmOrderRequest();
                else Snackbar.make(mainLayout,"Select an address to deliver",Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    private JSONObject getConfirmRequestJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutAddressActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("delivery_address_id",addressId);
            dataJson.put("total",Cart.getInstance().getTotal());
            JSONArray cartJsonArray = Cart.getInstance().getCartOrders();
            dataJson.put("cart",cartJsonArray);
            requestJson.put("data",dataJson);
            Log.i("Json Request", dataJson.toString());
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private JSONObject getComboRequestJson() {
        JSONObject comboRequestJson;
        if(Info.isLoggedIn(this)) comboRequestJson = JsonProvider.getStandardRequestJson(this);
        else comboRequestJson = JsonProvider.getAnonymousRequestJson(this);
        int packagingCentreId = Info.getPackagingCentreId(this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("packaging_centre_id",packagingCentreId);
            comboRequestJson.put("data", dataJson);
        }
        catch (Exception e) { Actions.handleIgnorableException(this,e); }
        return comboRequestJson;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void makeConfirmOrderRequest() {
        JsonObjectRequest getCombosRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/combos", getComboRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if (response.getBoolean("success")) {
                        Log.i("Combos", response.getJSONObject("data").getJSONArray("combos").length() + " combos found");
                        String comboJsonArrayString = response.getJSONObject("data").getJSONArray("combos").toString();
                        Actions.cacheCombos(CheckoutAddressActivity.this, comboJsonArrayString, new Date());
                        if(!Cart.getInstance().areCombosInCartAvailableIn(comboJsonArrayString)) {
                            Intent intent = new Intent(CheckoutAddressActivity.this, CartActivity.class);
                            intent.putExtra("combo_error", true);
                            startActivity(intent);
                            finish();
                        } else {
                            JsonObjectRequest confirmOrderRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/addCart", getConfirmRequestJson(), new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("Json Request", response.toString());
                                    fragmentContainer.setVisibility(View.GONE);
                                    try {
                                        if (response.getBoolean("success")) {
                                            intent = new Intent(CheckoutAddressActivity.this, CheckoutPaymentActivity.class);
                                            intent.putExtra("grand_total", response.getJSONObject("data").getDouble("grand_total"));
                                            intent.putExtra("total", response.getJSONObject("data").getDouble("total"));
                                            intent.putExtra("vat", response.getJSONObject("data").getDouble("vat"));
                                            intent.putExtra("vat_percentage", response.getJSONObject("data").getString("vat_percentage"));
                                            intent.putExtra("delivery_charges", response.getJSONObject("data").getDouble("delivery_charges"));
                                            intent.putExtra("order_id", response.getJSONObject("data").getString("order_id"));
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(CheckoutAddressActivity.this, CartActivity.class);
                                            intent.putExtra("combo_error", true);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(CheckoutAddressActivity.this, e); }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    fragmentContainer.setVisibility(View.VISIBLE);
                                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeConfirmOrderRequest")).commit();
                                    getSupportFragmentManager().executePendingTransactions();
                                }
                            });
                            fragmentContainer.setVisibility(View.VISIBLE);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
                            getSupportFragmentManager().executePendingTransactions();
                            Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(confirmOrderRequest);
                        }
                    } else Snackbar.make(mainLayout,"Request Failed. Reason: "+response.getString("error"),Snackbar.LENGTH_INDEFINITE).show();
                } catch (Exception e) { Actions.handleIgnorableException(CheckoutAddressActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeConfirmOrderRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(this).addToRequestQueue(getCombosRequest, 20000, 2, 1.0f);
    }

    private boolean isEverythingValid() { return addressId!=-1; }
    private JSONObject getMakeAddressRequestJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("area_id",Info.getAreaId(this));
            requestJson.put("data", dataJson);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        return requestJson;
    }

    public void makeAddressRequest() {
        Log.e("Testing", "MakeAddressCalled");
        JsonObjectRequest addressesRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses",getMakeAddressRequestJson(),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    if(response.getBoolean("success")) {
                        final ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        addresses = Arrays.asList(objectMapper.readValue(response.getJSONArray("data").toString(), Address[].class));
                        addressAdapter.notifyDataSetChanged();
                        if(addresses.size()==0) { emptyAddressLayout.setVisibility(View.VISIBLE); addressProgress.setVisibility(View.GONE); addressRecyclerView.setVisibility(View.GONE); return; }
                        else { addressRecyclerView.setVisibility(View.VISIBLE); addressProgress.setVisibility(View.VISIBLE); emptyAddressLayout.setVisibility(View.GONE); }
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { Actions.handleIgnorableException(CheckoutAddressActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeAddressRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(addressesRequest);
    }

    class AddressAdapter extends RecyclerView.Adapter {
        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.name) TextView name;
            @Bind(R.id.line1) TextView line1;
            @Bind(R.id.line2) TextView line2;
            @Bind(R.id.contact_no) TextView contactNo;
            @Bind(R.id.area_city) TextView areaCity;
            @Bind(R.id.edit) ImageView edit;
            @Bind(R.id.delete) ImageView delete;
            @Bind(R.id.selected_indicator) View selected;
            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
        @Override public int getItemCount() { return addresses.size(); }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(CheckoutAddressActivity.this).inflate(R.layout.repeatable_user_address, parent, false)); }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final Address address = addresses.get(position);
            viewHolder.name.setText(address.getName());
            viewHolder.line1.setText(address.getLine1());
            viewHolder.line2.setText(address.getLine2());
            viewHolder.contactNo.setText(address.getContactNo());

            int areaId = address.getAreaId();
            int cityPos = -1;
            for(int j=0;j<cities.size();j++) if (cities.get(j).indexOf(areaId)!=-1) cityPos = j;
            int areaPos = cities.get(cityPos).indexOf(areaId);
            String city = cities.get(cityPos).getName();
            String area = cities.get(cityPos).getAreas().get(areaPos).getName();
            String areaCity = area + ", " + city;
            viewHolder.areaCity.setText(areaCity);

            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CheckoutAddressActivity.this, PinYourLocationActivity.class);
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                    try { intent.putExtra("json", objectMapper.writeValueAsString(address)); }
                    catch (Exception e) { Actions.handleIgnorableException(CheckoutAddressActivity.this,e); }
                    intent.putExtra("edit", true);
                    intent.putExtra("cart", true);
                    startActivity(intent);
                }
            });

            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Snackbar deletingAddressSnackbar = Snackbar.make(mainLayout,"Deleting address...",Snackbar.LENGTH_INDEFINITE);
                    final Snackbar couldNotDeleteSnackbar = Snackbar.make(mainLayout,"Could not delete",Snackbar.LENGTH_INDEFINITE);
                    couldNotDeleteSnackbar.setAction("Try Again", new View.OnClickListener() { @Override public void onClick(View v) { couldNotDeleteSnackbar.dismiss(); } });

                    JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutAddressActivity.this);
                    JSONObject dataJson = new JSONObject();
                    try {
                        dataJson.put("id", address.getId());
                        requestJson.put("data", dataJson);
                    } catch (JSONException e) { e.printStackTrace(); }
                    JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses/destroy", requestJson, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if(deletingAddressSnackbar.isShown()) deletingAddressSnackbar.dismiss();
                            try {
                                if (response.getBoolean("success")) makeAddressRequest();
                                else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                            } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(CheckoutAddressActivity.this,e); }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            couldNotDeleteSnackbar.show();
                        }
                    });

                    deletingAddressSnackbar.show();
                    Swift.getInstance(CheckoutAddressActivity.this).addToRequestQueue(deleteRequest);
                }
            });
            if(addressId == address.getId()) viewHolder.selected.setVisibility(View.VISIBLE);
            else viewHolder.selected.setVisibility(View.GONE);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(mainLayout, "Address "+(position+1)+" Selected", Snackbar.LENGTH_SHORT).show();
                    addressId = address.getId();
                    notifyDataSetChanged();
                    chooseAddressLayout.setVisibility(View.GONE);
                }
            });
        }
    }

}
