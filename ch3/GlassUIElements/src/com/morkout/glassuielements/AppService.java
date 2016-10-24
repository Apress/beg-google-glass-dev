package com.morkout.glassuielements;

import java.util.Date;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

public class AppService extends Service {
	private static final String TAG = "AppService";
	private static final String LIVE_CARD_ID = "magictime";
	private static AppService mAppService; 

	private AppDrawer mCallback;
	private MyGLView mCallback3D;
	private LiveCard2DRenderer mCallbackCanvas;
	private LiveCard3DRenderer mCallbackLivecard3D;
	private LiveCard mLiveCard;
	private RemoteViews mRemoteViews;

	int percent_done = 0;

	public AppDrawer getAppDrawer() {
		return mCallback;
	}

	public static AppService appService() {
		return mAppService;
	}

	private void addMenuToLiveCard() {
		Intent menuIntent = new Intent(this, MenuActivity.class);
		mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

		mLiveCard.publish(PublishMode.REVEAL);
		Log.e(TAG, "Done publishing LiveCard");    	
	}

	private final Handler mHandler = new Handler();  
	private final Runnable mUpdateProgressRunnable = new Runnable() {
		@Override
		public void run() {
			if (++percent_done > 100) {
				mRemoteViews.setTextViewText(R.id.hello_view, "DONE!");     	
				mRemoteViews.setViewVisibility(R.id.ex_progress_bar, View.INVISIBLE);
				mLiveCard.setViews(mRemoteViews);
				return;
			}

			updateLowFrequencyLiveCard();
			mHandler.postDelayed(mUpdateProgressRunnable, 100);
		}
	};	

	public void updateProgress() {
		mRemoteViews.setViewVisibility(R.id.ex_progress_bar, View.VISIBLE);		
		mHandler.postDelayed(mUpdateProgressRunnable, 4000);
	}

	public void updateLowFrequencyLiveCard() {
		mRemoteViews.setTextViewText(R.id.hello_view, ""+percent_done + "%");     	
		mRemoteViews.setProgressBar(R.id.ex_progress_bar, 100, percent_done, false);   
		if (mLiveCard != null)
			mLiveCard.setViews(mRemoteViews); // has to be called to update the card

	}


	private void createLowFrequencyLiveCard() {
		mLiveCard = new LiveCard(this, LIVE_CARD_ID);    	
		mRemoteViews = new RemoteViews(this.getPackageName(), R.layout.start);
		//mRemoteViews.setString(R.id.hello_view, "setText", "Chapter Three!!!");
		mRemoteViews.setTextViewText(R.id.hello_view, (new Date()).toString());
		mRemoteViews.setViewVisibility(R.id.ex_progress_bar, View.INVISIBLE);
		mLiveCard.setViews(mRemoteViews);
		addMenuToLiveCard();

		updateProgress();

	}


	private void createHighFrequencyLiveCardForLayoutInflating() {
		mLiveCard = new LiveCard(this, LIVE_CARD_ID);
		mCallback = new AppDrawer(this);
		mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback);    	
		addMenuToLiveCard();
	}


	private void createHighFrequencyLiveCardForCanvasDrawing() {
		mLiveCard = new LiveCard(this, LIVE_CARD_ID);
		mCallbackCanvas = new LiveCard2DRenderer();
		mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallbackCanvas);    	
		addMenuToLiveCard();
	}    

	// the only type that doesn't work
	private void createHighFrequencyLiveCardFor3DDrawing() {
		//    	mCallback3D = new MyGLView(this);
		mCallbackLivecard3D = new LiveCard3DRenderer(this);

		//    	mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
		mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallbackLivecard3D);
		mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback3D);
		addMenuToLiveCard();
	}    

	public LiveCard3DRenderer getLivecard3D () {
		return mCallbackLivecard3D;
	}

	private void createGlassStyledCard() {
		Intent intent = new Intent(this, GlassStyledCardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		startActivity(intent);
	}  

	private void createImmersionFor2DDrawing() {
		Intent intent = new Intent(this, Immersion2DActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this, app crashed - android.app.Service 
		// is descendant of android.app.Context so you can use startActivity directly. However since you start 
		// this outside any activity you need to set FLAG_ACTIVITY_NEW_TASK flag on the intent.
		startActivity(intent);
	}      


	private void createImmersionFor3DDrawing() {
		Intent intent = new Intent(this, Immersion3DActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this, app crashed - android.app.Service 
		// is descendant of android.app.Context so you can use startActivity directly. However since you start 
		// this outside any activity you need to set FLAG_ACTIVITY_NEW_TASK flag on the intent.
		startActivity(intent);
	}       

	@Override
	public void onCreate() {
		Log.e(TAG, "onCreate");
		super.onCreate();
		mAppService = this;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "onBind");
		return null;
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.e(TAG,  "onStartCommand");
		if (mLiveCard == null) {
			Log.e(TAG, "onStartCommand: true");


			// there are two ways of displaying content on a live card
			// this is #1: Inflating a layout using remote views
			// createLowFrequencyLiveCard();    	

			// the other way is Drawing directly on the live card surface, using SurfaceHolder.Callback

			// If your application requires more frequent updates (several times per second) or rendering more 
			// elaborate graphics than the standard widgets support, enable direct rendering and add a 
			// SurfaceHolder.Callback to the card's surface.

			//createGlassStyledCard();
			//createLowFrequencyLiveCard(); 
			createHighFrequencyLiveCardForLayoutInflating();
			//createHighFrequencyLiveCardForCanvasDrawing(); // use LiveCardRenderer
			//createImmersionFor2DDrawing();
			//createImmersionFor3DDrawing();
		} 


		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "OnDestroy()");

		if (mLiveCard != null && mLiveCard.isPublished()) {
			Log.e(TAG, "OnDestroy: true");
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