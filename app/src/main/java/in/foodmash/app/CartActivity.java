package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.custom.Combo;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CartActivity extends FoodmashActivity implements View.OnClickListener {

    @Bind(R.id.buy) FloatingActionButton buy;
    @Bind(R.id.fill_layout) LinearLayout fillLayout;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.empty_cart_layout) LinearLayout emptyCartLayout;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.payable_amount) TextView total;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
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
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Mash","cart");

        buy.setOnClickListener(this);
        fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
        emptyCartLayout = (LinearLayout) findViewById(R.id.empty_cart_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);

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

        fillLayout.removeAllViews();
        total.setText(NumberUtils.getCurrencyFormat(cart.getTotal()));
        if(cart.getCount()>0) { emptyCartLayout.setVisibility(View.GONE); fillLayout.setVisibility(View.VISIBLE); }
        else { emptyCartLayout.setVisibility(View.VISIBLE); fillLayout.setVisibility(View.GONE); }
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
            final LinearLayout addNote = (LinearLayout) comboLayout.findViewById(R.id.add_note);
            final LinearLayout noteLayout = (LinearLayout) comboLayout.findViewById(R.id.note_layout);
            final TextView note = (TextView) comboLayout.findViewById(R.id.note);
            addNote.setOnClickListener(new View.OnClickListener() {
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
                    noteEditText.setTextSize(14);
                    linearLayout.addView(noteEditText);
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (noteEditText.getText().toString().length() != 0) {
                                combo.setNote(noteEditText.getText().toString());
                                note.setText(combo.getNote());
                                noteLayout.setVisibility(View.VISIBLE);
                                addNote.setVisibility(View.GONE);
                            }
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {} });
                    alertDialog.setTitle("Add Note");
                    alertDialog.setView(linearLayout);
                    alertDialog.show();
                }
            });
            noteLayout.setOnClickListener(new View.OnClickListener() {
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
                    noteEditText.setHint("Make noodles spicy!");
                    noteEditText.setTextSize(14);
                    linearLayout.addView(noteEditText);
                    final AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (noteEditText.getText().toString().length() != 0) {
                                combo.setNote(noteEditText.getText().toString());
                                note.setText(combo.getNote());
                                noteLayout.setVisibility(View.VISIBLE);
                                addNote.setVisibility(View.GONE);
                            }
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            combo.setNote("");
                            addNote.setVisibility(View.VISIBLE);
                            noteLayout.setVisibility(View.GONE);
                        }
                    });
                    alertDialog.setTitle("Edit Note");
                    alertDialog.setView(linearLayout);
                    alertDialog.show();
                }
            });
            fillLayout.addView(comboLayout);
        }

        swipeRefreshLayout.setRefreshing(false);
    }

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

    @Override public void onBackPressed() { intent = new Intent(CartActivity.this,MainActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent); }

}
