package in.foodmash.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
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
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.JsonProvider;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.utils.EmailUtils;
import in.foodmash.app.utils.NumberUtils;

/**
 * Created by Zeke on Aug 08 2015.
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    @Bind(R.id.save) FloatingActionButton save;
    @Bind(R.id.main_layout) ScrollView mainLayout;
    @Bind(R.id.loading_layout) LinearLayout loadingLayout;
    @Bind(R.id.saving_layout) LinearLayout savingLayout;
    @Bind(R.id.change_password) TextView changePassword;

    private Intent intent;

    private EditText name;
    private EditText dob;
    private EditText email;
    private EditText phone;
    private Switch promotionOffers;

    private ImageView nameValidate;
    private ImageView emailValidate;
    private ImageView phoneValidate;

    private ImageButton clearFields;
    private JsonObjectRequest profileRequest;
    private JsonObjectRequest jsonObjectRequest;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        RelativeLayout cartCountLayout = (RelativeLayout) menu.findItem(R.id.menu_cart).getActionView();
        TextView cartCount = (TextView) cartCountLayout.findViewById(R.id.cart_count); Actions.updateCartCount(cartCount);
        cartCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ProfileActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_log_out: Actions.logout(ProfileActivity.this); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); finish(); return true;
            case R.id.menu_reset: name.setText(null); dob.setText(null); email.setText(null); phone.setText(null); promotionOffers.setChecked(true); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        save.setOnClickListener(this);
        changePassword.setOnClickListener(this);

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
        email = (EditText) findViewById(R.id.email_or_phone); email.addTextChangedListener(this);
        phone = (EditText) findViewById(R.id.phone); phone.addTextChangedListener(this);
        promotionOffers = (Switch) findViewById(R.id.receive_promo); promotionOffers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked)
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

        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/profile", JsonProvider.getStandardRequestJson(ProfileActivity.this), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Animations.fadeOut(loadingLayout,500);
                        Animations.fadeIn(mainLayout,500);
                        JSONObject dataJson = response.getJSONObject("data");
                        JSONObject userJson = dataJson.getJSONObject("user");
                        name.setText(userJson.getString("name"));
                        dob.setText(userJson.getString("dob").equals("null")?null:userJson.getString("dob"));
                        email.setText(userJson.getString("email"));
                        phone.setText(userJson.getString("mobile_no"));
                        promotionOffers.setChecked(userJson.getBoolean("offers"));
                    } else {
                        Alerts.requestUnauthorisedAlert(ProfileActivity.this);
                        System.out.println(response.getString("error"));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Swift.getInstance(ProfileActivity.this).addToRequestQueue(jsonObjectRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(ProfileActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(ProfileActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(ProfileActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(loadingLayout, 500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password: intent = new Intent(this, ChangePasswordActivity.class); startActivity(intent); break;
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
        profileRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/profile/update", getRequestJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Actions.cacheEmailAndPhone(ProfileActivity.this, email.getText().toString().trim(), phone.getText().toString().trim());
                        finish();
                    } else {
                        Animations.fadeOut(savingLayout,500);
                        Animations.fadeIn(mainLayout,500);
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
                Animations.fadeOut(savingLayout,500);
                Animations.fadeIn(mainLayout,500);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animations.fadeIn(savingLayout,500);
                        Animations.fadeOut(mainLayout, 500);
                        Swift.getInstance(ProfileActivity.this).addToRequestQueue(profileRequest);
                    }
                };
                if(error instanceof TimeoutError) Alerts.timeoutErrorAlert(ProfileActivity.this, onClickTryAgain);
                else if(error instanceof NoConnectionError) Alerts.internetConnectionErrorAlert(ProfileActivity.this, onClickTryAgain);
                else Alerts.unknownErrorAlert(ProfileActivity.this);
                System.out.println("Response Error: " + error);
            }
        });
        Animations.fadeIn(savingLayout,500);
        Animations.fadeOut(mainLayout, 500);
        Swift.getInstance(ProfileActivity.this).addToRequestQueue(profileRequest);
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
