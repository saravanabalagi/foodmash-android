package in.foodmash.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

    protected void setTitle(Toolbar toolbar, String textWhite, String textRed) {
        ((TextView) toolbar.findViewById(R.id.logo_text_white)).setText(textWhite);
        ((TextView) toolbar.findViewById(R.id.logo_text_red)).setText(textRed);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException (Thread thread, Throwable e) {
                Intent intent = new Intent(FoodmashActivity.this, ErrorReportActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putSerializable("error", e);
                intent.putExtras(bundle);
                FoodmashActivity.this.startActivity(intent);
                System.exit(0);
            }
        });
    }
}
