package in.foodmash.app.payment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.R;
import in.foodmash.app.commons.Info;

/**
 * Created by Zeke on Oct 28 2015.
 */
public class CashOnDeliveryFragment extends Fragment {
    TextView email;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cash_on_delivery, container, false);

        email = (TextView) rootView.findViewById(R.id.email);
        email.setText(Info.getEmail(getActivity()));

        return rootView;
    }

}
