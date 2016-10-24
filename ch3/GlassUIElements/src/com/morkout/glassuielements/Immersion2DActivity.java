package com.morkout.glassuielements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.MenuUtils;
import com.google.android.glass.view.WindowUtils;

public class Immersion2DActivity extends Activity {
	private static final String TAG = "Immersion2DActivity";

	TextView mTextView;        
	DrawView mDrawView; // 2D
	private GestureDetector mGestureDetector;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 2D Canvas-based drawing
		mDrawView = new DrawView(this);
		setContentView(mDrawView);
		mDrawView.requestFocus();
		mGestureDetector = new GestureDetector(this);

		// Called when the following gestures happen: TAP, LONG_PRESS SWIPE_UP, 
		// SWIPE_LEFT, SWIPE_RIGHT, SWIPE_DOWN
		mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					// do something on tap
					Log.v(TAG, "TAP");
					Point point = new Point();
					point.x = (int )(Math.random() * 640 + 1);
					point.y = (int )(Math.random() * 360 + 1); 
					Paint paint = new Paint();
					paint.setARGB(255, (int )(Math.random() * 255), (int )(Math.random() * 255), (int )(Math.random() * 255));
					paint.setAntiAlias(true);
					mDrawView.points.add(point);
					mDrawView.paints.add(paint);
					mDrawView.invalidate();					
					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					Log.v(TAG, "TWO_TAP");
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					Log.v(TAG, "SWIPE_RIGHT");
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					return true;
				} else if (gesture == Gesture.LONG_PRESS) {
					Log.v(TAG, "LONG_PRESS");
					openOptionsMenu();
					return true;
				} else if (gesture == Gesture.SWIPE_DOWN) {
					Log.v(TAG, "SWIPE_DOWN");
					return false;
				} else if (gesture == Gesture.SWIPE_UP) {
					Log.v(TAG, "SWIPE_UP");
					return true;
				} else if (gesture == Gesture.THREE_LONG_PRESS) {
					Log.v(TAG, "THREE_LONG_PRESS");
					return true;
				} else if (gesture == Gesture.THREE_TAP) {
					Log.v(TAG, "THREE_TAP");
					return true;
				} else if (gesture == Gesture.TWO_LONG_PRESS) {
					Log.v(TAG, "TWO_LONG_PRESS");
					
					// test of screen capture
					
					// image naming and path  to include sd card  appending name you choose for file
					String mPath = Environment.getExternalStorageDirectory().toString() + "/captured-screen.jpg";   

					// create bitmap screen capture
					Bitmap bitmap;
					View v1 = mDrawView.getRootView();
					v1.setDrawingCacheEnabled(true);
					bitmap = Bitmap.createBitmap(v1.getDrawingCache());
					v1.setDrawingCacheEnabled(false);

					OutputStream fout = null;
					File imageFile = new File(mPath);

					try {
					    fout = new FileOutputStream(imageFile);
					    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
					    fout.flush();
					    fout.close();
					    Log.v(TAG, "saved:"+mPath);

					} catch (FileNotFoundException e) {
					    // TODO Auto-generated catch block
					    e.printStackTrace();
					} catch (IOException e) {
					    // TODO Auto-generated catch block
					    e.printStackTrace();
					}			
					
					
					
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_DOWN) {
					Log.v(TAG, "TWO_SWIPE_DOWN");
					return false;
				} else if (gesture == Gesture.TWO_SWIPE_LEFT) {
					Log.v(TAG, "TWO_SWIPE_LEFT");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_RIGHT) {
					Log.v(TAG, "TWO_SWIPE_RIGHT");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_UP) {
					Log.v(TAG, "TWO_SWIPE_UP");
					return true;
				}

				return false;
			}
		});

		// Called when the finger count changes on the touch pad
		mGestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged (int previousCount, int currentCount) {

				Log.v(TAG, "onFingerCountChanged:" + previousCount + "," + currentCount);
			}
		});

		// Called while the user is scrolling after initial horizontal scroll with one finger
		mGestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {
				// do something on scrolling
				Log.v(TAG, "onScroll");
				return true;
			}
		});


		// Called while the user is scrolling with two fingers
		mGestureDetector.setTwoFingerScrollListener(new GestureDetector.TwoFingerScrollListener() {

			@Override
			// displacement: average distance between scroll state entering x value
			// delta: average delta between two consecutive x motion events
			// velocity: sverage velocity of current x motion event
			// return: true if the events were handled
			public boolean onTwoFingerScroll(float displacement, float delta, float velocity) {
				Log.v(TAG, "onTwoFingerScroll");
				return false;
			}
		});
	}

	// Send generic motion events to the gesture detector
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}        



	//	@Override
	//    public boolean onKeyDown(int keycode, KeyEvent event) {
	//		Log.e(TAG, "onKeyDown");
	//        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
	//            // user tapped touchpad, do something
	//        	//openOptionsMenu(); // this works; commented for drawing test
	//  		  Point point = new Point();
	//  		  point.x = 400;
	//  		  point.y = 300;  		         	
	//          mDrawView.points.add(point);
	//          mDrawView.invalidate();
	//        	
	//            return true;
	//        }
	//        else if (keycode == KeyEvent.KEYCODE_BACK) {
	//        	finish();
	//        	return true;
	//        }
	//        return false;
	//    }    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.juststop, menu);

		MenuItem item2 = menu.add(0, R.id.stop+1, 0, R.string.headoff);
		MenuItem item3 = menu.add(0, R.id.stop+2, 0, R.string.headon);

		MenuUtils.setDescription(item2, R.string.headoffDesc);
		MenuUtils.setDescription(item3, R.string.headonDesc);
		MenuUtils.setInitialMenuItem(menu, item2);

		getWindow().addFlags(WindowUtils.FLAG_DISABLE_HEAD_GESTURES);
		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.stop:
			finish(); 
			return true;

		case R.id.stop+1:
			getWindow().addFlags(WindowUtils.FLAG_DISABLE_HEAD_GESTURES);
		return true;

		case R.id.stop+2:
			getWindow().clearFlags(WindowUtils.FLAG_DISABLE_HEAD_GESTURES);
		return true;			

		default:
			return super.onOptionsItemSelected(item);
		}
	}    	
}

