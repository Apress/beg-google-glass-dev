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



public class SocketClientActivity extends Activity implements Runnable{
	private TextView mTvInfo;
	String mResult;
	Socket mClientSocket;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);
		mTvInfo.setText("Connecting to server ...");
		Thread thread = new Thread(this);
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
		// String serverIP = "192.168.1.7"; // my Mac on 28
		String serverIP = "10.0.0.10"; // my Mac on 82
		//String serverIP = "192.168.1.9"; // my Nexus
		int port = SocketServerActivity.PORT;
		try
		{
			System.out.println("Connecting to " + serverIP + " on port " + port);
			mClientSocket = new Socket(serverIP, port);

			BufferedReader input = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
			mResult = input.readLine();	         
			System.out.println("result="+mResult);	         
			runOnUiThread(new Runnable() {
				public void run() {
					WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
					List<WifiConfiguration> l =  wim.getConfiguredNetworks(); 
					WifiConfiguration wc = l.get(0); 						

					mTvInfo.setText("CLIENT IP: " + Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress()) + ", connected to " + mClientSocket.getRemoteSocketAddress() + ", received: "+mResult);
				}
			});			         

			PrintWriter out = new PrintWriter(mClientSocket.getOutputStream(), true);
			out.println(Integer.parseInt(mResult)+50);
			mClientSocket.close();

		}
		catch(IOException e) {
			final IOException ex = e;
			runOnUiThread(new Runnable() {
				public void run() {
					mTvInfo.setText(ex.getMessage());
				}
			});	
		}
	}
}
