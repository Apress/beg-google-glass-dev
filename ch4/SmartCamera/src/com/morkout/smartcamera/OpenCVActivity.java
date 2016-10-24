package com.morkout.smartcamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.glass.app.Card;

public class OpenCVActivity extends Activity implements GestureDetector.OnGestureListener
{
	public static String TAG = "OpenCVActivity";
	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	private boolean mOpenCVProcessed = false;

	private GestureDetector mGestureDetector;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	File mCapturedFilePath;

	// code copied from http://developer.android.com/guide/topics/media/camera.html
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    	Log.e(TAG, "Camera is not available");
	    }
	    return c; // returns null if camera is unavailable
	}		
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.preview);

		if (!OpenCVLoader.initDebug()) {	    	
			finish();
		}		

		// as long as this window is visible to the user, keep the device's screen turned on and bright.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // WORKS ON GLASS!

		mPreview = (SurfaceView)findViewById(R.id.preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);

		// commented to test the press of Camera button during preview; otherwise long press would take the picture
		mGestureDetector = new GestureDetector(this, this);

		mCamera = getCameraInstance();
		if (mCamera != null)
			startPreview();
	}



	private void initPreview(int width, int height) 
	{
		if ( mCamera != null && mPreviewHolder.getSurface() != null) {
			try 
			{
				mCamera.setPreviewDisplay(mPreviewHolder);
			}
			catch (Throwable t) 
			{
				Log.e(TAG, "Exception in initPreview()", t);
				Toast.makeText(OpenCVActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if ( !mCameraConfigured ) 
			{
				Camera.Parameters parameters = mCamera.getParameters();

				//				parameters.setPreviewSize(240, 160); 
				parameters.setPreviewFpsRange(30000, 30000);
				parameters.setPreviewSize(640, 360);

				//parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);           
//				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				parameters.setJpegQuality(100);                

				mCamera.setParameters(parameters);

				mCameraConfigured = true;
			}
		}
	}



	private void startPreview() 
	{
		if ( mCameraConfigured && mCamera != null ) 
		{
			mCamera.startPreview();
			mInPreview = true;
		}
	}



	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated( SurfaceHolder holder ) 
		{
			// nothing
			Log.v(TAG, "surfaceCreated");
		}

		public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) 
		{
			Log.v(TAG, "surfaceChanged="+width+","+height);
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed( SurfaceHolder holder ) 
		{
			Log.v(TAG, "surfaceDestroyed");
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	};

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) 
	{
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) 
	{
		Log.v(TAG, "onDown");
		// TODO Auto-generated method stub
		if (mOpenCVProcessed) finish();
		return false;
	}

	@Override
	public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) 
	{
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		Log.v(TAG, "onLongPress");
		mCamera.takePicture(null, null, mPictureCallback);		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	{
		//Log.d(TAG, "distanceX: " + distanceX + ", distanceY: " + distanceY);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public static Bitmap decodeSampledBitmapFromData(byte[] data, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length,options);
		options.inSampleSize = 2; // saved image will be one half the width and height of the original (image captured is double the resolution of the screen size)
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length,options);
	}	

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			String mDataPath = Environment.getExternalStorageDirectory().toString() + "/SmartCamera/";

			File dir = new File(mDataPath);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "Creation of directory " + mDataPath + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + mDataPath + " on sdcard");
				}
			}
			
			//decode the data obtained by the camera into a Bitmap
			Bitmap bmp = decodeSampledBitmapFromData(data,640,360);
			try{
				FileOutputStream fos = new FileOutputStream(mDataPath + "captured.jpg");
				final BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 8);
				bmp.compress(CompressFormat.JPEG, 100, bos);
				bos.flush();
				bos.close();
				fos.close();
			} catch (FileNotFoundException e){
				Log.v(TAG, e.getMessage());
			} catch (IOException e){
				Log.v(TAG, e.getMessage());
			}			


			mCapturedFilePath = new File(mDataPath + "captured.jpg");
			if (mCapturedFilePath.exists()) {
				// OpenCV Canny Image Processing
				Mat matFrom = Highgui.imread(mCapturedFilePath.getAbsolutePath());

				Size sizeRgba = matFrom.size();
				Mat matFinal;
				Mat matIntermediate = new Mat();
				
				int rows = (int) sizeRgba.height;
				int cols = (int) sizeRgba.width;
				int left = cols / 8;
				int top = rows / 8;
				int width = cols * 3 / 4;
				int height = rows * 3 / 4;
				
				matFinal = matFrom.submat(top, top + height, left, left + width);

				Imgproc.Canny(matFinal, matIntermediate, 80, 90);
				Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_GRAY2BGRA, 4);
				Highgui.imwrite(mDataPath + "captured-canny.jpg", matFinal);

				matFinal.release();     	            

				Card card = new Card(AppService.appService());
				card.setImageLayout(Card.ImageLayout.FULL);
				card.addImage( BitmapFactory.decodeFile(mDataPath + "captured-canny.jpg"));
				card.setFootnote("OpenCV-processed Image");
				setContentView(card.getView());
				mOpenCVProcessed = true;
				
			}            
		}        
	};

	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.v(TAG,  "onKeyDown");
		if (keyCode == KeyEvent.KEYCODE_CAMERA) { // for both quick press (image capture) and long press (video capture)
			Log.v(TAG,  "KEYCODE_CAMERA: "+ (event.isLongPress()?"long press": "not long press"));

			if (event.isLongPress()) // video capture 
				return true; // If you return true from onKeyDown(), your activity consumes the event and the Glass camera 
			// doesn't start. Do this only if there is no way to interrupt your activity's use of the camera (for example, 
			// if you are capturing continuous video).

			// Stop the preview and release the camera.
			// Execute your logic as quickly as possible
			// so the capture happens quickly.


			return false;


		} else {
			Log.v(TAG,  "NOT KEYCODE_CAMERA");

			return super.onKeyDown(keyCode, event);
		}
	}


	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();

		// Re-acquire the camera and start the preview.
		if (mCamera == null) {
			mCamera = getCameraInstance();
			if (mCamera != null) {
				Log.v(TAG, "mCamera!=null");
				initPreview(640, 360);
				startPreview();
			}	
			else
				Log.v(TAG, "mCamera==null");
		}
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause");
		if ( mInPreview && mCamera != null) {
			Log.v(TAG,  "mInPreview is true");
			mCamera.stopPreview();

			mCamera.release();
			mCamera = null;
			mInPreview = false;
		}
		super.onPause();
	}    

	

}