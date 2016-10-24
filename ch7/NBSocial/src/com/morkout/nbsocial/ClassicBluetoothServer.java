package com.morkout.nbsocial;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;


public class ClassicBluetoothServer extends Activity {

	public final static String TAG = "ClassicBluetoothServer";
	BluetoothAdapter mBluetoothAdapter;
	BluetoothServerSocket mBluetoothServerSocket;
	public static final int REQUEST_TO_START_BT = 100;
	public static final int REQUEST_FOR_SELF_DISCOVERY = 200;
	private TextView mTvInfo;

	UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);

		// initialize Bluetooth and retrieve info about the BT radio interface
		//mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		if (mBluetoothAdapter == null) {
			mTvInfo.setText("Device does not support Bluetooth");
			return;
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				mTvInfo.setText("Bluetooth supported but not enabled");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_TO_START_BT);
			} else {
				mTvInfo.setText("Bluetooth supported and enabled");				
				new AcceptThread().start();
			}
		}
	}

	private class AcceptThread extends Thread {
		private BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			try {
				mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ClassicBluetoothServer", MY_UUID);
			} 
			catch (IOException e) {
				final IOException ex = e;
				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText(ex.getMessage());
					}
				});
			}
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					runOnUiThread(new Runnable() {
						public void run() {
							mTvInfo.setText(mTvInfo.getText() + "\n\nWaiting for Bluetooth Client ...");
						}
					});

					socket = mServerSocket.accept(); // blocking call

				} catch (IOException e) {
					Log.v(TAG, e.getMessage());
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work in a separate thread
					new ConnectedThread(socket).start();

					try {
						mServerSocket.close();						
					} catch (IOException e) {
						Log.v(TAG, e.getMessage());
					}
					break;
				}
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final OutputStream mOutStream;
		private int bytesRead;
		final private String FILE_TO_BE_TRANSFERRED = "marchmadness.png";
		final private String PATH = Environment.getExternalStorageDirectory().toString() + "/nbsocial/";		

		public ConnectedThread(BluetoothSocket socket) {
			mSocket = socket;
			OutputStream tmpOut = null;

			try {
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			mOutStream = tmpOut;
		}

		String copyAsset(String filename) {
			File dir = new File(PATH);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					return null;
				}
			}

			if (!(new File( PATH + filename).exists())) {
				try {

					AssetManager assetManager = getAssets();
					InputStream in = assetManager.open(filename);
					OutputStream out = new FileOutputStream(PATH + filename);

					// Transfer bytes from in to out
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					out.close();

				} catch (IOException e) {
					Log.e(TAG, "Was unable to copy " + filename + e.toString());
					return null;
				}
			}    	
			return PATH + filename; 
		}		


		public void run() {
			byte[] buffer = new byte[1024];

			// transfer a file
			if (mOutStream != null) {
				File myFile = new File( copyAsset("marchmadness.png") );
				FileInputStream fis = null;

				try {
					fis = new FileInputStream(myFile);
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getMessage());
				}
				BufferedInputStream bis = new BufferedInputStream(fis);
				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText(mTvInfo.getText() + "\nbefore sending file "+  PATH + FILE_TO_BE_TRANSFERRED + " of " + new File(  PATH + FILE_TO_BE_TRANSFERRED ).length() + " bytes");
					}
				});
				try {
					bytesRead = 0;
					for (int read = bis.read(buffer); read >= 0; read = bis.read(buffer))
					{
						mOutStream.write(buffer, 0, read);
						bytesRead += read;
						// uncomment for non-Glass
						//						runOnUiThread(new Runnable() {
						//							public void run() {
						//								mTvInfo.setText(mTvInfo.getText() + ".");
						//							}
						//						});					
					}

					mSocket.close();
					runOnUiThread(new Runnable() {
						public void run() {
							//							mTvInfo.setText(mTvInfo.getText() + "\n" + bytesRead + " bytes of file " +  PATH + FILE_TO_BE_TRANSFERRED + " has been sent"); 
							mTvInfo.setText(bytesRead + " bytes of file " +  PATH + FILE_TO_BE_TRANSFERRED + " has been sent."); // Glass
						}
					});                	

				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}

			new AcceptThread().start();				
		}
	}
}

