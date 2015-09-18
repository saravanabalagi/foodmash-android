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

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;
import in.foodmash.app.custom.ComboDish;
import in.foodmash.app.custom.ComboOption;
import in.foodmash.app.custom.ImmutableCombo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private Intent intent;

    private ImageView offers;
    private ImageView for_1;
    private ImageView for_2;
    private ImageView for_3;

    private ImageView offers_focus;
    private ImageView for_1_focus;
    private ImageView for_2_focus;
    private ImageView for_3_focus;

    private ViewPager viewPager;
    private List<Combo> combos;
    private Cart cart = Cart.getInstance();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        System.out.println("Resumed");
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
    protected void onCreate(final Bundle savedInstanceState) {
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
                TreeMap<Integer,LinearLayout> comboTreeMap = new TreeMap<>();

                for (final Combo combo: combos) {

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
                    final TreeMap<Integer,LinearLayout> layoutOrderTreeMap = new TreeMap<>();

                    for (final ComboOption comboOption: combo.getComboOptions()) {
                        final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, comboFoodLayout, false);
                        ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                        ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText(comboOption.getName());
                        ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText(comboOption.getDescription());
                        final LinearLayout optionsLayout = (LinearLayout) currentComboFoodLayout.findViewById(R.id.options_layout);

                        int i=0;
                        for (final ComboDish comboDish: comboOption.getComboOptionDishes()) {
                            LinearLayout comboOptionsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food_options, currentComboFoodLayout, false);
                            final ImageView selected = (ImageView) comboOptionsLayout.findViewById(R.id.selected);
                            if (i==0 && comboOption.getSelected()==0) { comboOption.setSelected(comboDish.getId()); selected.setVisibility(View.VISIBLE); }
                            else if(comboOption.getSelected()==comboDish.getId()) { selected.setVisibility(View.VISIBLE); }
                            ((TextView) comboOptionsLayout.findViewById(R.id.option_name)).setText(comboDish.getDish().getName());
                            ((TextView) comboOptionsLayout.findViewById(R.id.restaurant_name)).setText(comboDish.getDish().getRestaurant().getName());
                            comboOptionsLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    comboOption.setSelected(comboDish.getId());
                                    for (int l = 0; l < comboOption.getComboOptionDishes().size(); l++)
                                        optionsLayout.getChildAt(l).findViewById(R.id.selected).setVisibility(View.INVISIBLE);
                                    selected.setVisibility(View.VISIBLE);
                                }
                            });
                            if (i == comboOption.getComboOptionDishes().size() - 1)
                                Animations.bottomMargin(comboOptionsLayout, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), 0);
                            optionsLayout.addView(comboOptionsLayout);
                            i++;
                        }
                        if (comboOption.isFromSameRestaurant()) {
                            for (int m = 0; m < comboOption.getComboOptionDishes().size(); m++)
                                optionsLayout.getChildAt(m).findViewById(R.id.restaurant_layout).setVisibility(View.GONE);
                            ((TextView) currentComboFoodLayout.findViewById(R.id.restaurant_name)).setText(comboOption.getComboOptionDishes().get(0).getDish().getRestaurant().getName());
                        } else currentComboFoodLayout.findViewById(R.id.restaurant_layout).setVisibility(View.GONE);
                        layoutOrderTreeMap.put(comboOption.getPriority(), currentComboFoodLayout);
                    }
                    for (ComboDish comboDish: combo.getComboDishes()) {
                        final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, comboFoodLayout, false);
                        ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                        ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText(comboDish.getDish().getName());
                        ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText(comboDish.getDish().getDescription());
                        ((TextView) currentComboFoodLayout.findViewById(R.id.restaurant_name)).setText(comboDish.getDish().getRestaurant().getName());
                        layoutOrderTreeMap.put(comboDish.getPriority(), currentComboFoodLayout);
                    }
                    comboLayout.findViewById(R.id.clickable_layout).setOnClickListener(new View.OnClickListener() {
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
                    int quantity = cart.hasHowMany(combo.getId());
                    count.setText(String.valueOf(quantity));
                    if (quantity>0) { addedToCartLayout.setVisibility(View.VISIBLE); addToCart.setVisibility(View.GONE); countLayout.setVisibility(View.VISIBLE); }
                    ImageView plus = (ImageView) countLayout.findViewById(R.id.plus);
                    ImageView minus = (ImageView) countLayout.findViewById(R.id.minus);
                    plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cart.addToCart(new ImmutableCombo(combo));
                            count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                        }
                    });
                    minus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(count.getText().toString().equals("0")) return;
                            cart.decrementFromCart(new ImmutableCombo(combo));
                            count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                            if(Integer.parseInt(count.getText().toString())==0) {
                                Animations.fadeOut(addedToCartLayout, 200);
                                Animations.fadeOut(countLayout,200);
                                Animations.fadeIn(addToCart,200);
                            }
                        }
                    });
                    addToCart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cart.addToCart(new ImmutableCombo(combo));
                            Animations.fadeInOnlyIfInvisible(addedToCartLayout, 200);
                            Animations.fadeOut(addToCart, 200);
                            Animations.fadeIn(countLayout,200);
                            count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                        }
                    });
                    comboTreeMap.put(combo.getIntPrice(), comboLayout);
                }
                for (int n : comboTreeMap.navigableKeySet()) {
                    linearLayout.addView(comboTreeMap.get(n));
                    System.out.println("Tree contains" + String.valueOf(n));
                }
                scrollView.addView(linearLayout);
                container.addView(scrollView);
                return scrollView;
            }
        };

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/combos", JsonProvider.getStandardRequestJson(MainActivity.this) ,new Response.Listener<JSONObject>() {
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
                        Alerts.requestUnauthorisedAlert(MainActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
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
        return new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/sessions/destroy", JsonProvider.getStandardRequestJson(this), new Response.Listener<JSONObject>() {
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
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("session", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged_in", false);
        editor.remove("user_token");
        editor.remove("session_token");
        editor.remove("android_token");
        editor.apply();
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


}
