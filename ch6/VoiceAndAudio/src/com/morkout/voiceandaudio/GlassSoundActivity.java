package com.morkout.voiceandaudio;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;

public class GlassSoundActivity extends Activity {
	public final static String TAG = "GlassSoundActivity";

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
		inflater.inflate(R.menu.soundeffect, menu);

		return true;
	} 

	@Override
	public void onResume() {
		Log.v(TAG,  "onResume");
		super.onResume();
		if (getIntent().getExtras() == null) return;

		ArrayList<String> voiceResults = getIntent().getExtras()
				.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);

		if (voiceResults != null) { 
			Log.v(TAG,  "result size:" + voiceResults.size());

			if (voiceResults.size() > 0) {
				AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

				for (String result: voiceResults) {
					Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

					if (result.equalsIgnoreCase("disallowed")) {
						audio.playSoundEffect(Sounds.DISALLOWED);
						break;
					}
					else if (result.equalsIgnoreCase("dismissed")) {
						audio.playSoundEffect(Sounds.DISMISSED);
						break;
					}
					else if (result.equalsIgnoreCase("error")) {
						audio.playSoundEffect(Sounds.ERROR);
						break;
					}
					else if (result.equalsIgnoreCase("selected")) {
						audio.playSoundEffect(Sounds.SELECTED);
						break;
					}
					else if (result.equalsIgnoreCase("success")) {
						audio.playSoundEffect(Sounds.SUCCESS);
						break;
					}
					else if (result.equalsIgnoreCase("tap")) {
						audio.playSoundEffect(Sounds.TAP);
						break;
					}
				}  
			}
		}


	}

	@Override
	public void onOptionsMenuClosed (Menu menu) {
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

		switch (item.getItemId()) {
		case R.id.disallowed:
			audio.playSoundEffect(Sounds.DISALLOWED);
			Toast.makeText(this, "Disallowed", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.dismissed:
			audio.playSoundEffect(Sounds.DISMISSED);  
			Toast.makeText(this, "Dismissed", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.error:
			audio.playSoundEffect(Sounds.ERROR);  
			Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.selected:
			audio.playSoundEffect(Sounds.SELECTED);  
			Toast.makeText(this, "Selected", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.success:
			audio.playSoundEffect(Sounds.SUCCESS);  
			Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.tap:
			audio.playSoundEffect(Sounds.TAP);  
			Toast.makeText(this, "Tap", Toast.LENGTH_SHORT).show();
			return true;


		default:
			return super.onOptionsItemSelected(item);
		}
	}    	      
}
