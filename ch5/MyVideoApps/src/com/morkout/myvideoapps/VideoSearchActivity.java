package com.morkout.myvideoapps;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

class Video {
	String mtitle;
	String murl;
	String mviewCount;
	String mThumbnail;
	ImageView mImageView;
	Bitmap mBitmap;
	Boolean mDrawn;

	public Video(String title, String url, String viewCount, String thumbnail) {
		this.mtitle = title;
		this.murl = url;
		this.mviewCount = viewCount;
		this.mThumbnail = thumbnail;
		this.mDrawn = false;
	}

	public String getUrl() {
		return murl;
	}

	public String getTitle() {
		return mtitle;
	}

	public String getViewCount() {
		return mviewCount;
	}

	public String getThumbnail() {
		return mThumbnail;
	}
}


public class VideoSearchActivity extends Activity {
	JSONArray entries= null;
	private String url = "https://gdata.youtube.com/feeds/api/videos?q=<title>+lyrics&orderby=viewCount&v=2&alt=json";
	public static final String TAG = "VideoSearchActivity";
	WebView webView;
	public static final int VOICE_RECOGNIZER_REQUEST_CODE = 100;
	private String mTitle;

	public VideoSearchActivity mActivity;

	static Map<String, Bitmap> thumbnailMaps = new HashMap<String, Bitmap>();

	static ListView mListView; // a subview of AdapterView, used to bind data to its layout via an Adapter
	private VideoAdapter mMovieList; // Adapter, a middle man between the data source and the AdapterView layout (ListView here)
	private ArrayList<Video> mVideos = new ArrayList<Video>(); // data

	GestureDetector mGestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		setContentView(R.layout.videolist);

		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a song title:");
		startActivityForResult(i, VOICE_RECOGNIZER_REQUEST_CODE);  

		mGestureDetector = createGestureDetector(this);
	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) { // On Tap, generate a new number
					Video video = (Video)mListView.getSelectedItem();
					mActivity.launchVideo(video.murl);	            	
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					mListView.setSelection(mListView.getSelectedItemPosition()+1);
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					mListView.setSelection(mListView.getSelectedItemPosition()-1);
					return true;
				}
				return false;
			}
		});
		
		return gestureDetector;
	}

	// this method is required for tap on touchpad to work!
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}        	

	@Override
	protected void onActivityResult(int RequestCode, int ResultCode, Intent data) {
		Log.v(TAG, "onActivityResult:"+RequestCode + ","+ResultCode);
		switch(RequestCode) {
		case VOICE_RECOGNIZER_REQUEST_CODE:
			if(RequestCode == VOICE_RECOGNIZER_REQUEST_CODE && ResultCode == RESULT_OK) {
				ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				if (results.size() > 0) {
					Log.v(TAG, "results: " + results.get(0));
					mTitle = results.get(0);
					mTitle = mTitle.replace(' ', '+');
					url = url.replaceAll("<title>", mTitle);

					new GetVideos().execute();
				}
			}
			break;
		}
		super.onActivityResult(RequestCode, ResultCode, data);
	}

	// Async task class to get json via YouTube API 
	private class GetVideos extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {			
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse = httpClient.execute(httpGet);	 
				HttpEntity httpEntity = httpResponse.getEntity();

				String jsonStr = EntityUtils.toString(httpEntity);
				if (jsonStr != null) {
					JSONObject jsonObj = new JSONObject(jsonStr);
					entries = jsonObj.getJSONObject("feed").getJSONArray("entry");
				} else {
					Log.e(TAG, "Couldn't get any data from the url");
				}

			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}			
			return null;
		}		

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			Log.v(TAG, "entries size="+entries.length());
			try {	
				for (int i = 0; i < entries.length(); i++) {
					JSONObject entry = entries.getJSONObject(i);

					String title = entry.getJSONObject("title").getString("$t");
					String viewCount = entry.getJSONObject("yt$statistics").getString("viewCount");
					Log.v(TAG, "title="+title+",mTitle="+mTitle);

					String thumbnail = entry.getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
					Log.v(TAG, "thumbnail="+thumbnail);

					JSONObject link = entry.getJSONArray("link").getJSONObject(0);
					mVideos.add(new Video(title, link.getString("href"), viewCount, thumbnail));

				}

				mListView = (ListView) findViewById(R.id.listView);
				mMovieList = new VideoAdapter(mActivity, R.layout.listitem, mVideos); // middle man, passing parameters context, layout, and data
				mListView.setAdapter(mMovieList); // set adapeter for adapter view subclass (listview)
				mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				mListView.setClickable(true);
				//				mListView.setOnItemClickListener(new VideoLauncher());				

			}
			catch (Exception e) {
			}
		}

	}


	public void launchVideo(String youtubeurl) {
		Intent i = new Intent();
		i.setAction("com.google.glass.action.VIDEOPLAYER");
		i.putExtra("video_url", youtubeurl);
		startActivity(i);
	}

	//	private class VideoLauncher implements AdapterView.OnItemClickListener {
	//		public void onItemClick(AdapterView parent, View v, int position, long id) {
	//			Video video = (Video)parent.getSelectedItem();
	//			mActivity.launchVideo(video.murl);
	//		}
	//	}	

	//	void playVideo(String url) {
	//		setContentView(R.layout.videoview);
	//		VideoView video_player_view = (VideoView)  findViewById(R.id.video_player_view);
	//
	//		DisplayMetrics dm = new DisplayMetrics();
	//		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
	//		int height = dm.heightPixels;
	//		int width = dm.widthPixels;
	//		Log.v(TAG,  "width="+width+",height="+height);
	//		video_player_view.setMinimumWidth(width);
	//		video_player_view.setMinimumHeight(height);
	//
	//		MediaController media_Controller = new MediaController(this);
	//		video_player_view.setMediaController(media_Controller);
	//
	//		Uri uri = Uri.parse(url);        
	//		video_player_view.setVideoURI(uri);
	//		video_player_view.start();	    		
	//	}
}



