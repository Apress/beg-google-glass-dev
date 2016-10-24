package com.morkout.bluetooth;

import java.io.BufferedReader;
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
	String result;
	Socket socket;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);
		Thread thread = new Thread(SocketClientActivity.this);
		thread.start();		

	}

	
	public void run()
	{		
		String serverName = "192.168.1.9"; // Glass IP when connected with Netgear28
//		String serverName = "192.168.1.17"; // Empowered Nexus IP when connected with Netgear28
//		String serverName = "192.168.24.75"; // Empowered Nexus IP when connected with Empowered
	      int port = 6680;
	      try
	      {
	         System.out.println("Connecting to " + serverName
	                             + " on port " + port);
	         socket = new Socket(serverName, port);
	         System.out.println("Client!!!! Just connected to "
	                      + socket.getRemoteSocketAddress());
	         
	         BufferedReader input =
	                 new BufferedReader(new InputStreamReader(socket.getInputStream()));
	         result = input.readLine();	         
	         System.out.println("result="+result);	         
				runOnUiThread(new Runnable() {
					public void run() {
						WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
						List<WifiConfiguration> l =  wim.getConfiguredNetworks(); 
						WifiConfiguration wc = l.get(0); 						
						
						mTvInfo.setText("CLIENT IP: " + Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress()) + ", connected to " + socket.getRemoteSocketAddress() + ", received: "+result);// + socket.getRemoteSocketAddress());
					}
				});			         

				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				out.println(Integer.parseInt(result)+50);
			   socket.close();
	      
	      }catch(Exception e)
	      {
	         e.printStackTrace();
	      }
	}
}
