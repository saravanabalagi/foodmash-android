package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.buy) FloatingActionButton buy;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.empty_cart_layout) LinearLayout emptyCartLayout;
    @Bind(R.id.payable_amount) TextView total;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private Handler handler=new Handler();
    private Cart cart = Cart.getInstance();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete_cart) {
            new AlertDialog.Builder(CartActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Remove all from cart ?")
                .setMessage("Do you want to remove all combos added to the cart?")
                .setPositiveButton("Remove All", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        cart.removeAllOrders();
                        fillLayout.removeAllViews();
                        total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
                        Animations.fadeIn(emptyCartLayout,500);
                    }
                }).setNegativeButton("No, don't remove", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { }
                }).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        buy.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        emptyCartLayout = (LinearLayout) findViewById(R.id.empty_cart_layout);

        total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
        if(cart.getCount()>0) emptyCartLayout.setVisibility(View.GONE);
        for(final HashMap.Entry<Combo,Integer> order: cart.getOrders().entrySet()){
            final Combo combo = order.getKey();
            final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.repeatable_cart_item, fillLayout, false);
            ((TextView) comboLayout.findViewById(R.id.name)).setText(combo.getName());
            ((TextView) comboLayout.findViewById(R.id.dishes)).setText(combo.getDishNames());
            ((TextView) comboLayout.findViewById(R.id.count)).setText(String.valueOf(order.getValue()));
            ((TextView) comboLayout.findViewById(R.id.amount)).setText(String.valueOf((int)combo.calculatePrice() * order.getValue()));
            comboLayout.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    cart.addToCart(combo);
                    ((TextView) comboLayout.findViewById(R.id.count)).setText(String.valueOf(cart.getCount(combo)));
                    ((TextView) comboLayout.findViewById(R.id.amount)).setText(String.valueOf((int)combo.calculatePrice() * order.getValue()));
                    total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
            }});
            comboLayout.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ((TextView) comboLayout.findViewById(R.id.count)).setText(String.valueOf(cart.decrementFromCart(combo)));
                    if(cart.getCount()==0) Animations.fadeIn(emptyCartLayout, 500);
                    if(cart.getCount(combo)==0) fillLayout.removeView(comboLayout);
                    ((TextView) comboLayout.findViewById(R.id.amount)).setText(String.valueOf((int)combo.calculatePrice() * order.getValue()));
                    total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
            }});
            fillLayout.addView(comboLayout);
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy:
                if(cart.getCount()==0) Alerts.commonErrorAlert(CartActivity.this,"Empty Cart","Your cart is empty. Add some combos and we'll proceed!","Okay");
                else if(Info.isLoggedIn(this)) startActivity(new Intent(CartActivity.this, CheckoutAddressActivity.class));
                else {
                    Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                    intent.putExtra("from_cart", true);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override public void onBackPressed() { intent = new Intent(CartActivity.this,MainActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent); }

}
