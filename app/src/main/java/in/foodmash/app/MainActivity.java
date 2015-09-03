package in.foodmash.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.MailTo;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.NoConnectionError; import com.android.volley.TimeoutError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    JSONArray jsonArray;

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
            case R.id.menu_log_out:  logout(); return true;
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
                for (int i=0;i<jsonArray.length();i++) {
                    final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo, linearLayout, false);
                    try {
                        final JSONObject comboJson = jsonArray.getJSONObject(i);
                        if (Integer.parseInt(comboJson.getString("group_size"))!=position) { continue; }
                        ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                        ((TextView) comboLayout.findViewById(R.id.name)).setText(comboJson.getString("name"));
                        ((TextView) comboLayout.findViewById(R.id.description)).setText(comboJson.getString("description"));
                        ((TextView) comboLayout.findViewById(R.id.price)).setText(String.format("%.0f", Float.parseFloat(comboJson.getString("price"))));
                        final LinearLayout comboFoodLayout = (LinearLayout) comboLayout.findViewById(R.id.food_items_layout);
                        comboLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(comboFoodLayout.getChildCount()==0) {
                                    JSONArray comboOptions;
                                    try {
                                        comboOptions = comboJson.getJSONArray("combo_options");
                                        for (int j = 0; j < comboOptions.length(); j++) {
                                            JSONObject comboOptionsJson;
                                            LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, comboFoodLayout, false);
                                            try {
                                                comboOptionsJson = comboOptions.getJSONObject(j);
                                                ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                                                ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText(comboOptionsJson.getString("name"));
                                                ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText(comboOptionsJson.getString("description"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            comboFoodLayout.addView(currentComboFoodLayout, j);
                                        }
                                    } catch (JSONException e) { e.printStackTrace(); }
                                } else comboFoodLayout.removeAllViews();
                            }
                        });
                        LinearLayout addToCart = (LinearLayout) comboLayout.findViewById(R.id.add_to_cart);
                        final LinearLayout addedToCartLayout = (LinearLayout) comboLayout.findViewById(R.id.added_to_cart_layout);
                        final TextView count = (TextView) addedToCartLayout.findViewById(R.id.count);
                        ImageView plus = (ImageView) addedToCartLayout.findViewById(R.id.plus);
                        ImageView minus = (ImageView) addedToCartLayout.findViewById(R.id.minus);
                        plus.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { count.setText(String.valueOf(Integer.parseInt(count.getText().toString())+1)); } });
                        minus.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { count.setText(String.valueOf(Integer.parseInt(count.getText().toString())-1)); if(Integer.parseInt(count.getText().toString())==0) Animations.fadeOut(addedToCartLayout,200); } });
                        addToCart.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
                            comboFoodLayout.removeAllViews();
                            if(addedToCartLayout.getVisibility()==View.GONE) Animations.fadeIn(addedToCartLayout,200);
                            count.setText(String.valueOf(Integer.parseInt(count.getText().toString())+1));} });
                    } catch (JSONException e) { e.printStackTrace(); }
                    linearLayout.addView(comboLayout);
                }
                scrollView.addView(linearLayout);
                container.addView(scrollView);
                return scrollView;
            }
        };

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.api_root_path) + "/combos",new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        jsonArray = response.getJSONArray("data");
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
                        SharedPreferences sharedPreferences = getSharedPreferences("session",0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("logged_in", false);
                        editor.remove("user_token");
                        editor.remove("session_token");
                        editor.remove("android_token");
                        editor.commit();
                        intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
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
                        cacheEmailAndPhone(userJson.getString("email"),userJson.getString("mobile_no"));
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
        SharedPreferences sharedPreferences = getSharedPreferences("cache",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email",email);
        editor.putString("phone",phone);
        editor.apply();
    }


}
