package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.Random;
import java.util.TreeMap;

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cache;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;
import in.foodmash.app.custom.ComboDish;
import in.foodmash.app.custom.ComboOption;
import in.foodmash.app.custom.TouchableImageButton;

/**
 * Created by sarav on Sep 30 2015.
 */
public class ComboDescriptionActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView cartCount;
    private TextView currentPrice;
    private TouchableImageButton resetCombo;
    private Cart cart = Cart.getInstance();
    private Intent intent;
    private Combo combo;

    private LinearLayout back;
    private LinearLayout buy;
    private LinearLayout fillLayout;
    private ImageLoader imageLoader;
    private LinearLayout addedToCartLayout;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(Info.isLoggedIn(ComboDescriptionActivity.this)) getMenuInflater().inflate(R.menu.menu_main, menu);
        else getMenuInflater().inflate(R.menu.menu_main_anonymous_login,menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { intent = new Intent(ComboDescriptionActivity.this, CartActivity.class); startActivity(intent); } });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: Actions.logout(ComboDescriptionActivity.this); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_description);
        combo = Cache.getCombo(getIntent().getIntExtra("combo_id", -1));
        if(combo==null) { Alerts.unknownErrorAlert(ComboDescriptionActivity.this); return; }

        buy = (LinearLayout) findViewById(R.id.buy); buy.setOnClickListener(this);
        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        resetCombo = (TouchableImageButton) findViewById(R.id.reset_combo); resetCombo.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        currentPrice = (TextView) findViewById(R.id.price);

        imageLoader = Swift.getInstance(ComboDescriptionActivity.this).getImageLoader();
        updateFillLayout();
    }

    private void updateFillLayout() {
        fillLayout.removeAllViews();
        updateAddedToCartLayout();
        final TreeMap<Integer,LinearLayout> layoutOrderTreeMap = new TreeMap<>();

        for (final ComboOption comboOption: combo.getComboOptions()) {
            final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.combo_description_combo_option, fillLayout, false);
            final LinearLayout optionsLayout = (LinearLayout) currentComboFoodLayout.findViewById(R.id.combo_dishes_layout);

            for (final ComboDish comboDish: comboOption.getComboOptionDishes()) {
                final LinearLayout comboOptionsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.combo_description_combo_option_dish, currentComboFoodLayout, false);
                final ImageView selected = (ImageView) comboOptionsLayout.findViewById(R.id.selected);
                if (comboOption.getSelectedComboOptionDishes().contains(comboDish)) { selected.setColorFilter(getResources().getColor(R.color.transparent)); }
                else { selected.setColorFilter(getResources().getColor(R.color.white)); }
                ImageView foodLabel = (ImageView) comboOptionsLayout.findViewById(R.id.label);
                switch(comboDish.getDish().getLabel()) {
                    case "egg": foodLabel.setColorFilter(getResources().getColor(R.color.egg)); break;
                    case "veg": foodLabel.setColorFilter(getResources().getColor(R.color.veg)); break;
                    case "non-veg": foodLabel.setColorFilter(getResources().getColor(R.color.non_veg)); break;
                }
                ((TextView) comboOptionsLayout.findViewById(R.id.id)).setText(String.valueOf(comboDish.getId()));
                ((TextView) comboOptionsLayout.findViewById(R.id.name)).setText(comboDish.getDish().getName());
                ((NetworkImageView) comboOptionsLayout.findViewById(R.id.image)).setImageUrl(getImageUrl(), imageLoader);
                ((TextView) comboOptionsLayout.findViewById(R.id.description)).setText(comboDish.getDish().getDescription());
                ((TextView) comboOptionsLayout.findViewById(R.id.restaurant_name)).setText(comboDish.getDish().getRestaurant().getName());
                ((NetworkImageView) comboOptionsLayout.findViewById(R.id.restaurant_logo)).setImageUrl(getRestaurantImageUrl(), imageLoader);
                final LinearLayout addExtraLayout = (LinearLayout) comboOptionsLayout.findViewById(R.id.add_extra_layout);
                final LinearLayout countLayout = (LinearLayout) comboOptionsLayout.findViewById(R.id.count_layout);
                final TextView count = (TextView) countLayout.findViewById(R.id.count);
                int quantity = comboDish.getQuantity();
                count.setText(String.valueOf(quantity));
                if (comboOption.getSelectedComboOptionDishes().contains(comboDish)) {
                    Animations.fadeOut(addExtraLayout, 200);
                    Animations.fadeIn(countLayout, 200);
                    selected.setColorFilter(getResources().getColor(R.color.transparent));
                } else {
                    Animations.fadeIn(addExtraLayout, 200);
                    Animations.fadeOut(countLayout, 200);
                    selected.setColorFilter(getResources().getColor(R.color.white));
                }
                ImageView plus = (ImageView) countLayout.findViewById(R.id.plus);
                ImageView minus = (ImageView) countLayout.findViewById(R.id.minus);
                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!comboOption.incrementQuantity(comboDish))
                            Alerts.maxCountAlert(ComboDescriptionActivity.this, comboOption);
                        count.setText(String.valueOf(comboDish.getQuantity()));
                        updatePrice();
                    }
                });
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(count.getText().toString().equals("0")) return;
                        if(!comboOption.decrementQuantity(comboDish))
                            Alerts.minCountAlert(ComboDescriptionActivity.this, comboOption);
                        count.setText(String.valueOf(comboDish.getQuantity()));
                        if(comboDish.getQuantity()==1 && !comboOption.getSelectedComboOptionDishes().contains(comboDish)) {
                            selected.setColorFilter(getResources().getColor(R.color.white));
                            Animations.fadeOut(countLayout, 200);
                            Animations.fadeIn(addExtraLayout, 200);
                        }
                        updatePrice();
                    }
                });
                addExtraLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comboOption.addToSelected(comboDish);
                        Animations.fadeOut(addExtraLayout, 200);
                        Animations.fadeIn(countLayout, 200);
                        count.setText(String.valueOf(comboDish.getQuantity()));
                        selected.setColorFilter(getResources().getColor(R.color.transparent));
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
                            LinearLayout addExtraLayout = (LinearLayout) comboOptionsLayout.findViewById(R.id.add_extra_layout);
                            LinearLayout countLayout = (LinearLayout) comboOptionsLayout.findViewById(R.id.count_layout);
                            TextView id = (TextView) comboOptionsLayout.findViewById(R.id.id);
                            ComboDish comboDish = comboOption.fetch(Integer.parseInt(id.getText().toString()));
                            TextView count = (TextView) countLayout.findViewById(R.id.count);
                            if (comboOption.getSelectedComboOptionDishes().contains(comboDish)) {
                                Animations.fadeOut(addExtraLayout, 200);
                                Animations.fadeIn(countLayout, 200);
                                selected.setColorFilter(getResources().getColor(R.color.transparent));
                            } else {
                                comboDish.resetQuantity();
                                Animations.fadeIn(addExtraLayout, 200);
                                Animations.fadeOut(countLayout, 200);
                                selected.setColorFilter(getResources().getColor(R.color.white));
                            }
                            count.setText(String.valueOf(comboDish.getQuantity()));
                        }
                        Animations.fadeOut(addExtraLayout, 200);
                        Animations.fadeIn(countLayout, 200);
                        selected.setColorFilter(getResources().getColor(R.color.transparent));
                        updatePrice();
                    }
                });
                optionsLayout.addView(comboOptionsLayout);
            }

            if (comboOption.isFromSameRestaurant()) {
                for (int m = 0; m < comboOption.getComboOptionDishes().size(); m++)
                    optionsLayout.getChildAt(m).findViewById(R.id.restaurant_layout).setVisibility(View.GONE);
                ((TextView) currentComboFoodLayout.findViewById(R.id.restaurant_name)).setText(comboOption.getComboOptionDishes().get(0).getDish().getRestaurant().getName());
                ((NetworkImageView) currentComboFoodLayout.findViewById(R.id.restaurant_logo)).setImageUrl(getImageUrl(),imageLoader);
            } else currentComboFoodLayout.findViewById(R.id.restaurant_layout).setVisibility(View.GONE);
            layoutOrderTreeMap.put(comboOption.getPriority(), currentComboFoodLayout);

        }
        for (final ComboDish comboDish: combo.getComboDishes()) {
            final LinearLayout comboDishLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.combo_description_combo_dish, fillLayout, false);
            ((NetworkImageView) comboDishLayout.findViewById(R.id.image)).setImageUrl(getImageUrl(), imageLoader);
            ((TextView) comboDishLayout.findViewById(R.id.name)).setText(comboDish.getDish().getName());
            ((TextView) comboDishLayout.findViewById(R.id.description)).setText(comboDish.getDish().getDescription());
            ((TextView) comboDishLayout.findViewById(R.id.restaurant_name)).setText(comboDish.getDish().getRestaurant().getName());
            ((NetworkImageView) comboDishLayout.findViewById(R.id.restaurant_logo)).setImageUrl(getRestaurantImageUrl(), imageLoader);
            ImageView foodLabel = (ImageView) comboDishLayout.findViewById(R.id.label);
            switch(comboDish.getDish().getLabel()) {
                case "egg": foodLabel.setColorFilter(getResources().getColor(R.color.egg)); break;
                case "veg": foodLabel.setColorFilter(getResources().getColor(R.color.veg)); break;
                case "non-veg": foodLabel.setColorFilter(getResources().getColor(R.color.non_veg)); break;
            }
            LinearLayout countLayout = (LinearLayout) comboDishLayout.findViewById(R.id.count_layout);
            final TextView count = (TextView) countLayout.findViewById(R.id.count);
            count.setText(String.valueOf(comboDish.getQuantity()));
            countLayout.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!comboDish.incrementQuantity()) Alerts.maxCountAlert(ComboDescriptionActivity.this, comboDish);
                    count.setText(String.valueOf(comboDish.getQuantity()));
                    updatePrice();
                }
            });
            countLayout.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!comboDish.decrementQuantity()) Alerts.minCountAlert(ComboDescriptionActivity.this, comboDish);
                    count.setText(String.valueOf(comboDish.getQuantity()));
                    updatePrice();
                }
            });
            layoutOrderTreeMap.put(comboDish.getPriority(), comboDishLayout);
        }
        for (LinearLayout comboFoodLayout : layoutOrderTreeMap.values())
            fillLayout.addView(comboFoodLayout);
        updatePrice();
    }

    private void updateAddedToCartLayout() {
        if(cart.hasCombo(combo)) {
            addedToCartLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.combo_description_combo_in_cart, fillLayout, false);
            fillLayout.addView(addedToCartLayout, 0);
        } else fillLayout.removeView(addedToCartLayout);
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
            case R.id.reset_combo:
                new AlertDialog.Builder(ComboDescriptionActivity.this)
                        .setCancelable(false)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Do you want to reset the combo ?")
                        .setMessage("This will reset the combo to the original state discarding all changes you have made.")
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {
                            for(ComboOption comboOption: combo.getComboOptions()) {
                                for (ComboDish comboDish : comboOption.getComboOptionDishes())
                                    comboDish.resetQuantity();
                                comboOption.resetSelectedComboOptionDishes();
                            }
                            for(ComboDish comboDish: combo.getComboDishes())
                                comboDish.resetQuantity();
                            updateFillLayout();
                        } })
                        .setNegativeButton("No, Don't", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {} })
                        .show();
                break;
            case R.id.back: finish(); break;
            case R.id.buy:
                cart.addToCart(new Combo(combo));
                Toast.makeText(ComboDescriptionActivity.this, combo.getName() + " worth Rs. "+combo.calculatePrice()+" added to cart", Toast.LENGTH_SHORT).show();
                Actions.updateCartCount(cartCount);
                updateAddedToCartLayout();
                break;
        }
    }

    private String getImageUrl() {
        int randomNumber = new Random().nextInt(3 - 1 + 1) + 1;
        switch (randomNumber) {
            case 1: return "http://s19.postimg.org/mbcpkaupf/92t8_Zu_KH.jpg";
            case 2: return "http://s19.postimg.org/cs7m4kwkz/qka9d_YR.jpg";
            case 3: return "http://s19.postimg.org/e8j4mpzhv/zgdz_Ur_DV.jpg";
            default: return "http://s19.postimg.org/mbcpkaupf/92t8_Zu_KH.jpg";
        }
    }

    private String getRestaurantImageUrl() {
        int randomNumber = new Random().nextInt(5 - 1 + 1) + 1;
        switch (randomNumber) {
            case 1: return "http://s19.postimg.org/4l7uv6j1v/300px_Burger_King_Logo_svg.png";
            case 2: return "http://s19.postimg.org/kywfs2okz/Baskin_Robbins_svg.png";
            case 3: return "http://s19.postimg.org/ptljclxir/kfc_logo.png";
            case 4: return "http://s19.postimg.org/cj6vaklpv/logo_02.png";
            case 5: return "http://s19.postimg.org/ank2zewvn/pizza_hut_delivery_maidenhead_logo.png";
            default: return "http://s19.postimg.org/4l7uv6j1v/300px_Burger_King_Logo_svg.png";
        }
    }

}
