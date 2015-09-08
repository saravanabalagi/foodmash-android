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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.TreeMap;

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
    JSONArray combosJson;
    JSONObject cartJson;

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
            case R.id.menu_log_out: logout(); return true;
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

                    JSONArray ordersJson = cartJson.getJSONArray("orders");
                    for (int i = 0; i < ordersJson.length(); i++) {
                        JSONObject orderJson = ordersJson.getJSONObject(i);
                        JSONObject productJson = orderJson.getJSONObject("product");
                        int comboId = productJson.getInt("id");
                        if(quantityHashMap.containsKey(comboId))
                            quantityHashMap.put(comboId, quantityHashMap.get(comboId)+orderJson.getInt("quantity"));
                        else quantityHashMap.put(comboId, orderJson.getInt("quantity"));
                    }

                    for (int j = 0; j < combosJson.length(); j++) {
                        TreeMap<Integer, LinearLayout> comboTreeMap = new TreeMap<>();
                        final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo, linearLayout, false);
                        final JSONObject comboJson = combosJson.getJSONObject(j);
                        if (Integer.parseInt(comboJson.getString("group_size")) != position) {
                            continue;
                        }

                        final int comboId = comboJson.getInt("id");
                        final JSONArray comboOptions = comboJson.getJSONArray("combo_options");
                        final HashMap<Integer, Integer> comboSelectionHashMap = new HashMap<>();
                        final HashMap<Integer, Integer> comboDishesHashMap = new HashMap<>();

                        ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                        ((TextView) comboLayout.findViewById(R.id.name)).setText(comboJson.getString("name"));
                        ((TextView) comboLayout.findViewById(R.id.description)).setText(comboJson.getString("description").equals("null") ? "" : comboJson.getString("description"));
                        ((TextView) comboLayout.findViewById(R.id.price)).setText(String.format("%.0f", Float.parseFloat(comboJson.getString("price"))));
                        ImageView foodLabel = (ImageView) comboLayout.findViewById(R.id.label);
                        switch(comboJson.getString("label")) {
                            case "egg": foodLabel.setColorFilter(getResources().getColor(R.color.egg)); break;
                            case "veg": foodLabel.setColorFilter(getResources().getColor(R.color.veg)); break;
                            case "non-veg": foodLabel.setColorFilter(getResources().getColor(R.color.non_veg)); break;
                        }
                        final LinearLayout comboFoodLayout = (LinearLayout) comboLayout.findViewById(R.id.food_items_layout);

                        final TreeMap<Integer, LinearLayout> layoutOrderTreeMap = new TreeMap<>();
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
                        LinearLayout addToCart = (LinearLayout) comboLayout.findViewById(R.id.add_to_cart);
                        final LinearLayout addedToCartLayout = (LinearLayout) comboLayout.findViewById(R.id.added_to_cart_layout);
                        final TextView count = (TextView) addedToCartLayout.findViewById(R.id.count);
                        if(quantityHashMap.containsKey(comboId)) {
                            int quantity = quantityHashMap.get(comboId);
                            count.setText(String.valueOf(quantity));
                            if (quantity>0) addedToCartLayout.setVisibility(View.VISIBLE);
                        }
                        ImageView plus = (ImageView) addedToCartLayout.findViewById(R.id.plus);
                        ImageView minus = (ImageView) addedToCartLayout.findViewById(R.id.minus);
                        plus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JsonObjectRequest cartJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/add", getComboRequestJson(comboId, comboSelectionHashMap, comboDishesHashMap), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            if (response.getBoolean("success")) {
                                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                                            } else if (!(response.getBoolean("success"))) {
                                                Alerts.unableToProcessResponseAlert(MainActivity.this);
                                                System.out.println("Error Details: " + response.getString("error"));
                                            }
                                        } catch (JSONException e) { e.printStackTrace(); }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error instanceof NoConnectionError || error instanceof TimeoutError)
                                            Alerts.internetConnectionErrorAlert(MainActivity.this);
                                        else Alerts.unknownErrorAlert(MainActivity.this);
                                        System.out.println("Response Error: " + error);
                                    }
                                });
                                Swift.getInstance(MainActivity.this).addToRequestQueue(cartJsonObjectRequest);
                            }
                        });
                        minus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JsonObjectRequest cartJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/remove", getComboRequestJson(comboId), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            if (response.getBoolean("success")) {
                                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                                                if (Integer.parseInt(count.getText().toString()) == 0)
                                                    Animations.fadeOut(addedToCartLayout, 200);
                                            } else if (!(response.getBoolean("success"))) {
                                                Alerts.unableToProcessResponseAlert(MainActivity.this);
                                                System.out.println("Error Details: " + response.getString("error"));
                                            }
                                        } catch (JSONException e) { e.printStackTrace(); }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error instanceof NoConnectionError || error instanceof TimeoutError)
                                            Alerts.internetConnectionErrorAlert(MainActivity.this);
                                        else Alerts.unknownErrorAlert(MainActivity.this);
                                        System.out.println("Response Error: " + error);
                                    }
                                });
                                Swift.getInstance(MainActivity.this).addToRequestQueue(cartJsonObjectRequest);
                            }
                        });
                        addToCart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JsonObjectRequest cartJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/add", getComboRequestJson(comboId, comboSelectionHashMap, comboDishesHashMap), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            if (response.getBoolean("success")) {
                                                Animations.fadeInOnlyIfInvisible(addedToCartLayout, 200);
                                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                                            } else if (!(response.getBoolean("success"))) {
                                                Alerts.unableToProcessResponseAlert(MainActivity.this);
                                                System.out.println("Error Details: " + response.getString("error"));
                                            }
                                        } catch (JSONException e) { e.printStackTrace(); }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error instanceof NoConnectionError || error instanceof TimeoutError)
                                            Alerts.internetConnectionErrorAlert(MainActivity.this);
                                        else Alerts.unknownErrorAlert(MainActivity.this);
                                        System.out.println("Response Error: " + error);
                                    }
                                });
                                Swift.getInstance(MainActivity.this).addToRequestQueue(cartJsonObjectRequest);
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
                        JSONObject dataJson = response.getJSONObject("data");
                        combosJson = dataJson.getJSONArray("combos");
                        cartJson = dataJson.getJSONObject("cart");
                        viewPager.setAdapter(pagerAdapter);
                        viewPager.addOnPageChangeListener(MainActivity.this);
                    } else if (!response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(MainActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(MainActivity.this);
                else Alerts.unknownErrorAlert(MainActivity.this);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
        makeProfileRequest();

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

    private void logout() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/sessions/destroy", JsonProvider.getStandartRequestJson(MainActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
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
                    } else if(!(response.getBoolean("success"))) {
                        Alerts.commonErrorAlert(MainActivity.this, "Unable to Logout", "We are unable to sign you out. Try again later!", "Okay");
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError || error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(MainActivity.this);
                else Alerts.unknownErrorAlert(MainActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void makeProfileRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/profile", JsonProvider.getStandartRequestJson(MainActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        JSONObject userJson = dataJson.getJSONObject("user");
                        cacheEmailAndPhone(userJson.getString("email"), userJson.getString("mobile_no"));
                    } else if(response.getBoolean("success")) {
                        Alerts.unableToProcessResponseAlert(MainActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }); Swift.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    private void cacheEmailAndPhone(String email, String phone) {
        SharedPreferences sharedPreferences = getSharedPreferences("cache", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email",email);
        editor.putString("phone",phone);
        editor.apply();
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
