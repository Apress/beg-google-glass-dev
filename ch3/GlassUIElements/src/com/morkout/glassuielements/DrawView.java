package com.morkout.glassuielements;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class DrawView extends View {
	private static final String TAG = "DrawView";

	List<Point> points = new ArrayList<Point>();
	List<Paint> paints = new ArrayList<Paint>();
	//Paint paint = new Paint();

	public DrawView(Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);

		///this.setOnTouchListener(this);
		//       for (int i=1; i< 64; i++) {
		//		  Point point = new Point();
		//		  point.x = 10*i;
		//		  point.y = 5*i;
		//		  points.add(point);
		//       }      

		//        paint.setColor(Color.RED);
		//        paint.setAntiAlias(true);

		invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		//paint.setARGB(255, (int )(Math.random() * 255), (int )(Math.random() * 255), (int )(Math.random() * 255));

		// tested code for adding custom views to cardscrollview. Works!
		for (int j=0; j<5; j++) {
			Point pt = new Point();
			pt.x = (int )(Math.random() * 640 + 1);
			pt.y = (int )(Math.random() * 360 + 1); 
			Paint paint = new Paint();
			paint.setARGB(255, (int )(Math.random() * 255), (int )(Math.random() * 255), (int )(Math.random() * 255));
			paint.setAntiAlias(true);

			canvas.drawCircle(pt.x, pt.y, 25, paint);
		}
		
		int i=0;
		for (Point point : points) {
			//canvas.drawARGB(255, (int )(Math.random() * 255), (int )(Math.random() * 255), (int )(Math.random() * 255));
			canvas.drawCircle(point.x, point.y, 5, paints.get(i++));
			//Log.e(TAG, "Painting: "+point);
		}


	}

	//    public boolean onTouch(View view, MotionEvent event) {
	//        // if(event.getAction() != MotionEvent.ACTION_DOWN)
	//        // return super.onTouchEvent(event);
	//        Point point = new Point();
	//        point.x = event.getX();
	//        point.y = event.getY();
	//        points.add(point);
	//        invalidate();
	//        Log.d(TAG, "point: " + point);
	//        return true;
	//    }
}

class Point {
	float x, y;

	@Override
	public String toString() {
		return x + ", " + y;
	}
}