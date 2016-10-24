package com.morkout.smartcamerabasic;


import java.io.File;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class ImageViewActivity extends Activity implements Runnable { 
	public static String TAG = "ImageViewActivity";
	ImageView mImageview;
	private GestureDetector mGestureDetector;
	File mPictureFilePath;
	String mEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.imageview);
		Bundle extras = getIntent().getExtras();
		mPictureFilePath = (File)extras.get("picturefilepath");

		Log.v(TAG, "pictureFilePath=" + mPictureFilePath.getAbsolutePath());
		mImageview =  (ImageView) findViewById(R.id.picture);

		// need to scale down the image to avoid the error of loading a bitmap too big
		Bitmap myBitmap = BitmapFactory.decodeFile(mPictureFilePath.getAbsolutePath());
		int h = (int) ( myBitmap.getHeight() * (640.0 / myBitmap.getWidth()) );

		Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 640, h, true);
		mImageview.setImageBitmap(scaled);        

		mGestureDetector = new GestureDetector(this);

		mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					Log.v(TAG, "TAP");
					openOptionsMenu();

					return true;
				} 

				return false;
			}
		});
	}

	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}        
	


	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (String)msg.obj;
			Toast.makeText(ImageViewActivity.this, text, Toast.LENGTH_SHORT).show();            
		}
	};

	public void run() {
//		Mail m = new Mail("<youraddress>@gmail.com", "<yourpassword>");
		Mail m = new Mail("jeff.x.tang@gmail.com", "Daqi0530");

		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(AppService.appService()).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				mEmail = account.name;
				Log.v(TAG, "mEmail:"+ mEmail);
			}
		}		

		String[] toArr = {mEmail}; 
		m.setTo(toArr); 
//		m.setFrom("<youraddress>@gmail.com"); 
		m.setFrom("jeff.x.tang@gmail.com"); 
		m.setSubject("Picture taken with Smart Camera"); 
		m.setBody("To get the app for your Glass, go to https://github.com/xjefftang/smartcamera"); 

		try { 
			m.addAttachment(mPictureFilePath.getAbsolutePath()); 

			if(m.send()) { 
				Message msg = new Message();
				msg.obj = "Email sent successfully.";
				mHandler.sendMessage(msg); 
			} else { 
				Message msg = new Message();
				msg.obj = "Email not sent.";
				mHandler.sendMessage(msg); 
			} 
		} catch(Exception e) { 
			Log.e("MailApp", "Could not send email", e); 
		} 
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.imageview, menu);

		return true;
	} 



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {				
		case R.id.upload:
			Uri imgUri = Uri.parse("file://" + mPictureFilePath.getAbsolutePath());
			Intent shareIntent = ShareCompat.IntentBuilder.from(this)
					.setText("Share image taken by Glass")
					.setType("image/jpeg")
					.setStream(imgUri )
					.getIntent()
					.setPackage("com.google.android.apps.docs");

			startActivity(shareIntent);			

			return true;

		case R.id.email:
			Toast.makeText(ImageViewActivity.this, "Sending email...", Toast.LENGTH_LONG).show();
			// has to send network activity in the background, not the main thread, or app exception!	
			Thread thread = new Thread(ImageViewActivity.this);
			thread.start();

			return true;

		case R.id.delete:
			mPictureFilePath.delete();
			Toast.makeText(ImageViewActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
			finish();
			return true;


		default:
			return super.onOptionsItemSelected(item);
		}
	}    	      

}
