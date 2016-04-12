package in.foodmash.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Filters;
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
import in.foodmash.app.utils.DateUtils;

public class MainActivity extends FoodmashActivity {

    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.empty_combo_layout) LinearLayout emptyComboLayout;
    @Bind(R.id.filter) FloatingActionButton filterFab;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.filters) RecyclerView filtersRecyclerView;
    @Bind(R.id.combos) RecyclerView combosRecyclerView;

    @Bind(R.id.apply_filters) TextView applyFilters;
    @Bind(R.id.remove_all_filters) TextView removeFilters;
    @Bind(R.id.filter_combos_text) TextView filterCombosText;
    @Bind(R.id.no_of_filters) TextView noOfFilters;
    @Bind(R.id.no_of_filters_applied_text) TextView noOfFiltersAppliedText;
    @Bind(R.id.no_of_filters_layout) LinearLayout noOfFiltersLayout;

    private Snackbar snackbar;
    private Intent intent;
    private TextView cartCount;
    private Cart cart = Cart.getInstance();
    private ImageLoader imageLoader;
    private DisplayMetrics displayMetrics;
    private ObjectMapper objectMapper;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Filters filters;
    private CombosAdapter combosAdapter;
    private GestureDetector tapGesture;

    private Set<Combo.Category> categorySelected = new HashSet<>();
    private Set<Combo.Size> sizeSelected = new HashSet<>();
    private Set<Dish.Label> preferenceSelected = new HashSet<>();
    private boolean sortPriceLowToHigh = true;

    Handler handler = new Handler();
    Random random = new Random();

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
            case R.id.menu_login: intent = new Intent(this, LoginActivity.class); startActivity(intent); return true;
            case R.id.menu_cart: intent = new Intent(this, CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try { getSupportActionBar().setDisplayShowTitleEnabled(false); }
        catch (Exception e) { Actions.handleIgnorableException(this,e); }

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();

        imageLoader = Swift.getInstance(MainActivity.this).getImageLoader();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        tapGesture = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onSingleTapUp(MotionEvent e) { return true; } });
        filters = new Filters();

        filters.addHeader("Deliver to");
        filters.addFilter(Info.getAreaName(this), R.drawable.svg_marker_filled);

        filters.addHeader("Type");
        filters.addFilter("Regular", R.drawable.svg_hashtag);
        filters.addFilter("Budget", R.drawable.svg_coffee);
        filters.addFilter("Corporate", R.drawable.svg_sitemap);
        filters.addFilter("Health", R.drawable.svg_heartbeat);

        filters.addHeader("Size");
        filters.addFilter("Micro", R.drawable.svg_user1);
        filters.addFilter("Medium", R.drawable.svg_user2);
        filters.addFilter("Mega", R.drawable.svg_user3);

        filters.addHeader("Preference");
        filters.addFilter("Veg", R.drawable.svg_leaf);
        filters.addFilter("Egg", R.drawable.svg_egg);
        filters.addFilter("Non-Veg", R.drawable.svg_meat);

        filters.addHeader("Price");
        filters.addFilter("Low to High", R.drawable.svg_sort_amount_asc);
        filters.addFilter("High to Low", R.drawable.svg_sort_amount_desc);

        filters.setSelected(1);
        filters.setSelected(16);

        filtersRecyclerView.setHasFixedSize(true);
        filtersRecyclerView.setAdapter(filters);
        filtersRecyclerView.setLayoutManager(linearLayoutManager);
        filtersRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private void makeActive(View view, Integer position, Combo.Category category) {
                if (view.isActivated()) { categorySelected.remove(category); filters.removeSelected(position); }
                else { categorySelected.add(category); filters.setSelected(position); }
                filters.notifyItemChanged(position);
            }
            private void makeActive(View view, Integer position, Combo.Size size) {
                if (view.isActivated()) { sizeSelected.remove(size); filters.removeSelected(position); }
                else { sizeSelected.add(size); filters.setSelected(position); }
                filters.notifyItemChanged(position);
            }
            private void makeActive(View view, Integer position, Dish.Label preference) {
                if (view.isActivated()) { preferenceSelected.remove(preference); filters.removeSelected(position); }
                else { preferenceSelected.add(preference); filters.setSelected(position); }
                filters.notifyItemChanged(position);
            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = filtersRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if(child!=null && tapGesture.onTouchEvent(e)) {
                    int position = filtersRecyclerView.getChildAdapterPosition(child);
                    switch(position) {
                        case 1: startActivity(new Intent(MainActivity.this, SplashActivity.class)); break;

                        case 3: makeActive(child, position, Combo.Category.REGULAR); break;
                        case 4: makeActive(child, position, Combo.Category.BUDGET); break;
                        case 5: makeActive(child, position, Combo.Category.CORPORATE); break;
                        case 6: makeActive(child, position, Combo.Category.HEALTH); break;

                        case 8: makeActive(child, position, Combo.Size.MICRO); break;
                        case 9: makeActive(child, position, Combo.Size.MEDIUM); break;
                        case 10: makeActive(child, position, Combo.Size.MEGA); break;

                        case 12: makeActive(child, position, Dish.Label.VEG); break;
                        case 13: makeActive(child, position, Dish.Label.EGG); break;
                        case 14: makeActive(child, position, Dish.Label.NON_VEG); break;

                        case 16: sortPriceLowToHigh = true; if (!child.isActivated()) { filters.setSelected(position); filters.notifyItemChanged(position); filters.removeSelected(17); filters.notifyItemChanged(17); } break;
                        case 17: sortPriceLowToHigh = false; if (!child.isActivated()) { filters.setSelected(position); filters.notifyItemChanged(position); filters.removeSelected(16); filters.notifyItemChanged(16); } break;
                    }
                }
                return false;
            }

            @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) { }
            @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_navbar,
                R.string.close_navbar) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.i("Filters",categorySelected.toString());
                Log.i("Filters",sizeSelected.toString());
                Log.i("Filters",preferenceSelected.toString());
                try {updateFillLayout(Arrays.asList(objectMapper.readValue(Info.getComboJsonArrayString(MainActivity.this), Combo[].class))); }
                catch (Exception e) { Actions.handleIgnorableException(MainActivity.this,e); }
            }
        };

        applyFilters.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { drawerLayout.closeDrawer(Gravity.LEFT); } });
        removeFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.clearAllSelected();
                filters.setSelected(1);
                filters.setSelected(16);
                filters.notifyDataSetChanged();
                categorySelected.clear();
                sizeSelected.clear();
                preferenceSelected.clear();
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        filterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerOpen(Gravity.LEFT)) drawerLayout.closeDrawer(Gravity.LEFT);
                else drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        combosAdapter = new CombosAdapter();
        combosRecyclerView.hasFixedSize();
        combosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        combosRecyclerView.setAdapter(combosAdapter);
        combosRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int scrollDy = 0;
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) { super.onScrollStateChanged(recyclerView, newState); }
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollDy += dy;
                swipeRefreshLayout.setEnabled(scrollDy == 0);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() { @Override public void onRefresh() { onResume(); } });
        swipeRefreshLayout.setEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        invalidateOptionsMenu();

        filters.changeLocation(Info.getAreaName(this));
        filters.notifyDataSetChanged();

        Date comboUpdatedAt;
        boolean areCombosOutdated = true;
        if(Info.getComboUpdatedAtDate(this)!=null) {
            try {
                comboUpdatedAt = DateUtils.railsDateStringToJavaDate(Info.getComboUpdatedAtDate(this));
                areCombosOutdated = (DateUtils.howOldInHours(comboUpdatedAt) > 6);
            } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        }
        if(Info.getComboJsonArrayString(this)==null || areCombosOutdated) {
            ((VolleyProgressFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                    .setLoadingText("Loading Combos...", "We are loading as fast as we can");
            Animations.fadeIn(fragmentContainer, 300);
        } else {
            try {updateFillLayout(Arrays.asList(objectMapper.readValue(Info.getComboJsonArrayString(this), Combo[].class))); }
            catch (Exception e) { e.printStackTrace(); Actions.handleIgnorableException(this,e); }
            snackbar = Snackbar.make(mainLayout, "Updating combos...", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
        makeComboRequest();

    }

    public void makeComboRequest() {
        JsonObjectRequest getCombosRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/combos", getComboRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipeRefreshLayout.setRefreshing(false);
                try {
                    if (response.getBoolean("success")) {
                        if (snackbar!=null && snackbar.isShown()) snackbar.dismiss();
                        Animations.fadeOut(fragmentContainer,100);
                        Log.i("Combos", response.getJSONObject("data").getJSONArray("combos").length() + " combos found");
                        String comboJsonArrayString = response.getJSONObject("data").getJSONArray("combos").toString();
                        updateFillLayout(Arrays.asList(objectMapper.readValue(comboJsonArrayString, Combo[].class)));
                        Actions.cacheCombos(MainActivity.this, comboJsonArrayString, new Date());
                    } else Snackbar.make(mainLayout,"Unable to load combos: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { e.printStackTrace(); Actions.handleIgnorableException(MainActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (snackbar!=null && snackbar.isShown()) snackbar.setText("Update Failed!");
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyFailureFragment()).commit();
                    getSupportFragmentManager().executePendingTransactions();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeComboRequest")).commit();
                }
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        Swift.getInstance(this).addToRequestQueue(getCombosRequest, 20000, 2, 1.0f);
    }


    private JSONObject getComboRequestJson() {
        JSONObject comboRequestJson;
        if(Info.isLoggedIn(this)) comboRequestJson = JsonProvider.getStandardRequestJson(this);
        else comboRequestJson = JsonProvider.getAnonymousRequestJson(this);
        int packagingCentreId = Info.getPackagingCentreId(this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("packaging_centre_id",packagingCentreId);
            comboRequestJson.put("data", dataJson);
        }
        catch (Exception e) { Actions.handleIgnorableException(this,e); }
        return comboRequestJson;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(true)
                .setTitle("Exit App ?")
                .setMessage("We're sad to see you go. Do you really want to exit the app?");
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE ,"Exit", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { MainActivity.super.onBackPressed(); } });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"No, lemme eat more", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { alertDialog.dismiss(); } });
        alertDialog.show();
    }

    private void updateFillLayout(List<Combo> combos) {
        final List<Combo> filteredCombos = applyFilters(combos);
        if(filteredCombos.size()==0) { emptyComboLayout.setVisibility(View.VISIBLE); combosRecyclerView.setVisibility(View.GONE); return; }
        else { emptyComboLayout.setVisibility(View.GONE); combosRecyclerView.setVisibility(View.VISIBLE); }

        combosAdapter.setCombos(filteredCombos);
        combosAdapter.notifyDataSetChanged();
    }

    private List<Combo> applyFilters(List<Combo> combos) {

        Collections.sort(combos, new Comparator<Combo>() {
            @Override
            public int compare(Combo lhs, Combo rhs) {
                if(sortPriceLowToHigh) return Float.compare(lhs.getPrice(), rhs.getPrice());
                else return Float.compare(rhs.getPrice(), lhs.getPrice());
            }
        });

        if(categorySelected.isEmpty() && sizeSelected.isEmpty() && preferenceSelected.isEmpty()) {
            filterCombosText.setVisibility(View.VISIBLE);
            noOfFiltersLayout.setVisibility(View.GONE);
            return combos;
        }

        filterCombosText.setVisibility(View.GONE);
        noOfFiltersLayout.setVisibility(View.VISIBLE);
        int noOfFiltersInt = categorySelected.size()+sizeSelected.size()+preferenceSelected.size();
        if(noOfFiltersInt==1) noOfFiltersAppliedText.setText("Filter Applied");
        else noOfFiltersAppliedText.setText("Filters Applied");
        noOfFilters.setText(String.valueOf(noOfFiltersInt));

        List<Combo> filteredComboList = new ArrayList<>();
        for(Combo combo: combos) {
            boolean survived = true;
            if(!categorySelected.isEmpty() && !categorySelected.contains(combo.getCategory())) survived = false;
            if(!sizeSelected.isEmpty() && !sizeSelected.contains(combo.getSize())) survived = false;
            if(!preferenceSelected.isEmpty() && !preferenceSelected.contains(combo.getLabel())) survived = false;
            if(survived) filteredComboList.add(combo);
        }
        return filteredComboList;
    }

    private class CombosAdapter extends RecyclerView.Adapter {
        List<Combo> combos = new ArrayList<>();
        public void setCombos(List<Combo> combos) { this.combos = combos; }
        @Override public int getItemCount() { return combos.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView id;
            TextView name;
            TextView price;
            ViewPager imageSlider;
            ImageView comboSizeIcon;
            TextView groupSize;
            TextView count;
            TextView plus;
            LinearLayout comboOverlayLayout;
            TextView addToCartLayout;
            TextView minus;
            LinearLayout countLayout;
            LinearLayout contentsLayout;
            TextView view;
            LinearLayout addedToCartLayout;
            TextView viewComboSeparateButton;
            LinearLayout clickableLayout;
            LinearLayout viewOrCartLayout;
            LinearLayout restaurantsLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                id = (TextView) itemView.findViewById(R.id.id);
                name = (TextView) itemView.findViewById(R.id.name);
                price = (TextView) itemView.findViewById(R.id.price);
                imageSlider = (ViewPager) itemView.findViewById(R.id.imageSlider);
                comboSizeIcon = (ImageView) itemView.findViewById(R.id.combo_size_icon);
                groupSize = (TextView) itemView.findViewById(R.id.group_size);
                addToCartLayout = (TextView) itemView.findViewById(R.id.add_to_cart_layout);
                comboOverlayLayout = (LinearLayout) itemView.findViewById(R.id.combo_overlay_layout);
                countLayout = (LinearLayout) itemView.findViewById(R.id.count_layout);
                count = (TextView) itemView.findViewById(R.id.count);
                plus = (TextView) itemView.findViewById(R.id.plus);
                minus = (TextView) itemView.findViewById(R.id.minus);
                contentsLayout = (LinearLayout) itemView.findViewById(R.id.contents_layout);
                view = (TextView) itemView.findViewById(R.id.view);
                viewComboSeparateButton = (TextView) itemView.findViewById(R.id.view_combo_separate_button);
                clickableLayout = (LinearLayout) itemView.findViewById(R.id.clickable_layout);
                viewOrCartLayout = (LinearLayout) itemView.findViewById(R.id.view_or_cart_layout);
                addedToCartLayout = (LinearLayout) itemView.findViewById(R.id.added_to_cart_layout);
                restaurantsLayout = (LinearLayout) itemView.findViewById(R.id.restaurant_layout);
            }
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.repeatable_main_combo, parent, false)); }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final Combo combo = combos.get(position);
            View.OnClickListener showDescription = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(MainActivity.this, ComboDescriptionActivity.class);
                    intent.putExtra("combo_id", combo.getId());
                    startActivity(intent);
                }
            };
            viewHolder.id.setText(String.valueOf(combo.getId()));
            viewHolder.imageSlider.getLayoutParams().height = displayMetrics.widthPixels/2 - (int)(10 * MainActivity.this.getResources().getDisplayMetrics().density);
            viewHolder.imageSlider.setAdapter(new NetworkImageViewSlider(MainActivity.this, combo.getImages(), showDescription));
            viewHolder.imageSlider.setOffscreenPageLimit(1);
            viewHolder.name.setText(combo.getName());
            viewHolder.contentsLayout.setOnClickListener(showDescription);
            viewHolder.contentsLayout.removeAllViews();
            ArrayList<Pair<String,Dish.Label>> contents = combo.getContents();
            for (Pair<String, Dish.Label> labelPair: contents) {
                LinearLayout contentTextView = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_main_combo_content, viewHolder.contentsLayout, false);
                String dishNameString = labelPair.first;
                Dish.Label dishLabel = labelPair.second;
                ImageView label = (ImageView) contentTextView.findViewById(R.id.label);
                switch (dishLabel) {
                    case EGG: label.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.egg)); break;
                    case VEG: label.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.veg)); break;
                    case NON_VEG: label.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.non_veg)); break;
                }
                ((TextView) contentTextView.findViewById(R.id.content)).setText(dishNameString);
                viewHolder.contentsLayout.addView(contentTextView);
            }
            viewHolder.price.setText(String.valueOf((int) combo.getPrice()));
            switch (combo.getGroupSize()) {
                case 1: viewHolder.comboSizeIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.svg_user1)); break;
                case 2: viewHolder.comboSizeIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.svg_user2)); break;
                case 3: viewHolder.comboSizeIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.svg_user2)); break;
                default: viewHolder.comboSizeIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.svg_user3)); break;
            }
            viewHolder.groupSize.setText(String.valueOf(combo.getGroupSize()));
            if(combo.getGroupSize()==1) viewHolder.groupSize.setVisibility(View.GONE);
            viewHolder.view.setOnClickListener(showDescription);
            viewHolder.viewComboSeparateButton.setOnClickListener(showDescription);
