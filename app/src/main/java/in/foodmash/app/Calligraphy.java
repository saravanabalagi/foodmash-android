package in.foodmash.app;

import android.app.Application;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Zeke on Dec 26, 2015.
 */
public class Calligraphy extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/myriadpro.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
