package com.morkout.glassuielements;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.glass.app.Card;

public class GlassStyledCardActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Card card = new Card(this);
		card.setText("5 Basic Human Emotions");
		card.setFootnote("how are you feeling?");
		card.addImage(R.drawable.joy);
		card.addImage(R.drawable.anger);
		card.addImage(R.drawable.sadness);
		card.addImage(R.drawable.fear);
		card.addImage(R.drawable.surprise);        
		card.setImageLayout(Card.ImageLayout.LEFT);

		setContentView(card.getView());
	}    
}
