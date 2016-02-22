package in.foodmash.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Info;

/**
 * Created by Zeke on Feb 22, 2016.
 */
public class UpdateAppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);

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

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(Info.isKeepMeLoggedInSet(UpdateAppActivity.this) && Info.isLoggedIn(UpdateAppActivity.this)) {
                    intent = new Intent(UpdateAppActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if(!Info.isKeepMeLoggedInSet(UpdateAppActivity.this) && Info.isLoggedIn(UpdateAppActivity.this)) {
                    Actions.logout(UpdateAppActivity.this);
                } else {
                    intent = new Intent(UpdateAppActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


    }
}
