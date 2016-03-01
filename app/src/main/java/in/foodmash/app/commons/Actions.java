package in.foodmash.app.commons;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import in.foodmash.app.LoginActivity;
import in.foodmash.app.R;
import in.foodmash.app.custom.Cart;

/**
 * Created by Zeke on Sep 20 2015.
 */
public class Actions {

    public static void cacheUserDetails(Context context,String name, String email, String phone) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email",email);
        editor.putString("phone",phone);
        editor.putString("name",name);
        editor.apply();
    }

    public static void cacheLocationDetails(Context context, String cityName, String areaName, int areaId,int packagingCenterId) {
        int oldPackagingCenterId = Info.getPackagingCentreId(context);
        if(oldPackagingCenterId != -1 && packagingCenterId != oldPackagingCenterId) cacheCombos(context, null);
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("city_name", cityName);
        editor.putString("area_name", areaName);
        editor.putInt("area_id", areaId);
        editor.putInt("packaging_centre_id", packagingCenterId);
        editor.commit();
    }

    public static void cacheCities(Context context, String cityJsonArrayString) {
        if(Info.getCityJsonArrayString(context)!= null
                && Info.getCityJsonArrayString(context).equals(cityJsonArrayString)) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("city_list", cityJsonArrayString);
        editor.apply();
    }

    public static void cacheCombos(Context context, String comboJsonArrayString) {
        if(Info.getComboJsonArrayString(context)!= null
            && Info.getComboJsonArrayString(context).equals(comboJsonArrayString)) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences("cache", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("combo_list", comboJsonArrayString);
        editor.apply();
    }

    public static void logout(final Context context) {
        Swift.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST, context.getString(R.string.api_root_path) + "/sessions/destroy", JsonProvider.getStandardRequestJson(context), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                performLogout(context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                performLogout(context);
            }
        }));
    }

    public static void updateCartCount(TextView cartCount) {
        if(cartCount==null) return;
        int count = Cart.getInstance().getCount();
        if(count>0) { cartCount.setText(String.valueOf(count)); Animations.fadeInOnlyIfInvisible(cartCount, 500); }
        else Animations.fadeOut(cartCount,500);
    }

    private static void performLogout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("session", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged_in", false);
        editor.remove("user_token");
        editor.remove("session_token");
        editor.remove("android_token");
        editor.apply();
        Intent intent = new Intent(context,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
