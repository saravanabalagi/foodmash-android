package in.foodmash.app;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Zeke on Mar 20, 2016.
 */
public class FoodmashActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void setTitle(Toolbar toolbar, String firstHalf, String secondHalf) {
        ((TextView) toolbar.findViewById(R.id.logo_text_white)).setText(firstHalf);
        ((TextView) toolbar.findViewById(R.id.logo_text_red)).setText(secondHalf);
    }

}
