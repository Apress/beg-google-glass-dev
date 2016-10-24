package com.morkout.glassuielements;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

public class AppDrawer implements SurfaceHolder.Callback {

	private static final String TAG = "AppDrawer";


	private final AppViewer mView;
	private SurfaceHolder mHolder;

	public AppDrawer(Context context) {
		mView = new AppViewer(context);

		mView.setListener(new AppViewer.ChangeListener() {
			@Override
			public void onChange() {
				if (mHolder != null) {
					draw(mView);
				}
			}}
				);      
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		// Measure and layout the view with the canvas dimensions.
		int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
		int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

		mView.measure(measuredWidth, measuredHeight);
		mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());

		// without this, text won't show!
		draw(mView);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, ">>>>surfaceCreated");
		mHolder = holder;
		mView.start();
		draw(mView);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, ">>>>Surface destroyed");
		mHolder = null;
	}

	/**
	 * Draws the view in the SurfaceHolder's canvas.
	 */
	private void draw(View view) {
		Canvas canvas;
		try {
			canvas = mHolder.lockCanvas();
		} catch (Exception e) {
			return;
		}
		if (canvas != null) {
			view.draw(canvas);
			mHolder.unlockCanvasAndPost(canvas);
		}
	}	

}
