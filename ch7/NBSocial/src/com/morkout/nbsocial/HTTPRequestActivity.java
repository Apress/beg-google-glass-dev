package com.morkout.nbsocial;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;


public class HTTPRequestActivity extends Activity {
	public final static String TAG = "HTTPRequestActivity";
	TextView mTvInfo;
	String mWhat;
	HttpURLConnection mUrlConnection;
	String mResult;

	protected void onResume () {
		super.onResume();		
	}

	protected void onPause () {
		super.onPause();
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);

		Intent intent = getIntent();
		mWhat = intent.getStringExtra("WHAT");
		Log.i(TAG,  "WHAT="+mWhat);

		mTvInfo.setText("Making HTTP "+ mWhat + " request...");

		new HTTPRequest().execute();
	}

	// Async task class to make HTTP Get, Post and upload 
	private class HTTPRequest extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {			
			try {
				if (mWhat.equalsIgnoreCase("GET")) {
					Log.w(TAG, "GET");					

					// get json via YouTube API
					URL url = new URL("http://morkout.com");
					mUrlConnection = (HttpURLConnection) url.openConnection();
					InputStream in = new BufferedInputStream(mUrlConnection.getInputStream());
					int ch;
					StringBuffer b = new StringBuffer();
					while ((ch = in.read()) != -1) {
						b.append((char) ch);
					}

					mResult = new String(b);
					in.close();
				}
				else if (mWhat.equalsIgnoreCase("POST")) {
					URL url = new URL("http://morkout.com/glass/posttest.php");
					mUrlConnection = (HttpURLConnection) url.openConnection();
					mUrlConnection.setRequestMethod("POST");
					String urlParameters = "email=jeff.x.tang@gmail.com&name=Jeff Tang&pwd=1234567&vcode=2014";
					OutputStreamWriter writer = new OutputStreamWriter(mUrlConnection.getOutputStream());
					writer.write(urlParameters);
					writer.flush();					

					InputStream in = new BufferedInputStream(mUrlConnection.getInputStream());
					int ch;
					StringBuffer b = new StringBuffer();
					while ((ch = in.read()) != -1) {
						b.append((char) ch);
					}

					mResult = new String(b);

					in.close();
					writer.close();
				}
				else if (mWhat.equalsIgnoreCase("UPLOAD")) {
					int serverResponseCode = 0;        
					File sourceFile = new File(copyAsset("marchmadness.png"));
					DataOutputStream dos = null;  
					String lineEnd = "\r\n";
					String twoHyphens = "--";
					String boundary = "*****";
					int bytesRead, bytesAvailable, bufferSize;
					byte[] buffer;
					int maxBufferSize = 1 * 1024 * 1024; 

					if (!sourceFile.isFile()) {
						Log.e("uploadFile", "Source File not exist :" +sourceFile.getAbsolutePath());
					}
					else
					{
						FileInputStream fileInputStream = new FileInputStream(sourceFile);
						URL url = new URL("http://www.morkout.com/glass/upload.php");
						mUrlConnection = (HttpURLConnection) url.openConnection(); 
						mUrlConnection.setRequestMethod("POST");
						mUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
						mUrlConnection.setRequestProperty("Filedata", sourceFile.getName()); 

						dos = new DataOutputStream(mUrlConnection.getOutputStream());

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
						serverResponseCode = mUrlConnection.getResponseCode();
						String serverResponseMessage = mUrlConnection.getResponseMessage();

						Log.i("uploadFile", "HTTP Response is : "
								+ serverResponseMessage + ": " + serverResponseCode);

						if(serverResponseCode == 200) {
							Log.v(TAG, "File Upload Completed.");


							InputStream is = mUrlConnection.getInputStream();
							int ch;

							StringBuffer b = new StringBuffer();
							while ((ch = is.read()) != -1) {
								b.append((char) ch);
							}

							final String uploadedFilename = b.toString();
							Log.v(TAG, "uploaded file at http://www.morkout.com/glass/uploads/" + uploadedFilename);
							mResult = "uploaded file at http://www.morkout.com/glass/uploads/" + uploadedFilename;
							is.close();
						}
						
						fileInputStream.close();
						dos.close();
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}			
			finally {
				if (mUrlConnection != null)
					mUrlConnection.disconnect();
			}
			return null;
		}		

		String copyAsset(String filename) {
			final String PATH = Environment.getExternalStorageDirectory().toString() + "/nbsocial/";
			File dir = new File(PATH);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + PATH + " on sdcard failed");
					return null;
				} else {
					Log.v(TAG, "Created directory " + PATH + " on sdcard");
				}
			}

			if (!(new File( PATH + filename).exists())) {
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
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.w(TAG, "onPostExecute");
			mTvInfo.setText(mResult);
		}
	}
}
