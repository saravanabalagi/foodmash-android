package in.foodmash.app.volley;

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

/**
 * Created by Zeke on Jan 31, 2016.
 */
public class VolleyProgressFragment extends Fragment {

    @Bind(R.id.loading_text) TextView loadingText;
    @Bind(R.id.loading_text_description) TextView loadingTextDescription;

    private String loadingTextString;
    private String loadingTextDescriptionString;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volley_progress, container, false);
        ButterKnife.bind(this, rootView);

        if(loadingTextString!=null && loadingTextDescriptionString!=null) {
            loadingText.setText(loadingTextString);
            loadingTextDescription.setText(loadingTextDescriptionString);
        }
        return rootView;
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
