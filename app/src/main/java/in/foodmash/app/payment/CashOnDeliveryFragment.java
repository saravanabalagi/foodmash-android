package in.foodmash.app.payment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.foodmash.app.R;
import in.foodmash.app.commons.Info;
import in.foodmash.app.models.User;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Oct 28 2015.
 */
public class CashOnDeliveryFragment extends Fragment {
    TextView email;
    TextView phone;
    TextView mashCash;
    LinearLayout mashCashLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cash_on_delivery, container, false);

        email = (TextView) rootView.findViewById(R.id.email);
        phone = (TextView) rootView.findViewById(R.id.phone);
        mashCash = (TextView) rootView.findViewById(R.id.mash_cash_available);
        mashCashLayout = (LinearLayout) rootView.findViewById(R.id.mash_cash_layout);
        if(!Info.isMashCashEnabled(getActivity())) mashCashLayout.setVisibility(View.GONE);

        email.setText(Info.getEmail(getActivity()));
        phone.setText(Info.getPhone(getActivity()));
        mashCash.setText(NumberUtils.getCurrencyFormatWithoutDecimals(User.getInstance().getMashCash()));

        return rootView;
    }

}
