package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.models.Cart;
import in.foodmash.app.models.User;
import in.foodmash.app.payment.CashOnDeliveryFragment;
import in.foodmash.app.payment.CreditDebitCardFragment;
import in.foodmash.app.payment.NetbankingFragment;
import in.foodmash.app.utils.NumberUtils;
import in.foodmash.app.volley.Swift;
import in.foodmash.app.volley.VolleyFailureFragment;
import in.foodmash.app.volley.VolleyProgressFragment;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutPaymentActivity extends FoodmashActivity implements PaymentRelatedDetailsListener {

    public @Bind(R.id.view_pager) ViewPager viewPager;
    @Bind(R.id.pay) FloatingActionButton pay;
    @Bind(R.id.payable_amount) TextView payableAmount;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.promo_validate) ImageView promoValidate;

    @Bind(R.id.total) TextView total;
    @Bind(R.id.vat) TextView vat;
    @Bind(R.id.vat_percentage) TextView vatPercentage;
    @Bind(R.id.apply) TextView apply;
    @Bind(R.id.promo_discount) TextView promoDiscount;
    @Bind(R.id.confirmed_promo_mash_cash) TextView confirmedPromoMashCash;
    @Bind(R.id.promo_discount_layout) LinearLayout promoDiscountLayout;
    @Bind(R.id.promo_code_input_layout) TextInputLayout promoCodeInputLayout;
    @Bind(R.id.delivery_charges) TextView deliveryCharges;
    public @Bind(R.id.promo_code) EditText promoCode;

    private String paymentMethod;
    private String orderId;
    private Cart cart = Cart.getInstance();
    private Cart.Discount discount = Cart.Discount.NIL;
    public boolean mobileSdkObtained = false;

    public @Bind(R.id.promo_code_layout) View promoCodeLayout;
    public @Bind(R.id.billing_layout) View billingDivider;
    public @Bind(R.id.billing_divider) View billingLayout;
    public @Bind(R.id.payment_progress) View paymentProgress;

    PayuConfig payuConfig = new PayuConfig();
    PayuResponse payuResponse = new PayuResponse();
    PayuHashes payuHashes = new PayuHashes();
    PaymentParams paymentParams = new PaymentParams();

    public PayuResponse getPayuResponse() {return payuResponse;}
    public PayuHashes getPayuHashes() {return payuHashes;}
    public PaymentParams getPaymentParams() {return paymentParams;}
    public PayuConfig getPayuConfig() {return payuConfig;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_payment);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Payment","method");

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
                if(fragment instanceof CashOnDeliveryFragment) {
                    paymentMethod=getResources().getString(R.string.payment_cod);
                    if(isEverythingValid()) makeCodPaymentRequest();
                } else getHashAndDoPayment();
            }
        });

        class PaymentPagerAdapter extends FragmentPagerAdapter {
            public PaymentPagerAdapter(FragmentManager fm) { super(fm); }
            @Override public int getCount() { return (Info.isOnlinePaymentsEnabled(CheckoutPaymentActivity.this))?3:1; }
            @Override public Fragment getItem(int position) {
                if(Info.isOnlinePaymentsEnabled(CheckoutPaymentActivity.this)) {
                    switch (position) {
                        case 0: return new NetbankingFragment();
                        case 1: return new CashOnDeliveryFragment();
                        case 2: return new CreditDebitCardFragment();
                        default:
                            Toast.makeText(CheckoutPaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            return new CashOnDeliveryFragment();
                    }
                } else switch (position) {
                        case 0: return new CashOnDeliveryFragment();
                        default:
                            Toast.makeText(CheckoutPaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            return new CashOnDeliveryFragment();
                    }
            }
            @Override
            public CharSequence getPageTitle(int position) {
                if(Info.isOnlinePaymentsEnabled(CheckoutPaymentActivity.this)) {
                    switch (position) {
                        case 0: return getResources().getString(R.string.net_banking);
                        case 1: return getResources().getString(R.string.cash_on_delivery);
                        case 2: return getResources().getString(R.string.credit_debit_cart);
                        default: return getResources().getString(R.string.cash_on_delivery);
                    }
                } else switch (position) {
                    case 0: return getResources().getString(R.string.cash_on_delivery);
                    default: return getResources().getString(R.string.cash_on_delivery);
                }
            }
        }
        PaymentPagerAdapter paymentPagerAdapter = new PaymentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(paymentPagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override public void onPageScrollStateChanged(int state) { }
            @Override public void onPageSelected(int position) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
                if(fragment instanceof NetbankingFragment) { ((NetbankingFragment) fragment).fillLayout(); }
            }
        });
        promoCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                processDiscountType(s.toString().trim());
                promoCodeInputLayout.setErrorEnabled(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Info.isOnlinePaymentsEnabled(this) && !mobileSdkObtained) {
            setPaymentParams();
            getMobileSdkHash();
        }
        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            Intent intent = new Intent(CheckoutPaymentActivity.this,CheckoutAddressActivity.class);
            intent.putExtra("order_id_error", true);
            startActivity(intent);
            finish();
        }
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePromoCodeRequest();
            }
        });
        payableAmount.setText(NumberUtils.getCurrencyFormat(getIntent().getDoubleExtra("grand_total", 0)));
        total.setText(NumberUtils.getCurrencyFormat(getIntent().getDoubleExtra("total", 0)));
        if (getIntent().getDoubleExtra("total",0) == 0) {
            Intent intent = new Intent(CheckoutPaymentActivity.this,CheckoutAddressActivity.class);
            intent.putExtra("total_error", true);
            startActivity(intent);
            finish();
        }
        vat.setText(NumberUtils.getCurrencyFormat(getIntent().getDoubleExtra("vat", 0)));
        vatPercentage.setText(getIntent().getStringExtra("vat_percentage"));
        deliveryCharges.setText(NumberUtils.getCurrencyFormat(getIntent().getDoubleExtra("delivery_charges", 0)));

    }

    private void setPaymentParams() {
        paymentParams.setKey("i4GjyD");
        paymentParams.setAmount(NumberUtils.getCurrencyFormat(getIntent().getDoubleExtra("grand_total", 0)));
        paymentParams.setProductInfo("a bunch of combos from Foodmash");
        paymentParams.setFirstName(User.getInstance().getName());
        paymentParams.setEmail(User.getInstance().getEmail());
        paymentParams.setTxnId(orderId);
        paymentParams.setSurl(getString(R.string.routes_api_root_path)+getString(R.string.routes_payment_success));
        paymentParams.setFurl(getString(R.string.routes_api_root_path)+getString(R.string.routes_payment_failure));
        paymentParams.setUdf1("");
        paymentParams.setUdf2("");
        paymentParams.setUdf3("");
        paymentParams.setUdf4("");
        paymentParams.setUdf5("");
        paymentParams.setOfferKey("");
        payuConfig.setEnvironment(PayuConstants.PRODUCTION_ENV);
    }

    public void getMobileSdkHash() {
        JsonObjectRequest makeHashRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_get_mobile_sdk_hash), JsonProvider.getStandardRequestJson(this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                pay.setVisibility(View.VISIBLE);
                try {
                    if (response.getBoolean("success")) {
                        payuHashes.setPaymentRelatedDetailsForMobileSdkHash(response.getJSONObject("data").getString("hash"));
                        MerchantWebService merchantWebService = new MerchantWebService();
                        merchantWebService.setKey(paymentParams.getKey());
                        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
                        merchantWebService.setVar1(PayuConstants.VAR1);
                        merchantWebService.setHash(payuHashes.getPaymentRelatedDetailsForMobileSdkHash());
                        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
                        if (postData.getCode() == PayuErrors.NO_ERROR) {
                            payuConfig.setData(postData.getResult());
                            GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(CheckoutPaymentActivity.this);
                            fragmentContainer.setVisibility(View.VISIBLE);
                            pay.setVisibility(View.GONE);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
                            getSupportFragmentManager().executePendingTransactions();
                            paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
                        } else Snackbar.make(mainLayout, postData.getResult(), Snackbar.LENGTH_LONG).show();
                    } else Snackbar.make(mainLayout, "Unable to process your request: " + response.getString("error"), Snackbar.LENGTH_LONG).show();
                } catch (Exception e) { Actions.handleIgnorableException(CheckoutPaymentActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "getMobileSdkHash", pay)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        pay.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makeHashRequest);
    }

    public void getHashAndDoPayment() {
        JsonObjectRequest makeHashRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_get_payment_hash), getPromoMashCashJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                pay.setVisibility(View.VISIBLE);
                try {
                    if (response.getBoolean("success")) {
                        payuHashes.setPaymentHash(response.getJSONObject("data").getString("hash"));
                        paymentParams.setTxnId(response.getJSONObject("data").getString("order_id"));
                        paymentParams.setHash(payuHashes.getPaymentHash());
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
                        if(fragment instanceof NetbankingFragment) { paymentMethod = getString(R.string.payment_netbanking); ((NetbankingFragment) fragment).doPayment(); }
                        else if(fragment instanceof CreditDebitCardFragment) { paymentMethod = getString(R.string.payment_card); ((CreditDebitCardFragment) fragment).doPayment(); }
                    } else Snackbar.make(mainLayout,"Unable to process your request: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(CheckoutPaymentActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "getHashAndDoPayment", pay)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        pay.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makeHashRequest);
    }

    private JSONObject getPromoMashCashJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutPaymentActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("payment_method",paymentMethod);
            switch (discount) {
                case PROMO_CODE: dataJson.put("promo_code",promoCode.getText().toString().trim()); break;
                case MASH_CASH: dataJson.put("mash_cash", promoCode.getText().toString().trim()); break;
                case NIL: break;
            }
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    public void makeCodPaymentRequest() {
        JsonObjectRequest makePurchaseRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + getString(R.string.routes_pay_by_cod), getPromoMashCashJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                pay.setVisibility(View.VISIBLE);
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        String orderId = dataJson.getString("order_id");
                        Intent intent = new Intent(CheckoutPaymentActivity.this,OrderDescriptionActivity.class);
                        intent.putExtra("order_id",orderId);
                        intent.putExtra("cart",true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        cart.removeAllOrders();
                        startActivity(intent);
                        finish();
                    } else Snackbar.make(mainLayout, "Order was not placed: "+response.getString("error"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeCodPaymentRequest", pay)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });

        fragmentContainer.setVisibility(View.VISIBLE);
        pay.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makePurchaseRequest);
    }

    private JSONObject getPromoCodeJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutPaymentActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("promo_code",promoCode.getText().toString().trim());
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void processDiscountType(String discountString) {
        try { 
            if(discountString.length()==0) discount = Cart.Discount.NIL;
            else { 
                Integer.parseInt(discountString); 
                discount = Cart.Discount.MASH_CASH;
            }
        } catch (NumberFormatException e) { discount = Cart.Discount.PROMO_CODE; }
        switch (discount) {
            case MASH_CASH: promoCodeInputLayout.setHint("Mash Cash"); break;
            case PROMO_CODE: promoCodeInputLayout.setHint("Promo Code"); break;
            case NIL: promoCodeInputLayout.setHint("Mash Cash / Promo Code");
        }
    }

    public void makePromoCodeRequest() {
        if(discount== Cart.Discount.NIL) { Snackbar.make(mainLayout, "PromoCode or MashCash is empty", Snackbar.LENGTH_SHORT); return; }
        if(discount== Cart.Discount.MASH_CASH && !Info.isMashCashEnabled(this)) { Snackbar.make(mainLayout, "MashCash is not available yet", Snackbar.LENGTH_SHORT); return; }
        JsonObjectRequest makePurchaseRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.routes_api_root_path) + ((discount== Cart.Discount.MASH_CASH)?getString(R.string.routes_apply_mash_cash):getString(R.string.routes_apply_promo_code)), getPromoCodeJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                pay.setVisibility(View.VISIBLE);
                try {
                    if(response.getBoolean("success")) {
                        Snackbar.make(mainLayout, ((discount== Cart.Discount.MASH_CASH)?"Mash Cash applied successfully":"Promo Code applied successfully"), Snackbar.LENGTH_SHORT).show();
                        apply.setText("Remove");
                        apply.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onResume();
                                promoCode.setText("");
                                promoValidate.setVisibility(View.GONE);
                                promoDiscountLayout.setVisibility(View.GONE);
                                promoDiscount.setText("");
                                cart.discount = Cart.Discount.NIL;
                                confirmedPromoMashCash.setText("");
                                confirmedPromoMashCash.setVisibility(View.GONE);
                                promoCode.setVisibility(View.VISIBLE);
                                apply.setText("Apply");
                            }
                        });
                        payableAmount.setText(NumberUtils.getCurrencyFormat(response.getJSONObject("data").getDouble("grand_total")));
                        promoDiscount.setText(NumberUtils.getCurrencyFormat(response.getJSONObject("data").getDouble("promo_discount")));
                        promoDiscountLayout.setVisibility(View.VISIBLE);
                        cart.discount = discount;
                        confirmedPromoMashCash.setText(promoCode.getText().toString().trim());
                        confirmedPromoMashCash.setVisibility(View.VISIBLE);
                        promoCode.setVisibility(View.GONE);
                        promoCodeInputLayout.setErrorEnabled(false);
                        promoValidate.setVisibility(View.VISIBLE);
                    } else {
                        onResume();
                        confirmedPromoMashCash.setText("");
                        confirmedPromoMashCash.setVisibility(View.GONE);
                        promoCode.setVisibility(View.VISIBLE);
                        promoValidate.setVisibility(View.GONE);
                        promoDiscountLayout.setVisibility(View.GONE);
                        promoDiscount.setText("");
                        cart.discount = Cart.Discount.NIL;
                        promoCode.requestFocus();
                        String messageFromServer = "";
                        if(response.has("message") && !response.isNull("message"))
                            messageFromServer = response.getString("message");
                        if(messageFromServer.length()>0) promoCodeInputLayout.setError(messageFromServer);
                        else promoCodeInputLayout.setError(((discount == Cart.Discount.MASH_CASH) ? "Not enough Mash Cash" : "Invalid Promo Code"));
                        Snackbar.make(mainLayout, ((discount == Cart.Discount.MASH_CASH) ? "Mash Cash not applied" : "Promo Code not applied"), Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makePromoCodeRequest", pay)).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        });

        fragmentContainer.setVisibility(View.VISIBLE);
        pay.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makePurchaseRequest);
    }

    private boolean isEverythingValid() {
        return !(paymentMethod.length()<1);
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        Log.i("Payments", "Netbanking Response: "+payuResponse.getResponseStatus().getResult());
        if(payuResponse.getResponseStatus().getCode()!=0) {
            fragmentContainer.setVisibility(View.VISIBLE);
            pay.setVisibility(View.VISIBLE);
            NetworkResponse networkResponse = new NetworkResponse(payuResponse.getResponseStatus().getResult().getBytes());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(new NetworkError(networkResponse), "getMobileSdkHash", pay)).commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
            return;
        }
        fragmentContainer.setVisibility(View.GONE);
        pay.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
        this.payuResponse = payuResponse;
        mobileSdkObtained = true;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if(fragment instanceof NetbankingFragment) { ((NetbankingFragment) fragment).fillLayout(); }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            String result = data.getStringExtra("result");
            if (resultCode == RESULT_CANCELED) {
                Snackbar.make(mainLayout, "Transaction failed. " + result, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try again", new View.OnClickListener() { @Override public void onClick(View v) { } })
                        .show();
            } else if (resultCode == RESULT_OK) {
                orderId = data.getStringExtra("order_id");
                Intent intent = new Intent(CheckoutPaymentActivity.this, OrderDescriptionActivity.class);
                intent.putExtra("cart", true);
                intent.putExtra("order_id", orderId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                cart.removeAllOrders();
                cart.discount = Cart.Discount.NIL;
                startActivity(intent);
                finish();
            }
        }
    }

}
