package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Combo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    Intent intent;

    ImageView offers;
    ImageView for_1;
    ImageView for_2;
    ImageView for_3;

    ImageView offers_focus;
    ImageView for_1_focus;
    ImageView for_2_focus;
    ImageView for_3_focus;

    ViewPager viewPager;
    List<Combo> combos;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: Swift.getInstance(MainActivity.this).addToRequestQueue(getLogoutJsonObjectRequest()); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        offers = (ImageView) findViewById(R.id.offers); offers.setOnClickListener(this);
        for_1 = (ImageView) findViewById(R.id.for_1); for_1.setOnClickListener(this);
        for_2 = (ImageView) findViewById(R.id.for_2); for_2.setOnClickListener(this);
        for_3 = (ImageView) findViewById(R.id.for_3); for_3.setOnClickListener(this);

        offers_focus = (ImageView) findViewById(R.id.offers_focus);
        for_1_focus = (ImageView) findViewById(R.id.for_1_focus);
        for_2_focus = (ImageView) findViewById(R.id.for_2_focus);
        for_3_focus = (ImageView) findViewById(R.id.for_3_focus);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        final PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override public int getCount() { return 4; }
            @Override public boolean isViewFromObject(View view, Object object) { return view==object; }
            @Override public void destroyItem(ViewGroup container, int position, Object object) { container.removeView((ScrollView) object); }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ScrollView scrollView = new ScrollView(getBaseContext());
                scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                LinearLayout linearLayout = new LinearLayout(getBaseContext());
                linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                HashMap<Integer,Integer> quantityHashMap = new HashMap<>();
                try {
                    for (Combo combo: combos) {
                        final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo, linearLayout, false);
                        if (combo.getGroupSize()!= position) continue;
                        ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                        ((TextView) comboLayout.findViewById(R.id.name)).setText(combo.getName());
                        ((TextView) comboLayout.findViewById(R.id.description)).setText(combo.getDescription());
                        ((TextView) comboLayout.findViewById(R.id.price)).setText(combo.getStringPrice());
                        ImageView foodLabel = (ImageView) comboLayout.findViewById(R.id.label);
                        switch(combo.getLabel()) {
                            case "egg": foodLabel.setColorFilter(getResources().getColor(R.color.egg)); break;
                            case "veg": foodLabel.setColorFilter(getResources().getColor(R.color.veg)); break;
                            case "non-veg": foodLabel.setColorFilter(getResources().getColor(R.color.non_veg)); break;
                        }
                        final LinearLayout comboFoodLayout = (LinearLayout) comboLayout.findViewById(R.id.food_items_layout);
                        try {
                            for (int k = 0; k < comboOptions.length(); k++) {
                                final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, comboFoodLayout, false);
                                final JSONObject comboOptionsJson = comboOptions.getJSONObject(k);
                                ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                                ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText(comboOptionsJson.getString("name"));
                                ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText(comboOptionsJson.getString("description").equals("null") ? "" : comboOptionsJson.getString("description"));
                                final LinearLayout optionsLayout = (LinearLayout) currentComboFoodLayout.findViewById(R.id.options_layout);
                                final JSONArray comboOptionDishes = comboOptionsJson.getJSONArray("combo_option_dishes");
                                HashMap<Integer, String> restaurantHashMap = new HashMap<>();
                                for (int l = 0; l < comboOptionDishes.length(); l++) {
                                    JSONObject comboDishOptionJson = comboOptionDishes.getJSONObject(l);
                                    LinearLayout comboOptionsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food_options, currentComboFoodLayout, false);
                                    final ImageView selected = (ImageView) comboOptionsLayout.findViewById(R.id.selected);
                                    if (l == 0) selected.setVisibility(View.VISIBLE);
                                    final JSONObject dishJson = comboDishOptionJson.getJSONObject("dish");
                                    if (l == 0) comboSelectionHashMap.put(comboOptionsJson.getInt("id"), dishJson.getInt("id"));
                                    ((TextView) comboOptionsLayout.findViewById(R.id.option_name)).setText(dishJson.getString("name"));
                                    JSONObject restaurantJson = dishJson.getJSONObject("restaurant");
                                    restaurantHashMap.put(restaurantJson.getInt("id"), restaurantJson.getString("name"));
                                    ((TextView) comboOptionsLayout.findViewById(R.id.restaurant_name)).setText(restaurantJson.getString("name"));
                                    comboOptionsLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try { comboSelectionHashMap.put(comboOptionsJson.getInt("id"), dishJson.getInt("id")); }
                                            catch (JSONException e) { e.printStackTrace(); }
                                            for (int l = 0; l < comboOptionDishes.length(); l++)
                                                optionsLayout.getChildAt(l).findViewById(R.id.selected).setVisibility(View.INVISIBLE);
                                            selected.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    if (l == comboOptionDishes.length() - 1)
                                        Animations.bottomMargin(comboOptionsLayout, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), 0);
                                    optionsLayout.addView(comboOptionsLayout);
                                }
                                if (restaurantHashMap.size() == 1) {
                                    for (int m = 0; m < comboOptionDishes.length(); m++)
                                        optionsLayout.getChildAt(m).findViewById(R.id.restaurant_layout).setVisibility(View.GONE);
                                    ((TextView) currentComboFoodLayout.findViewById(R.id.restaurant_name)).setText(restaurantHashMap.values().toArray()[0].toString());
                                } else
                                    currentComboFoodLayout.findViewById(R.id.restaurant_layout).setVisibility(View.GONE);
                                if (k == comboOptions.length() - 1)
                                    Animations.bottomMargin(currentComboFoodLayout, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), 0);
                                layoutOrderTreeMap.put(comboOptionsJson.getInt("priority"), currentComboFoodLayout);
                            }
                            JSONArray comboDishes = comboJson.getJSONArray("combo_dishes");
                            for (int m = 0; m < comboDishes.length(); m++) {
                                JSONObject comboDishJson = comboDishes.getJSONObject(m);
                                JSONObject dishJson = comboDishJson.getJSONObject("dish");
                                comboDishesHashMap.put(comboDishJson.getInt("id"), dishJson.getInt("id"));
                                final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, comboFoodLayout, false);
                                ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                                ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText(dishJson.getString("name"));
                                ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText(dishJson.getString("description").equals("null") ? "" : dishJson.getString("description"));
                                JSONObject restaurantJson = dishJson.getJSONObject("restaurant");
                                ((TextView) currentComboFoodLayout.findViewById(R.id.restaurant_name)).setText(restaurantJson.getString("name"));
                                layoutOrderTreeMap.put(comboDishJson.getInt("priority"), currentComboFoodLayout);
                            }
                        } catch (JSONException e) { e.printStackTrace(); }
                        comboLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (comboFoodLayout.getChildCount() == 0) {
                                    for (Integer i : layoutOrderTreeMap.navigableKeySet())
                                        comboFoodLayout.addView(layoutOrderTreeMap.get(i));
                                } else comboFoodLayout.removeAllViews();
                            }
                        });
                        final ImageView addToCart = (ImageView) comboLayout.findViewById(R.id.add_to_cart);
                        final LinearLayout addedToCartLayout = (LinearLayout) comboLayout.findViewById(R.id.added_to_cart_layout);
                        final LinearLayout countLayout = (LinearLayout) comboLayout.findViewById(R.id.count_layout);
                        final TextView count = (TextView) countLayout.findViewById(R.id.count);
                        if(quantityHashMap.containsKey(comboId)) {
                            int quantity = quantityHashMap.get(comboId);
                            count.setText(String.valueOf(quantity));
                            if (quantity>0) addedToCartLayout.setVisibility(View.VISIBLE);
                        }
                        ImageView plus = (ImageView) countLayout.findViewById(R.id.plus);
                        ImageView minus = (ImageView) countLayout.findViewById(R.id.minus);
                        plus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                            }
                        });
                        minus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                                if(Integer.parseInt(count.getText().toString())==0) {
                                    Animations.fadeOut(addedToCartLayout, 200);
                                    Animations.fadeOutAndFadeIn(countLayout, addToCart, 200);
                                }
                            }
                        });
                        addToCart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Animations.fadeInOnlyIfInvisible(addedToCartLayout, 200);
                                Animations.fadeOutAndFadeIn(addToCart,countLayout,200);
                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                            }
                        });
                        comboTreeMap.put((int) Float.parseFloat(comboJson.getString("price")), comboLayout);
                        for (int n : comboTreeMap.navigableKeySet())
                            linearLayout.addView(comboTreeMap.get(n));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
                scrollView.addView(linearLayout);
                container.addView(scrollView);
                return scrollView;
            }
        };

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/combos", JsonProvider.getStandartRequestJson(MainActivity.this) ,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if (response.getBoolean("success")) {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        combos = Arrays.asList(mapper.readValue(response.getJSONArray("data").toString(), Combo[].class));
                        viewPager.setAdapter(pagerAdapter);
                        viewPager.addOnPageChangeListener(MainActivity.this);
                    } else if (!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(MainActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(MainActivity.this);
                else Alerts.unknownErrorAlert(MainActivity.this);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.offers: setFocus(v.getId()); break;
            case R.id.for_1: setFocus(v.getId()); break;
            case R.id.for_2: setFocus(v.getId()); break;
            case R.id.for_3: setFocus(v.getId()); break;
        }
    }

    private void setFocus(int id){
        findViewById(R.id.offers_focus).setVisibility((id==R.id.offers)?View.VISIBLE:View.INVISIBLE);
        findViewById(R.id.for_1_focus).setVisibility((id==R.id.for_1)?View.VISIBLE:View.INVISIBLE);
        findViewById(R.id.for_2_focus).setVisibility((id==R.id.for_2)?View.VISIBLE:View.INVISIBLE);
        findViewById(R.id.for_3_focus).setVisibility((id==R.id.for_3) ? View.VISIBLE : View.INVISIBLE);
        switch (id) {
            case R.id.offers: viewPager.setCurrentItem(0, true); break;
            case R.id.for_1: viewPager.setCurrentItem(1, true); break;
            case R.id.for_2: viewPager.setCurrentItem(2, true); break;
            case R.id.for_3: viewPager.setCurrentItem(3, true); break;
        }
    }

    @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
    @Override public void onPageScrollStateChanged(int state) {}
    @Override public void onPageSelected(int position) {
        switch (position) {
            case 0: setFocus(R.id.offers); break;
            case 1: setFocus(R.id.for_1); break;
            case 2: setFocus(R.id.for_2); break;
            case 3: setFocus(R.id.for_3); break;
        }
    }

    private JsonObjectRequest getLogoutJsonObjectRequest() {
        JsonObjectRequest logoutJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/sessions/destroy", JsonProvider.getStandartRequestJson(MainActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) { logout(); }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) {
                    Alerts.internetConnectionErrorAlert(MainActivity.this);
                } else Alerts.unknownErrorAlert(MainActivity.this);
                logout();
                System.out.println("Response Error: " + error);
            }
        });
        return logoutJsonObjectRequest;
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("session", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged_in", false);
        editor.remove("user_token");
        editor.remove("session_token");
        editor.remove("android_token");
        editor.commit();
        intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Exit App ?")
                .setMessage("Do you really want to exit the app")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private JSONObject getComboRequestJson(int comboId, HashMap<Integer,Integer> comboOptionsHashMap, HashMap<Integer,Integer> comboDishesHashMap) {
        JSONObject requestJson = JsonProvider.getStandartRequestJson(MainActivity.this);
        try {
            JSONArray comboOptionsSelected = new JSONArray();
            for(HashMap.Entry<Integer,Integer> entry : comboOptionsHashMap.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",entry.getKey());
                JSONObject dishJson = new JSONObject();
                dishJson.put("id",entry.getValue());
                jsonObject.put("dish",dishJson);
                comboOptionsSelected.put(jsonObject);
            }
            JSONArray comboDishesSelected = new JSONArray();
            for(HashMap.Entry<Integer,Integer> entry : comboDishesHashMap.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",entry.getKey());
                JSONObject dishJson = new JSONObject();
                dishJson.put("id",entry.getValue());
                jsonObject.put("dish",dishJson);
                comboDishesSelected.put(jsonObject);
            }
            JSONObject comboJson = new JSONObject();
            comboJson.put("id", comboId);
            comboJson.put("combo_options", comboOptionsSelected);
            comboJson.put("combo_dishes", comboDishesSelected);
            JSONObject dataJson = new JSONObject();
            dataJson.put("combo",comboJson);
            requestJson.put("data", dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private JSONObject getComboRequestJson(int comboId) {
        JSONObject requestJson = JsonProvider.getStandartRequestJson(MainActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            JSONObject comboJson = new JSONObject();
            comboJson.put("id",comboId);
            dataJson.put("combo",comboJson);
            requestJson.put("data", dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

}
