package com.morkout.smartcamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
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
import android.widget.Toast;

import com.google.android.glass.app.Card;

public class SearchActivity extends Activity implements GestureDetector.OnGestureListener, Runnable
{
	public static String TAG = "SearchActivity";

	public static float FULL_DISTANCE = 8000.0f;

	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;

	private GestureDetector mGestureDetector;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	File mCapturedFilePath;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.preview);

		// as long as this window is visible to the user, keep the device's screen turned on and bright.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // WORKS ON GLASS!

		mPreview = (SurfaceView)findViewById(R.id.preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);

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
				Toast.makeText(SearchActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
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
//				parameters.setJpegQuality(100);                

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
		// TODO Auto-generated method stub
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

	public static Bitmap decodeSampledBitmapFromData(byte[] data,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length,options);
		options.inSampleSize = 2; // saved image will be one half the width and height of the original (image captured is double the resolution of the screen size)
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length,options);
	}	

	Camera.PictureCallback mjpeg = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			String mDataPath = Environment.getExternalStorageDirectory().toString() + "/SmartCamera/";

			File dir = new File(mDataPath);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + mDataPath + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + mDataPath + " on sdcard");
				}
			}


			//decode the data obtained by the camera into a Bitmap
			Bitmap bmp = decodeSampledBitmapFromData(data,640,360);

			Log.v(TAG,"bmp width=" + bmp.getWidth() + " height=" + bmp.getHeight());
			try{
				FileOutputStream fos = new FileOutputStream(mDataPath + "captured.jpg");
				final BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 8);
				bmp.compress(CompressFormat.JPEG, 100, bos);
				bos.close();
				fos.close();
			} catch (FileNotFoundException e){
				Log.v(TAG, e.getMessage());
			} catch (IOException e){
				Log.v(TAG, e.getMessage());
			}			


			mCapturedFilePath = new File(mDataPath + "captured.jpg");
			if (mCapturedFilePath.exists()) {
				//performOnBackgroundThreadSingleFile(AppService.appService(), f);

				Thread thread = new Thread(SearchActivity.this);
				thread.start();

				Card card = new Card(AppService.appService());
				card.setImageLayout(Card.ImageLayout.FULL);
				card.addImage( BitmapFactory.decodeFile(mCapturedFilePath.getAbsolutePath()));
				card.setFootnote("processing image...");
				setContentView(card.getView());



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
//		if ( mInPreview ) {
//			mCamera.stopPreview();
//
//			mCamera.release();
//			mCamera = null;
//			mInPreview = false;
//		}
		super.onPause();
	}    



	public void run() {
		String new_result="";
		try {
			Log.d("Inside thread", "");
			new_result = getResult(mCapturedFilePath);
			Log.d("Stuff", "Result is: " + new_result);                

		} finally {
			Message msg = new Message();
			msg.obj = new Result(new_result, mCapturedFilePath);
			mHandler.sendMessage(msg); 
		}
	}


	static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Result result = (Result)msg.obj;
			Log.v(TAG, "handleMessage >>>> " + result.mText);
			Toast.makeText(AppService.appService(), result.mText, Toast.LENGTH_LONG).show();  

			updateResultsInUi(AppService.appService(), result.mText, result.mImage);

		}
	};	

	private class Result {
		private String mText;
		private File mImage;

		public Result(String text, File image)
		{
			mText = text;
			mImage = image;
		}
	}

	public static void updateResultsInUi(Context context, String results, File image){

		Card card = new Card(context);
		card.setImageLayout(Card.ImageLayout.FULL);
		card.addImage(BitmapFactory.decodeFile(image.getAbsolutePath()));

		if(results!=null){
			card.setFootnote("Image Info: " + results);
			Toast.makeText(AppService.appService(), results, Toast.LENGTH_LONG).show();
		} else {
			card.setFootnote("No results found.");
			Toast.makeText(AppService.appService(), "No results found.", Toast.LENGTH_LONG).show();
		}

		//AppService.appService().timelineManager().insert(card);
	}


	private static String sendPostRequest(File filename) {
		String response = null;

		Log.d("SendPostRequest", "sendPostRequest");
		@SuppressWarnings("rawtypes")
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter<String>("image_url", ""));
		params.add(new PostParameter<String>("btnG", "Search"));
		params.add(new PostParameter<String>("image_content", ""));
		params.add(new PostParameter<String>("filename", ""));
		params.add(new PostParameter<String>("hl", "en"));
		params.add(new PostParameter<String>("safe", "off"));
		params.add(new PostParameter<String>("bih", ""));
		params.add(new PostParameter<String>("biw", ""));
		params.add(new PostParameter<File>("encoded_image", filename));

		try {
			Log.d("INSTANT", "multipart post created");
			MultipartPost post = new MultipartPost(params);
			Log.d("INSTANT", "multipart post created");
			response = post.send("http://www.google.com/searchbyimage/upload", "http://images.google.com");

		} catch (Exception e) {
			Log.e("INSTANT", "Bad Post", e);
		}

		params.clear();
		params = null;
		return response;
	} 

	public static String getResult(File file){
		String tempresult = null;
		String result2 = sendPostRequest(file);
		boolean best_guess_found=false;

		StringTokenizer tok = new StringTokenizer(result2, "<>");
		String previous_entity=null;

		while(tok.hasMoreElements()){
			String nextitem = tok.nextElement().toString();

			if (best_guess_found==false && nextitem.startsWith("Best guess for this image")){
				Log.d("Tokenizer", nextitem);
				best_guess_found=true;
			} else if (best_guess_found==true && nextitem.contains("q=") && nextitem.contains("&amp")){
				int start = nextitem.indexOf("q=")+2;
				int end = nextitem.indexOf("&amp", start);
				String contents = previous_entity.substring( start , end);
				contents = contents.replace('+', ' ');
				Log.d("Result:", contents);

				tempresult = contents;
				break;
			} else if(nextitem.startsWith("Visually similar") && best_guess_found==false){
				Log.d("Tokenizer", "nextitem: " + nextitem + " previousitem: " + previous_entity);
				try{
					if(previous_entity.contains("q=") && previous_entity.contains("&amp")){
						int start = previous_entity.indexOf("q=")+2;
						int end = previous_entity.indexOf("&amp", start);
						String contents = previous_entity.substring( start , end);
						contents = contents.replace('+', ' ');

						Log.d("Result:", contents);

						tempresult = contents;
					} else {


					}
				} catch (Exception e){
					e.printStackTrace();						
				}
				break;
			}

			if(nextitem.startsWith("a")){
				StringTokenizer tok2 = new StringTokenizer(nextitem);

				while(tok2.hasMoreElements()){
					String subitem = tok2.nextElement().toString();
					if( subitem.startsWith("href") ){
						previous_entity=nextitem;
					}
				}
			}
		}

		return tempresult;
	}


}