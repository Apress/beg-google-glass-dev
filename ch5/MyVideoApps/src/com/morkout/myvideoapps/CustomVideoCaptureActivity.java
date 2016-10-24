package com.morkout.myvideoapps;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class CustomVideoCaptureActivity extends Activity implements GestureDetector.OnGestureListener//, Camera.PreviewCallback
{
	public static String TAG = "CustomVideoCaptureActivity";

	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	MediaRecorder mrec;
	File mOutputFile;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private boolean mRecording = false;
	private TextView mTimerInfo;
	int second;

	private GestureDetector mGestureDetector;	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.preview);

		// as long as this window is visible to the user, keep the device's screen turned on and bright.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		mTimerInfo = (TextView)findViewById(R.id.timer);
		mTimerInfo.setText("Long Press to Record");
		mPreview = (SurfaceView)findViewById(R.id.preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);

		mGestureDetector = new GestureDetector(this, this);

		mCamera = getCameraInstance();
	}

	private void initPreview() {    		
		if ( mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
			}
			catch (IOException e) {
				Toast.makeText(CustomVideoCaptureActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();			
			}

			if ( !mCameraConfigured ) {
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setPreviewFpsRange(30000, 30000);
				parameters.setPreviewSize(640, 360);
				mCamera.setParameters(parameters);

				mCameraConfigured = true;
			}
		}
	}

	private void startPreview() {
		if ( mCameraConfigured && mCamera != null ) {
			mCamera.startPreview();
			mInPreview = true;
		}
	}


	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated( SurfaceHolder holder ) {
			Log.v(TAG, "surfaceCreated");
			initPreview();
		}

		public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
			Log.v(TAG, "surfaceChanged="+width+","+height);
			startPreview();
		}

		public void surfaceDestroyed( SurfaceHolder holder ) {
			Log.v(TAG, "surfaceDestroyed");
			releaseCamera();
		}
	};


	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();

		// Re-acquire the camera and start the preview.
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) 
	{
		Log.v(TAG, "onGenericMotionEvent");
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		Log.v(TAG, "Down");
		return false;
	}

	@Override
	public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) 
	{
		Log.v(TAG,  mCamera==null?"onFling:mCamere is null":"onFling:mCamera NOT null");

		return false;
	}


	private final Handler mHandler = new Handler();  
	private final Runnable mUpdateTextRunnable = new Runnable() {
		@Override
		public void run() {
			if (mRecording) {
				mTimerInfo.setText(String.format("%02d:%02d:%02d", 
						TimeUnit.MILLISECONDS.toHours(second*1000),
						TimeUnit.MILLISECONDS.toMinutes(second*1000) -  
						TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(second*1000)), 
						TimeUnit.MILLISECONDS.toSeconds(second*1000) - 
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(second*1000))));
				second++;
			}
			mHandler.postDelayed(mUpdateTextRunnable, 1000);
		}
	};    


	@Override
	public void onLongPress(MotionEvent e) 
	{		
		Log.v(TAG, "onLongPress");
		if (mRecording)
			return;

		if (prepareVideoRecorder()) {
			Log.v(TAG, "before start");
			// Camera is available and unlocked, MediaRecorder is prepared,
			// now you can start recording
			//Toast.makeText(CustomVideoCaptureActivity.this, "Recording...", Toast.LENGTH_LONG).show();
			mrec.start();
			mRecording = true;
			second = 0;
			//			new Thread() {
			//				public void run() {
			//					while (mRecording) {
			//						try {
			//							runOnUiThread(new Runnable() {
			//
			//								@Override
			//								public void run() {
			//									Log.v(TAG, "second="+second);
			//
			//									mTimerInfo.setText(String.format("%02d:%02d:%02d", 
			//											TimeUnit.MILLISECONDS.toHours(second*1000),
			//											TimeUnit.MILLISECONDS.toMinutes(second*1000) -  
			//											TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(second*1000)), 
			//											TimeUnit.MILLISECONDS.toSeconds(second*1000) - 
			//											TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(second*1000))));
			//									second++;
			//								}
			//							});
			//							Thread.sleep(1000);							
			//						} catch (InterruptedException e) {
			//							e.printStackTrace();
			//						}
			//					}
			//				}
			//			}.start();

			// TODO: test the handler post above

			mHandler.post(mUpdateTextRunnable);

		} else {
			Log.v(TAG, "before releaseMediaRecorder");
			// prepare didn't work, release the camera
			releaseMediaRecorder();
			// inform user
		}		

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	{
		Log.v(TAG, "onScroll");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		Log.v(TAG, "onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		Log.v(TAG, "onSingleTapUp");
		if (mRecording)
			openOptionsMenu();
		else 
			finish();
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.videocapture, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.stoprecording:
			stopAndPlayVideo();
		}
		return true;
	}

	void stopAndPlayVideo() {
		stopRecording();
		mTimerInfo.setText("Recording Done");
		Toast.makeText(CustomVideoCaptureActivity.this, "Video saved to " + mOutputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

		//		Intent intent = new Intent(this, VideoPlayerActivity.class);
		//		intent.putExtra("filepath", mOutputFile.getAbsolutePath());
		//		startActivity(intent); 

		Intent i = new Intent();
		i.setAction("com.google.glass.action.VIDEOPLAYER");
		i.putExtra("video_url", mOutputFile.getAbsolutePath());
		startActivity(i);		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.v(TAG,  "onKeyDown");
		if (keyCode == KeyEvent.KEYCODE_CAMERA) { // for both quick press (image capture) and long press (video capture)
			Log.v(TAG,  "KEYCODE_CAMERA: "+ (event.isLongPress()?"long press": "not long press"));   	
			if ( mInPreview ) {
				mCamera.stopPreview();

				mCamera.release();
				mCamera = null;
				mInPreview = false;
			}

			return false;


		} else {
			Log.v(TAG,  "NOT KEYCODE_CAMERA");

			return super.onKeyDown(keyCode, event);
		}
	}    

	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist);
			Log.e(TAG, e.getMessage());
		}
		return c; // returns null if camera is unavailable
	}	

	private boolean prepareVideoRecorder(){

		if (mCamera != null){
			mCamera.release();        // release the camera for other applications
		}		
		mCamera = getCameraInstance();
		if (mCamera == null) return false;
		
		mrec = new MediaRecorder();

		mCamera.unlock();

		mrec.setCamera(mCamera);    
		mrec.setAudioSource(MediaRecorder.AudioSource.CAMCORDER); 
		mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// http://developer.android.com/guide/topics/media/camera.html
		// Set the video output format and encoding. For Android 2.2 (API Level 8) and higher, use the MediaRecorder.setProfile 
		// method, and get a profile instance using CamcorderProfile.get(). For versions of Android prior to 2.2, you must 
		// set the video output format and encoding parameters:
		// setOutputFormat() - Set the output format, specify the default setting or MediaRecorder.OutputFormat.MPEG_4.
		// setAudioEncoder() - Set the sound encoding type, specify the default setting or MediaRecorder.AudioEncoder.AMR_NB.
		// setVideoEncoder() - Set the video encoding type, specify the default setting or MediaRecorder.VideoEncoder.MPEG_4_SP.

		mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

		Log.v(TAG, "QUALITY_LOW framerate="+CamcorderProfile.get(CamcorderProfile.QUALITY_LOW).videoFrameRate);
		Log.v(TAG, "QUALITY_HIGH framerate="+CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH).videoFrameRate);
		Log.v(TAG, "QUALITY_480P framerate="+CamcorderProfile.get(CamcorderProfile.QUALITY_480P).videoFrameRate);

		mrec.setPreviewDisplay(mPreviewHolder.getSurface());

		mOutputFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
		mrec.setOutputFile(mOutputFile.toString());

		try {
			mrec.prepare();
		}
		catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}

		return true;
	}	

	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}	

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		super.onPause();
		releaseMediaRecorder();       // if you are using MediaRecorder, release it first
		releaseCamera();              // release the camera immediately on pause event
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		if (mRecording) 
			stopAndPlayVideo();
		mRecording = false;

		super.onDestroy();
	}
	private void releaseMediaRecorder(){
		Log.v(TAG, "releaseMediaRecorder");
		if (mrec != null) {
			mrec.reset();   
			mrec.release(); 
			mrec = null;
		}
	}

	private void releaseCamera(){
		Log.v(TAG, "releaseCamera");
		if (mCamera != null){
			mCamera.release();
			mCamera = null;
		}
	}	

	protected void stopRecording() {

		if(mrec!=null)
		{
			mrec.stop();
			mrec.release();
			mrec = null;
			releaseCamera();
			mRecording = false;
		}
	}
}