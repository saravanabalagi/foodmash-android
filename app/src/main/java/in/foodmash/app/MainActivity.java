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

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cache;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;

public class MainActivity extends AppCompatActivity {

    private Intent intent;

    private LinearLayout fillLayout;
    private List<Combo> combos;
    private TextView cartCount;
    private Cart cart = Cart.getInstance();
    private JsonObjectRequest getCombosRequest;
    private ImageLoader imageLoader;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(Info.isLoggedIn(MainActivity.this)) getMenuInflater().inflate(R.menu.menu_main, menu);
        else getMenuInflater().inflate(R.menu.menu_signed_out,menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); updateCartCount();
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        System.out.println("Resumed");
        updateCartCount();
        updateFillLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: Actions.logout(MainActivity.this); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        imageLoader = Swift.getInstance(MainActivity.this).getImageLoader();

        getCombosRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/combos", JsonProvider.getStandardRequestJson(MainActivity.this) ,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if (response.getBoolean("success")) {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                        Cache.setCombos(Arrays.asList(mapper.readValue(response.getJSONArray("data").toString(), Combo[].class)));
                        combos = Cache.getCombos();
                        updateFillLayout();
                    } else if (!response.getBoolean("success")) {
                        Alerts.requestUnauthorisedAlert(MainActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(MainActivity.this).addToRequestQueue(getCombosRequest);
                    }
                };
                if (error instanceof TimeoutError) Alerts.internetConnectionErrorAlert(MainActivity.this, onClickTryAgain);
                if (error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(MainActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(MainActivity.this);
            }
        });
        Swift.getInstance(this).addToRequestQueue(getCombosRequest);

    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Exit App ?")
                .setMessage("Do you really want to exit the app")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void updateFillLayout() {
        if(combos==null||combos.size()==0) return;
        fillLayout.removeAllViews();
        TreeMap<Integer,LinearLayout> comboTreeMap = new TreeMap<>();
        for (final Combo combo: combos) {
            final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo, fillLayout, false);
            ((TextView) comboLayout.findViewById(R.id.id)).setText(String.valueOf(combo.getId()));
            ((NetworkImageView) comboLayout.findViewById(R.id.image)).setImageUrl(getImageUrl(),imageLoader);
            ((TextView) comboLayout.findViewById(R.id.name)).setText(combo.getName());
            ((TextView) comboLayout.findViewById(R.id.description)).setText(combo.getDescription());
            ((TextView) comboLayout.findViewById(R.id.price)).setText(combo.getStringPrice());
            ImageView foodLabel = (ImageView) comboLayout.findViewById(R.id.label);
            switch(combo.getLabel()) {
                case "egg": foodLabel.setColorFilter(getResources().getColor(R.color.egg)); break;
                case "veg": foodLabel.setColorFilter(getResources().getColor(R.color.veg)); break;
                case "non-veg": foodLabel.setColorFilter(getResources().getColor(R.color.non_veg)); break;
            }

            comboLayout.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { intent = new Intent(MainActivity.this, ComboDescriptionActivity.class); intent.putExtra("combo_id",combo.getId()); startActivity(intent); } });
            final RelativeLayout addToCartLayout = (RelativeLayout) comboLayout.findViewById(R.id.add_to_cart_layout);
            final LinearLayout addedToCartLayout = (LinearLayout) comboLayout.findViewById(R.id.added_to_cart_layout);
            final LinearLayout countLayout = (LinearLayout) comboLayout.findViewById(R.id.count_layout);
            final TextView count = (TextView) countLayout.findViewById(R.id.count);
            int quantity = cart.hasHowMany(combo.getId());
            count.setText(String.valueOf(quantity));
            if (quantity>0) { addedToCartLayout.setVisibility(View.VISIBLE); addToCartLayout.setVisibility(View.GONE); countLayout.setVisibility(View.VISIBLE); }
            ImageView plus = (ImageView) countLayout.findViewById(R.id.plus);
            ImageView minus = (ImageView) countLayout.findViewById(R.id.minus);
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                    updateCartCount();
                }
            });
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(count.getText().toString().equals("0")) return;
                    cart.decrementFromCart(combo);
                    count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                    if(Integer.parseInt(count.getText().toString())==0) {
                        Animations.fadeOut(addedToCartLayout, 200);
                        Animations.fadeOut(countLayout, 200);
                        Animations.fadeIn(addToCartLayout, 200);
                    }
                    updateCartCount();
                }
            });
            addToCartLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    Animations.fadeInOnlyIfInvisible(addedToCartLayout, 200);
                    Animations.fadeOut(addToCartLayout, 200);
                    Animations.fadeIn(countLayout, 200);
                    count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                    updateCartCount();
                }
            });
            comboTreeMap.put(combo.getIntPrice(), comboLayout);
        }
        for (int n : comboTreeMap.navigableKeySet())
            fillLayout.addView(comboTreeMap.get(n));
    }

    private void updateCartCount() {
        if(cartCount==null) return;
        int count = cart.getCount();
        if(count>0) { cartCount.setText(String.valueOf(count)); Animations.fadeInOnlyIfInvisible(cartCount, 500); }
        else Animations.fadeOut(cartCount,500);
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
