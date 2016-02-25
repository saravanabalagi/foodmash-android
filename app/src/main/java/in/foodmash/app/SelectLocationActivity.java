package in.foodmash.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.City;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Zeke on Feb 23, 2016.
 */
public class SelectLocationActivity extends AppCompatActivity {

    @Bind(R.id.city) Spinner citySpinner;
    @Bind(R.id.area) Spinner areaSpinner;

    private ArrayList<String> citiesArrayList = new ArrayList<>();
    private JsonObjectRequest locationRequest;
    private List<City> cities;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        ButterKnife.bind(this);

        locationRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/cities", JsonProvider.getAnonymousRequestJson(SelectLocationActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        cities = Arrays.asList(objectMapper.readValue(response.getJSONArray("data").toString(), City[].class));
                        for (City city : cities) citiesArrayList.add(city.getName());
                        for (City city : cities) System.out.println(city.toString());
                        ArrayAdapter citySpinnerAdapter = new ArrayAdapter<>(
                                SelectLocationActivity.this,
                                R.layout.spinner_item,
                                addStringAsFirstItem(citiesArrayList,"City"));
                        citySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        citySpinner.setAdapter(citySpinnerAdapter);
                        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override public void onNothingSelected(AdapterView<?> parent) { }
                            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                ArrayAdapter areaSpinnerAdapter = new ArrayAdapter<>(
                                        SelectLocationActivity.this,
                                        R.layout.spinner_item,
                                        addStringAsFirstItem((position==0)?new ArrayList<String>():cities.get(position-1).getAreaStringArrayList(),"Area"));
                                areaSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                areaSpinner.setAdapter(areaSpinnerAdapter);
                            }
                        });

                        ArrayAdapter areaSpinnerAdapter = new ArrayAdapter<>(
                                SelectLocationActivity.this,
                                R.layout.spinner_item,
                                addStringAsFirstItem(cities.get(0).getAreaStringArrayList(),"Area"));
                        areaSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        areaSpinner.setAdapter(areaSpinnerAdapter);
                        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override public void onNothingSelected(AdapterView<?> parent) { }
                            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position==0) return;
                                int packagingCentreId = cities.get(citySpinner.getSelectedItemPosition() - 1).getPackagingCentreId(((TextView) view).getText().toString());
                                Intent intent = new Intent(SelectLocationActivity.this, MainActivity.class);
                                Actions.cachePackagingCentreId(SelectLocationActivity.this,packagingCentreId);
                                startActivity(intent);
                                finish();
                            }
                        });

                    } else {
                        Alerts.requestUnauthorisedAlert(SelectLocationActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Alerts.commonErrorAlert(
                        SelectLocationActivity.this,
                        "No Internet",
                        "Sometimes Internet gets sleepy and takes a nap. Turn it on and we'll give it another go.",
                        "Exit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        },false);
                System.out.println("Response Error: " + error);
            }
        });
        makeRequest(locationRequest);

    }

    private void makeRequest(JsonObjectRequest jsonObjectRequest) { Swift.getInstance(SelectLocationActivity.this).addToRequestQueue(jsonObjectRequest);}
    private ArrayList<String> addStringAsFirstItem(ArrayList<String> arrayList, String defaultString) {
        ArrayList<String> resultArrayList = new ArrayList<>();
        resultArrayList.add(0,defaultString);
        for(String string: arrayList)
            resultArrayList.add(string);
        return  resultArrayList;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}