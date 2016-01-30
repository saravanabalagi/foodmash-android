package in.foodmash.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.Cart;
import in.foodmash.app.payment.CashOnDeliveryFragment;
import in.foodmash.app.payment.CreditDebitCardFragment;
import in.foodmash.app.payment.NetbankingFragment;

/**
 * Created by Zeke on Jul 19 2015.
 */
public class CheckoutPaymentActivity extends AppCompatActivity implements View.OnClickListener, PaymentRelatedDetailsListener {

    @Bind(R.id.pay) FloatingActionButton pay;
    @Bind(R.id.total) TextView total;
    @Bind(R.id.connecting_layout) LinearLayout connectingLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.main_layout) ViewPager mainLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    private Intent intent;
    private String payableAmount;
    private String paymentMethod;

    private JsonObjectRequest makePurchaseRequest;
    private JsonObjectRequest makeHashRequest;
    private PaymentParams paymentParams = new PaymentParams();
    private PayuConfig payuConfig = new PayuConfig();
    private PayuHashes payuHashes = new PayuHashes();
    private PayuResponse payuResponse;

    public PaymentParams getPaymentParams() { return paymentParams; }
    public PayuConfig getPayuConfig() { return payuConfig; }
    public PayuHashes getPayuHashes() { return payuHashes; }
    public View getPayButton() { return pay; }
    public PayuResponse getPayuResponse() { return payuResponse; }

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

        if(getIntent().getDoubleExtra("payable_amount", 0)!=0) payableAmount = String.valueOf(getIntent().getDoubleExtra("payable_amount",0));
        else Alerts.commonErrorAlert(CheckoutPaymentActivity.this, "Transaction not authorized", "We found something suspicious about your current order. Try again!", "Back", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { finish(); } }, false);

        setPayDefaultOnClickListener();
        total.setText(payableAmount);

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

        //Animations.fadeIn(loadingLayout, 500);
        //Animations.fadeOut(mainLayout, 500);
        //fillPaymentParamsAndPayuEnv();
        PaymentPagerAdapter paymentPagerAdapter = new PaymentPagerAdapter(getSupportFragmentManager());
        mainLayout.setAdapter(paymentPagerAdapter);

    }

    public void fillPaymentParamsAndPayuEnv() {
        paymentParams.setKey("0MQaQP");
        paymentParams.setAmount(payableAmount);
        paymentParams.setProductInfo("Foodmash Order");
        paymentParams.setFirstName("Somename");
        paymentParams.setEmail(Info.getEmail(CheckoutPaymentActivity.this));
        paymentParams.setTxnId("OD" + Calendar.getInstance().getTimeInMillis());
        paymentParams.setSurl(getString(R.string.api_root_path)+"/payment/success");
        paymentParams.setFurl(getString(R.string.api_root_path)+"/payment/failure");
        paymentParams.setUdf1("");
        paymentParams.setUdf2("");
        paymentParams.setUdf3("");
        paymentParams.setUdf4("");
        paymentParams.setUdf5("");
        paymentParams.setUserCredentials("0MQaQP:payutest@payu.in");
        paymentParams.setOfferKey("");
        payuConfig.setEnvironment(PayuConstants.PRODUCTION_ENV);
        generateHashFromServer(paymentParams);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pay: paymentMethod=getResources().getString(R.string.payment_cod); if(isEverythingValid()) makePaymentRequest(); break;
        }
    }

    public void setPayDefaultOnClickListener() {
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod=getResources().getString(R.string.payment_cod); if(isEverythingValid()) makePaymentRequest();
            }
        });
    }

    private JSONObject getPaymentJson() {
        JSONObject requestJson = JsonProvider.getStandardRequestJson(CheckoutPaymentActivity.this);
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("payment_method",paymentMethod);
            requestJson.put("data",dataJson);
        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makePaymentRequest() {
        makePurchaseRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/carts/purchase", getPaymentJson(), new Response.Listener<JSONObject>() {
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
                    } else if(!response.getBoolean("success")){
                        Alerts.commonErrorAlert(CheckoutPaymentActivity.this, "Payment Failed", "Paymment you made was not successful. Please try again!", "Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        },false);
                    } else {
                        Animations.fadeOut(connectingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        Alerts.requestUnauthorisedAlert(CheckoutPaymentActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOut(connectingLayout,500);
                Animations.fadeIn(mainLayout,500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(connectingLayout,500);
                        Animations.fadeOut(mainLayout, 500);
                        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makePurchaseRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(CheckoutPaymentActivity.this);
                System.out.println("Response Error: " + error);
            }
        });

        Animations.fadeIn(connectingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makePurchaseRequest);
    }

    private void makeHashRequest() {
        makeHashRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/payments/getHash", JsonProvider.getStandardRequestJson(CheckoutPaymentActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        payuHashes.setPaymentHash(response.getJSONObject("data").getString("hash"));
                    } else {
                        Animations.fadeOut(connectingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        Alerts.requestUnauthorisedAlert(CheckoutPaymentActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Animations.fadeOut(connectingLayout, 500);
                Animations.fadeIn(mainLayout, 500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(connectingLayout, 500);
                        Animations.fadeOut(mainLayout, 500);
                        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makeHashRequest);
                    }
                };
                if (error instanceof TimeoutError)
                    Alerts.timeoutErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else if (error instanceof NoConnectionError)
                    Alerts.internetConnectionErrorAlert(CheckoutPaymentActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(CheckoutPaymentActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(connectingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(CheckoutPaymentActivity.this).addToRequestQueue(makeHashRequest);
    }

    private boolean isEverythingValid() {
        return !(paymentMethod.length()<1);
    }

    public void generateHashFromServer(PaymentParams mPaymentParams){
        // lets create the post params
        StringBuffer postParamsBuffer = new StringBuffer();
        postParamsBuffer.append(concatParams(PayuConstants.KEY, mPaymentParams.getKey()));
        postParamsBuffer.append(concatParams(PayuConstants.AMOUNT, mPaymentParams.getAmount()));
        postParamsBuffer.append(concatParams(PayuConstants.TXNID, mPaymentParams.getTxnId()));
        postParamsBuffer.append(concatParams(PayuConstants.EMAIL, null == mPaymentParams.getEmail() ? "" : mPaymentParams.getEmail()));
        postParamsBuffer.append(concatParams(PayuConstants.PRODUCT_INFO, mPaymentParams.getProductInfo()));
        postParamsBuffer.append(concatParams(PayuConstants.FIRST_NAME, null == mPaymentParams.getFirstName() ? "" : mPaymentParams.getFirstName()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF1, mPaymentParams.getUdf1() == null ? "" : mPaymentParams.getUdf1()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF2, mPaymentParams.getUdf2() == null ? "" : mPaymentParams.getUdf2()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF3, mPaymentParams.getUdf3() == null ? "" : mPaymentParams.getUdf3()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF4, mPaymentParams.getUdf4() == null ? "" : mPaymentParams.getUdf4()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF5, mPaymentParams.getUdf5() == null ? "" : mPaymentParams.getUdf5()));
        postParamsBuffer.append(concatParams(PayuConstants.USER_CREDENTIALS, mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials()));

        if(null != mPaymentParams.getOfferKey())  postParamsBuffer.append(concatParams(PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey()));
        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();
        // make api call
        GetHashesFromServerTask getHashesFromServerTask = new GetHashesFromServerTask();
        getHashesFromServerTask.execute(postParams);
    }


    protected String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }
    class GetHashesFromServerTask extends AsyncTask<String, String, PayuHashes> {

        @Override
        protected PayuHashes doInBackground(String ... postParams) {
            payuHashes = new PayuHashes();
            try {

                //makeHashRequest();
                URL url = new URL("https://payu.herokuapp.com/get_hash");
                String postParam = postParams[0];

                byte[] postParamsByte = postParam.getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postParamsByte);

                InputStream responseInputStream = conn.getInputStream();
                StringBuffer responseStringBuffer = new StringBuffer();
                byte[] byteContainer = new byte[1024];
                for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                    responseStringBuffer.append(new String(byteContainer, 0, i));
                }

                JSONObject response = new JSONObject(responseStringBuffer.toString());

                Iterator<String> payuHashIterator = response.keys();
                while(payuHashIterator.hasNext()){
                    String key = payuHashIterator.next();
                    switch (key){
                        case "payment_hash": payuHashes.setPaymentHash(response.getString(key)); break;
                        case "get_merchant_ibibo_codes_hash": payuHashes.setMerchantIbiboCodesHash(response.getString(key)); break;
                        case "vas_for_mobile_sdk_hash": payuHashes.setVasForMobileSdkHash(response.getString(key)); break;
                        case "payment_related_details_for_mobile_sdk_hash": payuHashes.setPaymentRelatedDetailsForMobileSdkHash(response.getString(key)); break;
                        case "delete_user_card_hash": payuHashes.setDeleteCardHash(response.getString(key)); break;
                        case "get_user_cards_hash": payuHashes.setStoredCardsHash(response.getString(key)); break;
                        case "edit_user_card_hash": payuHashes.setEditCardHash(response.getString(key)); break;
                        case "save_user_card_hash": payuHashes.setSaveCardHash(response.getString(key)); break;
                        case "check_offer_status_hash": payuHashes.setCheckOfferStatusHash(response.getString(key)); break;
                        case "check_isDomestic_hash": payuHashes.setCheckIsDomesticHash(response.getString(key)); break;
                        default: break;
                    }
                }

            } catch(Exception e) { e.printStackTrace(); }
            return payuHashes;
        }

        @Override
        protected void onPostExecute(PayuHashes payuHashes) {
            super.onPostExecute(payuHashes);
            MerchantWebService merchantWebService = new MerchantWebService();
            merchantWebService.setKey(paymentParams.getKey());
            merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
            merchantWebService.setVar1(paymentParams.getUserCredentials());
            merchantWebService.setHash(payuHashes.getPaymentRelatedDetailsForMobileSdkHash());

//            if(null == savedInstanceState){ // dont fetch the data if its been called from payment activity.
                PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
                if(postData.getCode() == PayuErrors.NO_ERROR){
                    payuConfig.setData(postData.getResult());
                    //findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                    GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(CheckoutPaymentActivity.this);
                    paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
                } else {
                    Toast.makeText(CheckoutPaymentActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    //findViewById(R.id.progress_bar).setVisibility(View.GONE);
                }
//            }

        }
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        this.payuResponse = payuResponse;
        Animations.fadeOut(loadingLayout,500);
        Animations.fadeIn(mainLayout, 500);
    }

}
