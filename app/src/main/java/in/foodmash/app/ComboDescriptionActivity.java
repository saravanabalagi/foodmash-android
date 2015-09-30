package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.TreeMap;

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.custom.Cache;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;
import in.foodmash.app.custom.ComboDish;
import in.foodmash.app.custom.ComboOption;

/**
 * Created by sarav on Sep 30 2015.
 */
public class ComboDescriptionActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView cartCount;
    private Cart cart = Cart.getInstance();
    private Intent intent;
    private Combo combo;

    private LinearLayout back;
    private LinearLayout buy;
    private LinearLayout fillLayout;
    private LinearLayout countLayout;
    private TextView count;
    private ImageView plus;
    private ImageView minus;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(Info.isLoggedIn(ComboDescriptionActivity.this)) getMenuInflater().inflate(R.menu.menu_main, menu);
        else getMenuInflater().inflate(R.menu.menu_signed_out,menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); updateCartCount();
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

        count = (TextView) findViewById(R.id.count);
        countLayout = (LinearLayout) findViewById(R.id.count_layout);
        buy = (LinearLayout) findViewById(R.id.buy); buy.setOnClickListener(this);
        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        plus = (ImageView) countLayout.findViewById(R.id.plus); plus.setOnClickListener(this);
        minus = (ImageView) countLayout.findViewById(R.id.minus); minus.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        final TreeMap<Integer,LinearLayout> layoutOrderTreeMap = new TreeMap<>();
        for (final ComboOption comboOption: combo.getComboOptions()) {
            final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, fillLayout, false);
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
                        System.out.println("Combo Options: "+comboOption.getComboOptionDishes().size());
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
            final LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, fillLayout, false);
            ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
            ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText(comboDish.getDish().getName());
            ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText(comboDish.getDish().getDescription());
            ((TextView) currentComboFoodLayout.findViewById(R.id.restaurant_name)).setText(comboDish.getDish().getRestaurant().getName());
            layoutOrderTreeMap.put(comboDish.getPriority(), currentComboFoodLayout);
        }
        for (LinearLayout comboFoodLayout : layoutOrderTreeMap.values())
            fillLayout.addView(comboFoodLayout);
    }

    private void updateCartCount() {
        if(cartCount==null) return;
        int count = cart.getCount();
        if(count>0) { cartCount.setText(String.valueOf(count)); Animations.fadeInOnlyIfInvisible(cartCount, 500); }
        else Animations.fadeOut(cartCount,500);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_combo_from_cart: break;
            case R.id.back: finish(); break;
            case R.id.plus:
                cart.addToCart(new Combo(combo));
                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                updateCartCount();
                break;
            case R.id.minus:
                if(count.getText().toString().equals("0")) return;
                cart.decrementFromCart(combo);
                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                if(Integer.parseInt(count.getText().toString())==0) {
                    Animations.fadeOut(countLayout, 200);
                    Animations.fadeIn(buy, 200);
                }
                updateCartCount();
                break;
            case R.id.buy:
                cart.addToCart(new Combo(combo));
                Animations.fadeIn(countLayout, 200);
                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                updateCartCount();
                break;
        }
    }

}
