package in.foodmash.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wallet.Cart;

import org.w3c.dom.Text;

import java.util.logging.Handler;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    android.os.Handler handler=new android.os.Handler();

    LinearLayout back;
    LinearLayout buy;
    LinearLayout fillLayout;

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
        if (id == R.id.menu_profile) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_addresses) { intent = new Intent(this,AddressActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_order_history) { intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_wallet_cash) { intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_contact_us) { intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true; }
        if (id == R.id.menu_log_out) { intent = new Intent(this,LoginActivity.class); startActivity(intent); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        total = (TextView) findViewById(R.id.total);
        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        buy = (LinearLayout) findViewById(R.id.buy); buy.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);

        for(int i=0;i<3;i++){
            final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.cart_combo, fillLayout, false);
            ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
            ((TextView) comboLayout.findViewById(R.id.name)).setText("Combo Cart " + i);
            final TextView price = (TextView) comboLayout.findViewById(R.id.price); price.setText("" + (100 + i));
            final EditText quantity = (EditText) comboLayout.findViewById(R.id.quantity); quantity.setText("1"); quantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText((s.length() < 1) ? "" + 1 : s.toString());
                    ((TextView) comboLayout.findViewById(R.id.amount)).setText("" + (((s.length() < 1) ? 1 : Integer.parseInt(s.toString())) * Integer.parseInt(price.getText().toString())));
                    updateCartValue();
                }
            }); quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if (((TextView) v).getText().toString().length() < 1) quantity.setText("1");
                        else if (Integer.parseInt(((TextView) v).getText().toString()) == 0) {
                            new AlertDialog.Builder(CartActivity.this)
                                    .setIconAttribute(android.R.attr.alertDialogIcon)
                                    .setTitle("Delete Combo ?")
                                    .setMessage("You set the quantity to zero. Do you want to delete it?")
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        @Override public void onClick(DialogInterface dialog, int which) { fillLayout.removeView(comboLayout); }
                                    }).setNegativeButton("No, don't delete", new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) { quantity.setText("1"); }
                            }).show();
                        }
                    }
                }
            });
            ((TextView) comboLayout.findViewById(R.id.amount)).setText("" + (Integer.parseInt(quantity.getText().toString()) * Integer.parseInt(price.getText().toString())));
            ((ImageButton) comboLayout.findViewById(R.id.remove)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fillLayout.removeView(comboLayout);
                    updateCartValue();
                }
            });
            fillLayout.addView(comboLayout);
        }

        clearCart = (TouchableImageButton) findViewById(R.id.clear_cart); clearCart.setOnClickListener(this);
        updateCartValue();
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
                                for (int i = 0; i < fillLayout.getChildCount(); i++) {
                                    handler.postDelayed(new Runnable() {
                                        @Override public void run() {
                                            fillLayout.removeViewAt(0); updateCartValue(); } }, i*500);
                                }
                            }
                        }).setNegativeButton("No, don't remove", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { }
                }).show(); break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.buy: intent = new Intent(this, CheckoutAddressActivity.class); startActivity(intent); break;
        }
    }

    private void updateCartValue() {
        Float totalCartValue = new Float(0);
        for(int i=0; i<fillLayout.getChildCount();i++)
            totalCartValue+= Float.parseFloat(((TextView) fillLayout.getChildAt(i).findViewById(R.id.amount)).getText().toString());
        total.setText(String.format("%.2f",totalCartValue));
    }

}
