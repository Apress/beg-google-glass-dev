package com.morkout.locationsensors;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity {
    private static final String TAG = "MenuActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

    		case R.id.location:
            	Intent intent = new Intent(this, LocationActivity.class);
                startActivity(intent);
                return true;
            
        	case R.id.sensor:
            	Intent intent2 = new Intent(this, SensorActivity.class);
                startActivity(intent2);
        		return true;

    		case R.id.map:
    			Intent intent3 = new Intent(Intent.ACTION_VIEW);
    			intent3.setData(Uri.parse("google.navigation:q=37.4038088,-121.9936342"));
    			startActivity(intent3);
                return true;
                
    		case R.id.nearby:
            	Intent intent4 = new Intent(this, NearbyPlacesActivity.class);
                startActivity(intent4);
        		return true;                

        	case R.id.stop:
                stopService(new Intent(this, AppService.class));
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        finish();
    }
   
}
