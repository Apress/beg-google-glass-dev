package com.morkout.glassuielements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;


public class LiveCard2DRenderer implements DirectRenderingCallback {
	private static final String TAG = "LiveCardRenderer";

	private static final long FRAME_TIME_MILLIS = 33; // about 30 FPS

	private SurfaceHolder mHolder;
	private boolean mPaused;
	private RenderThread mRenderThread;

	private int canvasWidth;
	private int canvasHeight;
	private int diffX = 25;
	private int incY = 1;

	private float bouncingX;
	private float bouncingY;
	private double angle;
	private Paint paint;
	private Path path;

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		System.out.println("surfaceChanged");
		canvasWidth = width;
		canvasHeight = height;		

		bouncingX = canvasWidth / 2;
		bouncingY = canvasHeight / 2;
		angle = - Math.PI/4.0; //(2.0 * Math.PI) * (double) (Math.random() * 360) / 360.0;
		Log.e(TAG, "angle=" + angle);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		paint.setStyle(Style.FILL);
		paint.setStyle(Style.STROKE);

		path = new Path();

		mHolder = holder;
		updateRendering();		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("surfaceCreated");

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHolder = null;
		updateRendering();
	}

	@Override
	public void renderingPaused(SurfaceHolder holder, boolean paused) {
		System.out.println("renderingPaused");
		mPaused = paused;
		updateRendering();
	}

	/**
	 * Start or stop rendering according to the timeline state.
	 */
	private synchronized void updateRendering() {
		boolean shouldRender = (mHolder != null) && !mPaused;
		boolean rendering = mRenderThread != null;

		if (shouldRender != rendering) {
			if (shouldRender) {
				mRenderThread = new RenderThread(this);
				mRenderThread.start();
			} else {
				mRenderThread.quit();
				mRenderThread = null;
			}
		}
	}

	/**
	 * Draws the view in the SurfaceHolder's canvas.
	 */
	public void drawInCanvas(View view) {
		Canvas canvas;

		try {
			canvas = mHolder.lockCanvas();
		} catch (Exception e) {
			return;
		}
		if (canvas != null) {
			bouncingX += diffX;
			bouncingY += diffX * Math.tan(angle);
			bouncingY *= incY;

			canvas.drawColor(Color.BLACK);
			canvas.drawCircle(bouncingX, bouncingY, 20, paint);   

			if (bouncingX > canvasWidth || bouncingX < 0) {
				diffX = -diffX;
				angle = -angle;
			}
			else if (bouncingY > canvasHeight || bouncingY < 0) {
				angle = -angle;
			}

			float mid = canvasWidth / 2;
			float min = canvasHeight;
			float half = min / 2;
			mid -= half;

			paint.setStrokeWidth(min / 10);
			paint.setStyle(Paint.Style.STROKE);
			path.reset();
			paint.setStyle(Paint.Style.FILL);

			path.moveTo(mid + half * 0.5f, half * 0.84f);
			path.lineTo(mid + half * 1.5f, half * 0.84f);
			path.lineTo(mid + half * 0.68f, half * 1.45f);
			path.lineTo(mid + half * 1.0f, half * 0.5f);
			path.lineTo(mid + half * 1.32f, half * 1.45f);
			path.lineTo(mid + half * 0.5f, half * 0.84f);

			path.close();
			canvas.drawPath(path, paint);

			mHolder.unlockCanvasAndPost(canvas);

		}
	}

	/**
	 * Redraws in the background.
	 */
	private class RenderThread extends Thread {
		private boolean mShouldRun;
		LiveCard2DRenderer mRenderer;

		/**
		 * Initializes the background rendering thread.
		 */
		public RenderThread(LiveCard2DRenderer renderer) {
			mShouldRun = true;
			mRenderer = renderer;
		}

		/**
		 * Returns true if the rendering thread should continue to run.
		 *
		 * @return true if the rendering thread should continue to run
		 */
		private synchronized boolean shouldRun() {
			return mShouldRun;
		}

		/**
		 * Requests that the rendering thread exit at the next opportunity.
		 */
		public synchronized void quit() {
			mShouldRun = false;
		}

		@Override
		public void run() {
			while (shouldRun()) {
				mRenderer.drawInCanvas(null);
				SystemClock.sleep(FRAME_TIME_MILLIS);
			}
		}
	}
}