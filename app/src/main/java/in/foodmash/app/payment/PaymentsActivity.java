package in.foodmash.app.payment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.payu.custombrowser.PayUWebChromeClient;
import com.payu.custombrowser.PayUWebViewClient;
import com.payu.india.Extras.PayUSdkDetails;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.R;


public class PaymentsActivity extends AppCompatActivity {

    @Bind(R.id.parent) View parent;
    @Bind(R.id.trans_overlay) View transOverlay;
    @Bind(R.id.webview) WebView webView;

    Bundle bundle;
    String url;
    boolean cancelTransaction = false;
    PayuConfig payuConfig;
    private BroadcastReceiver mReceiver = null;
    private static Context context;
    private String UTF = "UTF-8";
    private  boolean viewPortWide = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */

        if(savedInstanceState!=null){ super.onCreate(null); finish(); }
        else super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payments);
        context = PaymentsActivity.this;
        ButterKnife.bind(this);

        //region Replace the whole code by the commented code if you are NOT using custombrowser
        // Replace the whole code by the commented code if you are NOT using custombrowser.

        /*bundle = getIntent().getExtras();
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        mWebView = (WebView) findViewById(R.id.webview);
        url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV?  PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.MOBILE_TEST_PAYMENT_URL ;

        byte[] encodedData = EncodingUtils.getBytes(payuConfig.getData(), "base64");
        mWebView.postUrl(url, encodedData);

        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {});
        mWebView.setWebViewClient(new WebViewClient() {});*/
        //endregion

        bundle = getIntent().getExtras();
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV?  PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.MOBILE_TEST_PAYMENT_URL ;
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }*/

        String[] list =  payuConfig.getData().split("&");
        String txnId = null;
        String merchantKey = null;
        for (String item : list) {
            String[] items = item.split("=");
            if(items.length >= 2) {
                String id = items[0];
                switch (id) {
                    case "txnid": txnId = items[1]; break;
                    case "key": merchantKey = items[1]; break;
                    case "pg": if (items[1].contentEquals("NB")) viewPortWide = true; break;
                }
            }
        }

        try {
            Class.forName("com.payu.custombrowser.Bank");
            final Bank bank = new Bank();
            Bundle args = new Bundle();
            args.putInt(Bank.WEBVIEW, R.id.webview);
            args.putInt(Bank.TRANS_LAYOUT, R.id.trans_overlay);
            args.putInt(Bank.MAIN_LAYOUT, R.id.r_layout);
            args.putBoolean(Bank.VIEWPORTWIDE, viewPortWide);

            args.putString(Bank.TXN_ID, txnId == null ? String.valueOf(System.currentTimeMillis()) : txnId);
            args.putString(Bank.MERCHANT_KEY, null != merchantKey ? merchantKey : "could not find");
            PayUSdkDetails payUSdkDetails = new PayUSdkDetails();
            args.putString(Bank.SDK_DETAILS, payUSdkDetails.getSdkVersionName());
            if(getIntent().getExtras().containsKey("showCustom")) {
                args.putBoolean(Bank.SHOW_CUSTOMROWSER, getIntent().getBooleanExtra("showCustom", false));
            } else args.putBoolean(Bank.SHOW_CUSTOMROWSER, true);
            bank.setArguments(args);
            parent.bringToFront();

            try { getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.cb_face_out).add(R.id.parent, bank).commit(); }
            catch(Exception e) { e.printStackTrace(); finish(); }
            webView.setWebChromeClient(new PayUWebChromeClient(bank));
            webView.setWebViewClient(new PayUWebViewClient(bank));
            webView.addJavascriptInterface(new JsInterface(), "PayU");
            webView.postUrl(url, payuConfig.getData().getBytes());

        } catch (ClassNotFoundException e) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setSupportMultipleWindows(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            // Setting view port for NB
            if(viewPortWide) webView.getSettings().setUseWideViewPort(viewPortWide);
            // Hiding the overlay
            transOverlay.setVisibility(View.GONE);

            webView.addJavascriptInterface(new JsInterface(), "PayU");
            webView.setWebChromeClient(new WebChromeClient() );
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.postUrl(url, payuConfig.getData().getBytes());
        }

        /*mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        // url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV?  PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.MOBILE_TEST_PAYMENT_URL ;
        mWebView.postUrl(url, EncodingUtils.getBytes(payuConfig.getData(), "base64"));*/
    }

    @Override
    public void onBackPressed(){
        if(cancelTransaction){
            cancelTransaction = false;
            Intent intent = new Intent();
            intent.putExtra("result", "Transaction canceled due to back pressed!");
            setResult(RESULT_CANCELED, intent);
            super.onBackPressed();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Do you really want to cancel the transaction ?");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelTransaction = true;
                dialog.dismiss();
                onBackPressed();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        // Log.v("#### PAYU", "PAYMENTSACTIVITY: ondestroy");
        /*Debug.stopMethodTracing();*/
    }

    public static class Bank extends com.payu.custombrowser.Bank {

        @Override
        public void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
            ((PaymentsActivity)context).mReceiver = broadcastReceiver;
            context.registerReceiver(broadcastReceiver, filter);
        }

        @Override
        public void unregisterBroadcast(BroadcastReceiver broadcastReceiver) {
            if(((PaymentsActivity)context).mReceiver != null){
                context.unregisterReceiver(((PaymentsActivity)context).mReceiver);
                ((PaymentsActivity)context).mReceiver = null;
            }
        }

        @Override
        public void onHelpUnavailable() {
            ((PaymentsActivity)context).parent.setVisibility(View.GONE);
            ((PaymentsActivity)context).transOverlay.setVisibility(View.GONE);
        }

        @Override
        public void onBankError() {
            ((PaymentsActivity)context).parent.setVisibility(View.GONE);
            ((PaymentsActivity)context).transOverlay.setVisibility(View.GONE);
        }

        @Override
        public void onHelpAvailable() {
            ((PaymentsActivity)context).parent.setVisibility(View.VISIBLE);
        }
    }

    private class JsInterface {

        @JavascriptInterface
        public void onSuccess(final String result, final String orderId) {
            Log.i("Payments", "On Success triggered");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra("result", result);
                    intent.putExtra("order_id", orderId);
                    setResult(RESULT_OK, intent);
                    finish();
                }
//                }
            });
        }

        @JavascriptInterface
        public void onFailure(final String result) {
            Log.i("Payments", "On failure triggered");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra("result", result);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            });
        }
    }
}
