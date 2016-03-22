package in.foodmash.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Swift;

/**
 * Created by Zeke on Mar 22, 2016.
 */
public class ShowMessageActivity extends FoodmashActivity {

    @Bind(R.id.title) TextView title;
    @Bind(R.id.message) TextView message;
    @Bind(R.id.image) NetworkImageView image;

    @Bind(R.id.exit) LinearLayout exit;
    @Bind(R.id.okay) LinearLayout okay;

    String titleString;
    String messageString;
    String imageString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);
        ButterKnife.bind(this);

        if(getIntent().getBooleanExtra("blocking",false)) { exit.setVisibility(View.VISIBLE); okay.setVisibility(View.GONE); }
        else { exit.setVisibility(View.GONE); okay.setVisibility(View.VISIBLE); }

        titleString = getIntent().getStringExtra("title");
        messageString = getIntent().getStringExtra("message");
        imageString = getIntent().getStringExtra("image");

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowMessageActivity.this,SplashActivity.class);
                intent.putExtra("skip_maintenance", true);
                startActivity(intent);
                finish();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        title.setText(titleString);
        message.setText(messageString);
        image.setImageUrl(imageString,Swift.getInstance(this).getImageLoader());
    }
}