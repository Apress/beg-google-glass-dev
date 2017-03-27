package com.morkout.myvideoapps;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity implements OnInitListener {
	private static final String TAG = "MenuActivity";
	private TextToSpeech tts;
	public static final int VOICE_RECOGNIZER_REQUEST_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//		openOptionsMenu();
//	}

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

		case R.id.basicvideocapture:
			Intent intent = new Intent(this, BasicVideoCaptureActivity.class);
			startActivity(intent);  
			return true; 

		case R.id.customvideocapture:
			Intent intent2 = new Intent(this, CustomVideoCaptureActivity.class);
			startActivity(intent2);  
			return true; 

		case R.id.videoframes:
			Intent intent3 = new Intent(this, VideoFramesActivity.class);
			startActivity(intent3);  
			return true; 

		case R.id.videoplayer:
			Intent intent4 = new Intent(this, VideoPlayerActivity.class);
			startActivity(intent4);  
			return true; 

		case R.id.videoprocessing:
			Intent intent5 = new Intent(this, VideoProcessingActivity.class);
			startActivity(intent5);  
			return true; 

		case R.id.videosearch:
			Intent intent6 = new Intent(this, VideoSearchActivity.class);
			startActivity(intent6);  
			return true;                 

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	protected void onActivityResult(int RequestCode, int ResultCode, Intent data) {
		Log.v(TAG, "onActivityResult:"+RequestCode + ","+ResultCode);
		switch(RequestCode) {
		case VOICE_RECOGNIZER_REQUEST_CODE:
			if(RequestCode == VOICE_RECOGNIZER_REQUEST_CODE && ResultCode == RESULT_OK) {
				ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				for(int i = 0; i < results.size(); i++) {
					Log.v(TAG, "results: " + results.get(i));
				}
			}
			break;
		}
		super.onActivityResult(RequestCode, ResultCode, data);
	}


	@Override
	public void onOptionsMenuClosed(Menu menu) {
		finish();
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.v("TTS", "This Language is not supported");
			} else {
				tts.speak("Hello Glass!", TextToSpeech.QUEUE_FLUSH, null);
			}
		} else {
			Log.e("TTS", "Initilization Failed!");
		}
	}    
}
