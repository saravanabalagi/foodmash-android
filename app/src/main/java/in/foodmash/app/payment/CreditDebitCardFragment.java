package in.foodmash.app.payment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.PaymentPostParams;

import in.foodmash.app.CheckoutPaymentActivity;
import in.foodmash.app.R;

/**
 * Created by sarav on Oct 28 2015.
 */
public class CreditDebitCardFragment extends Fragment {

    private Button payNowButton;
    private EditText cardNameEditText;
    private EditText cardNumberEditText;
    private EditText cardCvvEditText;
    private EditText cardExpiryMonthEditText;
    private EditText cardExpiryYearEditText;
    private Bundle bundle;
    private CheckBox saveCardCheckBox;

    private String cardName;
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear;

    private PayuHashes payuHashes;
    private PaymentParams paymentParams;
    private PostData postData;
    private Toolbar toolbar;

    private PayuConfig payuConfig;

    private PayuUtils payuUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_credit_debit_card, container, false);

        (payNowButton = (Button) rootView.findViewById(R.id.button_card_make_payment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do i have to store the card
                if (saveCardCheckBox.isChecked()) paymentParams.setStoreCard(1);
                else paymentParams.setStoreCard(0);
                // setup the hash
                paymentParams.setHash(payuHashes.getPaymentHash());

                // lets try to get the post params

                postData = null;
                // lets get the current card number;
                cardNumber = String.valueOf(cardNumberEditText.getText());
                cardName = cardNameEditText.getText().toString();
                expiryMonth = cardExpiryMonthEditText.getText().toString();
                expiryYear = cardExpiryYearEditText.getText().toString();
                cvv = cardCvvEditText.getText().toString();

                // lets not worry about ui validations.
                paymentParams.setCardNumber(cardNumber);
                paymentParams.setCardName(cardName);
                paymentParams.setNameOnCard(cardName);
                paymentParams.setExpiryMonth(expiryMonth);
                paymentParams.setExpiryYear(expiryYear);
                paymentParams.setCvv(cvv);
                postData = new PaymentPostParams(paymentParams, PayuConstants.CC).getPaymentPostParams();
                if (postData.getCode() == PayuErrors.NO_ERROR) {
                    // okay good to go.. lets make a transaction
                    // launch webview
                    payuConfig.setData(postData.getResult());
                    Intent intent = new Intent(getActivity(), PaymentsActivity.class);
                    intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                    startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
                } else Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
            }
        });

        cardNameEditText = (EditText) rootView.findViewById(R.id.edit_text_name_on_card);
        cardNumberEditText = (EditText) rootView.findViewById(R.id.edit_text_card_number);
        cardCvvEditText = (EditText) rootView.findViewById(R.id.edit_text_card_cvv);
        cardExpiryMonthEditText = (EditText) rootView.findViewById(R.id.edit_text_expiry_month);
        cardExpiryYearEditText = (EditText) rootView.findViewById(R.id.edit_text_expiry_year);
        saveCardCheckBox = (CheckBox) rootView.findViewById(R.id.check_box_save_card);

        // lets get payment default params and hashes
        payuHashes = ((CheckoutPaymentActivity) getActivity()).getPayuHashes();
        paymentParams = ((CheckoutPaymentActivity) getActivity()).getPaymentParams();
        payuConfig = ((CheckoutPaymentActivity) getActivity()).getPayuConfig();

        // lets not show the save card check box if user credentials is not found!
        if(null == paymentParams.getUserCredentials()) saveCardCheckBox.setVisibility(View.GONE);
        else saveCardCheckBox.setVisibility(View.VISIBLE);
        payuUtils = new PayuUtils();


        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            String issuer;
            Drawable issuerDrawable;
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 5){ // to confirm rupay card we need min 6 digit.
                    if(null == issuer) issuer = payuUtils.getIssuer(charSequence.toString());
                    if (issuer != null && issuer.length() > 1 && issuerDrawable == null){
                        issuerDrawable = getIssuerDrawable(issuer);
                        if(issuer.contentEquals(PayuConstants.SMAE)){ // hide cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.GONE);
                            cardExpiryYearEditText.setVisibility(View.GONE);
                            cardCvvEditText.setVisibility(View.GONE);
                        }else{ //show cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.VISIBLE);
                            cardExpiryYearEditText.setVisibility(View.VISIBLE);
                            cardCvvEditText.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    issuer = null;
                    issuerDrawable = null;
                }
                cardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, issuerDrawable, null);
            }
        });

        return rootView;
    }

    private Drawable getIssuerDrawable(String issuer){

        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            switch (issuer) {
                case PayuConstants.VISA: return getResources().getDrawable(R.drawable.visa);
                case PayuConstants.LASER: return getResources().getDrawable(R.drawable.laser);
                case PayuConstants.DISCOVER: return getResources().getDrawable(R.drawable.discover);
                case PayuConstants.MAES: return getResources().getDrawable(R.drawable.maestro);
                case PayuConstants.MAST: return getResources().getDrawable(R.drawable.master);
                case PayuConstants.AMEX: return getResources().getDrawable(R.drawable.amex);
                case PayuConstants.DINR: return getResources().getDrawable(R.drawable.diner);
                case PayuConstants.JCB: return getResources().getDrawable(R.drawable.jcb);
                case PayuConstants.SMAE: return getResources().getDrawable(R.drawable.maestro);
                case PayuConstants.RUPAY: return getResources().getDrawable(R.drawable.rupay);
            }
            return null;
        }else {

            switch (issuer) {
                case PayuConstants.VISA: return getResources().getDrawable(R.drawable.visa, null);
                case PayuConstants.LASER: return getResources().getDrawable(R.drawable.laser, null);
                case PayuConstants.DISCOVER: return getResources().getDrawable(R.drawable.discover, null);
                case PayuConstants.MAES: return getResources().getDrawable(R.drawable.maestro, null);
                case PayuConstants.MAST: return getResources().getDrawable(R.drawable.master, null);
                case PayuConstants.AMEX: return getResources().getDrawable(R.drawable.amex, null);
                case PayuConstants.DINR: return getResources().getDrawable(R.drawable.diner, null);
                case PayuConstants.JCB: return getResources().getDrawable(R.drawable.jcb, null);
                case PayuConstants.SMAE: return getResources().getDrawable(R.drawable.maestro, null);
                case PayuConstants.RUPAY: return getResources().getDrawable(R.drawable.rupay, null);
            }
            return null;
        }
    }
}
