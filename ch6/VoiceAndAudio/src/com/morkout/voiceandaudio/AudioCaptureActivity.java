package com.morkout.voiceandaudio;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class AudioCaptureActivity extends Activity {

	private static final String TAG = "AudioCaptureActivity";
	private static String mFileName = null;

	private RecordButton mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private MediaPlayer   mPlayer = null;

	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mRecordButton.setText("Start recording");
				mPlayer.reset();
				mPlayer.release();
				mPlayer = null;				
			}
		});
		
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);							
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(TAG, "prepare() failed");
		}
	}


	private void startRecording() {
		mRecorder = new MediaRecorder();
		Log.v(TAG, "audiosource mic:"+MediaRecorder.AudioSource.MIC+",camcorder="+MediaRecorder.AudioSource.CAMCORDER + ",default="+MediaRecorder.AudioSource.DEFAULT+",VOICE_CALL="+MediaRecorder.AudioSource.VOICE_CALL+",VOICE_COMMUNICATION="+MediaRecorder.AudioSource.VOICE_COMMUNICATION+",VOICE_DOWNLINK="+MediaRecorder.AudioSource.VOICE_DOWNLINK+",VOICE_RECOGNITION="+MediaRecorder.AudioSource.VOICE_RECOGNITION+",VOICE_UPLINK="+MediaRecorder.AudioSource.VOICE_UPLINK);
		Log.v(TAG,"mFileName="+mFileName);
		//        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		//        mRecorder.setOutputFile(mFileName);
		//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);        

		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);		
		//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);                
		//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		mRecorder.setOutputFile(mFileName);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.reset();
		mRecorder.release();
		mRecorder = null;
	}

	class RecordButton extends Button {
		boolean mStartRecording = true;
		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onRecord(mStartRecording);
				if (mStartRecording) {
					setText("Stop recording");
				} else {
					setText("Playing...");

					startPlaying();			

				}
				mStartRecording = !mStartRecording;
			}
		};

		public RecordButton(Context ctx) {
			super(ctx);
			setText("Start recording");
			setOnClickListener(clicker);
		}
	}

	public AudioCaptureActivity() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.3gp";
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		LinearLayout ll = new LinearLayout(this);
		mRecordButton = new RecordButton(this);
		ll.addView(mRecordButton,
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						0));
		setContentView(ll);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}