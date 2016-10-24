package com.morkout.locationsensors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class ScrollingCardsActivity extends Activity {
	private List<View> mCards;
	private CardScrollView mCardScrollView;
	ExampleCardScrollAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		Bundle extras = getIntent().getExtras();
		String ptype = (String)extras.get("ptype");
		Double latitude = (Double)extras.get("latitude");
		Double longitude = (Double)extras.get("longitude");

		System.out.println("ptype="+ptype+",latitude="+latitude+",longitude="+longitude);

		mCardScrollView = new CardScrollView(this);      

		new QueryPlacesTask().execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
				latitude.doubleValue()+","+
				longitude.doubleValue()+"&radius=1500&types="+ptype +"&sensor=false&key=AIzaSyDGR5hcXU58CqTSO6ZlzK8B0d0Q4_duZ8U");


	}      


	private class QueryPlacesTask extends AsyncTask<String, Void, JSONArray> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected JSONArray doInBackground(String... stringURL) {		  
			JSONArray entries = null;
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(stringURL[0]);

				System.out.println(stringURL[0]);

				HttpResponse httpResponse = httpClient.execute(httpGet);	 
				HttpEntity httpEntity = httpResponse.getEntity();

				String jsonStr = EntityUtils.toString(httpEntity);

				System.out.println(jsonStr);


				if (jsonStr != null) {
					JSONObject jsonObj = new JSONObject(jsonStr);
					entries = jsonObj.getJSONArray("results");
				} else {
					Log.e(">>>", "Couldn't get any data from the url");
				}

			} catch (IOException e) {
				Log.e(">>>", e.getMessage());
			}		
			catch (JSONException e) {
				Log.e(">>>", e.getMessage());
			}

			return entries;
		}

		@Override
		protected void onPostExecute(JSONArray entries) {
			if (entries.length() == 0) {
				Toast.makeText(ScrollingCardsActivity.this, "No Results Found", Toast.LENGTH_LONG).show();

				return;
			}

			System.out.println("entries length="+entries.length());
			mCards = new ArrayList<View>();
			try {	
				for (int i = 0; i < entries.length(); i++) {
					JSONObject entry = entries.getJSONObject(i);

					String name = entry.getString("name");
					String address = entry.getString("vicinity");
					double lat = entry.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
					double lng = entry.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
					String id = entry.getString("id");

					Card card;
					card = new Card(ScrollingCardsActivity.this);
					card.setText(name);
					card.setFootnote(address);

					new ImageLoadingTask().execute(id, card, "http://maps.googleapis.com/maps/api/staticmap?zoom=15&size=640x360&markers=color:red%7C"+lat + "," + lng + "&sensor=false");
					//mCards.add(card.toView());
				}

			}
			catch (JSONException e) {
			}			



			mAdapter = new ExampleCardScrollAdapter();
			mCardScrollView.setAdapter(mAdapter);
			mCardScrollView.activate();
			setContentView(mCardScrollView);

			super.onPostExecute(entries);
		}

	}

	class CardImg {
		Card mCard;
		String mFilename;

		public CardImg(Card card, String filename) {
			mCard = card;
			mFilename = filename;
		}
	}	

	private class ImageLoadingTask extends AsyncTask<Object, Void, CardImg> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected CardImg doInBackground(Object... params) {		        
			Bitmap bmp = null;
			String id = (String)params[0];
			Card card = (Card)params[1];
			File cacheDir = null;
			try {
				URL url = new URL((String)params[2]);
				System.out.println("requesting " + url.getPath());

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				BitmapFactory.Options options = new BitmapFactory.Options();
				bmp = BitmapFactory.decodeStream(is, null, options);
				
				cacheDir = getCacheDir();
				//				if (! cacheDir.exists()){
				//					if (! cacheDir.mkdirs()){
				//						return null;
				//					}
				//				}				
				FileOutputStream out = new FileOutputStream(cacheDir + "/" + id);
				System.out.println("writing to " + cacheDir + "/" + id );
				bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			return new CardImg(card, cacheDir + "/" + id);
		}

		@Override
		protected void onPostExecute(CardImg cardImg) {
			System.out.println("add " + cardImg.mFilename + " to " + cardImg.mCard);
			//mCards.remove(cardImg.mCard.toView());
			cardImg.mCard.addImage(BitmapFactory.decodeFile(cardImg.mFilename));
			mCards.add(cardImg.mCard.getView());
			mAdapter.notifyDataSetChanged();
			super.onPostExecute(cardImg);
		}

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
