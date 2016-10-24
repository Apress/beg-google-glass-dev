/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity implements Runnable {
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;

	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;


	private ServerSocket serverSocket;
	private int port = 6680;
	private Socket socket;    

	BluetoothServerSocket mBluetoothServerSocket;
	public static final int REQUEST_FOR_SELF_DISCOVERY = 200;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.title_devices);
		mHandler = new Handler();

		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		Log.v("!!!!!", "Start socket thread");		

		// start a socket server
//		Thread thread = new Thread(DeviceScanActivity.this);
//		thread.start();

		// start a Classic Bluetooth Server
//		Intent discoverableIntent = new Intent(
//				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//		discoverableIntent.putExtra(
//				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500); // default is 120 seconds
//		startActivityForResult(discoverableIntent, REQUEST_FOR_SELF_DISCOVERY);
		
		new AcceptThread().start();
	}




	public void run()
	{
		try {
			
			Log.v("!!!!!", "before ServerSocket: " + port);		
			
			serverSocket = new ServerSocket(port);

			Log.v("!!!!!", "after ServerSocket: " + port);		

			runOnUiThread(new Runnable() {
				public void run() {
					WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
					List<WifiConfiguration> l =  wim.getConfiguredNetworks(); 
					WifiConfiguration wc = l.get(0); 
					Log.v("!!!!!", "SERVER::: IP=" + Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress())  + ", Waiting for client on port " + port);
				}
			});

			Log.v("!!!!!", "SERVER::: Waiting for client on port " + serverSocket.getLocalPort() + "...");
			socket = serverSocket.accept();
			Log.v("!!!!!", "after accept");

			runOnUiThread(new Runnable() {
				public void run() {
					Log.v("!!!!!", "SERVER::: Just connected from " + socket.getRemoteSocketAddress());
				}
			});				


			OutputStream oStream = socket.getOutputStream();				
			PrintWriter out = new PrintWriter(oStream, true);
			int count = 1;
			while (count < 120)
			{
				out.println(BluetoothLeService.getHeartRate());
				Thread.sleep(1000);
			}
			//BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//result = input.readLine();	         	         

			socket.close();

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




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			mLeDeviceListAdapter.clear();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
		// fire an intent to display a dialog asking the user to grant permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		setListAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		else if (requestCode == REQUEST_FOR_SELF_DISCOVERY) {
			new AcceptThread().start();
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}


	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			// Use a temporary object that is later assigned
			// to mmServerSocket, because mServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				tmp = mBluetoothAdapter
						.listenUsingRfcommWithServiceRecord(
								"BluetoothOnNexusEmp",
								UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7"));
			} catch (IOException e) {
			}
			mServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			try {
				// after server socket is obtained in the constructor,
				// accept is used to listen for a client request
				socket = mServerSocket.accept(); // blocking call so should
			} catch (IOException e) {
			}
			// If a connection was accepted
			if (socket != null) {
				new ConnectedThread(socket).start();
				try {
					mServerSocket.close();						
				} catch (IOException e) {
				}
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final OutputStream mOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mSocket = socket;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			mOutStream = tmpOut;
		}

		public void run() {
			int count = 1;
			while (count++ < 120) {
				PrintWriter out = new PrintWriter(mOutStream, true);
				out.println(BluetoothLeService.getHeartRate());
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}

			try {
				mSocket.close();

				new AcceptThread().start();				
			} catch (IOException e) {
			}

		}
	}





	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
		if (device == null) return;
		final Intent intent = new Intent(this, DeviceControlActivity.class);
		Log.i("BluetoothLeService", "device name="+device.getName());
		intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
		intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
		if (mScanning) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}
		startActivity(intent);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	// Adapter for holding devices found through scanning.
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = DeviceScanActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if(!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLeDeviceListAdapter.addDevice(device);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}