package in.foodmash.app.commons;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.ErrorReportActivity;
import in.foodmash.app.R;
import in.foodmash.app.SplashActivity;

/**
 * Created by Zeke on Jan 31, 2016.
 */
public class VolleyFailureFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.settings_mobile_network) LinearLayout mobileNetworkSettings;
    @Bind(R.id.settings_wifi) LinearLayout wifiSettings;
    @Bind(R.id.error_description) LinearLayout errorDescription;
    @Bind(R.id.error_wrapper) LinearLayout errorWrapper;
    @Bind(R.id.internet_wrapper) LinearLayout internetWrapper;
    @Bind(R.id.logout_wrapper) LinearLayout logoutWrapper;
    @Bind(R.id.retry) LinearLayout retry;
    @Bind(R.id.logout) LinearLayout logout;
    @Bind(R.id.back) LinearLayout back;
    @Bind(R.id.exit) LinearLayout exit;

    @Bind(R.id.error) TextView error;
    @Bind(R.id.message) TextView description;
    @Bind(R.id.image) ImageView image;

    Intent intent;
    VolleyError volleyError;
    String methodName;

    public static VolleyFailureFragment newInstance(VolleyError volleyError, String methodName) {
        VolleyFailureFragment volleyFailureFragment = new VolleyFailureFragment();
        volleyFailureFragment.volleyError = volleyError;
        volleyFailureFragment.methodName = methodName;
        return volleyFailureFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volley_failure, container, false);
        ButterKnife.bind(this, rootView);

        if(volleyError==null) { Log.e("Error Reporting Module","Should access VolleyFailureFragmentClass using newInstance method"); return null; }
        mobileNetworkSettings.setOnClickListener(this);
        wifiSettings.setOnClickListener(this);
        errorDescription.setOnClickListener(this);
        retry.setOnClickListener(this);
        exit.setOnClickListener(this);
        back.setOnClickListener(this);
        logout.setOnClickListener(this);

        if( volleyError instanceof NetworkError) setErrorDescription("Connection Failed","Internet is nowhere to be found.", ContextCompat.getDrawable(getActivity(),R.drawable.png_no_wifi));
        else if( volleyError instanceof ServerError) setErrorDescription("Server Error","Clouds did not respond properly!", ContextCompat.getDrawable(getActivity(),R.drawable.png_two_minions));
        else if( volleyError instanceof AuthFailureError) setErrorDescription("Authentication Failed","Log out and try again.", ContextCompat.getDrawable(getActivity(),R.drawable.png_broken_key));
        else if( volleyError instanceof ParseError) setErrorDescription("Parse Error","App doesn't understand 'Minionese'", ContextCompat.getDrawable(getActivity(),R.drawable.png_minionese));
        else if( volleyError instanceof TimeoutError) setErrorDescription("Connection Timed Out","Sometimes Internet gets lazy and takes a nap.", ContextCompat.getDrawable(getActivity(),R.drawable.png_broken_clock));
        else setErrorDescription("Something's not right","Error displaying the error.", ContextCompat.getDrawable(getActivity(),R.drawable.png_minion));

        if(volleyError instanceof NetworkError || volleyError instanceof TimeoutError) { logoutWrapper.setVisibility(View.GONE); errorWrapper.setVisibility(View.GONE);  }
        else if(volleyError instanceof AuthFailureError) { internetWrapper.setVisibility(View.GONE); errorWrapper.setVisibility(View.GONE);  }
        else { logoutWrapper.setVisibility(View.GONE); internetWrapper.setVisibility(View.GONE);  }

        return rootView;
    }

    public void setErrorDescription(String error, String description, Drawable drawableResource) {
        this.error.setText(error);
        this.description.setText(description);
        this.image.setImageDrawable(drawableResource);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_mobile_network: startActivityForResult(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),0); break;
            case R.id.settings_wifi: startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),0); break;
            case R.id.back: getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit(); break;
            case R.id.logout: Actions.logout(getActivity()); startActivity(new Intent(getActivity(), SplashActivity.class)); getActivity().finish(); break;
            case R.id.exit: getActivity().finish(); System.exit(0); break;
            case R.id.retry: retry(); break;
            case R.id.error_description:
                intent = new Intent(getActivity(), ErrorReportActivity.class);
                Bundle extras = new Bundle();
                extras.putSerializable("error", volleyError);
                intent.putExtras(extras);
                startActivity(intent); break;
        }
    }

    private void retry() {
        try  { Class.forName(getActivity().getClass().getName()).getDeclaredMethod(methodName).invoke(getActivity()); }
        catch (Exception e) { Actions.handleIgnorableException(getActivity(),e); }
    }

}
