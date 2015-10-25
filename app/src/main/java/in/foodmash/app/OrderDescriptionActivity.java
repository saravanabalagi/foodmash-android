package in.foodmash.app;

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

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.utils.WordUtils;


/**
 * Created by sarav on Aug 08 2015.
 */
public class OrderDescriptionActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private String orderId;
    private boolean cart;

    private TextView status;
    private TextView date;
    private TextView total;
    private TextView paymentMethod;
    private ImageView statusIcon;
    private LinearLayout orderHistory;
    private LinearLayout home;
    private LinearLayout fillLayout;
    private LinearLayout loadingLayout;
    private LinearLayout mainLayout;

    private JsonObjectRequest orderDescriptionRequest;
    private ImageLoader imageLoader;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        TextView cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(OrderDescriptionActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_log_out: Actions.logout(OrderDescriptionActivity.this); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_description);

        imageLoader = Swift.getInstance(OrderDescriptionActivity.this).getImageLoader();
        cart = getIntent().getBooleanExtra("cart", false);
        orderId = getIntent().getStringExtra("order_id");
        orderHistory = (LinearLayout) findViewById(R.id.order_history); orderHistory.setOnClickListener(this);
        home = (LinearLayout) findViewById(R.id.home); home.setOnClickListener(this);
        status = (TextView) findViewById(R.id.status);
        date = (TextView) findViewById(R.id.date);
        total = (TextView) findViewById(R.id.total);
        paymentMethod = (TextView) findViewById(R.id.payment_method);
        statusIcon = (ImageView) findViewById(R.id.status_icon);

        loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);

        orderDescriptionRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/show", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Animations.fadeOut(loadingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        JSONObject orderJson = response.getJSONObject("data");
                        total.setText(String.format("%.2f",Float.parseFloat(orderJson.getString("total"))));
                        paymentMethod.setText(WordUtils.titleize(orderJson.getString("payment_method")));
                        setStatus(statusIcon, orderJson.getString("aasm_state"));
                        date.setText(orderJson.getString("updated_at"));
                        status.setText(orderJson.getString("aasm_state"));
                        JSONArray subOrdersJson = orderJson.getJSONArray("orders");
                        for(int i=0; i<subOrdersJson.length(); i++) {
                            JSONObject subOrderJson = subOrdersJson.getJSONObject(i);
                            JSONObject productJson = subOrderJson.getJSONObject("product");
                            JSONArray comboDishesJson = subOrderJson.getJSONArray("order_items");
                            String dishes = "";
                            for(int j=0; j<comboDishesJson.length(); j++) {
                                JSONObject comboDishJson = comboDishesJson.getJSONObject(j);
                                JSONObject dishJson = comboDishJson.getJSONObject("item");
                                dishes += dishJson.getString("name") + ((j==comboDishesJson.length()-1)?"":",  ");
                            }
                            LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.order_combo, fillLayout, false);
                            ((NetworkImageView) comboLayout.findViewById(R.id.image)).setImageUrl(getImageUrl(), imageLoader);
                            ((TextView) comboLayout.findViewById(R.id.name)).setText(productJson.getString("name"));
                            TextView price = (TextView) comboLayout.findViewById(R.id.price); price.setText(productJson.getString("price"));
                            TextView quantity = (TextView) comboLayout.findViewById(R.id.quantity); quantity.setText(subOrderJson.getString("quantity"));
                            ((TextView) comboLayout.findViewById(R.id.quantity_display)).setText(subOrderJson.getString("quantity"));
                            ((TextView) comboLayout.findViewById(R.id.amount)).setText(subOrderJson.getString("total"));
                            ((TextView) comboLayout.findViewById(R.id.dishes)).setText(dishes);
                            fillLayout.addView(comboLayout);
                        }
                    } else {
                        Alerts.requestUnauthorisedAlert(OrderDescriptionActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(OrderDescriptionActivity.this).addToRequestQueue(orderDescriptionRequest);
                    }
                };
                if (error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(OrderDescriptionActivity.this, onClickTryAgain);
                else if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(OrderDescriptionActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(OrderDescriptionActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(OrderDescriptionActivity.this).addToRequestQueue(orderDescriptionRequest);

    }

    @Override
    public void onBackPressed() {
        if(cart) { intent = new Intent(OrderDescriptionActivity.this,MainActivity.class); startActivity(intent); }
        else super.onBackPressed();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order_history: if(cart) { intent = new Intent(OrderDescriptionActivity.this,OrderHistoryActivity.class); startActivity(intent); } else finish(); break;
            case R.id.home: intent = new Intent(OrderDescriptionActivity.this,MainActivity.class); startActivity(intent); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(OrderDescriptionActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("order_id",orderId);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void setStatus (ImageView statusImageView, String status) {
        switch (status) {
            case "delivered": statusImageView.setImageResource(R.mipmap.tick); statusImageView.setColorFilter(getResources().getColor(R.color.okay_green)); break;
            case "cancelled": statusImageView.setImageResource(R.mipmap.cancel); statusImageView.setColorFilter(getResources().getColor(R.color.color_accent)); break;
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
}
