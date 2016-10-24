package com.morkout.glassuielements;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.google.android.glass.touchpad.GestureDetector;

public class Immersion3DActivity extends Activity {
	private static final String TAG = "Immersion3DActivity";
    MyGLView view3D; // 3D

    private GestureDetector mGestureDetector;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	// 3D drawing with OpenGLES 1.0 or 2.0
    	//view3D = new MyGLView(this);
    	//        view3D = new MyGLView(AppService.appService()); // both this Service context and the above activity context work!
    	//        setContentView(view3D);



    	//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
    	 // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
     
        if (supportsEs2)
        {
        	Log.e(TAG, "supportsEs2");
        	view3D = new MyGLView(this);        	
        	
            // Request an OpenGL ES 2.0 compatible context.
        	// view3D.setEGLContextClientVersion(2);
        	// Activities which require OpenGL ES 2.0 should indicate this by setting @lt;uses-feature android:glEsVersion="0x00020000" /> in the activity's AndroidManifest.xml file.
        	// If this method is called, it must be called before setRenderer(Renderer) is called.
        	
        	
        	setContentView(view3D);
        }
        else
        {
        	Log.e(TAG, "DO NOT supportsEs2");
        	GLSurfaceView view = new GLSurfaceView(this);
           	view.setRenderer(new TetrahedronRenderer(true));
        	setContentView(view); 
        }    	
    	
    	
        

    	//mGestureDetector = createGestureDetector(this);
    }
    /*
    private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					// do something on tap
					Log.e(TAG, "TAP");
					openOptionsMenu(); 	
					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					// do something on two finger tap
					Log.e(TAG, "TWO_TAP");
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					Log.e(TAG, "SWIPE_RIGHT");
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					Log.e(TAG, "SWIPE_LEFT");
					return true;
				} else if (gesture == Gesture.LONG_PRESS) {
					Log.e(TAG, "LONG_PRESS");
					return true;
				} else if (gesture == Gesture.SWIPE_DOWN) {
					Log.e(TAG, "SWIPE_DOWN");
					return false;
				} else if (gesture == Gesture.SWIPE_UP) {
					Log.e(TAG, "SWIPE_UP");
					return true;
				} else if (gesture == Gesture.THREE_LONG_PRESS) {
					Log.e(TAG, "THREE_LONG_PRESS");
					return true;
				} else if (gesture == Gesture.THREE_TAP) {
					Log.e(TAG, "THREE_TAP");
					return true;
				} else if (gesture == Gesture.TWO_LONG_PRESS) {
					Log.e(TAG, "TWO_LONG_PRESS");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_DOWN) {
					Log.e(TAG, "TWO_SWIPE_DOWN");
					return false;
				} else if (gesture == Gesture.TWO_SWIPE_LEFT) {
					Log.e(TAG, "TWO_SWIPE_LEFT");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_RIGHT) {
					Log.e(TAG, "TWO_SWIPE_RIGHT");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_UP) {
					Log.e(TAG, "TWO_SWIPE_UP");
					return true;
				}

				return false;
			}
		});

		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {
				// do something on scrolling
				Log.e(TAG, "onScroll");
				return true;
			}
		});
		return gestureDetector;		
		
    }

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}        
    
    protected void onStart() {
        super.onStart();
    }

    
    @Override
    protected void onResume() {
      super.onResume();
      
      //view3D.onResume();
    }

    @Override
    protected void onPause() {
      super.onPause();
      
      //view3D.onPause();      
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.juststop, menu);
        return true;
    } 
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                finish(); // stop the immersion activity
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
        */
}

