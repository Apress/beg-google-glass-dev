package com.morkout.glassuielements;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class AppViewer extends FrameLayout {	
	private final TextView mTextView;

	private static ProgressBar m_progressBar; //UI reference
	private final TextView mFooter;
	
	int percent_done = 0;	 
	private ChangeListener mChangeListener;

	public interface ChangeListener {
		public void onChange();
	}	

	public void setListener(ChangeListener listener) {
		mChangeListener = listener;
	}


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
		mTextView.setText("Glass UI");

		m_progressBar = (ProgressBar) findViewById(R.id.ex_progress_bar);
		percent_done = 0;
		m_progressBar.setProgress(percent_done);
			    
	    mFooter =  (TextView) findViewById(R.id.footer);
	    mFooter.setText(R.string.footer);		
	}


	private final Handler mHandler = new Handler();  
	private final Runnable mUpdateTextRunnable = new Runnable() {
		@Override
		public void run() {
			if (++percent_done > 100) {
				m_progressBar.setVisibility(View.INVISIBLE);
				mTextView.setText("DONE!");
				if (mChangeListener != null) {
					mChangeListener.onChange();
				}				
				return;
			}


			m_progressBar.setProgress(percent_done);
			mTextView.setText(""+percent_done + "%");

			if (mChangeListener != null) {
				mChangeListener.onChange();
			}

			mHandler.postDelayed(mUpdateTextRunnable, 100);
		}
	};    


	public void start() {
		updateProgress();
	}


	public void updateProgress() {
		mHandler.post(mUpdateTextRunnable);
	}
}
