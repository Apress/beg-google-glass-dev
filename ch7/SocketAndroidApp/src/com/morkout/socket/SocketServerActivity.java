package com.morkout.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;

public class SocketServerActivity extends Activity implements Runnable {
	public final static int PORT = 6604;
	private TextView mTvInfo;	
	private ServerSocket mServerSocket;
	private Socket mClientSocket;
	String mResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);

		Thread thread = new Thread(this);
		thread.start();		
	}
	
	@Override
	protected void onStop() {
	    super.onStop();  
	    
	    try {
	    	if (mClientSocket != null) mClientSocket.close();
	    	if (mServerSocket != null) mServerSocket.close();
	    }
	    catch (IOException e) {
	    	e.printStackTrace();
	    }
	}

	public void run()
	{
		try {
			while(true)
			{
				mServerSocket = new ServerSocket(PORT);
				runOnUiThread(new Runnable() {
					public void run() {
						WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
						List<WifiConfiguration> l =  wim.getConfiguredNetworks(); 
						WifiConfiguration wc = l.get(0); 
						mTvInfo.setText(mTvInfo.getText() + "\n\nServer IP: " + Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress())  + ". Waiting for client on port " + PORT);
					}
				});
				mClientSocket = mServerSocket.accept();

				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText(mTvInfo.getText() + "\nJust connected from client" + mClientSocket.getRemoteSocketAddress());
					}
				});				


				OutputStream oStream = mClientSocket.getOutputStream();				
				PrintWriter out = new PrintWriter(oStream, true);
				BufferedReader input = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));

				out.println(2014);
				mResult = input.readLine();	         	         

				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText(mTvInfo.getText() + "\nSent: 2014. Received: "+mResult);
					}
				});		

				mClientSocket.close();
				mServerSocket.close();
			}
		}
		catch(SocketTimeoutException s)
		{
			runOnUiThread(new Runnable() {
				public void run() {
					mTvInfo.setText("Socket timed out!");
				}
			});			
		}catch(IOException e)
		{
			final IOException ex = e;
			runOnUiThread(new Runnable() {
				public void run() {
					mTvInfo.setText(ex.getMessage());
				}
			});			
		}
	}		
}
