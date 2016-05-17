package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
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
import in.foodmash.app.models.City;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class AddressActivity extends FoodmashActivity implements View.OnClickListener {

    @Bind(R.id.add_address) FloatingActionButton addAddress;
    @Bind(R.id.address_recycler_view) RecyclerView addressRecyclerView;
    @Bind(R.id.empty_address_layout) LinearLayout emptyAddressLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private List<Address> addresses = new ArrayList<>();
    private ObjectMapper objectMapper;
    private List<City> cities;
    private AddressAdapter addressAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Mash","addresses");

        try {
            objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            cities = Arrays.asList(objectMapper.readValue(Info.getCityJsonArrayString(this), City[].class));
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        addAddress.setOnClickListener(this);

        addressAdapter = new AddressAdapter();
        addressRecyclerView.hasFixedSize();
        linearLayoutManager = new LinearLayoutManager(this);
        addressRecyclerView.setLayoutManager(linearLayoutManager);
        addressRecyclerView.setAdapter(addressAdapter);
        addressRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) { super.onScrollStateChanged(recyclerView, newState); }
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView,dx,dy);
                swipeRefreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
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
        makeAddressRequest();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); startActivity(intent); break;
        }
    }

    public void makeAddressRequest() {
        JsonObjectRequest getAddressesRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_get_delivery_address), JsonProvider.getStandardRequestJson(AddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                addAddress.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    if(response.getBoolean("success")) {
                        final ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        addresses = Arrays.asList(objectMapper.readValue(response.getJSONArray("data").toString(), Address[].class));
                        addressAdapter.notifyDataSetChanged();
                        if(addresses.size()==0) { emptyAddressLayout.setVisibility(View.VISIBLE); addressRecyclerView.setVisibility(View.GONE); }
                        else { addressRecyclerView.setVisibility(View.VISIBLE); emptyAddressLayout.setVisibility(View.GONE); }
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { Actions.handleIgnorableException(AddressActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeAddressRequest")).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        addAddress.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(AddressActivity.this).addToRequestQueue(getAddressesRequest);
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
            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
        @Override public int getItemCount() { return addresses.size(); }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(AddressActivity.this).inflate(R.layout.repeatable_user_address, parent, false)); }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
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
                    Intent intent = new Intent(AddressActivity.this, PinYourLocationActivity.class);
                    try { intent.putExtra("json", objectMapper.writeValueAsString(address)); }
                    catch (Exception e) { Actions.handleIgnorableException(AddressActivity.this,e); }
                    intent.putExtra("edit", true);
                    startActivity(intent);
                }
            });

            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Snackbar deletingAddressSnackbar = Snackbar.make(mainLayout,"Deleting address...",Snackbar.LENGTH_INDEFINITE);
                    final Snackbar couldNotDeleteSnackbar = Snackbar.make(mainLayout,"Could not delete",Snackbar.LENGTH_INDEFINITE);
                    couldNotDeleteSnackbar.setAction("Try Again", new View.OnClickListener() { @Override public void onClick(View v) { couldNotDeleteSnackbar.dismiss(); } });

                    JSONObject requestJson = JsonProvider.getStandardRequestJson(AddressActivity.this);
                    JSONObject dataJson = new JSONObject();
                    try {
                        dataJson.put("id", address.getId());
                        requestJson.put("data", dataJson);
                    } catch (JSONException e) { e.printStackTrace(); }
                    JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_destroy_delivery_address), requestJson, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if(deletingAddressSnackbar.isShown()) deletingAddressSnackbar.dismiss();
                            try {
                                if (response.getBoolean("success")) makeAddressRequest();
                                else if (response.getBoolean("success")) Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                            } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(AddressActivity.this,e); }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            couldNotDeleteSnackbar.show();
                        }
                    });

                    deletingAddressSnackbar.show();
                    Swift.getInstance(AddressActivity.this).addToRequestQueue(deleteRequest);
                }
            });

            if(viewHolder.getLayoutPosition() == this.getItemCount()-1) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(40));
                viewHolder.itemView.setLayoutParams(layoutParams);
            } else {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(0));
                viewHolder.itemView.setLayoutParams(layoutParams);
            }
        }
    }

}
