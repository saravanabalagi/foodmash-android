package in.foodmash.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


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
            case R.id.menu_wallet_cash: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
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
            @Override public boolean isViewFromObject(View view, Object object) { return view==((ScrollView) object); }
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
                                    JSONArray comboOptions = null;
                                    try { comboOptions = comboJson.getJSONArray("combo_options"); }
                                    catch (JSONException e) { e.printStackTrace(); }
                                    for (int j=0;j<comboOptions.length();j++) {
                                        JSONObject comboOptionsJson = null;
                                        LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, comboFoodLayout, false);
                                        try {
                                            comboOptionsJson = comboOptions.getJSONObject(j);
                                            ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                                            ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText(comboOptionsJson.getString("name"));
                                            ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText(comboOptionsJson.getString("description"));
                                        }
                                        catch (JSONException e) { e.printStackTrace(); }
                                        comboFoodLayout.addView(currentComboFoodLayout,j);
                                    }
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

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getString(R.string.api_root_path) + "/combos", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.out.println("Array Response: "+response);
                jsonArray = response;
                viewPager.setAdapter(pagerAdapter);
                viewPager.addOnPageChangeListener(MainActivity.this);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) Alerts.showInternetConnectionError(MainActivity.this);
                else Alerts.showUnknownError(MainActivity.this);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonArrayRequest);

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

    private void show(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    private void logout() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/sessions/destroy", JsonProvider.getStandartRequestJson(MainActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                    } else if(!(response.getBoolean("success"))) {
                        Alerts.showCommonErrorAlert(MainActivity.this,"Unable to Logout","We are unable to sign you out. Try again later!","Okay");
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError) Alerts.showInternetConnectionError(MainActivity.this);
                else Alerts.showUnknownError(MainActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }



}
