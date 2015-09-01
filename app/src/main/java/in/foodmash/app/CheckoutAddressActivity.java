package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutAddressActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    LinearLayout cart;
    LinearLayout confirm;
    LinearLayout addAddress;
    LinearLayout fillLayout;

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
        setContentView(R.layout.activity_checkout_address);

        cart = (LinearLayout) findViewById(R.id.cart); cart.setOnClickListener(this);
        confirm = (LinearLayout) findViewById(R.id.confirm); confirm.setOnClickListener(this);
        addAddress = (LinearLayout) findViewById(R.id.add_address); addAddress.setOnClickListener(this);

        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        for(int i=0;i<3;i++) {
            final LinearLayout addressLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.user_address,fillLayout,false);
            ((TextView) addressLayout.findViewById(R.id.name)).setText("Name "+i);
            ((TextView) addressLayout.findViewById(R.id.address)).setText("Room no "+i+"\nABC Street, \nKottur");
            ((TextView) addressLayout.findViewById(R.id.phone)).setText("989876"+((i*345855)%100));
            if(i==0) addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
            addressLayout.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { Intent intent = new Intent(CheckoutAddressActivity.this, AddAddressActivity.class); startActivity(intent); }
            });
            addressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int childCount = fillLayout.getChildCount();
                    for (int i=0; i<childCount; i++) fillLayout.getChildAt(i).findViewById(R.id.selected).setVisibility(View.GONE);
                    addressLayout.findViewById(R.id.selected).setVisibility(View.VISIBLE);
                }
            });
            fillLayout.addView(addressLayout);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cart: intent = new Intent(this, CartActivity.class); startActivity(intent); break;
            case R.id.confirm: intent = new Intent(this, CheckoutPaymentActivity.class); startActivity(intent); break;
            case R.id.add_address:
                intent = new Intent(this, PinYourLocationActivity.class);
                intent.putExtra("cart",true);
                startActivity(intent); break;
        }
    }
}
