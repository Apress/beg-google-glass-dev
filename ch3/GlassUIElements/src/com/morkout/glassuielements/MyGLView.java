package com.morkout.glassuielements;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
//import com.morkout.glassuielements.GLSurfaceView; // used for changed source code
// http://stackoverflow.com/questions/12855103/eglcreatewindowsurface-fails-with-java-lang-illegalargumentexception

public class MyGLView extends GLSurfaceView {
	//private final MyGLRenderer renderer;
	private static final String TAG = "MyGLView";
	private final MyGL20Renderer renderer2;
    private SurfaceHolder mHolder;
    Context mContext;

	MyGLView(Context context) {
        super(context);
        Log.e(TAG, "before new MyGLRenderer");
        
        
//        renderer = new MyGLRenderer(context);
//        setRenderer(renderer);
        /* The renderer will be called on a separate thread, so that rendering
        * performance is decoupled from the UI thread. Clients typically need to
        * communicate with the renderer from the UI thread, because that's where
        * input events are received. Clients can communicate using any of the
        * standard Java techniques for cross-thread communication, or they can
        * use the {@link GLSurfaceView#queueEvent(Runnable)} convenience method. */
        
        
        
        setEGLContextClientVersion(2);
        renderer2 = new MyGL20Renderer();
        setRenderer(renderer2);
	
	}
	
	// code below can be removed and the graphics still gets drawn
	
	/**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
//    public void surfaceCreated(SurfaceHolder holder) {
//        mGLThread.surfaceCreated();
//    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        // Surface will be destroyed when we return
//        mGLThread.surfaceDestroyed();
//    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
//    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        mGLThread.onWindowResize(w, h);
//    }	
	
	
	
	
	// for test with high-frequency Live Card
	public void surfaceCreated(SurfaceHolder holder) {
		 Log.e(TAG, "surfaceCreated");		 
        super.surfaceCreated(holder);
        mHolder = holder;
        
        Log.e(TAG, mHolder.getSurface().isValid()?"VALID surface":"Invalid Surface");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return
    	 Log.e(TAG, "surfaceDestroyed");
        super.surfaceDestroyed(holder);
        mHolder = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface size or format has changed. This should not happen in this
        // example.
    	 Log.e(TAG, "surfaceChanged");
    	super.surfaceChanged(mHolder, format, width, height);
    	
    	int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

    	Log.e(TAG, "measuredWidth="+widthMeasureSpec+", measuredHeight="+heightMeasureSpec);
        this.measure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "width="+this.getMeasuredWidth()+", height="+this.getMeasuredHeight());
        this.layout(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());        
    	
        Log.e(TAG, mHolder.getSurface().isValid()?"surfaceChanged - VALID surface":"surfaceChanged - Invalid Surface");
        Log.e(TAG, holder.getSurface().isValid()?"holder - VALID surface":"holder - Invalid Surface");
        Log.e(TAG, this.getHolder().getSurface().isValid()?"this.getHolder()- VALID surface":"this.getHolder() - Invalid Surface");

    }	
	
    public void draw (Canvas canvas)
    {
    	Log.e(TAG, "draw");
    }
    
}