class VideoAdapter extends ArrayAdapter<Video> {

	// declaring our ArrayList of items
	private ArrayList<Video> videos;

	/* here we must override the constructor for ArrayAdapter
	 * the only variable we care about now is ArrayList<Item> objects,
	 * because it is the list of objects we want to display.
	 */
	public VideoAdapter(Context context, int resource, ArrayList<Video> videos) {
		super(context, resource, videos);
		this.videos = videos;
	}

	// for each list item 
	public View getView(int position, View convertView, ViewGroup parent){
		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.listitem, null);
		}

		Video video = videos.get(position);

		if (video != null) {
			TextView title = (TextView) v.findViewById(R.id.title);
			TextView viewCount = (TextView) v.findViewById(R.id.viewcount);
			ImageView thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
			video.mImageView = thumbnail;
			if (!(VideoSearchActivity.thumbnailMaps.containsKey(video.getThumbnail()))) {
				// set image asynchronously 
				new SetImageTask().execute(video); 
			}			

			// check to see if each individual textview is null.
			// if not, assign some text!
			if (title != null){
				title.setText(video.getTitle());
			}
			if (viewCount != null){
				viewCount.setText("Views: " + video.getViewCount());
			}

			if (VideoSearchActivity.thumbnailMaps.containsKey(video.getThumbnail()))
				thumbnail.setImageBitmap(VideoSearchActivity.thumbnailMaps.get(video.getThumbnail()));
		}

		// the view must be returned to our activity
		return v;

	}


	private class SetImageTask extends AsyncTask<Video,Void,Void>
	{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}


		@Override
		protected Void doInBackground(Video... params) {
			try
			{
				Video video = params[0];

				VideoSearchActivity.thumbnailMaps.put(video.getThumbnail(), downloadBitmap(video.getThumbnail()));
				Log.v(">>>>",  "url:"+video.getUrl()+",thumbnail="+video.getThumbnail());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			for (int i = 0; i < videos.size(); i++) {
				Video v = videos.get(i);
				if(v.mBitmap!=null && !v.mDrawn) {
					v.mImageView.setImageBitmap(v.mBitmap);
					v.mDrawn = true;
				}
				if (VideoSearchActivity.thumbnailMaps.containsKey(v.getThumbnail()))
					v.mImageView.setImageBitmap(VideoSearchActivity.thumbnailMaps.get(v.getThumbnail()));
			}
		}   
	}

	private Bitmap downloadBitmap(String url) {
		// initilize the default HTTP client object
		final DefaultHttpClient client = new DefaultHttpClient();
		Bitmap image = null;
		//forming a HttoGet request 
		final HttpGet getRequest = new HttpGet(url);
		try {

			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode + 
						" while retrieving bitmap from " + url);
				return null;

			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					// getting contents from the stream 
					inputStream = entity.getContent();

					// decoding stream data back into image Bitmap that android understands
					image = BitmapFactory.decodeStream(inputStream);


				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			// You Could provide a more explicit error message for IOException
			getRequest.abort();
			Log.e("ImageDownloader", "Something went wrong while" +
					" retrieving bitmap from " + url + e.toString());
		} 

		return image;
	}

}
