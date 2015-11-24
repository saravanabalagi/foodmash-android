package in.foodmash.app.payment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;

import java.util.ArrayList;

import in.foodmash.app.CheckoutPaymentActivity;
import in.foodmash.app.R;

/**
 * Created by sarav on Oct 28 2015.
 */
public class NetbankingFragment extends Fragment {

    private boolean created = false;
    private String bankcode;
    private ArrayList<PaymentDetails> netBankingList;
    private PaymentParams paymentParams;
    private PayuHashes payuHashes;
    private PayuConfig payuConfig;

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible && created) {
            ((CheckoutPaymentActivity) getActivity()).getPayButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paymentParams.setHash(payuHashes.getPaymentHash());
                    paymentParams.setBankCode(bankcode);
                    PostData postData = new PaymentPostParams(paymentParams, PayuConstants.NB).getPaymentPostParams();
                    if (postData.getCode() == PayuErrors.NO_ERROR) {
                        payuConfig.setData(postData.getResult());
                        Intent intent = new Intent(getActivity(), PaymentsActivity.class);
                        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                        startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
                    } else Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_netbanking, container, false);
        created = true;
        class PayUNetBankingAdapter extends BaseAdapter {
            Context mContext;
            ArrayList<PaymentDetails> mNetBankingList;
            public PayUNetBankingAdapter(Context context, ArrayList<PaymentDetails> netBankingList) { mContext = context; mNetBankingList = netBankingList; }
            @Override public int getCount() { return mNetBankingList.size(); }
            @Override public Object getItem(int i) { if (null != mNetBankingList) return mNetBankingList.get(i); else return 0; }
            @Override public long getItemId(int i) { return 0; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                NetbankingViewHolder netbankingViewHolder = null;
                if (convertView == null) {
                    LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.netbanking_list_item, null);
                    netbankingViewHolder = new NetbankingViewHolder(convertView);
                    convertView.setTag(netbankingViewHolder);
                } else netbankingViewHolder = (NetbankingViewHolder) convertView.getTag();
                PaymentDetails paymentDetails = mNetBankingList.get(position);
                netbankingViewHolder.netbankingTextView.setText(paymentDetails.getBankName());
                return convertView;
            }

            class NetbankingViewHolder {
                TextView netbankingTextView;
                NetbankingViewHolder(View view) { netbankingTextView = (TextView) view.findViewById(R.id.text_view_netbanking); }
            }
        }

        Spinner spinnerNetbanking = (Spinner) rootView.findViewById(R.id.spinner_netbanking);
        netBankingList = ((CheckoutPaymentActivity) getActivity()).getPayuResponse().getNetBanks();
        PayUNetBankingAdapter payUNetBankingAdapter = new PayUNetBankingAdapter(getActivity(), netBankingList);
        spinnerNetbanking.setAdapter(payUNetBankingAdapter);
        spinnerNetbanking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                bankcode = netBankingList.get(index).getBankCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        payuHashes = ((CheckoutPaymentActivity) getActivity()).getPayuHashes();
        paymentParams = ((CheckoutPaymentActivity) getActivity()).getPaymentParams();
        payuConfig = ((CheckoutPaymentActivity) getActivity()).getPayuConfig();

        return rootView;
    }
}
