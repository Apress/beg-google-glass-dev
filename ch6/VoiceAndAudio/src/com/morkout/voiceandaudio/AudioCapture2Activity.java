package com.morkout.voiceandaudio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;



public class AudioCapture2Activity extends Activity {
	Button startRec, stopRec, playBack;
	Boolean recording;
	GestureDetector mGestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiorecorder);

		startRec = (Button)findViewById(R.id.startrec);
		stopRec = (Button)findViewById(R.id.stoprec);
		playBack = (Button)findViewById(R.id.playback);

		startRec.setOnClickListener(startRecOnClickListener);
		stopRec.setOnClickListener(stopRecOnClickListener);
		playBack.setOnClickListener(playBackOnClickListener);

		stopRec.setEnabled(false);
		playBack.setEnabled(false);

		mGestureDetector = createGestureDetector(this);

	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.SWIPE_RIGHT) {
					if (startRec.isEnabled()) {
						startRec.setEnabled(false);
						stopRec.setEnabled(true);
						stopRec.requestFocus();
						playBack.setEnabled(false);
					}
					else if (stopRec.isEnabled()) {
						stopRec.setEnabled(false);
						playBack.setEnabled(true);
						playBack.requestFocus();						
						startRec.setEnabled(false);
					}
					else if (playBack.isEnabled()) {
						playBack.setEnabled(false);
						startRec.setEnabled(true);
						startRec.requestFocus();						
						stopRec.setEnabled(false);
					}
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					if (startRec.isEnabled()) {
						startRec.setEnabled(false);
						playBack.setEnabled(true);
						playBack.requestFocus();
						stopRec.setEnabled(false);
					}
					else if (stopRec.isEnabled()) {
						stopRec.setEnabled(false);
						startRec.setEnabled(true);
						startRec.requestFocus();						
						playBack.setEnabled(false);
					}
					else if (playBack.isEnabled()) {
						playBack.setEnabled(false);
						stopRec.setEnabled(true);
						stopRec.requestFocus();						
						startRec.setEnabled(false);
					}
					return true;
				}
				return false;
			}
		});

		return gestureDetector;
	}

	// this method is required for tap on touchpad to work!
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}        	


	OnClickListener startRecOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			System.out.println("startRecOnClickListener");

			startRec.setEnabled(false);
			stopRec.setEnabled(true);
			stopRec.requestFocus();

			Thread recordThread = new Thread(new Runnable() {
				@Override
				public void run() {
					recording = true;
					startRecord();
				}
			});

			recordThread.start();
		}
	};

	OnClickListener stopRecOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			System.out.println("stopRecOnClickListener");
			recording = false;
			startRec.setEnabled(false);
			stopRec.setEnabled(false);
			playBack.setEnabled(true);
			playBack.requestFocus();			
		}
	};

	OnClickListener playBackOnClickListener   = new OnClickListener() {
		@Override
		public void onClick(View v) {
			System.out.println("playBackOnClickListener");

			startRec.setEnabled(true);
			stopRec.setEnabled(false);
			playBack.setEnabled(false);
			startRec.requestFocus();			
			playRecord();
		}
	};

	private void startRecord() { 
		File file = new File(Environment.getExternalStorageDirectory(), "audiorecordtest.pcm");
		try {
			file.createNewFile();
			OutputStream outputStream = new FileOutputStream(file);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

			int minBufferSize = AudioRecord.getMinBufferSize(11025,
					//AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT);

			short[] audioData = new short[minBufferSize];

			AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					11025,
					//AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					minBufferSize);

			audioRecord.startRecording();

			while(recording) {
				int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
				for(int i = 0; i < numberOfShort; i++)
					dataOutputStream.writeShort(audioData[i]);
			}
			audioRecord.stop();
			dataOutputStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	void playRecord() {
		File file = new File(Environment.getExternalStorageDirectory(), "audiorecordtest.pcm");
		int shortSizeInBytes = Short.SIZE/Byte.SIZE;

		int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
		short[] audioData = new short[bufferSizeInBytes];

		try {
			InputStream inputStream = new FileInputStream(file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

			int i = 0;
			while(dataInputStream.available() > 0) {
				audioData[i] = dataInputStream.readShort();
				i++;
			}

			dataInputStream.close();

			AudioTrack audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					11025,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSizeInBytes,
					AudioTrack.MODE_STREAM);

			audioTrack.play();
			audioTrack.write(audioData, 0, bufferSizeInBytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
}