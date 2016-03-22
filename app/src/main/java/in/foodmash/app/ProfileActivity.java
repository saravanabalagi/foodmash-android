package in.foodmash.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.commons.VolleyProgressFragment;
import in.foodmash.app.utils.EmailUtils;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class ProfileActivity extends FoodmashActivity implements View.OnClickListener, TextWatcher {

    @Bind(R.id.save) FloatingActionButton save;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.change_password) TextView changePassword;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.name) EditText name;
    @Bind(R.id.dob) EditText dob;
    @Bind(R.id.email) EditText email;
    @Bind(R.id.contact_no) EditText phone;
    @Bind(R.id.receive_promo) SwitchCompat promotionalOffers;

    @Bind(R.id.name_validate) ImageView nameValidate;
    @Bind(R.id.email_validate) ImageView emailValidate;
    @Bind(R.id.contact_validate) ImageView phoneValidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Mash","profile");

        save.setOnClickListener(this);
        changePassword.setOnClickListener(this);

        name.addTextChangedListener(this);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dob.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
                    }
                }, 1985, Calendar.JANUARY, 1);
                Calendar minCalender = Calendar.getInstance();
                minCalender.set(1900, 0, 1);
                Calendar maxCalender = Calendar.getInstance();
                maxCalender.set(Calendar.YEAR - 7, Calendar.MONTH, Calendar.DAY_OF_MONTH);
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTime().getTime());
                datePickerDialog.getDatePicker().setMinDate(minCalender.getTime().getTime());
                datePickerDialog.show();
            }
        });

        email.addTextChangedListener(this);
        phone.addTextChangedListener(this);
        makeProfileDetailsRequest();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password:
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                startActivity(intent); break;
            case R.id.save: if(isEverythingValid()) makeProfileRequest(); else Snackbar.make(mainLayout,"One or more data you entered is invalid",Snackbar.LENGTH_LONG).show(); break;
        }
    }

    private JSONObject getRequestJson() {
        JSONObject requestJson = null;
        try {
            HashMap<String, String> profileHashMap = new HashMap<>();
            profileHashMap.put("name", name.getText().toString().trim());
            profileHashMap.put("dob", dob.getText().toString().trim());
            profileHashMap.put("email", email.getText().toString().trim());
            profileHashMap.put("mobile_no", phone.getText().toString().trim());
            JSONObject userJson = new JSONObject(profileHashMap);
            userJson.put("offers", promotionalOffers.isChecked());

            requestJson = JsonProvider.getStandardRequestJson(ProfileActivity.this);
            JSONObject dataJson = new JSONObject();
            dataJson.put("user",userJson);
            requestJson.put("data",dataJson);

        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    public void makeProfileRequest() {
        JsonObjectRequest profileRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/profile/update", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        Actions.cacheUserDetails(ProfileActivity.this, name.getText().toString().trim(), email.getText().toString().trim(), phone.getText().toString().trim());
                        finish();
                    } else Snackbar.make(mainLayout, response.getString("error"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeProfileRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(ProfileActivity.this).addToRequestQueue(profileRequest);
    }

    public void makeProfileDetailsRequest() {
        JsonObjectRequest getProfileDetailsRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/profile", JsonProvider.getStandardRequestJson(ProfileActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fragmentContainer.setVisibility(View.GONE);
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        JSONObject userJson = dataJson.getJSONObject("user");
                        name.setText(userJson.getString("name"));
                        dob.setText(userJson.getString("dob").equals("null")?null:userJson.getString("dob"));
                        email.setText(userJson.getString("email"));
                        phone.setText(userJson.getString("mobile_no"));
                        promotionalOffers.setChecked(userJson.getBoolean("offers"));
                    } else Snackbar.make(mainLayout,"Unable to save details: "+response.getString("error"),Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) { e.printStackTrace(); Actions.handleIgnorableException(ProfileActivity.this,e); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, VolleyFailureFragment.newInstance(error, "makeProfileDetailsRequest")).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyProgressFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();
        Swift.getInstance(this).addToRequestQueue(getProfileDetailsRequest);
    }


    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {      }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==name.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(nameValidate, 500); else Animations.fadeOut(nameValidate,500); }
        else if(s==email.getEditableText()) { if(!EmailUtils.isValidEmailAddress(s.toString().trim())) Animations.fadeInOnlyIfInvisible(emailValidate, 500); else Animations.fadeOut(emailValidate,500); }
        else if(s==phone.getEditableText()) { if(s.toString().trim().length()!=10) Animations.fadeInOnlyIfInvisible(phoneValidate, 500); else Animations.fadeOut(phoneValidate,500); }
    }

    private boolean isDobFilled() { return dob.getText().toString().length()>0; }
    private boolean isEverythingValid() {
        return name.getText().toString().trim().length()>=2 &&
                EmailUtils.isValidEmailAddress(email.getText().toString().trim()) &&
                phone.getText().toString().trim().length()==10 &&
                NumberUtils.isInteger(phone.getText().toString().trim());
    }

}
