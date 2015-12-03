package in.foodmash.app.payment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.foodmash.app.CheckoutPaymentActivity;
import in.foodmash.app.R;

/**
 * Created by Zeke on Oct 28 2015.
 */
public class CashOnDeliveryFragment extends Fragment {

    boolean created = false;

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible && created) { ((CheckoutPaymentActivity) getActivity()).setPayDefaultOnClickListener(); }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cash_on_delivery, container, false);
        created = true;
        return rootView;
    }
}
