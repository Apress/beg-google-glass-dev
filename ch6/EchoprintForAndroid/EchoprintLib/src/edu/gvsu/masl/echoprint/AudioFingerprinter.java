/**
 * AudioFingerprinter.java
 * EchoprintLib
 * 
 * Created by Alex Restrepo on 1/22/12.
 * Copyright (C) 2012 Grand Valley State University (http://masl.cis.gvsu.edu/)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.gvsu.masl.echoprint;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

/**
 * Main fingerprinting class<br>
 * This class will record audio from the microphone, generate the fingerprint code using a native library and query the data server for a match
 * 
 * @author Alex Restrepo (MASL)
 *
 */
public class AudioFingerprinter implements Runnable 
{
	public final static String META_SCORE_KEY = "meta_score";
	public final static String SCORE_KEY = "score";
	public final static String ALBUM_KEY = "release";
	public final static String TITLE_KEY = "track";
	public final static String TRACK_ID_KEY = "track_id";
	public final static String ARTIST_KEY = "artist";

	//private final String SERVER_URL = "<your server address here>/query?fp_code=";

	//private final String SERVER_URL = "http://developer.echonest.com/api/v4/song/identify?api_key=EYZZ6GD1S2NBJMGZR&code=";
	private final String SERVER_URL = "http://developer.echonest.com/api/v4/song/identify?api_key=AV6AWC8NL7IJPZ5QO&version=4.12&code=";
	private final int FREQUENCY = 11025;
	private final int CHANNEL = 16; // AudioFormat.CHANNEL_IN_MONO;
	private final int ENCODING = 2; //AudioFormat.ENCODING_PCM_16BIT;	

	private Thread thread;
	private volatile boolean isRunning = false;
	AudioRecord mRecordInstance = null;

	private short audioData[];
	private int bufferSize;	
	private int secondsToRecord;
	private volatile boolean continuous;

	private AudioFingerprinterListener listener;

