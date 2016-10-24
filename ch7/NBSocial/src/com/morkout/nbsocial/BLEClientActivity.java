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

package com.morkout.nbsocial;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;


public class BLEClientActivity extends Activity {
	private final static String TAG = BLEClientActivity.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mTvName;
	private TextView mTvLog;

	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	BluetoothServerSocket mBluetoothServerSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private Handler mHandler;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 30000; // stop scanning after 30 seconds
	private boolean mScanning;
	private BluetoothGatt mBluetoothGatt;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.bleclient);
		mTvName = (TextView) findViewById(R.id.info);
		mTvLog = (TextView) findViewById(R.id.log);

		// determine whether BLE is supported on the device
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "BLE not supported.", Toast.LENGTH_SHORT).show();
			finish();
		}

		mTvLog.setText("BLE supported!");

		// Initializes a Bluetooth adapter
		final BluetoothManager bluetoothManager = (BluetoothManager) 
				getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Cannot initialize Bluetooth adapter", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}        

		//mTvLog.setText("Initialized Bluetooth adapter");

		mHandler = new Handler();
		scanPolarH7(true);


	}	


	private void scanPolarH7(final boolean enable) {
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
			UUID[] uuids = new UUID[1];
			uuids[0] = UUID.fromString(SampleGattAttributes.HEART_RATE_SERVICE_UUID);
			Log.v(TAG, "before startLeScan");
			mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}	

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

			Log.v(TAG, "inside onLeScan");

			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					Log.v(TAG, "found device: " + device.getName());
					// found device: Polar H7 331C25
					mTvName.setText(device.getName());

					if (mScanning) {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						mScanning = false;
					}					
					mDeviceAddress = device.getAddress();

					Intent gattServiceIntent = new Intent(BLEClientActivity.this, BluetoothLeService.class);
					bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
				}

			});
		}
	};

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			Log.v(TAG, "inside onServiceConnected");

			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			else {
				Log.v(TAG,  "mBluetoothLeService initialized");
			}

			// Automatically connects to the device upon successful start-up initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				Log.v(TAG, "BroadcastReceiver ACTION_GATT_CONNECTED");
				mTvLog.setText("Connected");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Log.v(TAG, "BroadcastReceiver ACTION_GATT_DISCONNECTED");
				mTvLog.setText("Disconnected");
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				Log.v(TAG, "BroadcastReceiver ACTION_GATT_SERVICES_DISCOVERED");

				// find our interested service and characteristic (HEART_RATE_SERVICE_UUID and HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)
				//displayGattServices(mBluetoothLeService.getSupportedGattServices());
				mBluetoothGatt = mBluetoothLeService.getBluetoothGatt();
				for (BluetoothGattService gattService : mBluetoothGatt.getServices()) { //mBluetoothLeService.getSupportedGattServices()) {
					String uuid = gattService.getUuid().toString();

					Log.i(TAG, "service:"+uuid);

					if (!uuid.equalsIgnoreCase(SampleGattAttributes.HEART_RATE_SERVICE_UUID)) continue;

					List<BluetoothGattCharacteristic> gattCharacteristics =
							gattService.getCharacteristics();
					ArrayList<BluetoothGattCharacteristic> charas =
							new ArrayList<BluetoothGattCharacteristic>();

					// Loops through available Characteristics.
					for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
						charas.add(gattCharacteristic);
						uuid = gattCharacteristic.getUuid().toString();
						Log.i(TAG, "characterstic:"+uuid);
						if (!uuid.equalsIgnoreCase(SampleGattAttributes.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) continue;

						final int charaProp = gattCharacteristic.getProperties();
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
							// If there is an active notification on a characteristic, clear
							// it first so it doesn't update the data field on the user interface.
							if (mNotifyCharacteristic != null) {
								mBluetoothLeService.setCharacteristicNotification(
										mNotifyCharacteristic, false);
								mNotifyCharacteristic = null;
							}
							mBluetoothLeService.readCharacteristic(gattCharacteristic);
						}
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
							mNotifyCharacteristic = gattCharacteristic;
							mBluetoothLeService.setCharacteristicNotification(
									gattCharacteristic, true);
						}
						
						break;
					}

					break;
				}


			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				Log.i(TAG, "ACTION_DATA_AVAILABLE");
		        Log.v(TAG, "heartRate="+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));		        
		        mTvLog.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService.disconnect();
		mBluetoothLeService = null;
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
}
