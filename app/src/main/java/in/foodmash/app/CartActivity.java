package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.ImmutableCombo;
import in.foodmash.app.custom.TouchableImageButton;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    Handler handler=new Handler();
    Cart cart = Cart.getInstance();

    LinearLayout back;
    LinearLayout buy;
    LinearLayout fillLayout;
    LinearLayout emptyCartLayout;

    TextView total;
    TouchableImageButton clearCart;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_addresses) { intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_order_history) { intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_contact_us) { intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true; }
        if (id == R.id.menu_log_out) { intent = new Intent(this,LoginActivity.class); startActivity(intent); finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        total = (TextView) findViewById(R.id.total);
        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        buy = (LinearLayout) findViewById(R.id.buy); buy.setOnClickListener(this);
        clearCart = (TouchableImageButton) findViewById(R.id.clear_cart); clearCart.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        emptyCartLayout = (LinearLayout) findViewById(R.id.empty_cart_layout);

        total.setText(cart.getTotal());
        if(cart.getCount()>0) emptyCartLayout.setVisibility(View.GONE);
        for(final HashMap.Entry<ImmutableCombo,Integer> order: cart.getOrders().entrySet()){
            final ImmutableCombo combo = order.getKey();
            final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.cart_combo, fillLayout, false);
            ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
            ((TextView) comboLayout.findViewById(R.id.name)).setText(combo.getName());
            ((TextView) comboLayout.findViewById(R.id.dishes)).setText(combo.getDishNames());
            ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText(String.valueOf(order.getValue()));
            final TextView price = (TextView) comboLayout.findViewById(R.id.price); price.setText(combo.getStringPrice());
            final EditText quantity = (EditText) comboLayout.findViewById(R.id.quantity); quantity.setText(String.valueOf(order.getValue())); quantity.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if(s.toString().equals("0")) { quantity.setText(""); return; }
                        if (s.length() > 0 && NumberUtils.isInteger(s.toString())) {
                            cart.changeQuantity(combo,Integer.parseInt(s.toString()));
                            total.setText(cart.getTotal());
                            ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText(s.toString());
                            ((TextView) comboLayout.findViewById(R.id.amount)).setText(String.valueOf(combo.getIntPrice()*Integer.parseInt(s.toString())));
                        }
                    } catch (NumberFormatException e) { e.printStackTrace(); }
                }
            });
            quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus)
                        if(quantity.getText().length()<1) {
                            Alerts.commonErrorAlert(CartActivity.this,"Empty Quantity","Quantity field cannot be empty","Okay");
                            quantity.setText(((TextView)comboLayout.findViewById(R.id.quantity_display)).getText().toString());
                        }
                }
            });
            ((TextView) comboLayout.findViewById(R.id.amount)).setText(String.valueOf(combo.getIntPrice()*order.getValue()));
            comboLayout.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.removeOrder(combo);
                    total.setText(cart.getTotal());
                    fillLayout.removeView(comboLayout);
                    if(fillLayout.getChildCount()==0)
                        Animations.fadeIn(emptyCartLayout, 500);
                }
            });
            fillLayout.addView(comboLayout);
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_cart:
                new AlertDialog.Builder(CartActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Remove all from cart ?")
                        .setMessage("Do you want to remove all combos added to the cart?")
                        .setPositiveButton("Remove All", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                cart.removeAllOrders();
                                for (int i = 0; i < fillLayout.getChildCount(); i++)
                                    handler.postDelayed(new Runnable() {
                                        @Override public void run() {
                                            fillLayout.removeViewAt(0); } }, i*500);
                                total.setText(cart.getTotal());
                                Animations.fadeIn(emptyCartLayout,500);
                            }
                        }).setNegativeButton("No, don't remove", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { }
                }).show(); break;
            case R.id.back: finish(); break;
            case R.id.buy:
                if(cart.getCount()==0) Alerts.commonErrorAlert(CartActivity.this,"Empty Cart","Your cart is empty. Add some combos and we'll proceed!","Okay");
                else if(isEverythingValid()) {
                    intent = new Intent(this, CheckoutAddressActivity.class);
                    intent.putExtra("payable_amount", cart.getTotal());
                    startActivity(intent);
                } else {
                    Alerts.validityAlert(CartActivity.this);
                    for(int i=0;i<fillLayout.getChildCount();i++) {
                        LinearLayout linearLayout = (LinearLayout) fillLayout.getChildAt(i);
                        ((EditText) linearLayout.findViewById(R.id.quantity)).setText(((TextView)linearLayout.findViewById(R.id.quantity_display)).getText().toString());
                    }
                }
                break;
        }
    }

    private boolean isEverythingValid() {
        boolean valid = true;
        for(int i=0;i<fillLayout.getChildCount();i++) {
            LinearLayout linearLayout = (LinearLayout) fillLayout.getChildAt(i);
            if(!(((EditText) linearLayout.findViewById(R.id.quantity)).getText().toString().equals(((TextView) linearLayout.findViewById(R.id.quantity_display)).getText().toString()))) valid=false;
        }
        return valid;
    }

}
