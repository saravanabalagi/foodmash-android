package in.foodmash.app.commons;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.R;

/**
 * Created by Zeke on Jan 31, 2016.
 */
public class VolleyProgressFragment extends Fragment {

    @Bind(R.id.loading_text) TextView loadingText;
    @Bind(R.id.loading_text_description) TextView loadingTextDescription;

    @Bind(R.id.signal_25) ImageView signal25;
    @Bind(R.id.signal_50) ImageView signal50;
    @Bind(R.id.signal_75) ImageView signal75;
    @Bind(R.id.signal_100) ImageView signal100;

    private String loadingTextString;
    private String loadingTextDescriptionString;
    private Handler handler = new Handler();
    private int i=0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volley_progress, container, false);
        ButterKnife.bind(this, rootView);

        if(loadingTextString!=null && loadingTextDescriptionString!=null) {
            loadingText.setText(loadingTextString);
            loadingTextDescription.setText(loadingTextDescriptionString);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateSignal();
                handler.postDelayed(this, 250);
            }
        }, 250);
        return rootView;
    }

    private void animateSignal() {
        switch (i) {
            case 0:
                signal50.setVisibility(View.GONE);
                signal75.setVisibility(View.GONE);
                signal100.setVisibility(View.GONE);
                signal25.setVisibility(View.VISIBLE); i=1; break;
            case 1: signal50.setVisibility(View.VISIBLE); i=2; break;
            case 2: signal75.setVisibility(View.VISIBLE); i=3; break;
            case 3: signal100.setVisibility(View.VISIBLE); i=0; break;
        }
    }

    public void setLoadingText(String loadingText, String loadingTextDescription) {
        loadingTextString = loadingText;
        loadingTextDescriptionString = loadingTextDescription;
        try {
            this.loadingText.setText(loadingText);
            this.loadingTextDescription.setText(loadingTextDescription);
        } catch (NullPointerException e) {}
    }
}
