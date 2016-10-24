package com.morkout.locationsensors;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;


public class LocationActivity extends Activity  implements LocationListener{
	LocationManager mLocationManager; 
	static Location mLocation;
	TextView mTvLocation;   
	ImageView mIvMap;
	int mZoom;
	private GestureDetector mGestureDetector;
	private static final int MAX_ZOOM = 20;
	private static final int MIN_ZOOM = 2;
	private static final int START_ZOOM = 10;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.location);

		mTvLocation =  (TextView) findViewById(R.id.tvLocation);
		mIvMap =  (ImageView) findViewById(R.id.ivMap);
		mZoom = START_ZOOM;

		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		mGestureDetector = new GestureDetector(this);

		mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					openOptionsMenu();

					return true;
				} 
				else if (gesture == Gesture.SWIPE_RIGHT) {
					if (mZoom < MAX_ZOOM)
						new ImageLoadingTask().execute("http://maps.googleapis.com/maps/api/staticmap?zoom=" + ++mZoom + "&size=640x360&markers=color:red%7C"+mLocation.getLatitude() + "," + mLocation.getLongitude() + "&sensor=false");
					else 
						Toast.makeText(LocationActivity.this, "Max zoom reached...", Toast.LENGTH_LONG).show();
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					if (mZoom > MIN_ZOOM)
						new ImageLoadingTask().execute("http://maps.googleapis.com/maps/api/staticmap?zoom=" + --mZoom + "&size=640x360&markers=color:red%7C"+mLocation.getLatitude() + "," + mLocation.getLongitude() + "&sensor=false");
					else 
						Toast.makeText(LocationActivity.this, "Min zoom reached...", Toast.LENGTH_LONG).show();
						
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
		inflater.inflate(R.menu.location, menu);

		return true;
	} 


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.address:
			mIvMap.setVisibility(View.GONE);
			mTvLocation.setVisibility(View.VISIBLE);

			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(this, Locale.getDefault());
			try {
				addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getAddressLine(1);
				String country = addresses.get(0).getAddressLine(2);
				mTvLocation.setText(address + "," + city + "," + country);
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			return true;
		case R.id.map:
			// create an async task to get the image since network access isn't allowed on the main thread:
			new ImageLoadingTask().execute("http://maps.googleapis.com/maps/api/staticmap?zoom=" + mZoom + "&size=640x360&markers=color:red%7C"+mLocation.getLatitude() + "," + mLocation.getLongitude() + "&sensor=false");
			return true;				


		default:
			return super.onOptionsItemSelected(item);
		}
	}    	      


	private class ImageLoadingTask extends AsyncTask<String, Void, Bitmap> {


		@Override
		protected Bitmap doInBackground(String... stringURL) {		        
			Bitmap bmp = null;
			try {
				URL url = new URL(stringURL[0]);

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				BitmapFactory.Options options = new BitmapFactory.Options();
				bmp = BitmapFactory.decodeStream(is, null, options);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			mTvLocation.setVisibility(View.GONE);
			mIvMap.setVisibility(View.VISIBLE);
			mIvMap.setImageBitmap(result);
			super.onPostExecute(result);
		}

	}

	protected void onStart() {
		super.onStart();
		Criteria criteria = new Criteria();
		//criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		//criteria.setAccuracy(Criteria.NO_REQUIREMENT);

		System.out.println("best provider:" + mLocationManager.getBestProvider(criteria, true));
		String allString = "";
		List<String> providers = mLocationManager.getProviders(criteria, false); 
		for (String p : providers) {
			allString += p+":";

			if (mLocationManager.isProviderEnabled(p)) {
				allString += "Y;";
				mLocationManager.requestLocationUpdates(p, 10000, 0, this);
				// If it is greater than 0 then the location provider will only send your application an update when the location has changed by at least minDistance meters, AND at least minTime milliseconds have passed. 
				// http://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent) 
				Location location = mLocationManager.getLastKnownLocation(p);
				if (location == null)				
				{
					System.out.println("getLastKnownLocation for provider " + p + " returns null");
				}
				else {
					System.out.println("getLastKnownLocation for provider " + p + " returns NOT null");
					mTvLocation.setText(location.getLatitude() + ", " + location.getLongitude());
				}
			}
			else {
				allString += "N;";
			}
		}

		// on Glass, allString is: remote_gps:Y;remote_network:Y;network:Y;passive:Y
		System.out.println(allString);
		mTvLocation.setText(allString);	
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.i(">>>", "onStatusChanged");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.i(">>>", "onProviderEnabled");

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.i(">>>", "onProviderDisabled");
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mLocation = location;
		mTvLocation.setText(mLocation.getLatitude() + ", " + mLocation.getLongitude());
		Log.i(">>>onLocationChanged", mLocation.getLatitude() + ", " + mLocation.getLongitude());

	}
	
	public static Location getCurrentLocation() {
		return mLocation;
	}
}
