package com.morkout.smartcamera;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
	String shareTo = "wechat";

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
		Mail m = new Mail("<youraddress>@gmail.com", "<yourpassword>"); 

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
		m.setFrom("<youraddress>@gmail.com"); 
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


	// Async task class to make HTTP get request 
	private class SharingTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {			
			try {
				//sendPostRequest(mPictureFilePath);
				String uploadedFilename = uploadFile(mPictureFilePath);				
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet("http://www.morkout.com/iapps/social/notif.php?shareto=" + shareTo + "&uname=DyzXtz&imagename="+ URLEncoder.encode(uploadedFilename, "UTF-8"));
				HttpResponse httpResponse = httpClient.execute(httpGet);	 
				HttpEntity httpEntity = httpResponse.getEntity();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}			
			return null;
		}		

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}


	}

	public String uploadFile(File sourceFile) {
		int serverResponseCode = 0;        
		String uploadedFilename = null; //"http://www.morkout.com/iapps/social/uploads/";

		HttpURLConnection conn = null;
		DataOutputStream dos = null;  
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024; 

		if (!sourceFile.isFile()) {

			Log.e("uploadFile", "Source File not exist :"
					+sourceFile.getAbsolutePath());

			return null;

		}
		else
		{
			try { 
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL("http://www.morkout.com/iapps/social/glassupload.php?shareapp=" + shareTo);

				// Open a HTTP  connection to  the URL
				conn = (HttpURLConnection) url.openConnection(); 
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("Filedata", sourceFile.getName()); 

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd); 
				dos.writeBytes("Content-Disposition: form-data; name=Filedata;filename="+ sourceFile.getName()  + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of  maximum size
				bytesAvailable = fileInputStream.available(); 

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);  

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);   

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				if(serverResponseCode == 200){
					Log.v(TAG, "File Upload Completed.");
				}

				InputStream is = conn.getInputStream();
				int ch;

				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}

				uploadedFilename = b.toString();
				Log.v(TAG, "uploaded file at http://www.morkout.com/iapps/social/uploads/" + b.toString());     			
				is.close();

				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				ex.printStackTrace();

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
				return null;
			} catch (Exception e) {

				Log.e("Upload file to server Exception", "Exception : "
						+ e.getMessage(), e);
				return null;
			}
			return uploadedFilename; 

		} // End else block 
	} 


	//	private static String sendPostRequest(File filename) {
	//		String response = null;
	//
	//		Log.d("SendPostRequest", "filename="+filename.getAbsolutePath());
	//		@SuppressWarnings("rawtypes")
	//		List<PostParameter> params = new ArrayList<PostParameter>();
	//		params.add(new PostParameter<File>("Filedata", filename));
	//
	//		try {
	//			Log.d("INSTANT", "multipart post created");
	//			MultipartPost post = new MultipartPost(params);
	//			Log.d("INSTANT", "multipart post created");
	//			response = post.send("http://www.morkout.com/iapps/social/glassupload.php?shareapp=wechat", "http://www.morkout.com");
	//			Log.v(TAG, "response="+response);
	//
	//		} catch (Exception e) {
	//			Log.e("INSTANT", "Bad Post", e);
	//		}
	//
	//		params.clear();
	//		params = null;
	//		return response;
	//	} 




	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.wechat:
			shareTo = "wechat";
			new SharingTask().execute();	

			return true;
		case R.id.whatsapp:
			shareTo = "whatsapp";
			new SharingTask().execute();	

			return true;				
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
