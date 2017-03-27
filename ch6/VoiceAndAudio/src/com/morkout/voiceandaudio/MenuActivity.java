package com.morkout.voiceandaudio;

import wpam.recognizer.MainActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity {

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
		case R.id.stop:
			stopService(new Intent(this, AppService.class));
			return true;

		case R.id.pitchdetection:
			Intent intent = new Intent(this, PitchDetectionActivity.class);
			startActivity(intent);     
			return true;                 

		case R.id.soundeffect:
			Intent intent2 = new Intent(this, GlassSoundActivity.class);
			startActivity(intent2);     
			return true;                 

		case R.id.audiocapture:
			Intent intent3 = new Intent(this, AudioCaptureActivity.class);
			startActivity(intent3);     
			return true;                

		case R.id.musicrecognition:
			Intent intent4 = new Intent(this, MusicRecognitionActivity.class);
			startActivity(intent4);     
			return true;    

		case R.id.touchtone:
			Intent intent5 = new Intent(this, MainActivity.class);
			startActivity(intent5);     
			return true;                                 

		case R.id.audiocapture2:
			Intent intent6 = new Intent(this, AudioCapture2Activity.class);
			startActivity(intent6);     
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
