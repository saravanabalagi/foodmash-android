package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
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
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.payment.CashOnDeliveryFragment;
import in.foodmash.app.payment.CreditDebitCardFragment;
import in.foodmash.app.payment.NetbankingFragment;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutPaymentActivity extends AppCompatActivity implements PaymentRelatedDetailsListener {

    @Bind(R.id.pay) FloatingActionButton pay;
    @Bind(R.id.payable_amount) TextView payableAmount;
    @Bind(R.id.connecting_layout) LinearLayout connectingLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.view_pager) ViewPager viewPager;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.total) TextView total;
    @Bind(R.id.vat) TextView vat;
    @Bind(R.id.delivery_charges) TextView deliveryCharges;

    private Intent intent;
    private String paymentMethod;
    private String payableAmountString;
    private String password;
    private Snackbar snackbar;
    private String orderId;

    PayuConfig payuConfig = new PayuConfig();
    PayuResponse payuResponse = new PayuResponse();
    PayuHashes payuHashes = new PayuHashes();
    PaymentParams paymentParams = new PaymentParams();

    public PayuResponse getPayuResponse() {return payuResponse;}
    public PayuHashes getPayuHashes() {return payuHashes;}
    public PaymentParams getPaymentParams() {return paymentParams;}
    public PayuConfig getPayuConfig() {return payuConfig;}

    private JsonObjectRequest makePurchaseRequest;
    private JsonObjectRequest makeHashRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_payment);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        if(Cart.getInstance().getCount()==0) finish();
        if(getIntent().getDoubleExtra("payable_amount", 0)!=0) payableAmountString = NumberUtils.getCurrencyFormat(getIntent().getDoubleExtra("payable_amount",0));
        else Alerts.commonErrorAlert(CheckoutPaymentActivity.this, "Transaction not authorized", "We found something suspicious about your current order. Try again!", "Back", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { finish(); } }, false);
        if(!NumberUtils.getCurrencyFormat(Cart.getInstance().getGrandTotal()).equals(NumberUtils.getCurrencyFormat(getIntent().getDoubleExtra("payable_amount", 0)))) finish();
        orderId = getIntent().getStringExtra("order_id");

        total.setText(NumberUtils.getCurrencyFormat(Cart.getInstance().getTotal()));
        vat.setText(NumberUtils.getCurrencyFormat(Cart.getInstance().getVatForTotal()));
        deliveryCharges.setText(NumberUtils.getCurrencyFormat(Cart.getInstance().getDeliveryCharge()));

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
                if(fragment instanceof CashOnDeliveryFragment) {
                    password = ((CashOnDeliveryFragment) fragment).getPassword();
                    paymentMethod=getResources().getString(R.string.payment_cod);
                    if(isEverythingValid()) makeCodPaymentRequest();
                } else if(fragment instanceof NetbankingFragment) { paymentMethod = getString(R.string.payment_netbanking); ((NetbankingFragment) fragment).doPayment(); }
                else if(fragment instanceof CreditDebitCardFragment) { paymentMethod = getString(R.string.payment_card); ((CreditDebitCardFragment) fragment).doPayment(); }
            }
        });
        payableAmount.setText(payableAmountString);

        class PaymentPagerAdapter extends FragmentPagerAdapter {
            public PaymentPagerAdapter(FragmentManager fm) { super(fm); }
            @Override public int getCount() { return 1; }
            @Override public Fragment getItem(int position) {
                switch (position) {
                    case 0: return new CashOnDeliveryFragment();
                    case 1: return new CreditDebitCardFragment();
                    case 2: return new NetbankingFragment();
                    default:
                        Toast.makeText(CheckoutPaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        return new CashOnDeliveryFragment();
                }
            }
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0: return getResources().getString(R.string.cash_on_delivery);
                    case 1: return getResources().getString(R.string.credit_debit_cart);
                    case 2: return getResources().getString(R.string.net_banking);
                    default: return getResources().getString(R.string.cash_on_delivery);
                }
            }
        }
        PaymentPagerAdapter paymentPagerAdapter = new PaymentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(paymentPagerAdapter);
        //getHashAndPaymentRelatedDetails();
    }

    public void getHashAndPaymentRelatedDetails() {
        paymentParams.setKey("gtKFFx");
        paymentParams.setAmount(payableAmountString);
        paymentParams.setProductInfo("a bunch of combos from Foodmash");
        paymentParams.setFirstName(Info.getName(this));
        paymentParams.setPhone(Info.getPhone(this));
        paymentParams.setEmail(Info.getEmail(this));
        paymentParams.setTxnId(orderId);
        paymentParams.setSurl(getString(R.string.api_root_path)+"/payment/success");
        paymentParams.setFurl(getString(R.string.api_root_path)+"/payment/failure");
        paymentParams.setUdf1("");
        paymentParams.setUdf2("");
        paymentParams.setUdf3("");
        paymentParams.setUdf4("");
        paymentParams.setUdf5("");
        paymentParams.setUserCredentials("gtKFFx:foodmash@payu.in");
        paymentParams.setOfferKey("");
        payuConfig.setEnvironment(PayuConstants.MOBILE_STAGING_ENV);

        makeHashRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/payments/getHash", JsonProvider.getStandardRequestJson(CheckoutPaymentActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        payuHashes.setPaymentHash(response.getJSONObject("data").getString("hash"));
                        paymentParams.setHash(payuHashes.getPaymentHash());
                        Log.i("Payments", "Key: "+paymentParams.getKey());
                        Log.i("Payments", "Amount: "+paymentParams.getAmount());
                        Log.i("Payments", "Product Info: "+paymentParams.getProductInfo());
                        Log.i("Payments", "Firstname: "+paymentParams.getFirstName());
                        Log.i("Payments", "Phone: "+paymentParams.getPhone());
                        Log.i("Payments", "Email: "+paymentParams.getEmail());
                        Log.i("Payments", "TxnId: "+paymentParams.getTxnId());
                        Log.i("Payments", "Udf1: "+paymentParams.getUdf1());
                        Log.i("Payments", "Udf2: "+paymentParams.getUdf2());
                        Log.i("Payments", "Udf3: "+paymentParams.getUdf3());
                        Log.i("Payments", "Udf4: "+paymentParams.getUdf4());
                        Log.i("Payments", "Udf5: "+paymentParams.getUdf5());
                        Log.i("Payments", "Hash: "+payuHashes.getPaymentHash());
                        MerchantWebService merchantWebService = new MerchantWebService();
                        merchantWebService.setKey(paymentParams.getKey());
                        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
                        merchantWebService.setVar1(paymentParams.getUserCredentials());
                        merchantWebService.setHash(payuHashes.getPaymentRelatedDetailsForMobileSdkHash());
                        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
                        if (postData.getCode() == PayuErrors.NO_ERROR) {
                            payuConfig.setData(postData.getResult());
                            GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(CheckoutPaymentActivity.this);
                            paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
                            Log.i("Payments", "Making paymentDetails Request");
                        } else {
                            Animations.fadeOut(loadingLayout,500);
                            Animations.fadeIn(viewPager, 500);
                            Toast.makeText(CheckoutPaymentActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Animations.fadeOut(loadingLayout,500);
                        Animations.fadeIn(viewPager,500);
                        Alerts.requestUnauthorisedAlert(CheckoutPaymentActivity.this);
                        Log.e("Success False",response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOut(loadingLayout, 500);
                Animations.fadeIn(viewPager, 500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(loadingLayout, 500);
                        Animations.fadeOut(viewPager, 500);
                        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makeHashRequest);
                    }
                };
                if (error instanceof TimeoutError)
                    Alerts.timeoutErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else if (error instanceof NoConnectionError)
                    Alerts.internetConnectionErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(CheckoutPaymentActivity.this);
                Log.e("Json Request Failed", error.toString());
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(viewPager, 500);
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makeHashRequest);
    }

    private JSONObject getPaymentJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutPaymentActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("payment_method",paymentMethod);
            dataJson.put("password",password);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeCodPaymentRequest() {
        makePurchaseRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/payments/purchaseByCod", getPaymentJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        String orderId = dataJson.getString("order_id");
                        intent = new Intent(CheckoutPaymentActivity.this,OrderDescriptionActivity.class);
                        intent.putExtra("order_id",orderId);
                        intent.putExtra("cart",true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Cart.getInstance().removeAllOrders();
                        startActivity(intent);
                        finish();
                    } else {
                        Animations.fadeOut(connectingLayout,500);
                        Animations.fadeIn(viewPager,500);
                        snackbar = Snackbar.make(viewPager, "Wrong Password", Snackbar.LENGTH_LONG);
                        snackbar.setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOut(connectingLayout,500);
                Animations.fadeIn(viewPager,500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(connectingLayout,500);
                        Animations.fadeOut(viewPager, 500);
                        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makePurchaseRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(CheckoutPaymentActivity.this);
                Log.e("Json Request Failed", error.toString());
            }
        });

        Animations.fadeIn(connectingLayout, 500);
        Animations.fadeOut(viewPager, 500);
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makePurchaseRequest);
    }
    private boolean isEverythingValid() {
        return !(paymentMethod.length()<1);
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        Log.i("Payments", payuResponse.getResponseStatus().getCode()+", "+payuResponse.getResponseStatus().getStatus());
        Log.i("Payments", "Result: " + payuResponse.getResponseStatus().getResult());
        this.payuResponse = payuResponse;
        Animations.fadeOut(loadingLayout,500);
        Animations.fadeIn(viewPager, 500);
    }

}
