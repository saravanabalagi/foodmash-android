package in.foodmash.app;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    Intent intent;
    Context context;

    ImageView offers;
    ImageView for_1;
    ImageView for_2;
    ImageView for_3;

    ImageView offers_focus;
    ImageView for_1_focus;
    ImageView for_2_focus;
    ImageView for_3_focus;

    ViewPager viewPager;

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
        setContentView(R.layout.activity_main);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.api_root_path)+"/combos", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof NoConnectionError) { show("Network Error. Reconnect and try again!"); }
                else show("Unknown error. Try again!");
            }
        });
        Swift.getInstance(this).addToRequestQueue(jsonObjectRequest);

        context = getBaseContext();
        offers = (ImageView) findViewById(R.id.offers); offers.setOnClickListener(this);
        for_1 = (ImageView) findViewById(R.id.for_1); for_1.setOnClickListener(this);
        for_2 = (ImageView) findViewById(R.id.for_2); for_2.setOnClickListener(this);
        for_3 = (ImageView) findViewById(R.id.for_3); for_3.setOnClickListener(this);

        offers_focus = (ImageView) findViewById(R.id.offers_focus);
        for_1_focus = (ImageView) findViewById(R.id.for_1_focus);
        for_2_focus = (ImageView) findViewById(R.id.for_2_focus);
        for_3_focus = (ImageView) findViewById(R.id.for_3_focus);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override public int getCount() { return 4; }
            @Override public boolean isViewFromObject(View view, Object object) { return view==((ScrollView) object); }
            @Override public void destroyItem(ViewGroup container, int position, Object object) { container.removeView((ScrollView)object); }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ScrollView scrollView = new ScrollView(getBaseContext());
                scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                LinearLayout linearLayout = new LinearLayout(getBaseContext());
                linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                for (int i=position*4;i<(position*4)+4;i++) {
                    final RelativeLayout comboLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.main_combo, linearLayout, false);
                    ((ImageView) comboLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                    ((TextView) comboLayout.findViewById(R.id.name)).setText("Sample "+i);
                    ((TextView) comboLayout.findViewById(R.id.description)).setText("Sample desc "+i);
                    ((TextView) comboLayout.findViewById(R.id.price)).setText(""+(100+i));
                    final LinearLayout comboFoodLayout = (LinearLayout) comboLayout.findViewById(R.id.food_items_layout);
                    comboLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(comboFoodLayout.getChildCount()==0) {
                                for (int j=0;j<3;j++) {
                                    LinearLayout currentComboFoodLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_combo_food, comboFoodLayout, false);
                                    ((ImageView) currentComboFoodLayout.findViewById(R.id.image)).setImageResource(R.mipmap.image_default);
                                    ((TextView) currentComboFoodLayout.findViewById(R.id.name)).setText("Item " + j);
                                    ((TextView) currentComboFoodLayout.findViewById(R.id.description)).setText("Item Desc "+j);
                                    comboFoodLayout.addView(currentComboFoodLayout,j);
                                }
                            } else comboFoodLayout.removeAllViews();
                        }
                    });
                    linearLayout.addView(comboLayout);
                }
                scrollView.addView(linearLayout);
                container.addView(scrollView);
                return scrollView;
            }
        };
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.offers: setFocus(v.getId()); break;
            case R.id.for_1: setFocus(v.getId()); break;
            case R.id.for_2: setFocus(v.getId()); break;
            case R.id.for_3: setFocus(v.getId()); break;
        }
    }

    private void setFocus(int id){
        findViewById(R.id.offers_focus).setVisibility((id==R.id.offers)?View.VISIBLE:View.INVISIBLE);
        findViewById(R.id.for_1_focus).setVisibility((id==R.id.for_1)?View.VISIBLE:View.INVISIBLE);
        findViewById(R.id.for_2_focus).setVisibility((id==R.id.for_2)?View.VISIBLE:View.INVISIBLE);
        findViewById(R.id.for_3_focus).setVisibility((id==R.id.for_3) ? View.VISIBLE : View.INVISIBLE);
        switch (id) {
            case R.id.offers: viewPager.setCurrentItem(0, true); break;
            case R.id.for_1: viewPager.setCurrentItem(1, true); break;
            case R.id.for_2: viewPager.setCurrentItem(2, true); break;
            case R.id.for_3: viewPager.setCurrentItem(3, true); break;
        }
    }

    @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
    @Override public void onPageScrollStateChanged(int state) {}
    @Override public void onPageSelected(int position) {
        switch (position) {
            case 0: setFocus(R.id.offers); break;
            case 1: setFocus(R.id.for_1); break;
            case 2: setFocus(R.id.for_2); break;
            case 3: setFocus(R.id.for_3); break;
        }
    }

    private void show(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
