package com.morkout.smartcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;



public class OCRActivity extends Activity implements GestureDetector.OnGestureListener, Camera.OnZoomChangeListener, Runnable 
{
	public static String TAG = "OCRActivity";

	public static float FULL_DISTANCE = 8000.0f;

	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	private TextView mZoomLevelView;

	private GestureDetector mGestureDetector;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/SmartCamera/";

	public static final String lang = "eng";
	private String mPath;
	protected static final String PHOTO_TAKEN = "photo_taken";	


	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "SmartCamera");
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
	public void onCreate(Bundle savedInstanceState) 
	{
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);


		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}

		}

		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/" + lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();

				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}


		setContentView(R.layout.zoom);

		// as long as this window is visible to the user, keep the device's screen turned on and bright.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // WORKS ON GLASS!

		mPreview = (SurfaceView)findViewById(R.id.preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);

		mZoomLevelView = (TextView)findViewById(R.id.zoomLevel);

		// commented to test the press of Camera button during preview; otherwise long press would take the picture
		mGestureDetector = new GestureDetector(this, this);

		mCamera = Camera.open();
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
				Toast.makeText(OCRActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
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
				mCamera.setZoomChangeListener(this);

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) 
	{
		Log.v(TAG,  mCamera==null?"mCamere is null":"mCamera NOT null");
		if (mCamera==null || mPreviewHolder.getSurface() == null) return true;

		Camera.Parameters parameters = mCamera.getParameters();
		Log.v(TAG, "parameters.getMaxZoom="+ parameters.getMaxZoom()); // 60 on Glass

		int zoom = parameters.getZoom();

		if ( velocityX < 0.0f )
		{
			zoom -= 10;
			if ( zoom < 0 )
				zoom = 0;
		}
		else if ( velocityX > 0.0f )
		{
			zoom += 10;
			if ( zoom > parameters.getMaxZoom() )
				zoom = parameters.getMaxZoom();
		}

		// Applications can call stopSmoothZoom() to stop the zoom earlier. Applications should not call startSmoothZoom 
		// again or change the zoom value before zoom stops, or the app will crash! 
		mCamera.stopSmoothZoom();
		mCamera.startSmoothZoom(zoom);

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) 
	{
		// TODO Auto-generated method stub
		Log.v(TAG, "onLongPress");
		mCamera.takePicture(mShutterCallback, mPictureCallback,mjpeg);		
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

	@Override
	public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
		mZoomLevelView.setText("ZOOM: " + zoomValue);

	}


	Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub

		}

	};
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera c) {
			if(data !=null)
			{

			}
		}
	};

	Camera.PictureCallback mjpeg = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// copied from http://developer.android.com/guide/topics/media/camera.html#custom-camera
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null){
				Log.v(TAG, "Error creating media file, check storage permissions: ");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}

			Log.v(TAG, pictureFile.getAbsolutePath());
			mPath = pictureFile.getAbsolutePath();

			Thread thread = new Thread(OCRActivity.this);
			thread.start();

		}
	};

	public void run() {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(mPath, options);

		try {
			ExifInterface exif = new ExifInterface(mPath);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		// _image.setImageBitmap( bitmap );

		Log.v(TAG, "Before baseApi");

		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setImage(bitmap);

		String recognizedText = baseApi.getUTF8Text();

		baseApi.end();

		if ( lang.equalsIgnoreCase("eng") ) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}

		recognizedText = recognizedText.trim();

		if ( recognizedText.length() != 0 ) {
			Log.v(TAG, ">>>> " + recognizedText);

			Message msg = new Message();
			msg.obj = recognizedText;
			mHandler.sendMessage(msg); 
		}
	}        

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (String)msg.obj;
			Log.v(TAG, "handleMessage >>>> " + text);
			finish();
			Toast.makeText(OCRActivity.this, text, Toast.LENGTH_LONG).show();  
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


	@Override
	public void onResume() 
	{
		Log.v(TAG, "onResume");
		super.onResume();

	}

	@Override
	public void onPause() 
	{
		Log.v(TAG, "onPause");
		if ( mInPreview ) {
			mCamera.stopPreview();

			mCamera.release();
			mCamera = null;
			mInPreview = false;
		}
		super.onPause();
	}    


}