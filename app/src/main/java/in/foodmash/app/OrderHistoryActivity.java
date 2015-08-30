package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sarav on Aug 08 2015.
 */
public class OrderHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    LinearLayout back;
    LinearLayout fillLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_wallet_cash: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: intent = new Intent(this,LoginActivity.class); startActivity(intent); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        for(int i=0;i<3;i++) {
            LinearLayout orderLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.user_order,fillLayout,false);
            ((TextView) orderLayout.findViewById(R.id.order_id)).setText("OD"+((i*2132434)%10000));
//            ((TextView) comboLayout.findViewById(R.id.date)).setText("");
//            ((TextView) comboLayout.findViewById(R.id.status)).setText("");
            ((TextView) orderLayout.findViewById(R.id.price)).setText(""+((i*23423)%1000));
            ((ImageView) orderLayout.findViewById(R.id.statusIcon)).setImageResource(R.mipmap.tick);
            orderLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(OrderHistoryActivity.this, OrderDescriptionActivity.class);
                    startActivity(intent);
                }
            });
            fillLayout.addView(orderLayout);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
        }
    }
}
