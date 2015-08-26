package in.foodmash.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;


/**
 * Created by sarav on Aug 08 2015.
 */
public class PinYourLocationActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    Intent intent;

    LinearLayout back;
    LinearLayout proceed;

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
            case R.id.menu_profile: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_email_phone: intent = new Intent(this,EmailPhoneActivity.class); startActivity(intent); return true;
            case R.id.menu_addresses: intent = new Intent(this,AddressActivity.class); startActivity(intent); return true;
            case R.id.menu_order_history: intent = new Intent(this,OrderHistoryActivity.class); startActivity(intent); return true;
            case R.id.menu_wallet_cash: intent = new Intent(this,ProfileActivity.class); startActivity(intent); return true;
            case R.id.menu_contact_us: intent = new Intent(this,ContactUsActivity.class); startActivity(intent); return true;
            case R.id.menu_log_out: intent = new Intent(this,LoginActivity.class); startActivity(intent); return true;
            case R.id.menu_cart: intent = new Intent(this,CartActivity.class); startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_your_location);

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        back = (LinearLayout) findViewById(R.id.back); back.setOnClickListener(this);
        proceed = (LinearLayout) findViewById(R.id.proceed); proceed.setOnClickListener(this);
        resetMap = (TouchableImageButton) findViewById(R.id.reset_map); resetMap.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset_map: mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation,13));break;
            case R.id.back: intent = new Intent(this, MainActivity.class); startActivity(intent); break;
            case R.id.proceed: intent = new Intent(this, AddEditAddressActivity.class); startActivity(intent); break;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 13));

        map.addMarker(new MarkerOptions()
                .draggable(true)
                .title("Kottur & RA Puram")
                .snippet("Pin your location and we'll be right there, at your doorstep...")
                .position(initialLocation));
    }
}
