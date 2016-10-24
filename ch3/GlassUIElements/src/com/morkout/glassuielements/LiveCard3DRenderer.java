package com.morkout.glassuielements;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;


public class LiveCard3DRenderer extends GLSurfaceView implements DirectRenderingCallback {
    private static final String TAG = "LiveCardRenderer";

    // About 30 FPS.
    private static final long FRAME_TIME_MILLIS = 330;

    private SurfaceHolder mHolder;
    private boolean mPaused;
    
    
    private int canvasWidth = 640;
    private int canvasHeight = 360;
    private static final int SPEED = 2;
      
    private float bubbleX;
    private float bubbleY;
    private float headingX;
    private float headingY;  
    private Paint paint;

    MyGLView view3D;
    Context mContext;

    LiveCard3DRenderer(Context context) {
        super(context);
        Log.e(TAG, "before new MyGLRenderer");
        
       mContext = context;
       
      view3D = new MyGLView(mContext);   
	}
	    

    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Update your views accordingly.
    	Log.e(TAG, "surfaceChanged");    	
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.e(TAG, "surfaceCreated");
       
        mHolder = holder;
        //updateRendering();
        Log.e(TAG, "!!! draw3D");
    	 Log.e(TAG, mHolder.getSurface().isValid()?"VALID surface":"Invalid Surface");
         Log.e(TAG, this.getHolder().getSurface().isValid()?"this.getHolder()- VALID surface":"this.getHolder() - Invalid Surface");

     
        
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.e(TAG, "surfaceDestroyed");    	
    	
        mHolder = null;
    }

    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mPaused = paused;
    }

   
}