package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;
import in.foodmash.app.custom.ComboDish;
import in.foodmash.app.custom.ComboOption;

/**
 * Created by Zeke on Sep 30 2015.
 */
public class ComboDescriptionActivity extends FoodmashActivity implements View.OnClickListener {

    @Bind(R.id.main_layout) View mainLayout;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.price) TextView currentPrice;
    @Bind(R.id.combo_unavailable) TextView comboUnavailable;
    @Bind(R.id.buy) FloatingActionButton buy;
    @Bind(R.id.back) FloatingActionButton back;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private TextView cartCount;
    private Cart cart = Cart.getInstance();
    private Intent intent;
    private Combo combo;
    private ImageLoader imageLoader;
    private DisplayMetrics displayMetrics;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Info.isLoggedIn(ComboDescriptionActivity.this)) getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        else getMenuInflater().inflate(R.menu.menu_main_anonymous_login, menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count);
        Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ComboDescriptionActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile: intent = new Intent(this, ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this, AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this, OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this, ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: Actions.logout(ComboDescriptionActivity.this); return true;
            case R.id.menu_cart: intent = new Intent(this, CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_description);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Combo","contents");

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        int comboId = getIntent().getIntExtra("combo_id", -1);
        if(comboId==-1) { Snackbar.make(mainLayout,"Something went wrong. Try again later!",Snackbar.LENGTH_LONG).show(); return; }
        List<Combo> combos = null;
        try { combos = Arrays.asList(objectMapper.readValue(Info.getComboJsonArrayString(this), Combo[].class)); }
        catch (Exception e) { Actions.handleIgnorableException(this,e); }
        for (Combo c : combos)
            if (c.getId()==comboId)
                combo = c;
        if(combo==null) { Snackbar.make(mainLayout,"Something went wrong. Try again later!",Snackbar.LENGTH_LONG).show(); return; }

        if (!combo.isAvailable()) {
            comboUnavailable.setVisibility(View.VISIBLE);
            buy.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
        }
        buy.setOnClickListener(this);
        back.setOnClickListener(this);
        imageLoader = Swift.getInstance(ComboDescriptionActivity.this).getImageLoader();
        updateFillLayout();
    }

    private void updateFillLayout() {

        fillLayout.removeAllViews();
        final ArrayList<Pair<Object,LinearLayout>> layoutOrderArrayList = new ArrayList<>();

        for (final ComboOption comboOption: combo.getComboOptions()) {
            final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_combo_description_combo_option, fillLayout, false);
            final LinearLayout optionsLayout = (LinearLayout) currentComboFoodLayout.findViewById(R.id.combo_dishes_layout);

            for (final ComboDish comboDish: comboOption.getComboOptionDishes()) {
                final LinearLayout comboOptionsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_combo_description_combo_option_dish, currentComboFoodLayout, false);
                final ImageView selected = (ImageView) comboOptionsLayout.findViewById(R.id.selected);
                if (comboOption.getSelectedComboOptionDishes().contains(comboDish)) { Animations.fadeIn(selected, 500); }
                else { Animations.fadeOut(selected, 500); }
                ImageView foodLabel = (ImageView) comboOptionsLayout.findViewById(R.id.label);
                switch(comboDish.getDish().getLabel()) {
                    case EGG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.egg)); break;
                    case VEG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.veg)); break;
                    case NON_VEG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.non_veg)); break;
                }
                ((TextView) comboOptionsLayout.findViewById(R.id.id)).setText(String.valueOf(comboDish.getId()));
                ((TextView) comboOptionsLayout.findViewById(R.id.name)).setText(comboDish.getDish().getName());
                NetworkImageView comboDishPicture = (NetworkImageView) comboOptionsLayout.findViewById(R.id.image);
                comboDishPicture.setImageUrl(comboDish.getDish().getPicture(), imageLoader);
                comboDishPicture.getLayoutParams().height = displayMetrics.widthPixels/2 - (int)(10 * getResources().getDisplayMetrics().density);
                ((TextView) comboOptionsLayout.findViewById(R.id.description)).setText(comboDish.getDish().getDescription());
                ((TextView) comboOptionsLayout.findViewById(R.id.restaurant_name)).setText(comboDish.getDish().getRestaurant().getName());
                ((NetworkImageView) comboOptionsLayout.findViewById(R.id.restaurant_logo)).setImageUrl(comboDish.getDish().getRestaurant().getLogo(), imageLoader);
                final TextView addExtraLayout = (TextView) comboOptionsLayout.findViewById(R.id.add_extra);
                final LinearLayout countLayout = (LinearLayout) comboOptionsLayout.findViewById(R.id.count_layout);
                final TextView count = (TextView) countLayout.findViewById(R.id.count);
                int quantity = comboOption.getMinCount();
                count.setText(String.valueOf(quantity));
                if (comboOption.getSelectedComboOptionDishes().contains(comboDish)) {
                    Animations.fadeOut(addExtraLayout, 500);
                    Animations.fadeIn(countLayout, 500);
                    Animations.fadeIn(selected, 500);
                } else {
                    Animations.fadeIn(addExtraLayout, 500);
                    Animations.fadeOut(countLayout, 500);
                    Animations.fadeOut(selected, 500);
                }
                TextView plus = (TextView) countLayout.findViewById(R.id.plus);
                TextView minus = (TextView) countLayout.findViewById(R.id.minus);
                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!comboOption.incrementQuantity(comboDish))
                            Snackbar.make(mainLayout, "For bulk orders, contact Foodmash", Snackbar.LENGTH_SHORT).show();
                        count.setText(String.valueOf(comboDish.getQuantity()));
                        updatePrice();
                    }
                });
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(count.getText().toString().equals("0")) return;
                        if(!comboOption.decrementQuantity(comboDish))
                            Snackbar.make(mainLayout, "Combo should contain minimum "+comboOption.getMinCount()+" "+comboOption.getContents(), Snackbar.LENGTH_SHORT).show();
                        count.setText(String.valueOf(comboDish.getQuantity()));
                        if(comboDish.getQuantity()==1 && !comboOption.getSelectedComboOptionDishes().contains(comboDish)) {
                            Animations.fadeOut(selected, 500);
                            Animations.fadeOut(countLayout, 500);
                            Animations.fadeIn(addExtraLayout, 500);
                        }
                        updatePrice();
                    }
                });
                addExtraLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comboOption.addToSelected(comboDish);
                        Animations.fadeOut(addExtraLayout, 500);
                        Animations.fadeIn(countLayout, 500);
                        count.setText(String.valueOf(comboDish.getQuantity()));
                        Animations.fadeIn(selected, 500);
                        updatePrice();
                    }
                });

                ((View) comboOptionsLayout.findViewById(R.id.count_layout).getParent()).setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { } });
                comboOptionsLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comboOption.addToSelectedAfterClear(comboDish);
                        for (int l = 0; l < comboOption.getComboOptionDishes().size(); l++) {
                            LinearLayout comboOptionsLayout = (LinearLayout) optionsLayout.getChildAt(l);
                            ImageView selected = (ImageView) comboOptionsLayout.findViewById(R.id.selected);
                            TextView addExtraLayout = (TextView) comboOptionsLayout.findViewById(R.id.add_extra);
                            LinearLayout countLayout = (LinearLayout) comboOptionsLayout.findViewById(R.id.count_layout);
                            TextView id = (TextView) comboOptionsLayout.findViewById(R.id.id);
                            ComboDish comboDish = comboOption.fetch(Integer.parseInt(id.getText().toString()));
                            TextView count = (TextView) countLayout.findViewById(R.id.count);
                            if (comboOption.getSelectedComboOptionDishes().contains(comboDish)) {
                                Animations.fadeOut(addExtraLayout, 500);
                                Animations.fadeIn(countLayout, 500);
                                Animations.fadeIn(selected, 500);
                            } else {
                                comboDish.setQuantity(0);
                                Animations.fadeIn(addExtraLayout, 500);
                                Animations.fadeOut(countLayout, 500);
                                Animations.fadeOut(selected, 500);
                            }
                            count.setText(String.valueOf(comboDish.getQuantity()));
                        }
                        Animations.fadeOut(addExtraLayout, 500);
                        Animations.fadeIn(countLayout, 500);
                        Animations.fadeIn(selected, 500);
                        updatePrice();
                    }
                });
                optionsLayout.addView(comboOptionsLayout);
            }
            layoutOrderArrayList.add(new Pair<>((Object)comboOption, currentComboFoodLayout));

        }
        for (final ComboDish comboDish: combo.getComboDishes()) {
            final LinearLayout comboDishLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_combo_description_combo_dish, fillLayout, false);
            NetworkImageView comboDishPicture = (NetworkImageView) comboDishLayout.findViewById(R.id.image);
            comboDishPicture.setImageUrl(comboDish.getDish().getPicture(), imageLoader);
            comboDishPicture.getLayoutParams().height = displayMetrics.widthPixels/2 - (int)(10 * getResources().getDisplayMetrics().density);
            ((TextView) comboDishLayout.findViewById(R.id.name)).setText(comboDish.getDish().getName());
            ((TextView) comboDishLayout.findViewById(R.id.description)).setText(comboDish.getDish().getDescription());
            ((TextView) comboDishLayout.findViewById(R.id.restaurant_name)).setText(comboDish.getDish().getRestaurant().getName());
            ((NetworkImageView) comboDishLayout.findViewById(R.id.restaurant_logo)).setImageUrl(comboDish.getDish().getRestaurant().getLogo(), imageLoader);
            ImageView foodLabel = (ImageView) comboDishLayout.findViewById(R.id.label);
            switch(comboDish.getDish().getLabel()) {
                case EGG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.egg)); break;
                case VEG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.veg)); break;
                case NON_VEG: foodLabel.setColorFilter(ContextCompat.getColor(this, R.color.non_veg)); break;
            }
            LinearLayout countLayout = (LinearLayout) comboDishLayout.findViewById(R.id.count_layout);
            final TextView count = (TextView) countLayout.findViewById(R.id.count);
            count.setText(String.valueOf(comboDish.getQuantity()));
            countLayout.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!comboDish.incrementQuantity()) Snackbar.make(mainLayout, "For bulk orders, contact Foodmash", Snackbar.LENGTH_SHORT).show();
                    count.setText(String.valueOf(comboDish.getQuantity()));
                    updatePrice();
                }
            });
            countLayout.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!comboDish.decrementQuantity()) Snackbar.make(mainLayout, "Combo should contain minimum "+comboDish.getMinCount()+" "+comboDish.getDish().getName(), Snackbar.LENGTH_SHORT).show();
                    count.setText(String.valueOf(comboDish.getQuantity()));
                    updatePrice();
                }
            });
            layoutOrderArrayList.add(new Pair<>((Object)comboDish, comboDishLayout));
        }
        Collections.sort(layoutOrderArrayList, new Comparator<Pair<Object, LinearLayout>>() {
            @Override
            public int compare(Pair<Object, LinearLayout> lhs, Pair<Object, LinearLayout> rhs) {
                int priorityLhs = (lhs.first instanceof ComboOption) ? ((ComboOption) lhs.first).getPriority() : ((ComboDish) lhs.first).getPriority();
                int priorityRhs = (rhs.first instanceof ComboOption) ? ((ComboOption) rhs.first).getPriority() : ((ComboDish) rhs.first).getPriority();
                return priorityLhs - priorityRhs;
            }
        });
        for (Pair<Object,LinearLayout> comboFoodLayoutPair : layoutOrderArrayList)
            fillLayout.addView(comboFoodLayoutPair.second);
        updatePrice();

        swipeRefreshLayout.setRefreshing(false);
    }

    private void updatePrice() {
        float price = 0;
        for(ComboDish comboDish: combo.getComboDishes())
            price += comboDish.getDish().getPrice() * comboDish.getQuantity();
        for(ComboOption comboOption: combo.getComboOptions())
            for(ComboDish comboDish: comboOption.getSelectedComboOptionDishes())
                price += comboDish.getDish().getPrice()*comboDish.getQuantity();
        currentPrice.setText(String.valueOf((int)price));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy:
                cart.addToCart(new Combo(combo));
                Snackbar.make(mainLayout, "Added to Cart", Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() { @Override public void onClick(View v) {
                        cart.decrementFromCart(combo.getId());
                        Actions.updateCartCount(cartCount); } })
                    .show();
                Actions.updateCartCount(cartCount);
                break;
            case R.id.back:
                onBackPressed();
        }
    }

}