	/**
	 * Constructor for the class
	 * 
	 * @param listener is the AudioFingerprinterListener that will receive the callbacks
	 */
	public AudioFingerprinter(AudioFingerprinterListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Starts the listening / fingerprinting process using the default parameters:<br>
	 * A single listening pass of 20 seconds 
	 */
	public void fingerprint()
	{
		// set dafault listening time to 20 seconds
		this.fingerprint(20);
	}

	/**
	 * Starts a single listening / fingerprinting pass
	 * 
	 * @param seconds the seconds of audio to record.
	 */
	public void fingerprint(int seconds)
	{
		// no continuous listening
		this.fingerprint(seconds, false);
	}

	/**
	 * Starts the listening / fingerprinting process
	 * 
	 * @param seconds the number of seconds to record per pass
	 * @param continuous if true, the class will start a new fingerprinting pass after each pass
	 */
	public void fingerprint(int seconds, boolean continuous)
	{
		if(this.isRunning)
			return;

		this.continuous = continuous;

		// cap to 30 seconds max, 10 seconds min.
		this.secondsToRecord = Math.max(Math.min(seconds, 30), 10);

		// start the recording thread
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * stops the listening / fingerprinting process if there's one in process
	 */
	public void stop() 
	{
		this.continuous = false;
		if(mRecordInstance != null)
			mRecordInstance.stop();
	}

	/**
	 * The main thread<br>
	 * Records audio and generates the audio fingerprint, then it queries the server for a match and forwards the results to the listener.
	 */
	public void run() 
	{
		this.isRunning = true;
		try 
		{			
			// create the audio buffer
			// get the minimum buffer size
			int minBufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);

			// and the actual buffer size for the audio to record
			// frequency * seconds to record.
			bufferSize = Math.max(minBufferSize, this.FREQUENCY * this.secondsToRecord);
			Log.d("Fingerprinter", "minBufferSize="+minBufferSize+",bufferSize="+bufferSize);
			audioData = new short[bufferSize];

			// start recorder
			mRecordInstance = new AudioRecord(
					MediaRecorder.AudioSource.MIC,
					FREQUENCY, CHANNEL, 
					ENCODING, minBufferSize);

			willStartListening();

			mRecordInstance.startRecording();
			boolean firstRun = true;
			
			File file = new File(Environment.getExternalStorageDirectory(), "songrec.pcm");
			
			do 
			{		
				try
				{
					willStartListeningPass();

					file.createNewFile();
					OutputStream outputStream = new FileOutputStream(file);
					BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
					DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
					
					
					long time = System.currentTimeMillis();
					// fill audio buffer with mic data.
					int samplesIn = 0;
					do 
					{				
						Log.d("Fingerprinter", "inner do, samplesIn="+samplesIn);
						samplesIn += mRecordInstance.read(audioData, samplesIn, bufferSize - samplesIn);

						if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
							break;
					} 
					while (samplesIn < bufferSize);				
					Log.d("Fingerprinter", "Audio recorded: " + (System.currentTimeMillis() - time) + " millis");

					
					for(int i = 0; i < samplesIn; i++)
					{
						dataOutputStream.writeShort(audioData[i]);
					}					
					dataOutputStream.close();
					
					// see if the process was stopped.
					if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED || (!firstRun && !this.continuous))
						break;

					// create an echoprint codegen wrapper and get the code
					time = System.currentTimeMillis();
					Codegen codegen = new Codegen();
					String code = codegen.generate(audioData, samplesIn);

					// this code works!
					//code = "eJxVlIuNwzAMQ1fxCDL133-xo1rnGqNAEcWy_ERa2aKeZmW9ustWVYrXrl5bthn_laFkzguNWpklEmoTB74JKYZSPlbJ0sy9fQrsrbEaO9W3bsbaWOoK7IhkHFaf_ag2d75oOQSZczbz5CKA7XgTIBIXASvFi0A3W8pMUZ7FZTWTVbujCcADlQ_f_WbdRNJ2vDUwSF0EZmFvAku_CVy440fgiIvArWZZWoJ7GWd-CVTYC5FCFI8GQdECdROE20UQfLoIUmhLC7IiByF1gzbAs3tsSKctyC76MPJlHRsZ5qhSQhu_CJFcKtW4EMrHSIrpTGLFqsdItj1H9JYHQYN7W2nkC6GDPjZTAzL9dx0fS4M1FoROHh9YhLHWdRchQSd_CLTpOHkQQP3xQsA2-sLOUD7CzxU0GmHVdIxh46Oide0NrNEmjghG44Ax_k2AoDHsiV6WsiD6OFm8y-0Lyt8haDBBzeMlAnTuuGYIB4WA2lEPAWbdeOabgFN6TQMs6ctLA5fHyKMBB0veGrjPfP00IAlWNm9n7hEh5PiYYBGKQDP-x4F0CL8HkhoQnRWN997JyEpnHFR7EhLPQMZmgXS68hsHktEVErranvSSR2VwfJhQCnkuwhBUcINNY-xu1pmw3PmBqU9-8xu0kiF1ngOa8vwBSSzzNw==";
					// 03-04 22:10:52.276: D/Fingerprinter(8877): result={"response": {"status": 
					// {"version": "4.2", "code": 0, "message": "Success"}, "songs": [{"score": 54, "title": 
					// "Billie Jean", "message": "OK (match type 5)", "artist_id": "ARXPPEY1187FB51DF4", 
					// "artist_name": "Michael Jackson", "id": "SODJXOA1313438FB61"}]}}

					//writeAudioDataToFile();

					Log.d("Fingerprinter", "Codegen created in: " + (System.currentTimeMillis() - time) + " millis");

					if(code.length() == 0)
					{
						// no code?
						// not enough audio data?
						continue;
					}
					Log.d("FingerPrinter", "code="+code);
					didGenerateFingerprintCode(code);

					// fetch data from echonest
					time = System.currentTimeMillis();

					String urlstr = SERVER_URL + code;			
					HttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet(urlstr);

					Log.d("Fingerprinter", "urlstr="+urlstr);

					// get response
					HttpResponse response = client.execute(get);                
					// Examine the response status
					Log.d("Fingerprinter",response.getStatusLine().toString());

					// Get hold of the response entity
					HttpEntity entity = response.getEntity();
					// If the response does not enclose an entity, there is no need
					// to worry about connection release

					String result = "";
					if (entity != null) 
					{
						// A Simple JSON Response Read
						InputStream instream = entity.getContent();
						result= convertStreamToString(instream);
						// now you have the string representation of the HTML request
						Log.d("Fingerprinter", "result="+result);
						instream.close();
					}
					Log.d("Fingerprinter", "Results fetched in: " + (System.currentTimeMillis() - time) + " millis");


					// parse JSON
					JSONObject jobj = new JSONObject(result).getJSONObject("response");

					if(jobj.has("code"))
						Log.d("Fingerprinter", "Response code:" + jobj.getInt("code") + " (" + this.messageForCode(jobj.getInt("code")) + ")");

					if(jobj.has("songs"))
					{
						//if(jobj.getBoolean("match"))
						{
							Hashtable<String, String> match = new Hashtable<String, String>();
//							match.put(SCORE_KEY, jobj.getDouble(SCORE_KEY) + "");
//							match.put(TRACK_ID_KEY, jobj.getString(TRACK_ID_KEY));

							// the metadata dictionary IS NOT included by default in the API demo server
							// replace line 66/67 in API.py with:
							// return json.dumps({"ok":True,"message":response.message(), "match":response.match(), "score":response.score, \
							// "qtime":response.qtime, "track_id":response.TRID, "total_time":response.total_time, "metadata":response.metadata})
							//if(jobj.has("metadata"))
							{
								JSONArray songs = jobj.getJSONArray("songs");
								if (songs.length() == 0) didNotFindMatchForCode(code);
								else {
									JSONObject song = songs.getJSONObject(0);
									match.put(song.getString("title"), song.getString("artist_name"));
								}

//								if(metadata.has(SCORE_KEY)) match.put(META_SCORE_KEY, metadata.getDouble(SCORE_KEY) + "");
//								if(metadata.has(TITLE_KEY)) match.put(TITLE_KEY, metadata.getString(TITLE_KEY));
//								if(metadata.has(ARTIST_KEY)) match.put(ARTIST_KEY, metadata.getString(ARTIST_KEY));
//								if(metadata.has(ALBUM_KEY)) match.put(ALBUM_KEY, metadata.getString(ALBUM_KEY));
							}

							didFindMatchForCode(match, code);
						}
//						else
//							didNotFindMatchForCode(code);	    			
					}	    		
					else
					{
						didFailWithException(new Exception("no match found"));
					}

					firstRun = false;

					didFinishListeningPass();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Log.e("Fingerprinter", e.getLocalizedMessage());

					didFailWithException(e);
				}
				
				break;
			}
			while (this.continuous);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			Log.e("Fingerprinter", e.getLocalizedMessage());

			didFailWithException(e);
		}

		if(mRecordInstance != null)
		{
			mRecordInstance.stop();
			mRecordInstance.release();
			mRecordInstance = null;
		}
		this.isRunning = false;

		didFinishListening();
	}


