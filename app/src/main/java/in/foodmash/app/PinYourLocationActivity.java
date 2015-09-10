package in.foodmash.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import in.foodmash.app.custom.TouchableImageButton;


/**
 * Created by sarav on Aug 08 2015.
 */
public class PinYourLocationActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    Intent intent;
    LocationManager locationManager;
    LocationListener locationListener;
    JSONObject jsonObject;

    LinearLayout back;
    LinearLayout proceed;
    boolean edit = false;
    boolean cart = false;

    MapFragment mapFragment;
    TouchableImageButton resetMap;
    LatLng initialLocation = new LatLng(13.0220501,80.2437108);

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
        setContentView(R.layout.activity_pin_your_location);

        cart = getIntent().getBooleanExtra("cart",false);
        if(getIntent().getBooleanExtra("edit",false)) {
            try {
                jsonObject = new JSONObject(getIntent().getStringExtra("json"));
                JSONObject geolocationJson = jsonObject.getJSONObject("geolocation");
                initialLocation = new LatLng(geolocationJson.getDouble("latitude"),geolocationJson.getDouble("longitude"));
                edit = true;
            } catch (JSONException e) { e.printStackTrace(); }
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) enableGpsAlert();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        proceed = (LinearLayout) findViewById(R.id.proceed); proceed.setOnClickListener(this);
        resetMap = (TouchableImageButton) findViewById(R.id.reset_map); resetMap.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset_map: mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation,15));break;
            case R.id.back: finish(); break;
            case R.id.proceed:
                locationManager.removeUpdates(locationListener);
                intent = new Intent(this, AddAddressActivity.class);
                CameraPosition cameraPosition = mapFragment.getMap().getCameraPosition();
                LatLng latLng = cameraPosition.target;
                intent.putExtra("latitude",latLng.latitude);
                intent.putExtra("longitude",latLng.longitude);
                if(edit) {
                    intent.putExtra("edit",true);
                    intent.putExtra("json",jsonObject.toString());
                } if(cart) intent.putExtra("cart",true);
                startActivity(intent); break;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, (edit)?17:14));
        locationListener = new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                initialLocation = new LatLng(location.getLatitude(),location.getLongitude());
                mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation,15));
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {  }
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) { }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    private void enableGpsAlert() {
        new AlertDialog.Builder(PinYourLocationActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("GPS Turned Off")
                .setMessage("Enabling GPS helps pinpoint your location on map. Enable GPS from Settings.")
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                locationManager.removeUpdates(locationListener);
            }
        }).show();
    }
}
