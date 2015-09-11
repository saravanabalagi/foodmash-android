package in.foodmash.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.custom.TouchableImageButton;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by sarav on Aug 08 2015.
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    Intent intent;

    EditText name;
    EditText dob;
    EditText email;
    EditText phone;
    Switch promotionOffers;

    LinearLayout cancel;
    LinearLayout save;
    LinearLayout changePassword;

    ImageView nameValidate;
    ImageView emailValidate;
    ImageView phoneValidate;

    TouchableImageButton clearFields;
    JsonObjectRequest profileRequest;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_log_out: intent = new Intent(this,LoginActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        cancel = (LinearLayout) findViewById(R.id.cancel); cancel.setOnClickListener(this);
        save = (LinearLayout) findViewById(R.id.save); save.setOnClickListener(this);
        changePassword = (LinearLayout) findViewById(R.id.change_password); changePassword.setOnClickListener(this);
        clearFields = (TouchableImageButton) findViewById(R.id.clear_fields); clearFields.setOnClickListener(this);

        nameValidate = (ImageView) findViewById(R.id.name_validate);
        emailValidate = (ImageView) findViewById(R.id.email_validate);
        phoneValidate = (ImageView) findViewById(R.id.phone_validate);

        name = (EditText) findViewById(R.id.name); name.addTextChangedListener(this);
        dob = (EditText) findViewById(R.id.dob); dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR,year);
                        calendar.set(Calendar.MONTH,monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        dob.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
                    }
                }, 1985, Calendar.JANUARY, 1);
                Calendar minCalender = Calendar.getInstance(); minCalender.set(1900,0,1);
                Calendar maxCalender = Calendar.getInstance(); maxCalender.set(Calendar.YEAR-7,Calendar.MONTH,Calendar.DAY_OF_MONTH);
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTime().getTime());
                datePickerDialog.getDatePicker().setMinDate(minCalender.getTime().getTime());
                datePickerDialog.show();
            }
        });
        email = (EditText) findViewById(R.id.email_or_phone); email.addTextChangedListener(this);
        phone = (EditText) findViewById(R.id.phone); phone.addTextChangedListener(this);
        promotionOffers = (Switch) findViewById(R.id.receive_promo); promotionOffers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                    new AlertDialog.Builder(ProfileActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Stop receiving offers ?")
                        .setMessage("You have chosen to unsubscribe from all promotional offers via email and SMS. Are you sure to want to disable sending promotional offers?")
                        .setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton("Enable", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                promotionOffers.setChecked(true);
                            }
                        }).show();
            }
        });

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/profile", JsonProvider.getStandardRequestJson(ProfileActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONObject dataJson = response.getJSONObject("data");
                        JSONObject userJson = dataJson.getJSONObject("user");
                        name.setText(userJson.getString("name"));
                        dob.setText(userJson.getString("dob").equals("null")?null:userJson.getString("dob"));
                        email.setText(userJson.getString("email"));
                        phone.setText(userJson.getString("mobile_no"));
                        promotionOffers.setChecked(userJson.getBoolean("offers"));
                    } else if(response.getBoolean("success")) {
                        Alerts.requestUnauthorisedAlert(ProfileActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_fields: name.setText(null); dob.setText(null); email.setText(null); phone.setText(null); promotionOffers.setChecked(true); break;
            case R.id.change_password: intent = new Intent(this, ChangePasswordActivity.class); startActivity(intent); break;
            case R.id.cancel: finish(); break;
            case R.id.save: if(isEverythingValid()) makeJsonRequest(); else Alerts.validityAlert(ProfileActivity.this); break;
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
            userJson.put("offers", promotionOffers.isChecked());

            requestJson = JsonProvider.getStandardRequestJson(ProfileActivity.this);
            JSONObject dataJson = new JSONObject();
            dataJson.put("user",userJson);
            requestJson.put("data",dataJson);

        } catch (JSONException e) { e.printStackTrace(); }
        return requestJson;
    }

    private void makeJsonRequest() {
        profileRequest = new JsonObjectRequest(Request.Method.PATCH, getString(R.string.api_root_path) + "/profile", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        cacheEmailAndPhone(email.getText().toString().trim(),phone.getText().toString().trim());
                        finish();
                    } else if(!(response.getBoolean("success"))) {
                        Alerts.commonErrorAlert(ProfileActivity.this,
                                "Invalid Details",
                                "We are unable to save your profile details as they are invalid. Try again later!",
                                "Okay");
                        System.out.println("Response error: "+response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(ProfileActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(ProfileActivity.this).addToRequestQueue(profileRequest);
                    }
                });
                if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(ProfileActivity.this);
                else Alerts.unknownErrorAlert(ProfileActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Swift.getInstance(ProfileActivity.this).addToRequestQueue(profileRequest);
    }


    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {      }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
    @Override public void afterTextChanged(Editable s) {
        if(s==name.getEditableText()) { if(s.toString().trim().length()<2) Animations.fadeInOnlyIfInvisible(nameValidate, 500); else Animations.fadeOut(nameValidate,500); }
        else if(s==email.getEditableText()) { if(!EmailValidator.getInstance().isValid(s.toString().trim())) Animations.fadeInOnlyIfInvisible(emailValidate, 500); else Animations.fadeOut(emailValidate,500); }
        else if(s==phone.getEditableText()) { if(s.toString().trim().length()!=10) Animations.fadeInOnlyIfInvisible(phoneValidate, 500); else Animations.fadeOut(phoneValidate,500); }
    }

    private boolean isDobFilled() { return dob.getText().toString().length()>0; }
    private boolean isEverythingValid() {
        return name.getText().toString().trim().length()>=2 &&
                EmailValidator.getInstance().isValid(email.getText().toString().trim()) &&
                phone.getText().toString().trim().length()==10 &&
                NumberUtils.isInteger(phone.getText().toString().trim());
    }

    private void cacheEmailAndPhone(String email, String phone) {
        SharedPreferences sharedPreferences = getSharedPreferences("cache", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email",email);
        editor.putString("phone",phone);
        editor.apply();
    }


}
