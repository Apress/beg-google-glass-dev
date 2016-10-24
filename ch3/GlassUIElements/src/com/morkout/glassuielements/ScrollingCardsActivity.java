package com.morkout.glassuielements;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.MenuUtils;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class ScrollingCardsActivity extends Activity {
	private List<View> mCards;
	private CardScrollView mCardScrollView;
	private static final String TAG = "ScrollingCardsActivity";

	private GestureDetector mGestureDetector;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createCards();

		mCardScrollView = new CardScrollView(this);

		ExampleCardScrollAdapter adapter = new ExampleCardScrollAdapter();
		mCardScrollView.setAdapter(adapter);
		mCardScrollView.activate();
		setContentView(mCardScrollView);

		mGestureDetector = new GestureDetector(this);

		// Called when the following gestures happen: TAP, LONG_PRESS SWIPE_UP, 
		// SWIPE_LEFT, SWIPE_RIGHT, SWIPE_DOWN
		mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					Log.v(TAG, "TAP");

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
	}


	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}        


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.juststop, menu);

		MenuItem item2 = menu.add(0, R.id.stop+1, 0, R.string.headoff);
		MenuItem item3 = menu.add(0, R.id.stop+2, 0, R.string.headon);

		MenuUtils.setDescription(item2, R.string.headoffDesc);
		MenuUtils.setDescription(item3, R.string.headonDesc);
		MenuUtils.setInitialMenuItem(menu, item2);

		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.stop:
			Toast.makeText(ScrollingCardsActivity.this, "stop", Toast.LENGTH_SHORT).show();
			finish();

			return true;

		case R.id.stop+1:
			Toast.makeText(ScrollingCardsActivity.this, "headoff", Toast.LENGTH_SHORT).show();
		return true;

		case R.id.stop+2:
			Toast.makeText(ScrollingCardsActivity.this, "headon", Toast.LENGTH_SHORT).show();
		return true;			

		default:
			return super.onOptionsItemSelected(item);
		}
	}    	      


	private void createCards() {
		mCards = new ArrayList<View>();

		Card card;

		card = new Card(this);
		card.setText("This card has a footer.");
		card.setFootnote("I'm the footer!");
		mCards.add(card.getView());

		card = new Card(this);
		card.setText("This card has a puppy background image.");
		card.setFootnote("How can you resist?");
		card.setImageLayout(Card.ImageLayout.FULL);
		card.addImage(R.drawable.frown);
		mCards.add(card.getView());

		card = new Card(this);
		card.setText("This card has a mosaic of puppies.");
		card.setFootnote("Aren't they precious?");
		card.addImage(R.drawable.laugh);
		card.addImage(R.drawable.smile);
		card.addImage(R.drawable.surprise);
		mCards.add(card.getView());


		MyGLView v = new MyGLView(this);
		mCards.add(v);
		GLSurfaceView v2 = new GLSurfaceView(this);
		v2.setRenderer(new TetrahedronRenderer(true));
		mCards.add(v2);
		DrawView v3 = new DrawView(this);
		mCards.add(v3);     

	}

	private class ExampleCardScrollAdapter extends CardScrollAdapter {
		@Override
		public int getPosition(Object item) {
			return mCards.indexOf(item);
		}

		@Override
		public int getCount() {
			return mCards.size();
		}

		@Override
		public Object getItem(int position) {
			return mCards.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return mCards.get(position);
		}
	}
}
