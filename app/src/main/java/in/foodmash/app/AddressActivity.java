package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

/**
 * Created by Zeke on Aug 08 2015.
 */
public class AddressActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.add_address) FloatingActionButton addAddress;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.connecting_layout) LinearLayout connectingLayout;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private JSONArray jsonArray;
    private JsonObjectRequest deleteAddressRequest;
    private JsonObjectRequest getAddressesRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        addAddress.setOnClickListener(this);
        fillLayout();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address: intent = new Intent(this, PinYourLocationActivity.class); startActivity(intent); break;
        }
    }

    private void fillLayout() {
        fillLayout.removeAllViews();
        getAddressesRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses", JsonProvider.getStandardRequestJson(AddressActivity.this),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Animations.fadeOut(loadingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        jsonArray = response.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            final JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject addressJson = jsonObject.getJSONObject("address");
                            final LinearLayout addressLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_user_address, fillLayout, false);
                            String address = addressJson.getString("line1") + ",\n" +
                                    addressJson.getString("line2") + ",\n" +
                                    addressJson.getString("area") + ",\n" +
                                    addressJson.getString("city") + " - " +
                                    addressJson.getString("pincode");
                            ((TextView) addressLayout.findViewById(R.id.name)).setText(jsonObject.getString("name"));
                            ((TextView) addressLayout.findViewById(R.id.address)).setText(address);
                            ((TextView) addressLayout.findViewById(R.id.phone)).setText(((jsonObject.getString("phone").length()==10)?"+91 ":"+91 44 ")+jsonObject.getString("phone"));
                            if (jsonObject.getBoolean("primary"))
                                addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
                            addressLayout.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(AddressActivity.this, PinYourLocationActivity.class);
                                    intent.putExtra("json", jsonObject.toString());
                                    intent.putExtra("edit", true);
                                    startActivity(intent);
                                }
                            });
                            addressLayout.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    JSONObject requestJson = JsonProvider.getStandardRequestJson(AddressActivity.this);
                                    JSONObject dataJson = new JSONObject();
                                    try {
                                        dataJson.put("id", jsonObject.getString("id"));
                                        requestJson.put("data", dataJson);
                                    } catch (JSONException e) { e.printStackTrace(); }
                                    deleteAddressRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/delivery_addresses/destroy", requestJson, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if (response.getBoolean("success")) {
                                                    Animations.fadeOut(connectingLayout,500);
                                                    Animations.fadeIn(mainLayout,500);
                                                    fillLayout();
                                                }
                                                else {
                                                    Animations.fadeOut(connectingLayout,500);
                                                    Animations.fadeIn(mainLayout,500);
                                                    Alerts.commonErrorAlert(AddressActivity.this, "Could not delete !", "The address that you want to remove could not be removed. Try again!", "Okay");
                                                }
                                            } catch (JSONException e) { e.printStackTrace(); }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Animations.fadeOut(connectingLayout,500);
                                            Animations.fadeIn(mainLayout,500);
                                            DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Animations.fadeIn(connectingLayout,500);
                                                    Animations.fadeOut(mainLayout, 500);
                                                    Swift.getInstance(AddressActivity.this).addToRequestQueue(deleteAddressRequest);
                                                }
                                            };
                                            if (error instanceof TimeoutError) Alerts.timeoutErrorAlert(AddressActivity.this, onClickTryAgain);
                                            else if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(AddressActivity.this, onClickTryAgain);
                                            else Alerts.unknownErrorAlert(AddressActivity.this);
                                            System.out.println("Response Error: " + error);
                                        }
                                    });
                                    Animations.fadeIn(connectingLayout,500);
                                    Animations.fadeOut(mainLayout, 500);
                                    Swift.getInstance(AddressActivity.this).addToRequestQueue(deleteAddressRequest);
                                }
                            });
                            fillLayout.addView(addressLayout);
                        }
                    } else {
                        Alerts.requestUnauthorisedAlert(AddressActivity.this);
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
                        Swift.getInstance(AddressActivity.this).addToRequestQueue(getAddressesRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(AddressActivity.this, onClickTryAgain);
                if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(AddressActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(AddressActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(AddressActivity.this).addToRequestQueue(getAddressesRequest);
    }

}
