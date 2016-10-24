package com.morkout.smartcamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MenuActivity extends Activity {
    private static final String TAG = "MenuActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
//    @Override
//    public void onResume() {
//        super.onResume();
//        openOptionsMenu();
//    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
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

            case R.id.preview:
            	Intent intent = new Intent(this, PreviewActivity.class);
                startActivity(intent);  
                return true;
           
            case R.id.zoom:
            	Intent intent2 = new Intent(this, ZoomActivity.class);
                startActivity(intent2);            	
                return true;

            case R.id.scanner:
            	Intent intent3 = new Intent(this, ScannerActivity.class);
                startActivity(intent3);            	
                return true;                            

            case R.id.ocr:
            	Intent intent4 = new Intent(this, OCRActivity.class);
                startActivity(intent4);            	
                return true;   
                
            case R.id.photo:
                Toast.makeText(MenuActivity.this, "Loading photos...", Toast.LENGTH_LONG).show();
            	
            	Intent intent5 = new Intent(this, PhotoViewActivity.class);
                startActivity(intent5);            	
                return true;   
                
            case R.id.search:
            	Intent intent6 = new Intent(this, SearchActivity.class);
                startActivity(intent6);            	
                return true;   
                
            case R.id.opencv:
            	Intent intent7 = new Intent(this, OpenCVActivity.class);
                startActivity(intent7);            	
                return true;   
                
            case R.id.stop:
                stopService(new Intent(this, AppService.class));
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