	private void writeAudioDataToFile() {
		// Write the output audio in byte
		String filePath = "/sdcard/voice4.pcm";

		try {

			OutputStream outputStream = new FileOutputStream(filePath);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);


			int numberOfShort = audioData.length;
			for(int i = 0; i < numberOfShort; i++)
			{
				dataOutputStream.writeShort(audioData[i]);
			}
			dataOutputStream.close();		
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		//		FileOutputStream os = null;
		//		try {
		//			Log.d("Fingerprinter", "write to /sdcard/voice8K16bitmono2.pcm");
		//			os = new FileOutputStream(filePath);
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
		//
		//		try {
		//			// // writes the data to file from buffer
		//			// // stores the voice buffer
		//			byte bData[] = short2byte(audioData);
		//			os.write(bData, 0, bData.length);
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//		try {
		//			os.close();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
	}

	private byte[] short2byte(short[] sData) {
		int shortArrsize = sData.length;
		byte[] bytes = new byte[shortArrsize * 2];
		for (int i = 0; i < shortArrsize; i++) {
			bytes[i * 2] = (byte) (sData[i] & 0x00FF);
			bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
			sData[i] = 0;
		}
		return bytes;

	}

	private static String convertStreamToString(InputStream is) 
	{
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private String messageForCode(int code)
	{
		try{
			String codes[] = {
					"NOT_ENOUGH_CODE", "CANNOT_DECODE", "SINGLE_BAD_MATCH", 
					"SINGLE_GOOD_MATCH", "NO_RESULTS", "MULTIPLE_GOOD_MATCH_HISTOGRAM_INCREASED",
					"MULTIPLE_GOOD_MATCH_HISTOGRAM_DECREASED", "MULTIPLE_BAD_HISTOGRAM_MATCH", "MULTIPLE_GOOD_MATCH"
			}; 

			return codes[code];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return "UNKNOWN";
		}
	}

	private void didFinishListening()
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFinishListening();
				}
			});
		}
		else
			listener.didFinishListening();
	}

	private void didFinishListeningPass()
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFinishListeningPass();
				}
			});
		}
		else
			listener.didFinishListeningPass();
	}

	private void willStartListening()
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.willStartListening();
				}
			});
		}
		else	
			listener.willStartListening();
	}

	private void willStartListeningPass()
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.willStartListeningPass();
				}
			});
		}
		else
			listener.willStartListeningPass();
	}

	private void didGenerateFingerprintCode(final String code)
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didGenerateFingerprintCode(code);
				}
			});
		}
		else
			listener.didGenerateFingerprintCode(code);
	}

	private void didFindMatchForCode(final Hashtable<String, String> table, final String code)
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFindMatchForCode(table, code);
				}
			});
		}
		else
			listener.didFindMatchForCode(table, code);
	}

	private void didNotFindMatchForCode(final String code)
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didNotFindMatchForCode(code);
				}
			});
		}
		else
			listener.didNotFindMatchForCode(code);
	}

	private void didFailWithException(final Exception e)
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFailWithException(e);
				}
			});
		}
		else
			listener.didFailWithException(e);
	}

	/**
	 * Interface for the fingerprinter listener<br>
	 * Contains the different delegate methods for the fingerprinting process
	 * @author Alex Restrepo
	 *
	 */
	public interface AudioFingerprinterListener
	{		
		/**
		 * Called when the fingerprinter process loop has finished
		 */
		public void didFinishListening();

		/**
		 * Called when a single fingerprinter pass has finished
		 */
		public void didFinishListeningPass();

		/**
		 * Called when the fingerprinter is about to start
		 */
		public void willStartListening();

		/**
		 * Called when a single listening pass is about to start
		 */
		public void willStartListeningPass();

		/**
		 * Called when the codegen libary generates a fingerprint code
		 * @param code the generated fingerprint as a zcompressed, base64 string
		 */
		public void didGenerateFingerprintCode(String code);

		/**
		 * Called if the server finds a match for the submitted fingerprint code 
		 * @param table a hashtable with the metadata returned from the server
		 * @param code the submited fingerprint code
		 */
		public void didFindMatchForCode(Hashtable<String, String> table, String code);

		/**
		 * Called if the server DOES NOT find a match for the submitted fingerprint code
		 * @param code the submited fingerprint code
		 */
		public void didNotFindMatchForCode(String code);

		/**
		 * Called if there is an error / exception in the fingerprinting process
		 * @param e an exception with the error
		 */
		public void didFailWithException(Exception e);
	}
}
