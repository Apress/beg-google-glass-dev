package com.morkout.myvideoapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.netcompss.ffmpeg4android_client.BaseWizard;

public class VideoProcessingActivity extends BaseWizard {
	private GestureDetector mGestureDetector;
	private final String PATH = Environment.getExternalStorageDirectory()
			.toString() + "/myvideoapps/";
	private static final String TAG = "VideoProcessingActivity";
	private String mOriginal = "in.mp4";
	private String mProcessed = null;
	TextView mTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		mTextView = (TextView) findViewById(R.id.hello_view);
		mTextView.setText("Tap for Menu");

		copyAsset(mOriginal);

		setWorkingFolder(PATH);
		copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();

		mGestureDetector = new GestureDetector(this);

		mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				Log.v(">>>>", "onGesture");
				if (gesture == Gesture.TAP) {
					openOptionsMenu();
				}
				return true;
			}
		});
	}

	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.videoprocessing, menu);

		return true;
	}

	String copyAsset(String filename) {
		final String PATH = Environment.getExternalStorageDirectory()
				.toString() + "/myvideoapps/";
		File dir = new File(PATH);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Log.v(TAG, "ERROR: Creation of directory " + PATH
						+ " on sdcard failed");
				return null;
			} else {
				Log.v(TAG, "Created directory " + PATH + " on sdcard");
			}
		}

		if (!(new File(PATH + filename).exists())) {
			Log.v(TAG, "copying file " + filename);
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open(filename);
				OutputStream out = new FileOutputStream(PATH + filename);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + filename + e.toString());
				return null;
			}
		}
		return PATH + filename;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.original:
			Intent i = new Intent();
			i.setAction("com.google.glass.action.VIDEOPLAYER");
			i.putExtra("video_url", PATH + mOriginal);
			startActivity(i);
			return true;

		case R.id.processed:
			if (mProcessed == null)
				Toast.makeText(VideoProcessingActivity.this,
						"No video or image just processed yet", Toast.LENGTH_LONG)
						.show();
			else if (mProcessed.equalsIgnoreCase("image_extracted")) {
				mTextView.setText("");
				Intent intent = new Intent(this, PhotoViewActivity.class);
				startActivity(intent);
				finish();
			}
			else {				
				Intent i2 = new Intent();
				i2.setAction("com.google.glass.action.VIDEOPLAYER");
				i2.putExtra("video_url", PATH + mProcessed);
				startActivity(i2);
			}
			return true;

		case R.id.sepia:
			mProcessed = "m-sepia.mp4";
			setCommand("ffmpeg -y -i /sdcard/myvideoapps/in.mp4 -strict experimental -vf colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131 -s 640x480 -r 30 -aspect 4:3 -ab 48000 -ac 2 -ar 22050 -b 2097k -vcodec mpeg4 /sdcard/myvideoapps/m-sepia.mp4");
			setProgressDialogTitle("Adding Sepia Effect...");
			setProgressDialogMessage("After completed, tap to see more options");
			runTranscoing();
			return true;

		case R.id.vintage:
			mProcessed = "m-vintage.mp4";
			setCommand("ffmpeg -y -i /sdcard/myvideoapps/in.mp4 -strict experimental -vf curves=vintage -s 640x480 -r 30 -aspect 4:3 -ab 48000 -ac 2 -ar 22050 -b 2097k -vcodec mpeg4 /sdcard/myvideoapps/m-vintage.mp4");
			setProgressDialogTitle("Adding Vintage Effect...");
			setProgressDialogMessage("After completed, tap to see more options");
			runTranscoing();
			return true;

		case R.id.canny:
			mProcessed = "m-canny.mp4";
			setCommand("ffmpeg -y -i /sdcard/myvideoapps/in.mp4 -strict experimental -vf edgedetect=low=0.1:high=0.2 -s 640x480 -r 30 -aspect 4:3 -ab 48000 -ac 2 -ar 22050 -b 2097k -vcodec mpeg4 /sdcard/myvideoapps/m-canny.mp4");
			setProgressDialogTitle("Adding Canny Effect...");
			setProgressDialogMessage("After completed, tap to see more options");
			runTranscoing();
			return true;

		case R.id.rotate:
			mProcessed = "m-rotate.mp4";
			setCommand("ffmpeg -y -i /sdcard/myvideoapps/in.mp4 -strict experimental -vf transpose=1 -s 160x120 -r 30 -aspect 4:3 -ab 48000 -ac 2 -ar 22050 -b 2097k /sdcard/myvideoapps/m-rotate.mp4");
			setProgressDialogTitle("Rotating Video...");
			setProgressDialogMessage("After completed, tap to see more options");
			runTranscoing();
			return true;

		case R.id.extractsound:
			mProcessed = "m-sound.mp3";
			setCommand("ffmpeg -i /sdcard/myvideoapps/in.mp4 -vn -ar 44100 -ac 2 -ab 192 -f mp3 /sdcard/myvideoapps/m-sound.mp3");
			setProgressDialogTitle("Sound Extracting...");
			setProgressDialogMessage("After completed, tap to see more options");
			runTranscoing();
			return true;

		case R.id.extractimages:
			mProcessed = "image_extracted";
			setCommand("ffmpeg -i /sdcard/myvideoapps/in.mp4 /sdcard/myvideoapps/in-image%d.jpg");
			setProgressDialogTitle("Image Extracting...");
			setProgressDialogMessage("After completed, tap to see more options");
			runTranscoing();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
