package in.foodmash.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.City;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class SplashActivity extends AppCompatActivity {

    @Bind(R.id.city) Spinner citySpinner;
    @Bind(R.id.area) Spinner areaSpinner;

    private ArrayList<String> citiesArrayList = new ArrayList<>();
    private List<City> cities;
    private JsonObjectRequest locationRequest;
    private JsonObjectRequest checkConnectionRequest;
    private boolean skipUpdate;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        skipUpdate = getIntent().getBooleanExtra("skip_update", false);
        checkConnectionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/versions", JsonProvider.getAnonymousRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        if(skipUpdate) makeLocationRequest();
                        int newVersion = Integer.parseInt(response.getJSONObject("data").getString("id"));
                        int currentVersion = BuildConfig.VERSION_CODE;
                        if(currentVersion < newVersion) { startActivity(new Intent(SplashActivity.this, UpdateAppActivity.class)); finish(); }
                        else makeLocationRequest();
                    } else Alerts.requestUnauthorisedAlert(SplashActivity.this);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Alerts.commonErrorAlert(
                        SplashActivity.this,
                        "No Internet",
                        "Sometimes Internet gets sleepy and takes a nap. Turn it on and we'll give it another go.",
                        "Exit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        },false);
                Log.e("Json Request Failed", error.toString());
            }
        });
        makeCheckConnectionRequest(checkConnectionRequest);
    }

    private void makeCheckConnectionRequest(JsonObjectRequest jsonObjectRequest) { Swift.getInstance(SplashActivity.this).addToRequestQueue(jsonObjectRequest, 500, 10, 2f); }
    private void makeLocationRequest() {
        locationRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/cities", JsonProvider.getAnonymousRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        Actions.cacheCities(SplashActivity.this,response.getJSONArray("data").toString());
                        cities = Arrays.asList(objectMapper.readValue(Info.getCityJsonArrayString(SplashActivity.this), City[].class));
                        for (City city : cities) citiesArrayList.add(city.getName());
                        ArrayAdapter citySpinnerAdapter = new ArrayAdapter<>(
                                SplashActivity.this,
                                R.layout.spinner_item,
                                citiesArrayList);
                        citySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        citySpinner.setAdapter(citySpinnerAdapter);
                        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override public void onNothingSelected(AdapterView<?> parent) { }
                            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                ArrayAdapter areaSpinnerAdapter = new ArrayAdapter<>(
                                        SplashActivity.this,
                                        R.layout.spinner_item,
                                        addStringAsFirstItem(cities.get(position).getAreaStringArrayList(),"Area"));
                                areaSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                areaSpinner.setAdapter(areaSpinnerAdapter);
                            }
                        });

                        ArrayAdapter areaSpinnerAdapter = new ArrayAdapter<>(
                                SplashActivity.this,
                                R.layout.spinner_item,
                                addStringAsFirstItem(cities.get(0).getAreaStringArrayList(),"Area"));
                        areaSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        areaSpinner.setAdapter(areaSpinnerAdapter);
                        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override public void onNothingSelected(AdapterView<?> parent) { }
                            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position==0) return;
                                String cityName = cities.get(citySpinner.getSelectedItemPosition()).getName();
                                String areaName = ((TextView) view).getText().toString();
                                int packagingCentreId = cities.get(citySpinner.getSelectedItemPosition()).getPackagingCentreId(((TextView) view).getText().toString());
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                Actions.cacheLocationDetails(SplashActivity.this,cityName,areaName,packagingCentreId);
                                startActivity(intent);
                                finish();
                            }
                        });
                        Animations.fadeOutAndFadeIn(
                                SplashActivity.this.findViewById(R.id.loading_layout),
                                SplashActivity.this.findViewById(R.id.location_layout), 500);
                    } else {
                        Alerts.requestUnauthorisedAlert(SplashActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Alerts.commonErrorAlert(
                        SplashActivity.this,
                        "No Internet",
                        "Sometimes Internet gets sleepy and takes a nap. Turn it on and we'll give it another go.",
                        "Exit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        },false);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Swift.getInstance(this).addToRequestQueue(locationRequest);
    }
    private ArrayList<String> addStringAsFirstItem(ArrayList<String> arrayList, String defaultString) {
        ArrayList<String> resultArrayList = new ArrayList<>();
        resultArrayList.add(0,defaultString);
        for(String string: arrayList)
            resultArrayList.add(string);
        return  resultArrayList;
    }

}
