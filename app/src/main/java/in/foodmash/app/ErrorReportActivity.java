package in.foodmash.app;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

/**
 * Created by Zeke on Feb 22, 2016.
 */
public class ErrorReportActivity extends AppCompatActivity {

    String release;
    int sdkVersion;
    String manufacturer;
    String model;
    String orientation;
    String sizeCategory;
    int height;
    int width;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);

        release = Build.VERSION.RELEASE;
        sdkVersion = Build.VERSION.SDK_INT;
        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE: orientation = "landscape"; break;
            case Configuration.ORIENTATION_PORTRAIT: orientation = "portrait"; break;
            case Configuration.ORIENTATION_UNDEFINED: orientation = "undefined"; break;
        }

        switch ((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK)) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE: sizeCategory = "xlarge"; break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE: sizeCategory = "large"; break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL: sizeCategory = "normal"; break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL: sizeCategory = "small"; break;
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED: sizeCategory = "undefined"; break;
        }


    }
}
