package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.models.Cart;
import in.foodmash.app.models.Combo;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CartActivity extends FoodmashActivity implements View.OnClickListener {

    @Bind(R.id.buy) FloatingActionButton buy;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.empty_cart_layout) LinearLayout emptyCartLayout;
    @Bind(R.id.cart_progress) LinearLayout cartProgress;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.cart_recycler_view) RecyclerView cartRecyclerView;
    @Bind(R.id.payable_amount) TextView total;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private Menu menu;
    private Cart cart = Cart.getInstance();
    private CartAdapter cartAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_activity_cart, menu);
        updateDeleteButtonVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete_cart) {
            new AlertDialog.Builder(CartActivity.this)
                .setTitle("Remove all from cart ?")
                .setMessage("Do you want to remove all combos added to the cart?")
                .setPositiveButton("Remove All", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        cart.removeAllOrders();
                        cartAdapter.notifyDataSetChanged();
                        total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
                        cartProgress.setVisibility(View.GONE);
                        Animations.fadeIn(emptyCartLayout,500);
                        updateDeleteButtonVisibility();
                    }
                }).setNegativeButton("No, don't remove", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { }
                }).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDeleteButtonVisibility() {
        if(menu == null) return;
        if(cart.getCount()==0) menu.findItem(R.id.menu_delete_cart).setVisible(false);
        else menu.findItem(R.id.menu_delete_cart).setVisible(true);
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
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Mash","cart");

        buy.setOnClickListener(this);
        cartAdapter = new CartAdapter();
        cartRecyclerView.hasFixedSize();
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
        cartRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int scrollDy = 0;
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) { super.onScrollStateChanged(recyclerView, newState); }
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollDy += dy;
                swipeRefreshLayout.setEnabled(scrollDy == 0);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() { @Override public void onRefresh() { onResume(); } });


    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);

        updateDeleteButtonVisibility();
        if(getIntent().getBooleanExtra("order_id_error",false)) {
            final Snackbar totalErrorSnackbar = Snackbar.make(mainLayout, "Something went wrong!", Snackbar.LENGTH_INDEFINITE);
            totalErrorSnackbar.setAction("Try Again", new View.OnClickListener() { @Override public void onClick(View v) { totalErrorSnackbar.dismiss(); } });
            totalErrorSnackbar.show();
        }

        if(getIntent().getBooleanExtra("combo_error",false)) {
            final Snackbar invalidCombos = Snackbar.make(mainLayout, "Combos that are not currently available are removed.", Snackbar.LENGTH_INDEFINITE);
            invalidCombos.setAction("Try Again", new View.OnClickListener() { @Override public void onClick(View v) { invalidCombos.dismiss(); } });
            invalidCombos.show();
        }

        total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
        if(cart.getCount()>0) { emptyCartLayout.setVisibility(View.GONE); cartProgress.setVisibility(View.VISIBLE); cartRecyclerView.setVisibility(View.VISIBLE); }
        else { emptyCartLayout.setVisibility(View.VISIBLE); cartProgress.setVisibility(View.GONE); cartRecyclerView.setVisibility(View.GONE); }

        cartAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override public void onBackPressed() { intent = new Intent(CartActivity.this,MainActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent); }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy:
                if(cart.getCount()==0) Snackbar.make(mainLayout,"Cart is empty, Add some combos and we'll give it a go!",Snackbar.LENGTH_LONG).show();
                else if(Info.isLoggedIn(this)) startActivity(new Intent(CartActivity.this, CheckoutAddressActivity.class));
                else {
                    Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                    intent.putExtra("from_cart", true);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    class CartAdapter extends RecyclerView.Adapter {

        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.name) TextView name;
            @Bind(R.id.amount) TextView amount;
            @Bind(R.id.count) TextView count;
            @Bind(R.id.note) TextView note;
            @Bind(R.id.dishes) TextView dishes;
            @Bind(R.id.note_layout) LinearLayout noteLayout;
            @Bind(R.id.add_note) LinearLayout addNote;
            @Bind(R.id.minus) ImageView minus;
            @Bind(R.id.plus) ImageView plus;
            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        @Override public int getItemCount() { return cart.getOrders().size(); }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(CartActivity.this).inflate(R.layout.repeatable_cart_item, parent, false)); }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final Combo combo = new ArrayList<>(cart.getOrders().keySet()).get(position);
            viewHolder.name.setText(combo.getName());
            viewHolder.dishes.setText(combo.getDishNames());
            viewHolder.count.setText(String.valueOf(cart.getOrders().get(combo)));
            viewHolder.amount.setText(String.valueOf((int)combo.calculatePrice() * cart.getOrders().get(combo)));
            viewHolder.plus.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    cart.addToCart(combo);
                    viewHolder.count.setText(String.valueOf(cart.getCount(combo)));
                    viewHolder.amount.setText(String.valueOf((int)combo.calculatePrice() * cart.getOrders().get(combo)));
                    total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
                    updateDeleteButtonVisibility();
                }});
            viewHolder.minus.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    viewHolder.count.setText(String.valueOf(cart.decrementFromCart(combo)));
                    if(cart.hasCombo(combo)) viewHolder.amount.setText(String.valueOf((int)combo.calculatePrice() * cart.getOrders().get(combo)));
                    if(cart.getCount()==0) { Animations.fadeIn(emptyCartLayout, 500); cartProgress.setVisibility(View.GONE); }
                    total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
                    updateDeleteButtonVisibility();
                    notifyDataSetChanged();
                }});
            viewHolder.addNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CartActivity.this);
                    alertBuilder.setCancelable(true);
                    LinearLayout linearLayout = new LinearLayout(CartActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setPadding(dpToPx(25),dpToPx(10),dpToPx(25),0);
                    linearLayout.setLayoutParams(layoutParams);
                    TextView textView = new TextView(CartActivity.this);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setText("You shall request for customization of dishes here.");
                    textView.setPadding(0,0,0,dpToPx(10));
                    linearLayout.addView(textView);
                    final EditText noteEditText = new EditText(CartActivity.this);
                    noteEditText.setLayoutParams(layoutParams);
                    noteEditText.setHint("Make noodles spicy!");
                    noteEditText.setTextSize(14);
                    linearLayout.addView(noteEditText);
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (noteEditText.getText().toString().length() != 0) {
                                combo.setNote(noteEditText.getText().toString());
                                viewHolder.note.setText(combo.getNote());
                                viewHolder.noteLayout.setVisibility(View.VISIBLE);
                                viewHolder.addNote.setVisibility(View.GONE);
                            }
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {} });
                    alertDialog.setTitle("Add Note");
                    alertDialog.setView(linearLayout);
                    alertDialog.show();
                }
            });
            viewHolder.noteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CartActivity.this);
                    alertBuilder.setCancelable(true);
                    LinearLayout linearLayout = new LinearLayout(CartActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setPadding(dpToPx(25),dpToPx(10),dpToPx(25),0);
                    linearLayout.setLayoutParams(layoutParams);
                    final EditText noteEditText = new EditText(CartActivity.this);
                    noteEditText.setLayoutParams(layoutParams);
                    noteEditText.setText(combo.getNote());
                    noteEditText.setTextSize(14);
                    linearLayout.addView(noteEditText);
                    final AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (noteEditText.getText().toString().length() != 0) {
                                combo.setNote(noteEditText.getText().toString());
                                viewHolder.note.setText(combo.getNote());
                                viewHolder.noteLayout.setVisibility(View.VISIBLE);
                                viewHolder.addNote.setVisibility(View.GONE);
                            }
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            combo.setNote("");
                            viewHolder.addNote.setVisibility(View.VISIBLE);
                            viewHolder.noteLayout.setVisibility(View.GONE);
                        }
                    });
                    alertDialog.setTitle("Edit Note");
                    alertDialog.setView(linearLayout);
                    alertDialog.show();
                }
            });
            if(viewHolder.getLayoutPosition() == this.getItemCount()-1) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(40));
                viewHolder.itemView.setLayoutParams(layoutParams);
            } else {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(0));
                viewHolder.itemView.setLayoutParams(layoutParams);
            }
        }
    }

}
