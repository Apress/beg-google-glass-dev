package com.morkout.graphicsanimation;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class AndroidAnimationActivity extends Activity {

	ImageView mImage1, mImage2, mImage3, mImage4, mImage5, mImage6;
	Animation mAnimationIn, mAnimationOut;
	ImageView mCurImage;
	AnimatorSet mAnimSet;
	TextView myTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.animation);
		mImage1 = (ImageView)findViewById(R.id.image1);
		mImage2 = (ImageView)findViewById(R.id.image2);
		mImage3 = (ImageView)findViewById(R.id.image3);
		mImage4 = (ImageView)findViewById(R.id.image4);
		mImage5 = (ImageView)findViewById(R.id.image5);
		mImage6 = (ImageView)findViewById(R.id.image6);

		AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.property_animator_alpha);
		set.setTarget(mImage1);
		set.start();		

		// save animation effect as in the xml above - change alpha value indefinitely
		ValueAnimator testAnim = ObjectAnimator.ofFloat(mImage2, "alpha", 1.0f, 0.0f);
		testAnim.setDuration(3000);
		testAnim.setRepeatCount(ValueAnimator.INFINITE);
		testAnim.setRepeatMode(ValueAnimator.REVERSE);
		testAnim.start();	

		myTextView = (TextView)findViewById(R.id.mytextview);
		ValueAnimator colorAnim = ObjectAnimator.ofFloat(myTextView, "rotation", 180.0f, 360.0f);
		colorAnim.setDuration(3000);
		colorAnim.setRepeatCount(ValueAnimator.INFINITE);
		colorAnim.setRepeatMode(ValueAnimator.REVERSE);
		colorAnim.start();		

		set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.property_animator_group);
		set.setTarget(mImage3);
		set.start();

		ObjectAnimator mover = ObjectAnimator.ofFloat(mImage6, "y", 0f, 360f);
		mover.setDuration(3000);
		ObjectAnimator rotation = ObjectAnimator.ofFloat(mImage6, "rotation", 0.0f, 360.0f);
		rotation.setDuration(3000);
		ObjectAnimator fade = ObjectAnimator.ofFloat(mImage6, "alpha", 0.0f, 1.0f);
		fade.setDuration(3000);			    

		ArrayList<Animator> animators = new ArrayList<Animator>();
		animators.add(rotation);
		animators.add(fade);
		animators.add(mover);
		mAnimSet = new AnimatorSet();
		mAnimSet.setInterpolator(new DecelerateInterpolator());
		mAnimSet.playTogether(animators);
		mAnimSet.start();

//		// both AnimatorSet and ValueAnimator are direct subclasses of Animator
		mAnimSet.addListener(//new AnimatorListener(){
				new AnimatorListenerAdapter() { // This adapter class provides empty implementations of the methods from Animator.AnimatorListener. 
					// Any custom listener that cares only about a subset of the methods of this listener can simply subclass this adapter class instead of 
					// implementing the interface directly.
					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub)
						System.out.println("onAnimationEnd");
						ObjectAnimator rotation = ObjectAnimator.ofFloat(mImage6, "rotation", 0.0f, 360.0f);
						rotation.setDuration(3000);
						ObjectAnimator mover = ObjectAnimator.ofFloat(mImage6, "y", 0f, 360f);
						mover.setDuration(3000);
						ObjectAnimator fade = ObjectAnimator.ofFloat(mImage6, "alpha", 0.0f, 1.0f);
						fade.setDuration(3000);	

						ArrayList<Animator> animators = new ArrayList<Animator>();
						animators.add(rotation);
						animators.add(fade);			    
						animators.add(mover);
						mAnimSet.playTogether(animators);
						//fadeOut.start();			    
						mAnimSet.start();

					}
				});		
		
		// http://developer.android.com/reference/android/R.anim.html
		mAnimationIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
		mAnimationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
		mAnimationIn.setDuration(1000);
		mAnimationOut.setDuration(1000);
		mAnimationIn.setAnimationListener(animationSlideInLeftListener);
		mAnimationOut.setAnimationListener(animationSlideOutRightListener);

		mCurImage = mImage4;
		mImage4.startAnimation(mAnimationIn);
		mImage4.setVisibility(View.VISIBLE);	    

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mImage4.clearAnimation();
		mImage5.clearAnimation();
	}

	AnimationListener animationSlideInLeftListener = new AnimationListener(){

		@Override
		public void onAnimationEnd(Animation animation) {
			if(mCurImage == mImage4){
				mImage4.startAnimation(mAnimationOut);
			}else if(mCurImage == mImage5){
				mImage5.startAnimation(mAnimationOut);
			} 
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}};

		AnimationListener animationSlideOutRightListener = new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				if(mCurImage == mImage4){
					mCurImage = mImage5;
					mImage5.startAnimation(mAnimationIn);
					mImage4.setVisibility(View.INVISIBLE);
					mImage5.setVisibility(View.VISIBLE);
				}else if(mCurImage == mImage5){
					mCurImage = mImage4;
					mImage4.startAnimation(mAnimationIn);
					mImage4.setVisibility(View.VISIBLE);
					mImage5.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationStart(Animation animation) {
			}};
}
