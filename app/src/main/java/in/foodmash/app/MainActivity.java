package in.foodmash.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;
import in.foodmash.app.custom.ComboDish;
import in.foodmash.app.custom.ComboOption;
import in.foodmash.app.custom.Dish;
import in.foodmash.app.custom.Restaurant;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view) NavigationView navigationView;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;

    private Intent intent;
    private TextView cartCount;
    private Cart cart = Cart.getInstance();
    private JsonObjectRequest getCombosRequest;
    private ImageLoader imageLoader;
    private DisplayMetrics displayMetrics;
    private ObjectMapper objectMapper;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private List<Combo.Category> categorySelected = new ArrayList<>();
    private List<Combo.Size> sizeSelected = new ArrayList<>();
    private List<Dish.Label> preferenceSelected = new ArrayList<>();

    private boolean categoryAll = true;
    private boolean sizeAll = true;
    private boolean preferenceAll = true;
    private boolean sortPriceLowToHigh = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Info.isLoggedIn(MainActivity.this)) getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        else getMenuInflater().inflate(R.menu.menu_main_anonymous_login, menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count);
        Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) return true;
        switch (item.getItemId()) {
            case R.id.menu_profile: intent = new Intent(this, ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this, AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this, OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this, ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: Actions.logout(MainActivity.this); return true;
            case R.id.menu_cart: intent = new Intent(this, CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Executing onCreate");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try { getSupportActionBar().setDisplayShowTitleEnabled(false); }
        catch (Exception e) { e.printStackTrace(); }

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();

        imageLoader = Swift.getInstance(MainActivity.this).getImageLoader();
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_navbar,
                R.string.close_navbar) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                try {updateFillLayout(Arrays.asList(objectMapper.readValue(Info.getComboJsonArrayString(MainActivity.this), Combo[].class))); }
                catch (Exception e) { e.printStackTrace(); }
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.category_all: categorySelected.clear(); break;
                    case R.id.size_all: sizeSelected.clear(); break;
                    case R.id.preference_all: preferenceSelected.clear(); break;

                    case R.id.category_regular: check(menuItem, Combo.Category.REGULAR); break;
                    case R.id.category_budget: check(menuItem, Combo.Category.BUDGET); break;
                    case R.id.category_corporate: check(menuItem, Combo.Category.CORPORATE); break;
                    case R.id.category_health: check(menuItem, Combo.Category.HEALTH); break;

                    case R.id.size_micro: check(menuItem, Combo.Size.MICRO); break;
                    case R.id.size_medium: check(menuItem, Combo.Size.MEDIUM); break;
                    case R.id.size_mega: check(menuItem, Combo.Size.MEGA); break;

                    case R.id.preference_egg: check(menuItem, Dish.Label.EGG); break;
                    case R.id.preference_veg: check(menuItem, Dish.Label.VEG); break;
                    case R.id.preference_non_veg: check(menuItem, Dish.Label.NON_VEG); break;

                    case R.id.price_low_to_high: sortPriceLowToHigh = true; menuItem.setChecked(true);
                    case R.id.price_high_to_low: sortPriceLowToHigh = false; menuItem.setChecked(true);

                }
                return true;
            }
            private void check(MenuItem menuItem, Combo.Category category) {
                if (menuItem.isChecked()) { categorySelected.remove(category); menuItem.setChecked(false); }
                else { categorySelected.add(category); menuItem.setChecked(true); }
            }
            private void check(MenuItem menuItem, Combo.Size size) {
                if (menuItem.isChecked()) { sizeSelected.remove(size); menuItem.setChecked(false); }
                else { sizeSelected.add(size); menuItem.setChecked(true); }
            }
            private void check(MenuItem menuItem, Dish.Label preference) {
                if (menuItem.isChecked()) { preferenceSelected.remove(preference); menuItem.setChecked(false); }
                else { preferenceSelected.add(preference); menuItem.setChecked(true); }
            }
        });

        getCombosRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/combos", getComboRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if (response.getBoolean("success")) {
                        Animations.fadeOut(fragmentContainer,100);
                        System.out.println(response.getJSONObject("data"));
                        String comboJsonArrayString = response.getJSONObject("data").getJSONArray("combos").toString();
                        updateFillLayout(Arrays.asList(objectMapper.readValue(comboJsonArrayString, Combo[].class)));
                        Actions.cacheCombos(MainActivity.this, comboJsonArrayString);
                    } else {
                        Alerts.requestUnauthorisedAlert(MainActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyFailureFragment()).commit();
                getSupportFragmentManager().executePendingTransactions();
                ((VolleyFailureFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                        .setJsonObjectRequest(getCombosRequest);
                System.out.println("Response Error: " + error);
            }
        });

        if(Info.getComboJsonArrayString(this) == null) {
            System.out.println("combos is empty");
            ((VolleyProgressFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                    .setLoadingText("Loading Combos...", "We are loading as fast as we can");
            Animations.fadeIn(fragmentContainer, 300);
        } else {
            try {updateFillLayout(Arrays.asList(objectMapper.readValue(Info.getComboJsonArrayString(this), Combo[].class))); }
            catch (Exception e) { e.printStackTrace(); }
        }
        Swift.getInstance(this).addToRequestQueue(getCombosRequest);
    }


    private JSONObject getComboRequestJson() {
        JSONObject comboRequestJson;
        if(Info.isLoggedIn(this)) comboRequestJson = JsonProvider.getStandardRequestJson(this);
        else comboRequestJson = JsonProvider.getAnonymousRequestJson(this);
        int packagingCentreId = Info.getPackagingCentreId(this);
        try { comboRequestJson.put("packaging_centre_id",packagingCentreId); }
        catch (Exception e) { e.printStackTrace(); }
        return comboRequestJson;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Exit App ?")
                .setMessage("We're sad to see you go. Do you really want to exit the app?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                }).setNegativeButton("No, lemme eat more", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void updateFillLayout(List<Combo> combos) {
        List<Combo> filteredCombos = applyFilters(combos);
        TreeMap<Integer, LinearLayout> comboTreeMap = new TreeMap<>();
        for (final Combo combo : filteredCombos) {
            View.OnClickListener showDescription = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(MainActivity.this, ComboDescriptionActivity.class);
                    intent.putExtra("combo_id", combo.getId());
                    startActivity(intent);
                }
            };
            final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_main_combo, fillLayout, false);
            ((TextView) comboLayout.findViewById(R.id.id)).setText(String.valueOf(combo.getId()));
            NetworkImageView comboPicture = (NetworkImageView) comboLayout.findViewById(R.id.image);
            comboPicture.setImageUrl(combo.getPicture(), imageLoader);
            comboPicture.getLayoutParams().height = displayMetrics.widthPixels/2 - (int)(10 * getResources().getDisplayMetrics().density);
            ((TextView) comboLayout.findViewById(R.id.name)).setText(combo.getName());
            comboLayout.findViewById(R.id.contents_scroll_layout).setOnClickListener(showDescription);
            LinearLayout contentsLayout = (LinearLayout) comboLayout.findViewById(R.id.contents_layout);
            TreeMap<Integer, Pair<String,Dish.Label>> contents = combo.getContents();
            for (int n : contents.navigableKeySet()) {
                LinearLayout contentTextView = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_main_combo_content, contentsLayout, false);
                String dishNameString = contents.get(n).first;
                Dish.Label dishLabel = contents.get(n).second;
                ImageView label = (ImageView) contentTextView.findViewById(R.id.label);
                switch (dishLabel) {
                    case EGG: label.setColorFilter(ContextCompat.getColor(this, R.color.egg)); break;
                    case VEG: label.setColorFilter(ContextCompat.getColor(this, R.color.veg)); break;
                    case NON_VEG: label.setColorFilter(ContextCompat.getColor(this, R.color.non_veg)); break;
                }
                ((TextView) contentTextView.findViewById(R.id.content)).setText(dishNameString);
                contentsLayout.addView(contentTextView);
            }
            ((TextView) comboLayout.findViewById(R.id.price)).setText(String.valueOf((int) combo.getPrice()));
            ImageView foodLabel = (ImageView) comboLayout.findViewById(R.id.label);
            switch (combo.getLabel()) {
                case EGG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.egg)); break;
                case VEG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.veg)); break;
                case NON_VEG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.non_veg)); break;
            }
            comboLayout.findViewById(R.id.clickable_layout).setOnClickListener(showDescription);
            comboLayout.findViewById(R.id.image).setOnClickListener(showDescription);
            final TextView addToCartLayout = (TextView) comboLayout.findViewById(R.id.add_to_cart_layout);
            final LinearLayout addedToCartLayout = (LinearLayout) comboLayout.findViewById(R.id.added_to_cart_layout);
            final LinearLayout countLayout = (LinearLayout) comboLayout.findViewById(R.id.count_layout);
            final TextView count = (TextView) countLayout.findViewById(R.id.count);
            int quantity = cart.getCount(combo.getId());
            count.setText(String.valueOf(quantity));
            if (quantity > 0) {
                addedToCartLayout.setVisibility(View.VISIBLE);
                addToCartLayout.setVisibility(View.GONE);
                countLayout.setVisibility(View.VISIBLE);
            }
            TextView plus = (TextView) countLayout.findViewById(R.id.plus);
            TextView minus = (TextView) countLayout.findViewById(R.id.minus);
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    count.setText(String.valueOf(cart.getCount(combo.getId())));
                    Actions.updateCartCount(cartCount);
                }
            });
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (count.getText().toString().equals("0")) return;
                    cart.decrementFromCart(combo);
                    count.setText(String.valueOf(cart.getCount(combo.getId())));
                    if (cart.getCount(combo.getId()) == 0) {
                        Animations.fadeOut(addedToCartLayout, 200);
                        Animations.fadeOut(countLayout, 200);
                        Animations.fadeIn(addToCartLayout, 200);
                    }
                    Actions.updateCartCount(cartCount);
                }
            });
            addToCartLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    Animations.fadeInOnlyIfInvisible(addedToCartLayout, 500);
                    Animations.fadeOut(addToCartLayout, 200);
                    Animations.fadeIn(countLayout, 200);
                    count.setText(String.valueOf(cart.getCount(combo.getId())));
                    Actions.updateCartCount(cartCount);
                }
            });

            LinearLayout restaurantsLayout = (LinearLayout) comboLayout.findViewById(R.id.restaurant_layout);
            HashSet<Restaurant> restaurantsList = new HashSet<>();
            for (ComboOption comboOption : combo.getComboOptions())
                if (comboOption.isFromSameRestaurant())
                    restaurantsList.add(comboOption.getComboOptionDishes().get(0).getDish().getRestaurant());
                else for (ComboDish comboDish : comboOption.getComboOptionDishes())
                    restaurantsList.add(comboDish.getDish().getRestaurant());
            for (ComboDish comboDish : combo.getComboDishes())
                restaurantsList.add(comboDish.getDish().getRestaurant());
            for (Restaurant restaurant : restaurantsList) {
                LinearLayout restaurantLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_restaurant_logo, restaurantsLayout, false);
                ((TextView) restaurantLayout.findViewById(R.id.name)).setText(restaurant.getName());
                ((NetworkImageView) restaurantLayout.findViewById(R.id.logo)).setImageUrl(restaurant.getLogo(), imageLoader);
                restaurantsLayout.addView(restaurantLayout);
            }

            comboTreeMap.put((int) combo.getPrice(), comboLayout);
        }

        fillLayout.removeAllViews();
        if(sortPriceLowToHigh)
            for (int n : comboTreeMap.navigableKeySet())
                fillLayout.addView(comboTreeMap.get(n));
        else for (int n : comboTreeMap.descendingKeySet())
            fillLayout.addView(comboTreeMap.get(n));
    }

    private List<Combo> applyFilters(List<Combo> combos) {
        List<Combo> filteredComboList = new ArrayList<>();
        for(Combo combo: combos) {
            boolean survived = true;
            if(!categoryAll && !categorySelected.contains(combo.getCategory())) survived = false;
            if(!sizeAll && !sizeSelected.contains(combo.getSize())) survived = false;
            if(!preferenceAll && !preferenceSelected.contains(combo.getLabel())) survived = false;
            if(survived) filteredComboList.add(combo);
        }
        System.out.println("Filtered list: "+filteredComboList);
        return filteredComboList;
    }
}
