package com.morkout.voiceandaudio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

public class AppService extends Service {
	private static final String TAG = "AppService";
	private static final String LIVE_CARD_ID = "magictime";

	private AppDrawer mCallback;
	private LiveCard mLiveCard;
	private static AppService mAppService;

	public static AppService appService() {
		return mAppService;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate");
		super.onCreate();
		mAppService = this;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "onBind");
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG,  "onStartCommand");

//		ArrayList<String> voiceResults = intent.getExtras()
//				.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);    	
//		for (String result: voiceResults) {
//			Log.v(TAG, "result="+result);
////			Toast.makeText(mAppService, result, Toast.LENGTH_LONG).show();
////			AudioManager audio = (AudioManager) AppService.appService().getSystemService(Context.AUDIO_SERVICE);
////			if (result.equalsIgnoreCase("tap"))
////				audio.playSoundEffect(Sounds.TAP);
////			else if (result.equalsIgnoreCase("disallowed"))
////				audio.playSoundEffect(Sounds.DISALLOWED);
////			else if (result.equalsIgnoreCase("dismissed"))
////				audio.playSoundEffect(Sounds.DISMISSED);
////			else if (result.equalsIgnoreCase("error"))
////				audio.playSoundEffect(Sounds.ERROR);
////			else if (result.equalsIgnoreCase("selected"))
////				audio.playSoundEffect(Sounds.SELECTED);            	            	 
////			else if (result.equalsIgnoreCase("success"))
////				audio.playSoundEffect(Sounds.SUCCESS);
//		}

		if (mLiveCard == null) {
			Log.v(TAG, "onStartCommand: true");
			mLiveCard = new LiveCard(this, LIVE_CARD_ID);

			// Keep track of the callback to remove it before unpublishing.
			mCallback = new AppDrawer(this);
			mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback);

			Intent menuIntent = new Intent(this, MenuActivity.class);
			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

			mLiveCard.publish(PublishMode.REVEAL);
			Log.v(TAG, "Done publishing LiveCard");
		} 
		else {
			Log.v(TAG, "onStartCommand: false");
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "OnDestroy()");

		if (mLiveCard != null && mLiveCard.isPublished()) {
			Log.v(TAG, "OnDestroy: true");
			if (mCallback != null) {
				mLiveCard.getSurfaceHolder().removeCallback(mCallback);
			}
			mLiveCard.unpublish();
			mLiveCard = null;
		}
		else {
			Log.e(TAG, "OnDestroy: false");
		}
		super.onDestroy();
	}
}