package in.foodmash.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;

/**
 * Created by Zeke on Feb 22, 2016.
 */
public class ErrorDescriptionActivity extends AppCompatActivity {

    @Bind(R.id.send) FloatingActionButton send;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.title) TextView titleTextView;
    @Bind(R.id.message) TextView messageTextView;
    @Bind(R.id.time) TextView timeTextView;
    @Bind(R.id.stacktrace) TextView stacktraceTextView;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;

    @Bind(R.id.release) TextView releaseTextView;
    @Bind(R.id.sdk_version) TextView sdkVersionTextView;
    @Bind(R.id.manufacturer) TextView manufacturerTextView;
    @Bind(R.id.model) TextView modelTextView;
    @Bind(R.id.orientation) TextView orientationTextView;
    @Bind(R.id.size_category) TextView sizeCategoryTextView;
    @Bind(R.id.height) TextView heightTextView;
    @Bind(R.id.width) TextView widthTextView;

    Throwable e;
    String timeNow;
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
        setContentView(R.layout.activity_error_description);
        ButterKnife.bind(this);

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

        Bundle extras = getIntent().getExtras();
        e = (Throwable) extras.getSerializable("error");

        if(e!=null) {
            titleTextView.setText(e.getClass().getName());
            messageTextView.setText(e.getMessage());
            stacktraceTextView.setText(Arrays.toString(e.getStackTrace()));
        }

        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US);
        timeNow = dateFormat.format(calendar.getTime());
        timeTextView.setText(timeNow);
        releaseTextView.setText(release);
        sdkVersionTextView.setText(String.valueOf(sdkVersion));
        manufacturerTextView.setText(manufacturer);
        modelTextView.setText(model);
        orientationTextView.setText(orientation);
        sizeCategoryTextView.setText(sizeCategory);
        heightTextView.setText(String.valueOf(height));
        widthTextView.setText(String.valueOf(width));

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

    }

    private void sendEmail() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"bugs@foodmash.in"});
            i.putExtra(Intent.EXTRA_SUBJECT, "App Error | Android "+ sdkVersion);
            i.putExtra(Intent.EXTRA_TEXT   , new ObjectMapper().writeValueAsString(getMakeErrorRequestJson()));
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (Exception e) { Snackbar.make(mainLayout, "There are no email clients installed.", Snackbar.LENGTH_LONG).show(); }
    }

    private JSONObject getMakeErrorRequestJson() {
        JSONObject requestJson = (Info.isLoggedIn(this)) ? JsonProvider.getStandardRequestJson(this) : JsonProvider.getAnonymousRequestJson(this);
        try {
            HashMap<String, String> dataHashMap = new HashMap<>();
            dataHashMap.put("class",e.getClass().getName());
            dataHashMap.put("message",e.getMessage());
            dataHashMap.put("stacktrace",Arrays.toString(e.getStackTrace()));
            dataHashMap.put("time",timeNow);
            JSONObject dataJson = new JSONObject(dataHashMap);
            HashMap<String, String> hostHashMap = new HashMap<>();
            hostHashMap.put("release", release);
            hostHashMap.put("sdkVersion", String.valueOf(sdkVersion));
            hostHashMap.put("manufacturer", manufacturer);
            hostHashMap.put("model", model);
            hostHashMap.put("orientation", orientation);
            hostHashMap.put("sizeCategory", sizeCategory);
            hostHashMap.put("height", String.valueOf(height));
            hostHashMap.put("width", String.valueOf(width));
            JSONObject hostJson = new JSONObject(hostHashMap);
            dataJson.put("host", hostJson);
            requestJson.put("data", dataJson);
        } catch (Exception e) { e.printStackTrace(); }
        return requestJson;
    }

    public void makeErrorRequest() {
        JsonObjectRequest errorRequestJson = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/error/addError", getMakeErrorRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeErrorRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(this).addToRequestQueue(errorRequestJson);
    }
}
