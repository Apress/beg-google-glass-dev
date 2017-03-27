package com.morkout.nbsocial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity {
	private boolean mAttachedToWindow;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		mAttachedToWindow = true;
		openOptionsMenu();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAttachedToWindow = false;
	}	

	@Override
	public void openOptionsMenu() {
		if (mAttachedToWindow) {
			super.openOptionsMenu();
		}
	}	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                stopService(new Intent(this, AppService.class));
                return true;
                
            case R.id.httpget:
            	Intent intent = new Intent(this, HTTPRequestActivity.class);
            	intent.putExtra( "WHAT", "GET" );
                startActivity(intent); 
                return true;
                
            case R.id.httppost:
            	Intent intent2 = new Intent(this, HTTPRequestActivity.class);
            	intent2.putExtra( "WHAT", "POST" );
                startActivity(intent2); 
                return true;

            case R.id.httpupload:
            	Intent intent3 = new Intent(this, HTTPRequestActivity.class);
            	intent3.putExtra( "WHAT", "UPLOAD" );
                startActivity(intent3); 
                return true;
               
            case R.id.socketclient:
            	Intent intent4 = new Intent(this, SocketClientActivity.class);
                startActivity(intent4); 
                return true;
               
            case R.id.socketserver:
            	Intent intent5 = new Intent(this, SocketServerActivity.class);
                startActivity(intent5); 
                return true;
                
            case R.id.cbtclient:
            	Intent intent6 = new Intent(this, ClassicBluetoothClient.class);
                startActivity(intent6); 
                return true;
                
            case R.id.cbtserver:
            	Intent intent7 = new Intent(this, ClassicBluetoothServer.class);
                startActivity(intent7); 
                return true;                
                
            case R.id.bleclient:
            	Intent intent8 = new Intent(this, BLEClientActivity.class);
                startActivity(intent8); 
                return true;                 
                           
            case R.id.socketheartrate:
            	Intent intent9 = new Intent(this, HeartrateSocketClientActivity.class);
                startActivity(intent9); 
                return true;            

            case R.id.blheartrate:
            	Intent intent10 = new Intent(this, HeartrateClassicBluetoothClient.class);
                startActivity(intent10); 
                return true;                           
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        finish();
    }
}
