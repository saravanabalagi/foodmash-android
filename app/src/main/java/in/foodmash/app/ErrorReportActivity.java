package in.foodmash.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Info;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;

/**
 * Created by Zeke on Feb 22, 2016.
 */
public class ErrorReportActivity extends FoodmashActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.send) FloatingActionButton send;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.title) TextView titleTextView;
    @Bind(R.id.message) TextView messageTextView;
    @Bind(R.id.cause) TextView causeTextView;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.error_description_layout) LinearLayout errorDescriptionLayout;
    @Bind(R.id.view_error_description) TextView viewErrorDescription;

    Throwable e;
    String stackTrace;
    String causeTrace;
    String timeNow;
    String release;
    int sdkVersion;
    String manufacturer;
    String model;
    String orientation;
    String sizeCategory;
    int height;
    int width;

    boolean ignorable = false;
    boolean isShowingErrorDescription = false;

    @Override
    public void onBackPressed() {
        if(!ignorable) { startActivity(new Intent(this, SplashActivity.class)); finish(); }
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Mash","error");

        ignorable = getIntent().getBooleanExtra("ignorable", false);
        viewErrorDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowingErrorDescription) {
                    viewErrorDescription.setText("Show Error Description");
                    isShowingErrorDescription = false;
                    errorDescriptionLayout.setVisibility(View.GONE);
                } else {
                    viewErrorDescription.setText("Hide Error Description");
                    isShowingErrorDescription = true;
                    errorDescriptionLayout.setVisibility(View.VISIBLE);
                }
            }
        });

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

            stackTrace = "";
            stackTrace += "[ERROR] "+e.getMessage()+"\r\n";
            StackTraceElement stackTraceElements[] = e.getStackTrace();
            for (StackTraceElement stackTraceElement: stackTraceElements)
                stackTrace += stackTraceElement.toString() + "\r\n";
            stackTrace += "\r\n";
            Throwable cause = e.getCause();
            causeTrace = "";
            while (cause != null) {
                causeTrace += "[CAUSE] "+cause.getMessage() + "\r\n";
                stackTrace += "[CAUSE] "+cause.getMessage() + "\r\n";
                StackTraceElement stackTraceElementsForCause[] = cause.getStackTrace();
                for (StackTraceElement stackTraceElement: stackTraceElementsForCause)
                    stackTrace += stackTraceElement.toString() + "\r\n";
                stackTrace += "\r\n";
                cause = cause.getCause();
            }

            titleTextView.setText(e.getClass().getName());
            messageTextView.setText(e.getMessage());
            causeTextView.setText(causeTrace);


        }

        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US);
        timeNow = dateFormat.format(calendar.getTime());

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        if(!ignorable) new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mainLayout, "App has encountered a fatal error", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Restart", new View.OnClickListener() { @Override public void onClick(View v) { onBackPressed(); } })
                        .show();
            }
        }, 5000);
        else new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mainLayout, "Something went wrong", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Go Back", new View.OnClickListener() { @Override public void onClick(View v) { onBackPressed(); } })
                        .show();
            }
        }, 5000);

    }

    private void sendEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"bugs@foodmash.in"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "App Error | Android API "+ sdkVersion);
            intent.putExtra(Intent.EXTRA_TEXT   , getMakeErrorRequestJson().toString(4));
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (JSONException e) { Snackbar.make(mainLayout, "Json Exception occurred!", Snackbar.LENGTH_LONG).show(); }
        catch (Exception e) { Snackbar.make(mainLayout, "There are no email clients installed.", Snackbar.LENGTH_LONG).show(); }
    }

    private JSONObject getMakeErrorRequestJson() {
        JSONObject requestJson = (Info.isLoggedIn(this)) ? JsonProvider.getStandardRequestJson(this) : JsonProvider.getAnonymousRequestJson(this);
        try {
            HashMap<String, String> dataHashMap = new HashMap<>();
            dataHashMap.put("class",e.getClass().getName());
            dataHashMap.put("message",e.getMessage());
            dataHashMap.put("causetrace",causeTrace);
            dataHashMap.put("stacktrace",stackTrace);
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
            if(Info.isLoggedIn(this)) {
                JSONObject userJson = new JSONObject();
                userJson.put("name", Info.getName(this));
                userJson.put("email", Info.getEmail(this));
                userJson.put("phone", Info.getPhone(this));
                userJson.put("area", Info.getAreaName(this));
                userJson.put("city", Info.getCityName(this));
                dataJson.put("user", userJson);
            }
            requestJson.put("data", dataJson);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
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
