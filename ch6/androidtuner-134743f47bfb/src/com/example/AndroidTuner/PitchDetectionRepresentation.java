/** Copyright (C) 2009 by Aleksey Surkov.
 **
 ** Permission to use, copy, modify, and distribute this software and its
 ** documentation for any purpose and without fee is hereby granted, provided
 ** that the above copyright notice appear in all copies and that both that
 ** copyright notice and this permission notice appear in supporting
 ** documentation.  This software is provided "as is" without express or
 ** implied warranty.
 */

package com.example.AndroidTuner;

import android.util.Log;

public class PitchDetectionRepresentation {
	PitchDetectionRepresentation(double pitch_, int string_no_, int fret_) {
		pitch = pitch_; string_no = string_no_; fret = fret_;
		creation_date_ = System.currentTimeMillis();
		string_detected = true;
	}
	
	PitchDetectionRepresentation(double pitch_) {
		pitch = pitch_;
		creation_date_ = System.currentTimeMillis();
		string_detected = false;
	}

	public int GetAlpha() {
		final long age = System.currentTimeMillis() - creation_date_;
		if (age > LIFE_TIME_MS) return 0;
		if (age < BRIGHT_TIME_MS) return 255;
		return (int) Math.floor(255 - (age - BRIGHT_TIME_MS) * 1.0 / 
				                      (LIFE_TIME_MS - BRIGHT_TIME_MS) * 255);
	}
	
	
	public double pitch;
	public int string_no;
	public int fret;
	public boolean string_detected;
	private long creation_date_;
	
	private final static int LIFE_TIME_MS = 4000;
	private final static int BRIGHT_TIME_MS = 2000;

}
