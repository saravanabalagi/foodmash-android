package in.foodmash.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Actions;


/**
 * Created by Zeke on Aug 08 2015.
 */
public class PinYourLocationActivity extends FoodmashActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    @Bind(R.id.proceed) FloatingActionButton proceed;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.main_layout) RelativeLayout mainLayout;

    Intent intent;
    LocationManager locationManager;
    LocationListener locationListener;
    JSONObject jsonObject;

    boolean edit = false;
    boolean cart = false;

    MapFragment mapFragment;
    LatLng initialLocation = new LatLng(13.0220501,80.2437108);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_your_location);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { Actions.handleIgnorableException(this,e); }
        setTitle(toolbar,"Pin","location");

        cart = getIntent().getBooleanExtra("cart",false);
        if(getIntent().getBooleanExtra("edit",false)) {
            edit = true;
            try {
                jsonObject = new JSONObject(getIntent().getStringExtra("json"));
                JSONObject geolocationJson = jsonObject.getJSONObject("geolocation");
                initialLocation = new LatLng(geolocationJson.getDouble("latitude"),geolocationJson.getDouble("longitude"));
            } catch (JSONException e) { Snackbar.make(mainLayout, "No location chosen before!", Snackbar.LENGTH_LONG); }
        }

        if(!isPlayServicesAvailable()) {
            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle("Google Play Services Outdated");
            alertBuilder.setMessage("Your phone does not seem to have recent version of Google Play Services installed which is very essential for basic functioning of Maps Services. You will be taken to next page.");
            final AlertDialog alertDialog = alertBuilder.create();
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                    proceed(initialLocation);
                    finish();
                }
            });
            alertDialog.show();
        }

        else if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_PERMISSION_ACCESS_FINE_LOCATION );
        } else {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!isGpsAvailable()) enableGpsAlert();
            if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)))
                enableGpsAlert();

            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            proceed.setOnClickListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSION_ACCESS_FINE_LOCATION) {
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(getIntent());
                finish();
            } else if (!isGpsAvailable()) enableGpsAlert();
        }
    }

    @Override
    protected void onDestroy() {
        if(locationManager!=null && locationListener!=null)
            if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
                locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.proceed:
                if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
                    locationManager.removeUpdates(locationListener);
                intent = new Intent(this, AddAddressActivity.class);
                CameraPosition cameraPosition = mapFragment.getMap().getCameraPosition();
                LatLng latLng = cameraPosition.target;
                proceed(latLng);
                break;
        }
    }

    private void proceed(LatLng latLng) {
        intent = new Intent(this, AddAddressActivity.class);
        intent.putExtra("latitude",latLng.latitude);
        intent.putExtra("longitude",latLng.longitude);
        if(edit) {
            intent.putExtra("edit",true);
            intent.putExtra("json",jsonObject.toString());
        } if(cart) intent.putExtra("cart",true);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)))
            enableGpsAlert();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
            map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, (edit)?16:14));
        locationListener = new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                if(mapFragment!=null && locationListener!=null) {
                    initialLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));
                }
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {  }
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) { }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    private void enableGpsAlert() {
        Snackbar snackbar = Snackbar.make(mainLayout, "GPS Disabled", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Enable", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,0);
            }
        });
        snackbar.show();
    }


    private boolean isPlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        return result == ConnectionResult.SUCCESS;
    }

    private boolean isGpsAvailable() {
        PackageManager pm = this.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }
}
