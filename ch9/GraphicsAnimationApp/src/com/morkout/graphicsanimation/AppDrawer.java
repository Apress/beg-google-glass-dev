package com.morkout.graphicsanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.View;
import android.util.Log;

public class AppDrawer implements SurfaceHolder.Callback {
    private static final String TAG = "AppDrawer";
    private final AppViewer mView;
    private SurfaceHolder mHolder;

    public AppDrawer(Context context) {
        mView = new AppViewer(context);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	// Measure and layout the view with the canvas dimensions.
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        mView.measure(widthMeasureSpec, heightMeasureSpec);
        mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());        
        draw(mView);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
    }

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
