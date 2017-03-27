package com.morkout.graphicsanimation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.apis.graphics.kube.Kube;

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

		case R.id.graphics_arcs:
			Intent i = new Intent(this, Arcs.class);
			startActivityForResult(i, 0);  
			return true;			

		case R.id.graphics_shape:
			Intent i2 = new Intent(this,ShapeDrawable1.class);
			startActivityForResult(i2, 0);  
			return true;			

		case R.id.graphics_paint:
			Intent i3 = new Intent(this, FingerPaint.class);
			startActivityForResult(i3, 0);  
			return true;			

		case R.id.graphics_bitmap:
			Intent i4 = new Intent(this, BitmapMesh.class);
			startActivityForResult(i4, 0);  
			return true;			

		case R.id.opengles_cube:
			Intent i5 = new Intent(this, CubeMapActivity.class);
			startActivityForResult(i5, 0);  
			return true;			

		case R.id.opengles_kube:
			Intent i6 = new Intent(this, Kube.class);
			startActivityForResult(i6, 0);  
			return true;			

		case R.id.opengles_rotate:
			Intent i7 = new Intent(this, TouchRotateActivity.class);
			startActivityForResult(i7, 0);  
			return true;			

		case R.id.rotationvector:
			Intent i8 = new Intent(this, RotationVectorDemo.class);
			startActivityForResult(i8, 0);  
			return true;			

		case R.id.animation:
			Intent i9 = new Intent(this, AndroidAnimationActivity.class);
			startActivityForResult(i9, 0);  
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