//            viewHolder.clickableLayout.setOnClickListener(showDescription);
//            viewHolder.imageSlider.setOnClickListener(showDescription);
            if(combo.isAvailable())  {
                viewHolder.viewOrCartLayout.setVisibility(View.VISIBLE);
                viewHolder.viewComboSeparateButton.setVisibility(View.GONE);
                viewHolder.comboOverlayLayout.setVisibility(View.GONE);
            } else {
                viewHolder.viewOrCartLayout.setVisibility(View.GONE);
                viewHolder.viewComboSeparateButton.setVisibility(View.VISIBLE);
                viewHolder.comboOverlayLayout.setVisibility(View.VISIBLE);
            }
            int quantity = cart.getCount(combo.getId());
            if(!combo.isAvailable()) cart.removeOrder(combo);
            viewHolder.count.setText(String.valueOf(quantity));
            if (quantity > 0) {
                viewHolder.addedToCartLayout.setVisibility(View.VISIBLE);
                viewHolder.addToCartLayout.setVisibility(View.GONE);
                viewHolder.countLayout.setVisibility(View.VISIBLE);
            }
            viewHolder.plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    viewHolder.count.setText(String.valueOf(cart.getCount(combo.getId())));
                    Actions.updateCartCount(cartCount);
                }
            });
            viewHolder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.count.getText().toString().equals("0")) return;
                    cart.decrementFromCart(combo.getId());
                    viewHolder.count.setText(String.valueOf(cart.getCount(combo.getId())));
                    if (cart.getCount(combo.getId()) == 0) {
                        Animations.fadeOut(viewHolder.addedToCartLayout, 200);
                        Animations.fadeOut(viewHolder.countLayout, 200);
                        Animations.fadeIn(viewHolder.addToCartLayout, 200);
                    }
                    Actions.updateCartCount(cartCount);
                }
            });
            viewHolder.addToCartLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    Animations.fadeInOnlyIfInvisible(viewHolder.addedToCartLayout, 500);
                    Animations.fadeOut(viewHolder.addToCartLayout, 200);
                    Animations.fadeIn(viewHolder.countLayout, 200);
                    viewHolder.count.setText(String.valueOf(cart.getCount(combo.getId())));
                    Actions.updateCartCount(cartCount);
                }
            });

            viewHolder.restaurantsLayout.removeAllViews();
            HashSet<Restaurant> restaurantsList = new HashSet<>();
            for (ComboOption comboOption : combo.getComboOptions())
                if (comboOption.isFromSameRestaurant())
                    restaurantsList.add(comboOption.getComboOptionDishes().get(0).getDish().getRestaurant());
                else for (ComboDish comboDish : comboOption.getComboOptionDishes())
                    restaurantsList.add(comboDish.getDish().getRestaurant());
            for (ComboDish comboDish : combo.getComboDishes())
                restaurantsList.add(comboDish.getDish().getRestaurant());
            for (Restaurant restaurant : restaurantsList) {
                LinearLayout restaurantLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_restaurant_logo, viewHolder.restaurantsLayout, false);
                ((TextView) restaurantLayout.findViewById(R.id.name)).setText(restaurant.getName());
                ((NetworkImageView) restaurantLayout.findViewById(R.id.logo)).setImageUrl(restaurant.getLogo(), imageLoader);
                viewHolder.restaurantsLayout.addView(restaurantLayout);
            }

        }
    }

    private class NetworkImageViewSlider extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        View.OnClickListener onClickListener;
        ArrayList<String> imageUrls = new ArrayList<>();
        public NetworkImageViewSlider(Context context, ArrayList<String> imageUrls, View.OnClickListener onClickListener) {
            layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.context = context;
            this.imageUrls = imageUrls;
            this.onClickListener = onClickListener;
        }
        @Override public int getCount() { return imageUrls.size(); }
        @Override public boolean isViewFromObject(View view, Object object) { return view == ((FrameLayout)object); }
        @Override public void destroyItem(ViewGroup container, int position, Object object) { container.removeView((FrameLayout)object); }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = layoutInflater.inflate(R.layout.repeatable_main_combo_image_slider, container, false);
            NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.image);
            imageView.setImageUrl(imageUrls.get(position), Swift.getInstance(context).getImageLoader());
            imageView.setOnClickListener(onClickListener);
            container.addView(view);
            return view;
        }
    }
}
