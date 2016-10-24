package com.morkout.myvideoapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class VideoFramesActivity extends Activity {

	ImageView mFrame;
	MediaMetadataRetriever mRetriever;
	int sec = 0; // frame rate is 30. so 1 frame per 1/30 second = 1000000/30 microseconds = 33333
	public final static String TAG = "VideoFramesActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videoframe);

		mFrame = (ImageView) findViewById(R.id.frame);

		if (!OpenCVLoader.initDebug()) {	    	
			finish();
		}			

		showFrame();

	}	

	String copyAsset(String filename) {
		final String PATH = Environment.getExternalStorageDirectory().toString() + "/myvideoapps/";
		File dir = new File(PATH);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Log.v(TAG, "ERROR: Creation of directory " + PATH + " on sdcard failed");
				return null;
			} else {
				Log.v(TAG, "Created directory " + PATH + " on sdcard");
			}
		}

		if (!(new File( PATH + filename).exists())) {
			Log.v(TAG, "copying file " + filename);
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open(filename);
				OutputStream out = new FileOutputStream(PATH + filename);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + filename + e.toString());
				return null;
			}
		}    	
		return PATH + filename; 
	}

	private final Handler mHandler = new Handler();  
	private final Runnable mUpdateFrameRunnable = new Runnable() {
		@Override
		public void run() {

			mRetriever = new MediaMetadataRetriever();

			try {
				mRetriever.setDataSource(copyAsset("in.mp4"));
				mFrame.setImageBitmap(processBitmap(mRetriever.getFrameAtTime(sec,MediaMetadataRetriever.OPTION_CLOSEST)));
				//mFrame.setImageBitmap(mRetriever.getFrameAtTime(sec,MediaMetadataRetriever.OPTION_CLOSEST));
				//getFrameAtTime takes 0.6-1.2 seconds to return.

				mFrame.invalidate();
				Log.v(">>>", "sec="+sec);
				sec+=500000;
				if (sec>3000000) return;

				//mHandler.postDelayed(mUpdateFrameRunnable, 33); //500);
				mHandler.post(mUpdateFrameRunnable);


			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			} catch (RuntimeException ex) {
				ex.printStackTrace();
			} finally {
				try {
					mRetriever.release();
				} catch (RuntimeException ex) {
				}
			}
		}
	};    


	public Bitmap processBitmap(Bitmap bitmap) {
		Bitmap newBitmap;
		Mat matFrom = new Mat();
		
		int h = (int) ( bitmap.getHeight() * (640.0 / bitmap.getWidth()) );
		Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 640, h, true);
		
		Utils.bitmapToMat(scaled, matFrom);


		//////////////////////////////////////////////////////////////////////////////////
		Mat matIntermediate = new Mat();
		Mat matFinal = new Mat();

		Imgproc.resize(matFrom, matIntermediate, new Size(), 2.0, 2.0, Imgproc.INTER_NEAREST);

		// OpenCV has powerful core and imgproc modules, which may be useful in a wide range of applications, 
		// especially in the field of Computational Photography

		// canny
		Mat matIntermediate2 = new Mat();
		Imgproc.Canny(matIntermediate, matIntermediate2, 80, 90);
		Imgproc.cvtColor(matIntermediate2, matFinal, Imgproc.COLOR_GRAY2BGRA, 4);

		// sepia
//		 Mat mSepiaKernel;
//		 mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
//		 mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
//		 mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
//		 mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
//		 mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
//		 Core.transform(matIntermediate, matFinal, mSepiaKernel);

		// pixelize
		// Size size = new Size();
		// Mat matIntermediate2 = new Mat();
		// Imgproc.resize(matIntermediate, matIntermediate2, size, 0.1, 0.1, Imgproc.INTER_NEAREST);
		// Imgproc.resize(matIntermediate2, matFinal, matIntermediate.size(), 0., 0., Imgproc.INTER_NEAREST);

		// posterize
		// Mat matIntermediate2 = new Mat();
		// Imgproc.Canny(matIntermediate, matIntermediate2, 80, 90);
		// matIntermediate.setTo(new Scalar(0, 0, 0, 255), matIntermediate2);
		// Core.convertScaleAbs(matIntermediate, matIntermediate2, 1./16, 0);
		// Core.convertScaleAbs(matIntermediate2, matFinal, 16, 0);		

		// color change
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2GRAY); // black and white
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2RGB);  // blue
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2Luv); // colorful #1
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2HLS); // colorful #2
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2HSV); // colorful #3
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2XYZ); // a little black and white
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2YUV); // similar to colorful #1
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2BGR555); // no image
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2BGR565); // no image
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGRA2BGR); // no effect
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2BGRA); // a little blue
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2Lab); // colorful #4 
		//Imgproc.cvtColor(matIntermediate, matFinal, Imgproc.COLOR_BGR2YCrCb); // similar to colorful #4 

		////////////////////////////////////////////////////////////////////////////////		



		newBitmap = Bitmap.createBitmap(matFinal.cols(), matFinal.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(matFinal, newBitmap);

		matFinal.release();     	     		 


		return newBitmap;
	}

	public void showFrame() {
		mHandler.post(mUpdateFrameRunnable);

		//		 mHandler.postDelayed(new Runnable() {
		//			    public void run () {
		//			        mTimelineManager.delete(cardId);
		//			    }
		//			}, 5000);		 
	}	

}
