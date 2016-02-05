package in.foodmash.app.commons;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.toolbox.JsonObjectRequest;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.R;

/**
 * Created by Zeke on Jan 31, 2016.
 */
public class VolleyFailureFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.settings_mobile_network) LinearLayout mobileNetworkSettings;
    @Bind(R.id.settings_wifi) LinearLayout wifiSettings;
    @Bind(R.id.parent_layout) LinearLayout parentLayout;
    @Bind(R.id.retry) LinearLayout retry;

    private JsonObjectRequest jsonObjectRequest;


    private boolean setDestroyOnRetry = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volley_failure, container, false);

        ButterKnife.bind(this, rootView);
        mobileNetworkSettings.setOnClickListener(this);
        wifiSettings.setOnClickListener(this);
        parentLayout.setOnClickListener(this);
        retry.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_mobile_network: startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)); break;
            case R.id.settings_wifi: startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); break;
            case R.id.parent_layout: retry(); break;
            case R.id.retry: retry(); break;
        }
    }

    public void setSetDestroyOnRetry(boolean setDestroyOnRetry) { this.setDestroyOnRetry = setDestroyOnRetry; }
    public void setJsonObjectRequest(JsonObjectRequest jsonObjectRequest) { this.jsonObjectRequest = jsonObjectRequest; }
    private void retry() {
        System.out.println("Retrying...");
        Swift.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
        if(!setDestroyOnRetry) getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,new VolleyProgressFragment()).commit();
        else getActivity().getSupportFragmentManager().beginTransaction()
                .remove(this).commit();
    }
}
