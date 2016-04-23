package in.foodmash.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.models.Cart;
import in.foodmash.app.models.Combo;
import in.foodmash.app.models.ComboOption;
import in.foodmash.app.models.ComboOptionDish;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Sep 30 2015.
 */
public class ComboDescriptionActivity extends FoodmashActivity implements View.OnClickListener {

    private static final int MY_PERMISSION_CALL_PHONE = 17;

    @Bind(R.id.main_layout) View mainLayout;
    @Bind(R.id.combo_option_view_pager) ViewPager comboOptionViewPager;
    @Bind(R.id.price) TextView currentPrice;
    @Bind(R.id.combo_unavailable) TextView comboUnavailable;
    @Bind(R.id.buy) FloatingActionButton buy;
    @Bind(R.id.back) FloatingActionButton back;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.cost_layout) LinearLayout costLayout;

    private TextView cartCount;
    private Cart cart = Cart.getInstance();
    private Intent intent;
    private Combo combo;
    private ImageLoader imageLoader;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Info.isLoggedIn(ComboDescriptionActivity.this)) getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        else getMenuInflater().inflate(R.menu.menu_main_anonymous_login, menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count);
        Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ComboDescriptionActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile: intent = new Intent(this, ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this, AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this, OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this, ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: Actions.logout(ComboDescriptionActivity.this); return true;
            case R.id.menu_cart: intent = new Intent(this, CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_description);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Combo","contents");

        imageLoader = Swift.getInstance(ComboDescriptionActivity.this).getImageLoader();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        try {
            int comboId = getIntent().getIntExtra("combo_id", -1);
            if(comboId==-1) throw new NullPointerException("Combo is not found");
            List<Combo> combos = Arrays.asList(objectMapper.readValue(Info.getComboJsonArrayString(this), Combo[].class));
            for (Combo c : combos) if (c.getId() ==comboId) combo = c;
            if(combo==null) throw new NullPointerException("Combo is not found");
            if(combo.isCustomizable())
                if(cart.hasCombo(combo.getId()))
                    combo = cart.fetchLastCombo(combo.getId());
            Collections.sort(combo.getComboOptions(), new Comparator<ComboOption>() {
                @Override
                public int compare(ComboOption lhs, ComboOption rhs) {
                    return lhs.getPriority()-rhs.getPriority();
                }
            });
        } catch (Exception e) {
            Snackbar.make(mainLayout,"Something went wrong. Try again later!",Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }

        comboOptionViewPager.setAdapter(new ComboOptionAdapter());
        buy.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!combo.isAvailable()) {
            comboUnavailable.setVisibility(View.VISIBLE);
            buy.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
        }
        updatePrice();
    }

    private void updatePrice() {
        float price = 0;
        for(ComboOption comboOption: combo.getComboOptions())
            for(ComboOptionDish comboDish: comboOption.getSelectedComboOptionDishes())
                price += comboDish.getDish().getPrice()*comboDish.getQuantity();
        currentPrice.setText(String.valueOf((int)price));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy:
                if(!combo.isValid()) {
                    Snackbar.make(mainLayout, "Combo should contain atleast one " + combo.getOptionalComboOptionsNames(), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(combo.isCustomizable())
                    if(cart.hasCombo(combo.getId()))
                        cart.decrementFromCart(combo.getId());
                cart.addToCart(new Combo(combo));
                Snackbar.make(mainLayout, "Added to Cart", Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() { @Override public void onClick(View v) {
                        cart.decrementFromCart(combo.getId());
                        Actions.updateCartCount(cartCount); } })
                    .show();
                Actions.updateCartCount(cartCount);
                break;
            case R.id.back: onBackPressed(); break;
        }
    }

    class ComboDishAdapter extends RecyclerView.Adapter {
        ComboOption comboOption;
        ComboDishAdapter(ComboOption comboOption) { this.comboOption = comboOption; }
        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.id) TextView id;
            @Bind(R.id.name) TextView name;
            @Bind(R.id.price) TextView price;
            @Bind(R.id.count) TextView count;
            @Bind(R.id.label) ImageView label;
            @Bind(R.id.plus) ImageView plus;
            @Bind(R.id.minus) ImageView minus;
            @Bind(R.id.image) NetworkImageView image;
            @Bind(R.id.add_to_cart) ImageView addToCart;
            @Bind(R.id.restaurant_logo) NetworkImageView restaurantLogo;
            @Bind(R.id.count_layout) LinearLayout countLayout;
            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
        @Override public int getItemCount() { return comboOption.getComboOptionDishes().size(); }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(ComboDescriptionActivity.this).inflate(R.layout.repeatable_combo_description_combo_option_dish,parent,false)); }
        @Override public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final ComboOptionDish comboOptionDish = comboOption.getComboOptionDishes().get(position);
            switch(comboOptionDish.getDish().getLabel()) {
                case EGG: viewHolder.label.setColorFilter(ContextCompat.getColor(ComboDescriptionActivity.this, R.color.egg)); break;
                case VEG: viewHolder.label.setColorFilter(ContextCompat.getColor(ComboDescriptionActivity.this, R.color.veg)); break;
                case NON_VEG: viewHolder.label.setColorFilter(ContextCompat.getColor(ComboDescriptionActivity.this, R.color.non_veg)); break;
            }
            viewHolder.id.setText(String.valueOf(comboOptionDish.getId()));
            viewHolder.name.setText(comboOptionDish.getDish().getName());
            viewHolder.price.setText(NumberUtils.getCurrencyFormatWithoutDecimals(comboOptionDish.getDish().getPrice()));
            viewHolder.image.getLayoutParams().height = (int)(getWidthPx()*0.6) - dpToPx(10);
            viewHolder.image.setImageUrl(comboOptionDish.getDish().getPicture(), imageLoader);
            viewHolder.restaurantLogo.setImageUrl(comboOptionDish.getDish().getRestaurant().getLogo(), imageLoader);
            int quantity = comboOptionDish.getQuantity();
            viewHolder.count.setText(String.valueOf(quantity));
            if(quantity>0) { viewHolder.addToCart.setVisibility(View.GONE); viewHolder.countLayout.setVisibility(View.VISIBLE); }
            else { viewHolder.countLayout.setVisibility(View.GONE); viewHolder.addToCart.setVisibility(View.VISIBLE); }
            View.OnClickListener incrementQuantity = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!comboOption.incrementQuantity(comboOptionDish))
                        Snackbar.make(mainLayout, "For bulk orders, contact Foodmash", Snackbar.LENGTH_LONG)
                                .setAction("Call", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if ( ContextCompat.checkSelfPermission( ComboDescriptionActivity.this, android.Manifest.permission.CALL_PHONE ) != PackageManager.PERMISSION_GRANTED )
                                            ActivityCompat.requestPermissions( ComboDescriptionActivity.this, new String[] {  android.Manifest.permission.CALL_PHONE  },
                                                    MY_PERMISSION_CALL_PHONE );
                                        else {
                                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                                            callIntent.setData(Uri.parse("tel:+918056249612"));
                                            startActivity(callIntent);
                                        }
                                    }
                                }).show();
                    int quantity = comboOptionDish.getQuantity();
                    viewHolder.count.setText(String.valueOf(quantity));
                    if(quantity>0) { viewHolder.addToCart.setVisibility(View.GONE); viewHolder.countLayout.setVisibility(View.VISIBLE); }
                    else { viewHolder.countLayout.setVisibility(View.GONE); viewHolder.addToCart.setVisibility(View.VISIBLE); }
                    updatePrice();
                }
            };
            viewHolder.plus.setOnClickListener(incrementQuantity);
            viewHolder.addToCart.setOnClickListener(incrementQuantity);
            viewHolder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(comboOptionDish.getQuantity()==0) return;
                    if(!comboOption.decrementQuantity(comboOptionDish))
                        Snackbar.make(mainLayout, "Combo should contain minimum "+comboOption.getMinCount()+" "+comboOption.getName(), Snackbar.LENGTH_SHORT).show();
                    int quantity = comboOptionDish.getQuantity();
                    viewHolder.count.setText(String.valueOf(quantity));
                    if(quantity>0) { viewHolder.addToCart.setVisibility(View.GONE); viewHolder.countLayout.setVisibility(View.VISIBLE); }
                    else { viewHolder.countLayout.setVisibility(View.GONE); viewHolder.addToCart.setVisibility(View.VISIBLE); }
                    updatePrice();
                }
            });

            if(position == this.getItemCount()-1) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(40));
                viewHolder.itemView.setLayoutParams(layoutParams);
            }
        }
    }

    class ComboOptionAdapter extends PagerAdapter {
        @Override public int getCount() { return combo.getComboOptions().size(); }
        @Override public CharSequence getPageTitle(int position) { return combo.getComboOptions().get(position).getName(); }
        @Override public boolean isViewFromObject(View view, Object object) { return view == (LinearLayout)object; }
        @Override public void destroyItem(ViewGroup container, int position, Object object) { container.removeView((LinearLayout)object); }
        @Override public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.repeatable_combo_description_page, container, false);
            RecyclerView comboDishRecyclerView = (RecyclerView) view.findViewById(R.id.combo_dish_recycler_view);
            comboDishRecyclerView.hasFixedSize();
            comboDishRecyclerView.setLayoutManager(new LinearLayoutManager(ComboDescriptionActivity.this));
            comboDishRecyclerView.setAdapter(new ComboDishAdapter(combo.getComboOptions().get(position)));
            comboDishRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                int scrollDy = 0;
                @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) { super.onScrollStateChanged(recyclerView, newState); }
                @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    scrollDy+= dy;
//                    if(scrollDy > 40) getSupportActionBar().hide();
//                    else getSupportActionBar().show();
                }
            });
            container.addView(view);
            return view;
        }
    }
}
