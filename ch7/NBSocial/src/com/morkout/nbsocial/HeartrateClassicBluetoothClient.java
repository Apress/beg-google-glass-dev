package com.morkout.nbsocial;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class HeartrateClassicBluetoothClient extends Activity {

	public final static String TAG = "ClassicBluetoothClient";
	public static final int REQUEST_TO_ENABLE_BT = 100;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothServerSocket mBluetoothServerSocket;
	private TextView mTvInfo;

	UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");
	/** Called when the activity is first created. */
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a ListView
				Log.v(TAG, "BroadcastReceiver on Receive - " + device.getName() + ": " + device.getAddress());

				String name = device.getName();
				if (name.equalsIgnoreCase("Jeff Nexus 7")) {
//				if (name.startsWith("SAMSUNG")) {
					new ConnectThread(device).start();
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Log.v(TAG, " BluetoothAdapter.ACTION_DISCOVERY_STARTED");
			}			
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.v(TAG, "BluetoothAdapter.ACTION_DISCOVERY_FINISHED");
			}			
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);
		mTvInfo.setTextSize(72);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.v(TAG,  "onCreate");
		if (mBluetoothAdapter == null) {
			Log.v(TAG, "Device does not support Bluetooth");
			return;
		}else{ 
			// device supports bluetooth
			if (!mBluetoothAdapter.isEnabled()) {
				Log.v(TAG, "Bluetooth supported but not enabled");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_TO_ENABLE_BT);
			}else{
				Log.v(TAG, "Bluetooth supported and enabled");

				discoverBluetoothDevices();

				getBondedDevices();

				//makeMeDiscoverable();

				//new AcceptThread().start();
				String myAddress = mBluetoothAdapter.getAddress();
				Log.v(TAG, "myAddress="+myAddress);
				//new ConnectThread(mBluetoothAdapter.getRemoteDevice(myAddress)).start();

			}        	
		}        

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "onActivityResult, requestCode="+requestCode);
		if (requestCode == REQUEST_TO_ENABLE_BT) { 

			discoverBluetoothDevices();

			getBondedDevices();

			//makeMeDiscoverable();

			//new AcceptThread().start();

			String myAddress = mBluetoothAdapter.getAddress();
			Log.v(TAG, "myAddress="+myAddress);
			//new ConnectThread(mBluetoothAdapter.getRemoteDevice(myAddress)).start();

			return;
		}
	}


	void discoverBluetoothDevices () {

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);       
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		mBluetoothAdapter.startDiscovery();
	}

	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}	

	// bonded devices are those that have already paired with the current device sometime in the past (and have not been unpaired)  
	void getBondedDevices () {
		Log.v(TAG, "getBondedDevices");
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
				Log.v(TAG, "bounded device - " + device.getName() + ": " + device.getAddress());

				if (device.getName().equalsIgnoreCase("Jeff Nexus 7")) {
					new ConnectThread(device).start();
					break;
				}
			}
		}		
		else {
			Log.v(TAG, "No bonded devices");
		}
	}

	void makeMeDiscoverable () {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivity(discoverableIntent);

	}	

	// client thread
	private class ConnectThread extends Thread {
		private InputStream mmInStream;
		private OutputStream mmOutStream;		
		String result;
		int bytesRead;
		int total;		
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app�s UUID string, also used by the server code
				Log.v(TAG, "before createRfcommSocketToServiceRecord");
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				Log.v(TAG, "after createRfcommSocketToServiceRecord");
			} catch (IOException e) { 
				Log.v(TAG, " createRfcommSocketToServiceRecord exception: "+ e.getMessage());
			}
			mmSocket = tmp;
		}
		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				Log.v(TAG, "before  connect");

				mmSocket.connect();

				Log.v(TAG, "after connect");				
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				Log.v(TAG, "connectException: "+ connectException.getMessage());
				try {
					mmSocket.close();
				} catch (IOException closeException) { }
				return;
			}
			// Do work to manage the connection (in a separate thread)
			Log.v(TAG, "manageConnectedSocket");
			manageConnectedSocket(mmSocket);
		}	


		private void manageConnectedSocket(BluetoothSocket socket) {

			try {
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				do {
					result = input.readLine();
					runOnUiThread(new Runnable() {
						public void run() {
							mTvInfo.setText("BL Heart rate: " + result);
						}
					});					
				} while (result != null);				
				socket.close();
			} catch (IOException e) {
				//Log.e(TAG, "Message received failed.", e);
				try {socket.close(); }
				catch (IOException e2) {
					Log.e(TAG, "socket close exception:", e2);
				}
			}

		}	
	}
}


