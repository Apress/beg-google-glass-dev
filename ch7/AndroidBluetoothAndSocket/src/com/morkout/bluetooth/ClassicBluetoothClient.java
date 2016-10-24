package com.morkout.bluetooth;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ClassicBluetoothClient extends Activity {

	public final static String TAG = "ClassicBluetoothClient";
	public static final int REQUEST_TO_ENABLE_BT = 100;
	private BluetoothAdapter mBluetoothAdapter;
	private TextView mTvInfo;
	private UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");	
	private final static String FILE_PATH_RECEIVED = Environment.getExternalStorageDirectory().getPath()  +"/filefromCBTserver"; 

	// replace this with your own device names  
//	private final static String CBT_SERVER_DEVICE_NAME = "Jeff Nexus 7";  
	private final static String CBT_SERVER_DEVICE_NAME = "Jeff Tang's Glass"; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);
		mTvInfo.setText("Classic Bluetooth Client");
		//mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();		
		
		if (mBluetoothAdapter == null) {
			Log.v(TAG, "Device does not support Bluetooth");
			Toast.makeText(ClassicBluetoothClient.this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show();			
			return;
		}
		else { 
			if (!mBluetoothAdapter.isEnabled()) {
				Log.v(TAG, "Bluetooth supported but not enabled");
				Toast.makeText(ClassicBluetoothClient.this, "Bluetooth supported but not enabled", Toast.LENGTH_LONG).show();							
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_TO_ENABLE_BT); 
			}else{
				Log.v(TAG, "Bluetooth supported and enabled");
				// discover new Bluetooth devices
				discoverBluetoothDevices();

				// find devices that have been paired 
				getBondedDevices();
			}        	
		}        

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TO_ENABLE_BT) { 
			discoverBluetoothDevices();
			getBondedDevices();
			return;
		}
	}	

	void discoverBluetoothDevices () {
		// register a BroadcastReceiver for the ACTION_FOUND Intent 
		// to receive info about each device discovered.
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);       
		registerReceiver(mReceiver, filter); 
		mBluetoothAdapter.startDiscovery();
	}

	// for each device discovered, the broadcast info is received
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.v(TAG, "BroadcastReceiver on Receive - " + device.getName() + ": " + device.getAddress());
				String name = device.getName();

				// found another Android device of mine and start communication
				if (name != null && name.equalsIgnoreCase(CBT_SERVER_DEVICE_NAME)) {
					new ConnectThread(device).start();
				}
			}			
		}
	};

	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}	

	// bonded devices are those that have already paired with the current device sometime in the past (and have not been unpaired)  
	void getBondedDevices () {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.v(TAG, "bonded device - " + device.getName() + ": " + device.getAddress());
				if (device.getName().equalsIgnoreCase(CBT_SERVER_DEVICE_NAME)) {
					Log.d(TAG, CBT_SERVER_DEVICE_NAME);
					new ConnectThread(device).start();
					break;
				}
			}
		}		
		else {
			Toast.makeText(ClassicBluetoothClient.this, "No bonded devices", Toast.LENGTH_LONG).show();			
		}
	}

	private class ConnectThread extends Thread {
		int bytesRead;
		int total;		
		private final BluetoothSocket mmSocket;
		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
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
				mmSocket.connect();
			} catch (IOException e) {
				Log.v(TAG, e.getMessage());
				try {
					mmSocket.close();
				} catch (IOException closeException) { }
				return;
			}
			manageConnectedSocket(mmSocket);
		}	

		private void manageConnectedSocket(BluetoothSocket socket) {
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;

			try {
				InputStream instream = socket.getInputStream();
				fos = new FileOutputStream( FILE_PATH_RECEIVED );
				bos = new BufferedOutputStream(fos);
				bytesRead = -1;
				total = 0;
				while ((bytesRead = instream.read(buffer)) > 0) {
					total += bytesRead;
					bos.write(buffer, 0, bytesRead);
					Log.i(TAG, "bytesRead="+bytesRead+",bufferSize="+bufferSize+",total="+total);
					runOnUiThread(new Runnable() {
						public void run() {
							mTvInfo.setText("bytesRead="+bytesRead+", total="+total);
						}
					});						
				}
				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText(total + " bytes of file " + FILE_PATH_RECEIVED + "has been received");
					}
				});								
				bos.close();
				socket.close();
			} catch (IOException e) {
				try {
					socket.close(); 
					bos.close();}
				catch (IOException e2) {
					Log.e(TAG, "socket close exception:", e2);
				}
			}
		}	
	}
}


