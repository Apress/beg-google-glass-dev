package com.morkout.glassuielements;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLSurfaceView;
import android.util.Log;

import java.lang.Math;


class TetrahedronRenderer implements GLSurfaceView.Renderer 
{
	private boolean mTranslucentBackground;
	private Tetrahedron mCube;
	private float mTransX;

	public TetrahedronRenderer(boolean useTranslucentBackground) 
	{
		mTranslucentBackground = useTranslucentBackground;	
		mCube = new Tetrahedron();
	}

	public void onDrawFrame(GL10 gl) 
	{
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_MODELVIEW);       
		gl.glLoadIdentity();                              
		gl.glTranslatef((float)Math.sin(mTransX), -1.0f, -3.0f);   

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);             
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		mCube.draw(gl);                            

		mTransX += .075f;                                                                                                   
	}


	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		gl.glViewport(0, 0, width, height);

		float ratio = (float) width / height;
		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		gl.glDisable(GL11.GL_DITHER);
		gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT,GL11.GL_FASTEST);

		if (mTranslucentBackground) 
			gl.glClearColor(0,0,0,0);
		else 
			gl.glClearColor(1,1,1,1);

		gl.glEnable(GL11.GL_CULL_FACE);
		gl.glShadeModel(GL11.GL_SMOOTH);
		gl.glEnable(GL11.GL_DEPTH_TEST);
	}

}
