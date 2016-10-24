package com.morkout.locationsensors;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;


public class NearbyPlacesActivity extends Activity {
	LocationManager mLocationManager; 
	Location mLocation;
	TextView mTvLocation;   
	private GestureDetector mGestureDetector;
	String[] PLACE_TYPES = { 

			"accounting",
			"airport",
			"amusement_park",
			"aquarium",
			"art_gallery",
			"atm",
			"bakery",
			"bank",
			"bar",
			"beauty_salon",
			"bicycle_store",
			"book_store",
			"bowling_alley",
			"bus_station",
			"cafe",
			"campground",
			"car_dealer",
			"car_rental",
			"car_repair",
			"car_wash",
			"casino",
			"cemetery",
			"church",
			"city_hall",
			"clothing_store",
			"convenience_store",
			"courthouse",
			"dentist",
			"department_store",
			"doctor",
			"electrician",
			"electronics_store",
			"embassy",
			"establishment",
			"finance",
			"fire_station",
			"florist",
			"food",
			"funeral_home",
			"furniture_store",
			"gas_station",
			"general_contractor",
			"grocery_or_supermarket",
			"gym",
			"hair_care",
			"hardware_store",
			"health",
			"hindu_temple",
			"home_goods_store",
			"hospital",
			"insurance_agency",
			"jewelry_store",
			"laundry",
			"lawyer",
			"library",
			"liquor_store",
			"local_government_office",
			"locksmith",
			"lodging",
			"meal_delivery",
			"meal_takeaway",
			"mosque",
			"movie_rental",
			"movie_theater",
			"moving_company",
			"museum",
			"night_club",
			"painter",
			"park",
			"parking",
			"pet_store",
			"pharmacy",
			"physiotherapist",
			"place_of_worship",
			"plumber",
			"police",
			"post_office",
			"real_estate_agency",
			"restaurant",
			"roofing_contractor",
			"rv_park",
			"school",
			"shoe_store",
			"shopping_mall",
			"spa",
			"stadium",
			"storage",
			"store",
			"subway_station",
			"synagogue",
			"taxi_stand",
			"train_station",
			"travel_agency",
			"university",
			"veterinary_care",
	"zoo"};


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.location);

		mTvLocation =  (TextView) findViewById(R.id.tvLocation);

		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		mGestureDetector = new GestureDetector(this);

		mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					openOptionsMenu();

					return true;
				} 
				return false;
			}
		});		
	}




	// this method is required for tap on touchpad to work!
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}        




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.juststop, menu);

		for (int i=1; i<=PLACE_TYPES.length; i++)
			menu.add(PLACE_TYPES[i-1].toUpperCase());

		return true;
	} 



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println("seleccted:"+item.getTitle().toString());
		String ptype = item.getTitle().toString().toLowerCase();
		if (ptype.equals("stop")) {
			finish();
			return true;
		}

		Intent intent = new Intent(this, ScrollingCardsActivity.class);
		intent.putExtra("ptype", ptype);
		intent.putExtra("latitude", Double.valueOf(mLocation.getLatitude()));
		intent.putExtra("longitude", Double.valueOf(mLocation.getLongitude()));
		startActivity(intent);                    


		return super.onOptionsItemSelected(item);

	}    	      

	protected void onStart() {
		super.onStart();
		Criteria criteria = new Criteria();
		//criteria.setAccuracy(Criteria.ACCURACY_FINE);
		//criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAccuracy(Criteria.NO_REQUIREMENT);

		List<String> providers = mLocationManager.getProviders(criteria, false); 
		for (String p : providers) {
			if (mLocationManager.isProviderEnabled(p)) {
				mLocation = mLocationManager.getLastKnownLocation(p);
				if (mLocation != null) {
					mTvLocation.setText(mLocation.getLatitude() + ", " + mLocation.getLongitude());
					break;
				}
			}
		}
	}
}
