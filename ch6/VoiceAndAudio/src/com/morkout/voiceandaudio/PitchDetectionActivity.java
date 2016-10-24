/** Copyright (C) 2009 by Aleksey Surkov.
 **
 ** Permission to use, copy, modify, and distribute this software and its
 ** documentation for any purpose and without fee is hereby granted, provided
 ** that the above copyright notice appear in all copies and that both that
 ** copyright notice and this permission notice appear in supporting
 ** documentation.  This software is provided "as is" without express or
 ** implied warranty.
 */      

package com.morkout.voiceandaudio;

import java.util.HashMap;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class PitchDetectionActivity extends Activity {
	
	public DrawableView tv_;
	Thread pitch_detector_thread_;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv_ = new DrawableView(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(tv_);
	}

	@Override
	public void onStart() {
		super.onStart();
		pitch_detector_thread_ = new Thread(new PitchDetector(this, new Handler()));
		pitch_detector_thread_.start();
	}

	@Override
	public void onStop() {
		super.onStop();
		pitch_detector_thread_.interrupt();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	public void ShowPitchDetectionResult(
			final HashMap<Double, Double> frequencies,
			final double pitch) {
		tv_.setDetectionResults(frequencies, pitch);
	}
}