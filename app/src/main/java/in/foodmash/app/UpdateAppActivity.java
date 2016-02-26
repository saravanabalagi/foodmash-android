package in.foodmash.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Zeke on Feb 22, 2016.
 */
public class UpdateAppActivity extends AppCompatActivity {

    boolean force;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);
        force = getIntent().getBooleanExtra("force", false);

        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("market://details?id="+getPackageName()));
                startActivity(viewIntent);
                finish();
            }
        });

        findViewById(R.id.weblink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName()));
                startActivity(viewIntent);
                finish();
            }
        });

        if(force) findViewById(R.id.back).setVisibility(View.GONE);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateAppActivity.this, SplashActivity.class);
                intent.putExtra("skip_update", true);
                startActivity(intent);
                finish();
            }
        });


    }
}
