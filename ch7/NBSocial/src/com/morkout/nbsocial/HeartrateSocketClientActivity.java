package com.morkout.nbsocial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;

public class HeartrateSocketClientActivity extends Activity implements Runnable{
	private TextView mTvInfo;
	String mResult;
	Socket mClientSocket;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);
		mTvInfo.setTextSize(72);
		mTvInfo.setText("Connecting to server ...");
		Thread thread = new Thread(HeartrateSocketClientActivity.this);
		thread.start();		
	}

	@Override
	protected void onStop() {
		super.onStop();  
		try {
			if (mClientSocket != null) mClientSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void run()
	{
		String serverName = "10.0.0.11";
		int port = 6683;
		try
		{
			System.out.println("Connecting to " + serverName + " on port " + port);
			mClientSocket = new Socket(serverName, port);
			System.out.println("Client!!!! Just connected to " + mClientSocket.getRemoteSocketAddress());

			BufferedReader input = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
			do {
				mResult = input.readLine();
				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText("Heart rate: " + mResult);
					}
				});					
			} while (mResult != null);
			
			System.out.println("result="+mResult);	         
			runOnUiThread(new Runnable() {
				public void run() {
					WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
					List<WifiConfiguration> l =  wim.getConfiguredNetworks(); 
					WifiConfiguration wc = l.get(0); 						

					mTvInfo.setText("CLIENT IP: " + Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress()) + ", connected to " + mClientSocket.getRemoteSocketAddress() + ", received: "+mResult);// + socket.getRemoteSocketAddress());
				}
			});			         

//			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//			out.println(Integer.parseInt(result)+50);
			mClientSocket.close();

		}
		catch(Exception e) {
			try { mClientSocket.close(); }
			catch (Exception e2) {};
			e.printStackTrace();
		}
	}
}
