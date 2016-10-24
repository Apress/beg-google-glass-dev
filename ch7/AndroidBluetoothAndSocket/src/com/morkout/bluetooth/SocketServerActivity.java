package com.morkout.bluetooth;

import java.io.BufferedReader;
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
	private TextView mTvInfo;	
	private ServerSocket serverSocket;
	private int port = 6680;
	private Socket socket;
	String result;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);

		Thread thread = new Thread(SocketServerActivity.this);
		thread.start();		

	}


	public void run()
	{
		runOnUiThread(new Runnable() {
			public void run() {
				mTvInfo.setText("inside run");
			}
		});
		
		try {
			serverSocket = new ServerSocket(port);
			//serverSocket.setSoTimeout(100000);

			while(true)
			{
				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText("SERVER::: Waiting for client on port " + port);
					}
				});
				
				System.out.println("SERVER::: Waiting for client on port " +
						serverSocket.getLocalPort() + "...");
				socket = serverSocket.accept();
				//System.out.println("SERVER::: Just connected from " + socket.getRemoteSocketAddress());					);
				
				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText("SERVER::: Just connected from ");// + socket.getRemoteSocketAddress());
					}
				});				
				

				OutputStream oStream = socket.getOutputStream();				
				PrintWriter out = new PrintWriter(oStream, true);
				out.println(2014);
//				out.println("Empowered Nexus 7 Date Time Info: " + new Date().toString());

		         BufferedReader input =
		                 new BufferedReader(new InputStreamReader(socket.getInputStream()));
		         result = input.readLine();	         	         
				
					runOnUiThread(new Runnable() {
						public void run() {
							WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
							List<WifiConfiguration> l =  wim.getConfiguredNetworks(); 
							WifiConfiguration wc = l.get(0); 						
							
							mTvInfo.setText("SERVER IP: " + Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress()) + ", connected from " + socket.getRemoteSocketAddress() + ", received: "+result);// + socket.getRemoteSocketAddress());
							
						}
					});		
				
				socket.close();
				break;
			}
			
			serverSocket.close();

		}
		catch(SocketTimeoutException s)
		{
			System.out.println("Socket timed out!");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}		
}
