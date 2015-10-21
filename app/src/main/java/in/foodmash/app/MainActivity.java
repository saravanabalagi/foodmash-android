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
import java.util.HashSet;
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
import in.foodmash.app.custom.ComboDish;
import in.foodmash.app.custom.ComboOption;
import in.foodmash.app.custom.Restaurant;

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
        else getMenuInflater().inflate(R.menu.menu_main_anonymous_login,menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { intent = new Intent(MainActivity.this, CartActivity.class); startActivity(intent); } });
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        System.out.println("Resumed");
        Actions.updateCartCount(cartCount);
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
                        Cache.setCombos(Arrays.asList(mapper.readValue(response.getJSONObject("data").getJSONArray("combos").toString(), Combo[].class)));
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
        if(combos==null||combos.size()==0) { Swift.getInstance(MainActivity.this).addToRequestQueue(getCombosRequest); return; }
        fillLayout.removeAllViews();
        TreeMap<Integer,LinearLayout> comboTreeMap = new TreeMap<>();
        for (final Combo combo: combos) {
            View.OnClickListener showDescription = new View.OnClickListener() { @Override public void onClick(View v) { intent = new Intent(MainActivity.this, ComboDescriptionActivity.class); intent.putExtra("combo_id",combo.getId()); startActivity(intent); } };
            final LinearLayout comboLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo, fillLayout, false);
            ((TextView) comboLayout.findViewById(R.id.id)).setText(String.valueOf(combo.getId()));
            ((NetworkImageView) comboLayout.findViewById(R.id.image)).setImageUrl(getImageUrl(), imageLoader);
            ((TextView) comboLayout.findViewById(R.id.name)).setText(combo.getName());
            LinearLayout contentsLayout = (LinearLayout) comboLayout.findViewById(R.id.contents_layout);
            TreeMap<Integer,String> contents = combo.getContents();
            for(int n:contents.navigableKeySet()) {
                LinearLayout contentTextView = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_content,contentsLayout,false);
                ((TextView)contentTextView.findViewById(R.id.content)).setText(contents.get(n));
                contentTextView.findViewById(R.id.content).setOnClickListener(showDescription);
                contentsLayout.addView(contentTextView);
            }
            ((TextView) comboLayout.findViewById(R.id.price)).setText(String.valueOf((int)combo.getPrice()));
            ImageView foodLabel = (ImageView) comboLayout.findViewById(R.id.label);
            switch(combo.getLabel()) {
                case "egg": foodLabel.setColorFilter(getResources().getColor(R.color.egg)); break;
                case "veg": foodLabel.setColorFilter(getResources().getColor(R.color.veg)); break;
                case "non-veg": foodLabel.setColorFilter(getResources().getColor(R.color.non_veg)); break;
            }
            comboLayout.findViewById(R.id.clickable_layout).setOnClickListener(showDescription);
            comboLayout.findViewById(R.id.image).setOnClickListener(showDescription);
            final LinearLayout addToCartLayout = (LinearLayout) comboLayout.findViewById(R.id.add_to_cart_layout);
            final LinearLayout addedToCartLayout = (LinearLayout) comboLayout.findViewById(R.id.added_to_cart_layout);
            final LinearLayout countLayout = (LinearLayout) comboLayout.findViewById(R.id.count_layout);
            final TextView count = (TextView) countLayout.findViewById(R.id.count);
            int quantity = cart.getCount(combo.getId());
            count.setText(String.valueOf(quantity));
            if (quantity>0) { addedToCartLayout.setVisibility(View.VISIBLE); addToCartLayout.setVisibility(View.GONE); countLayout.setVisibility(View.VISIBLE); }
            ImageView plus = (ImageView) countLayout.findViewById(R.id.plus);
            ImageView minus = (ImageView) countLayout.findViewById(R.id.minus);
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    count.setText(String.valueOf(cart.getCount(combo.getId())));
                    Actions.updateCartCount(cartCount);
                }
            });
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(count.getText().toString().equals("0")) return;
                    cart.decrementFromCart(combo);
                    count.setText(String.valueOf(cart.getCount(combo.getId())));
                    if(cart.getCount(combo.getId())==0) {
                        Animations.fadeOut(addedToCartLayout, 200);
                        Animations.fadeOut(countLayout, 200);
                        Animations.fadeIn(addToCartLayout, 200);
                    }
                    Actions.updateCartCount(cartCount);
                }
            });
            addToCartLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cart.addToCart(new Combo(combo));
                    Animations.fadeInOnlyIfInvisible(addedToCartLayout, 200);
                    Animations.fadeOut(addToCartLayout, 200);
                    Animations.fadeIn(countLayout, 200);
                    count.setText(String.valueOf(cart.getCount(combo.getId())));
                    Actions.updateCartCount(cartCount);
                }
            });

            LinearLayout restaurantsLayout = (LinearLayout) comboLayout.findViewById(R.id.restaurant_layout);
            HashSet<Restaurant> restaurantsList = new HashSet<>();
            for (ComboOption comboOption : combo.getComboOptions())
                if(comboOption.isFromSameRestaurant()) restaurantsList.add(comboOption.getComboOptionDishes().get(0).getDish().getRestaurant());
                else for (ComboDish comboDish : comboOption.getComboOptionDishes())
                        restaurantsList.add(comboDish.getDish().getRestaurant());
            for (ComboDish comboDish : combo.getComboDishes())
                restaurantsList.add(comboDish.getDish().getRestaurant());
            for (Restaurant restaurant: restaurantsList) {
                LinearLayout restaurantLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.restaurant_logo,restaurantsLayout,false);
                ((TextView)restaurantLayout.findViewById(R.id.name)).setText(restaurant.getName());
                ((NetworkImageView)restaurantLayout.findViewById(R.id.logo)).setImageUrl(getRestaurantImageUrl(),imageLoader);
                restaurantsLayout.addView(restaurantLayout);
            }

            comboTreeMap.put((int)combo.getPrice(), comboLayout);
        }
        for (int n : comboTreeMap.navigableKeySet())
            fillLayout.addView(comboTreeMap.get(n));
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
