package in.foodmash.app;

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
public class OrderDescriptionActivity extends AppCompatActivity implements View.OnClickListener {

    Intent intent;

    TextView status;
    TextView date;
    TextView total;
    ImageView statusIcon;
    LinearLayout orderHistory;
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
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: intent = new Intent(this,LoginActivity.class); startActivity(intent); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_description);

        orderHistory = (LinearLayout) findViewById(R.id.order_history); orderHistory.setOnClickListener(this);
        status = (TextView) findViewById(R.id.status);
        date = (TextView) findViewById(R.id.date);
        total = (TextView) findViewById(R.id.total);
        statusIcon = (ImageView) findViewById(R.id.status_icon);

        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        for(int i=0; i<3; i++) {
            LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.order_combo, fillLayout, false);
            ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
            ((TextView) comboLayout.findViewById(R.id.name)).setText("Combo Cart " + i);
            TextView price = (TextView) comboLayout.findViewById(R.id.price); price.setText("" + (100 + i));
            TextView quantity = (TextView) comboLayout.findViewById(R.id.quantity); quantity.setText(String.valueOf(i+1));
            ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText(quantity.getText());
            ((TextView) comboLayout.findViewById(R.id.amount)).setText(String.valueOf(Integer.parseInt(quantity.getText().toString()) * Integer.parseInt(price.getText().toString())));
            fillLayout.addView(comboLayout);
        }


        updateOrderValue();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order_history: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
        }
    }

    private void updateOrderValue() {
        float totalCartValue = 0;
        for(int i=0; i<fillLayout.getChildCount();i++)
            totalCartValue+= Float.parseFloat(((TextView) fillLayout.getChildAt(i).findViewById(R.id.amount)).getText().toString());
        total.setText(String.format("%.02f",totalCartValue));
    }
}
