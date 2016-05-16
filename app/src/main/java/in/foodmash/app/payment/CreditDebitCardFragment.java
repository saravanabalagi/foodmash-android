package in.foodmash.app.payment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
 * Created by Zeke on Oct 28 2015.
 */
public class CreditDebitCardFragment extends Fragment {

    private boolean created = false;
    private EditText cardNameEditText;
    private EditText cardNumberEditText;
    private EditText cardCvvEditText;
    private EditText cardExpiryMonthEditText;
    private EditText cardExpiryYearEditText;
    private ImageView cardType;
    private CheckBox saveCardCheckBox;

    private String cardName;
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear;

    private PayuHashes payuHashes;
    private PaymentParams paymentParams;
    private PostData postData;
    private PayuConfig payuConfig;

    private PayuUtils payuUtils;

    public void doPayment() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_credit_debit_card, container, false);

        created = true;
        cardNameEditText = (EditText) rootView.findViewById(R.id.edit_text_name_on_card);
        cardNumberEditText = (EditText) rootView.findViewById(R.id.edit_text_card_number);
        cardCvvEditText = (EditText) rootView.findViewById(R.id.edit_text_card_cvv);
        cardExpiryMonthEditText = (EditText) rootView.findViewById(R.id.edit_text_expiry_month);
        cardExpiryYearEditText = (EditText) rootView.findViewById(R.id.edit_text_expiry_year);
        saveCardCheckBox = (CheckBox) rootView.findViewById(R.id.check_box_save_card);
        cardType = (ImageView) rootView.findViewById(R.id.card_type);

        cardNumberEditText.setText("5123456789012346");
        cardNameEditText.setText("Test Card");
        cardCvvEditText.setText("123");
        cardExpiryMonthEditText.setText("05");
        cardExpiryYearEditText.setText("2017");

        // lets get payment default params and hashes
        payuHashes = ((CheckoutPaymentActivity) getActivity()).getPayuHashes();
        paymentParams = ((CheckoutPaymentActivity) getActivity()).getPaymentParams();
        payuConfig = ((CheckoutPaymentActivity) getActivity()).getPayuConfig();

        // lets not show the save card check box if user credentials is not found!
        if(null == paymentParams.getUserCredentials()) saveCardCheckBox.setVisibility(View.GONE);
        else saveCardCheckBox.setVisibility(View.GONE); //for time being
        //else saveCardCheckBox.setVisibility(View.VISIBLE);
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
                        cardType.setImageDrawable(issuerDrawable);
                        if (issuer.contentEquals(PayuConstants.SMAE)){ // hide cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.GONE);
                            cardExpiryYearEditText.setVisibility(View.GONE);
                            cardCvvEditText.setVisibility(View.GONE);
                        } else{ //show cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.VISIBLE);
                            cardExpiryYearEditText.setVisibility(View.VISIBLE);
                            cardCvvEditText.setVisibility(View.VISIBLE);
                        }
                    }
                } else{ issuer = null; issuerDrawable = null; }
            }
        });

        return rootView;
    }

    private Drawable getIssuerDrawable(String issuer){
        switch (issuer) {
            case PayuConstants.VISA: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_visa);
            case PayuConstants.LASER: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_laser);
            case PayuConstants.DISCOVER: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_discover);
            case PayuConstants.MAES: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_maestro);
            case PayuConstants.MAST: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_master);
            case PayuConstants.AMEX: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_amex);
            case PayuConstants.DINR: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_diner);
            case PayuConstants.JCB: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_jcb);
            case PayuConstants.SMAE: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_maestro);
            case PayuConstants.RUPAY: return ContextCompat.getDrawable(getActivity(), R.drawable.png_payment_rupay);
        }
        return null;
    }
}
