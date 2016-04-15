package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.custom.City;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class SplashActivity extends FoodmashActivity {

    @Bind(R.id.city) Spinner citySpinner;
    @Bind(R.id.area) Spinner areaSpinner;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.location_layout) LinearLayout locationLayout;
    @Bind(R.id.retry) TextView retryButton;

    private List<City> cities;
    private boolean skipUpdate = false;
    private boolean skipMaintenance = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        retryButton.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { onResume(); } });
        makeCheckConnectionRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        skipUpdate = getIntent().getBooleanExtra("skip_update", false);
        skipMaintenance = getIntent().getBooleanExtra("skip_maintenance", false);
        makeCheckConnectionRequest();
    }

    public void makeCheckConnectionRequest() {
        JsonObjectRequest checkConnectionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/instantiate", JsonProvider.getAnonymousRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        if (skipUpdate) makeLocationRequest();
                        int newVersion = Integer.parseInt(response.getJSONObject("data").getJSONObject("versions").getString("version_code"));
                        int currentVersion = BuildConfig.VERSION_CODE;
                        if (currentVersion < newVersion) {
                            startActivity(new Intent(SplashActivity.this, UpdateAppActivity.class));
                            finish();
                        } else if(skipMaintenance || !response.getJSONObject("data").has("maintenance")) makeLocationRequest();
                        else {
                            JSONObject maintenance = response.getJSONObject("data").getJSONObject("maintenance");
                            Intent intent = new Intent(SplashActivity.this, ShowMessageActivity.class);
                            intent.putExtra("title", maintenance.getString("title"));
                            intent.putExtra("message", maintenance.getString("message"));
                            intent.putExtra("image", maintenance.getString("image"));
                            intent.putExtra("blocking", maintenance.getBoolean("blocking"));
                            if(maintenance.has("url")) {
                                intent.putExtra("url", maintenance.getString("url"));
                                intent.putExtra("url_caption", maintenance.getString("url_caption"));
                                intent.putExtra("url_hides_exit", maintenance.getBoolean("url_hides_exit"));
                            }
                            startActivity(intent);
                            finish();
                        }
                    } else Snackbar.make(mainLayout,"Unable to check for updates: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { e.printStackTrace(); Actions.handleIgnorableException(SplashActivity.this, e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOutAndFadeIn(loadingLayout,retryButton,500);
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeCheckConnectionRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.GONE);
        Swift.getInstance(SplashActivity.this).addToRequestQueue(checkConnectionRequest,500,5,1.5f);
    }

    public void makeLocationRequest() {
        JsonObjectRequest locationRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/cities", JsonProvider.getAnonymousRequestJson(SplashActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        Actions.cacheCities(SplashActivity.this, response.getJSONArray("data").toString());
                        cities = Arrays.asList(objectMapper.readValue(Info.getCityJsonArrayString(SplashActivity.this), City[].class));
                        ArrayList<String> citiesArrayList = new ArrayList<>();
                        for (City city : cities) citiesArrayList.add(city.getName());
                        ArrayAdapter citySpinnerAdapter = new ArrayAdapter<>(
                                SplashActivity.this,
                                R.layout.spinner_item,
                                citiesArrayList);
                        citySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        citySpinner.setAdapter(citySpinnerAdapter);
                        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }

                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                ArrayAdapter areaSpinnerAdapter = new ArrayAdapter<>(
                                        SplashActivity.this,
                                        R.layout.spinner_item,
                                        addStringAsFirstItem(cities.get(position).getAreaStringArrayList(), "Area"));
                                areaSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                areaSpinner.setAdapter(areaSpinnerAdapter);
                            }
                        });

                        ArrayAdapter areaSpinnerAdapter = new ArrayAdapter<>(
                                SplashActivity.this,
                                R.layout.spinner_item,
                                addStringAsFirstItem(cities.get(0).getAreaStringArrayList(), "Area"));
                        areaSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        areaSpinner.setAdapter(areaSpinnerAdapter);
                        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override public void onNothingSelected(AdapterView<?> parent) { }
                            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position == 0) return;
                                String cityName = cities.get(citySpinner.getSelectedItemPosition()).getName();
                                String areaName = ((TextView) view).getText().toString();
                                int packagingCentreId = cities.get(citySpinner.getSelectedItemPosition()).getPackagingCentreId(((TextView) view).getText().toString());
                                int areaId = cities.get(citySpinner.getSelectedItemPosition()).getAreas().get(position - 1).getId();
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                Actions.cacheLocationDetails(SplashActivity.this, cityName, areaName, areaId, packagingCentreId);
                                startActivity(intent);
                                finish();
                            }
                        });
                        Animations.fadeOutAndFadeIn(loadingLayout,locationLayout, 500);
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { Actions.handleIgnorableException(SplashActivity.this, e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOutAndFadeIn(loadingLayout, retryButton, 500);
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeLocationRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.GONE);
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
