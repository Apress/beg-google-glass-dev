package com.morkout.smartcamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class PhotoViewActivity extends Activity
{
	public static String TAG = "PhotoViewActivity";
	private List<View> mCards;
	private CardScrollView mCardScrollView;

	private GestureDetector mGestureDetector;


	ArrayList<FilenameDir> mPicInfo = new ArrayList<FilenameDir>();    
	final private String mAppPicDir = Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_PICTURES + "/SmartCamera";
	final private String mPicDir = Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DCIM + "/Camera";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (! new File(mAppPicDir).exists()){
			if (! new File(mAppPicDir).mkdirs()){
				Log.d(TAG, "failed to create directory: " + mAppPicDir);
			}
		}			
		
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
		inflater.inflate(R.menu.imageview, menu);

		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.upload:
			Toast.makeText(PhotoViewActivity.this, "upload", Toast.LENGTH_SHORT).show();

			return true;

		case R.id.email:
			Toast.makeText(PhotoViewActivity.this, "email", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.delete:
			Toast.makeText(PhotoViewActivity.this, "delete", Toast.LENGTH_SHORT).show();
			return true;			

		default:
			return super.onOptionsItemSelected(item);
		}
	}    	      


	private void createCards() {
		mCards = new ArrayList<View>();

		getPictureLists(mPicDir);

		Log.v(TAG, "mPicInfo size:"+mPicInfo.size());

		// TODO: test performance improvement - show only 3 cards, when moving to next or previous card, update the cards in cardscrollview
		for (FilenameDir fDir : mPicInfo) {
			Log.v(TAG, fDir.mFilename + ", " + fDir.mDirname);

			Card card = new Card(this);
			card.setFootnote(fDir.mDirname + "/" + fDir.mFilename);
			Log.v(TAG, "addImage: "+ fDir.mDirname + "/" + fDir.mFilename);
			card.setImageLayout(Card.ImageLayout.FULL);

			//card.addImage(Uri.fromFile(new File(fDir.mDirname + "/" + fDir.mFilename)));            
			// without scale down, you get "Bitmap too large to be uploaded into a texture" error
			Bitmap myBitmap = BitmapFactory.decodeFile(fDir.mDirname + "/" + fDir.mFilename);
			Log.v(TAG, "myBitmap:"+myBitmap.getHeight()+","+myBitmap.getWidth());
			int h = (int) ( myBitmap.getHeight() * (640.0 / myBitmap.getWidth()) );
			Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 640, h, true);            

			try {
				File file = new File(mAppPicDir + "/scaled-" + fDir.mFilename);
				FileOutputStream fOut = new FileOutputStream(file);
				scaled.compress(Bitmap.CompressFormat.PNG, 85, fOut);
				fOut.flush();
				fOut.close();
			}
			catch (Exception e) {
				Log.v(TAG, ">>>" + e.getMessage());
			}

			card.addImage(BitmapFactory.decodeFile(mAppPicDir + "/scaled-" + fDir.mFilename));            

			mCards.add(card.getView());        	
		}


	}

	private class ExampleCardScrollAdapter extends CardScrollAdapter {
//		@Override
//		public int findIdPosition(Object id) {
//			return -1;
//		}

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
			return mCards.get(position);//.toView();
		}
	}	



	// access the external directory
	private void getPictureLists(String directory) {
		Log.v(TAG, "Pictures directory "+directory);

		File dir = new File(directory);
		File[] files = dir.listFiles();
		int count = 1;
		for (File file : files) {

			if (file.isDirectory()) {
				Log.v(TAG, ">>> " + file.getAbsolutePath() + " is a directory");
				if (file.getAbsolutePath().indexOf("/Pictures/cache") == -1)
					getPictureLists(file.getAbsolutePath());
			}
			else {
				if (file.getName().indexOf(".jpg") == -1) {
					Log.v(TAG, "non jpg file found, continue loop");
					continue;
				}        		

				Log.v(TAG, ">>> " + file.getAbsolutePath() + " is a file");
				if (count++ == 10) break; // 20 still ok. 50 out of memory
				mPicInfo.add(new FilenameDir(file.getName(), directory));
			}
		}
		//java.util.Collections.sort(filenames);


	}

	private void copyFromTo(String fromFilepath, String toDir) {
		File toFile = new File(toDir);
		if (!toFile.exists()) {
			try {
				if (! toFile.mkdirs()){
					Log.d(TAG, "failed to create directory " + toDir);
					return;
				}
			} catch (Exception e) {
				Log.e(TAG, "Was unable to copy " + fromFilepath +  e.toString());
				return;
			}
		}   
		try {
			InputStream in = new FileInputStream(fromFilepath);
			OutputStream out = new FileOutputStream(toDir + "/" + new File(fromFilepath).getName());		 

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

			Log.v(TAG, "Copied " + fromFilepath + " to " + toDir + "/" + new File(fromFilepath).getName());		
		}
		catch (Exception e) {
			Log.e(TAG, "Was unable to copy " + fromFilepath +  e.toString());
			return;
		}
	}


	private class FilenameDir {
		private String mFilename;
		private String mDirname;

		public FilenameDir(String filename, String dirname)
		{
			mFilename = filename;
			mDirname = dirname;
		}
	}
}