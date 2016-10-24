package com.morkout.myvideoapps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.glass.media.CameraManager;


public class BasicVideoCaptureActivity extends Activity {
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 100;
	private static final int VIDEO_PLAY_REQUEST_CODE = 200;
	private static final String TAG = "BasicVideoCaptureActivity";

	private Uri mFileUri;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_MOVIES), "MyVideoApps");
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
			mediaFile = new File(mediaStorageDir.getPath(), "IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath(), "VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		mFileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  // create a file to save the video

		Log.v(TAG,  "fileUri="+mFileUri);
		// This setting requires a Uri object specifying a path and file name where you'd like to save the picture or movie. 
		// This setting is optional but strongly recommended. If you do not specify this value, the camera application 
		// saves the requested picture in the default location with a default name, specified in the returned intent's Intent.getData() field.

		intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);  
		// BUT on Glass this doesn't save the video to /mnt/sdcard/Movies/MyVideoApps/VID_20140204_230644.mp4: 
		// the video still is saved in /mnt/sdcard/DCIM/Camera/20140204_230644_477.mp4

		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high

		// start the Video Capture Intent
		startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
	}

	// intercept the callback from a camera intent so your application can do further processing of the captured image or video
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VIDEO_PLAY_REQUEST_CODE) { 
			finish();
			return;
		}
		
//		Log.v(TAG, "onActivityResult, requestCode=" + requestCode + ", resultCode="+ resultCode + ",OK="+RESULT_OK+",CANCEL="+RESULT_CANCELED);
//
//		Log.v(TAG, "getData:"+data.getData()); // null if with or without intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri). so don't use this for file path
//

		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Video captured and saved to fileUri specified in the Intent

				Toast.makeText(this, "Video saved to:\n" + data.getExtras().get(CameraManager.EXTRA_VIDEO_FILE_PATH), Toast.LENGTH_LONG).show();

				//Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_LONG).show(); 
				// null, if without intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  

				Intent i = new Intent(this, VideoPlayerActivity.class);
//
//
//				if (data.getExtras() == null)
//					Log.v(TAG,  "data.getExtras() is null");
//				else {    	// if intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  is called before startActivityForResult
//					// then code reaches here and need to use data.getExtras().get(CameraManager.EXTRA_VIDEO_FILE_PATH) to get saved file path
//					// which is actually the same as data.getData() if intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri) is not called 
//					for (String key : data.getExtras().keySet()) {
//						Object value = data.getExtras().get(key);
//						Log.v(TAG, String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
//					}
//				}
//				
//				
				i.putExtra("filepath", ""+data.getExtras().get(CameraManager.EXTRA_VIDEO_FILE_PATH));
				startActivityForResult(i, VIDEO_PLAY_REQUEST_CODE);
				
//				Intent i = new Intent();
//				i.setAction("com.google.glass.action.VIDEOPLAYER");
//				i.putExtra("video_url", ""+data.getExtras().get(CameraManager.EXTRA_VIDEO_FILE_PATH));
//				startActivityForResult(i, VIDEO_PLAY_REQUEST_CODE);				
				

			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the video capture
				Toast.makeText(this, "Video capture cancelled", Toast.LENGTH_LONG).show();
			} else {
				// Video capture failed, advise user
				Log.v(TAG, "capture failed");
			}
		}

	}


}
