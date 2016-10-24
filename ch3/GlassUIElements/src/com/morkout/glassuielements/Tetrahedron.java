package com.morkout.glassuielements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


class Tetrahedron
{
	private FloatBuffer mFVertexBuffer;
	private ByteBuffer  mColorBuffer;
	private ByteBuffer  mIndexBuffer;

	public Tetrahedron()
	{
		byte ff = (byte)255;

		// four unique vertices for the cube
		float vertices[] = {
				-1.0f, 0.0f, 0.0f, 
				1.0f, 0.0f, 0.0f, 
				0.0f,  2.0f, 0.0f, 
				0.0f, 0.6f, 0.5f
		};        

		// color for each vertex
		byte colors[] = {
				ff, ff, 0, ff,
				0, ff, ff, ff,
				ff, 0, ff, ff,
				0, 0, ff, ff
		}; 

		// how to draw triangles based on the four unique vertices - 0 means the first 3 values in vertices array
		byte indices[] = {
				0, 2, 1, 
				0, 2, 3,
				0, 3, 1,
				3, 2, 1
		};        

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mFVertexBuffer = vbb.asFloatBuffer();
		mFVertexBuffer.put(vertices);
		mFVertexBuffer.position(0);

		mColorBuffer = ByteBuffer.allocateDirect(colors.length);
		mColorBuffer.put(colors);
		mColorBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}

	public void draw(GL10 gl)
	{
		gl.glFrontFace(GL11.GL_CW);
		// 3 is for 3D, meaning each vertex consists of 3 values
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, mFVertexBuffer);
		gl.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 0, mColorBuffer);
		// 12 means there're 4 vertices, as each vertex has 3 values
		gl.glDrawElements(GL11.GL_TRIANGLES, 12, GL11.GL_UNSIGNED_BYTE, mIndexBuffer);

		gl.glFrontFace(GL11.GL_CCW);
	}
}
