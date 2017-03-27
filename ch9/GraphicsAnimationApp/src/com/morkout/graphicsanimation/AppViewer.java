package com.morkout.graphicsanimation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

public class AppViewer extends FrameLayout {
    private static final String TAG = "AppViewer";
	private final TextView mTextView;
	private final TextView mFooter;

	public AppViewer(Context context) {
	    this(context, null, 0);
	}

	public AppViewer(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}
	    	
	public AppViewer(Context context, AttributeSet attrs, int style) {
	    super(context, attrs, style);
	    LayoutInflater.from(context).inflate(R.layout.start, this);
	    mTextView =  (TextView) findViewById(R.id.hello_view);
	    mTextView.setText("Graphics Animation");
	    
	    mFooter =  (TextView) findViewById(R.id.footer);
	    mFooter.setText(R.string.footer);	    
	}
}
