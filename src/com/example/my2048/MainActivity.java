package com.example.my2048;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my2048.My2048View.GameChangeListener;

public class MainActivity extends Activity {
	private TextView scoreText;
	private TextView maxScoreText;
	private My2048View my2048View;
	private static final String DATA_NAME = "my2048Data";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		scoreText = (TextView) findViewById(R.id.score);
		maxScoreText = (TextView) findViewById(R.id.maxScore);
		my2048View = (My2048View) findViewById(R.id.my2048view);
		my2048View.setOnGameChangeListener(new GameChangeListener() {
			
			@Override
			public void onChangedScore(int score) {
				scoreText.setText(score + "");
			}
			
			@Override
			public void onChangedGameOver(int score, int maxScore) {
				scoreText.setText(score + "");
				maxScoreText.setText(maxScore + "");
			}
		});
		
		if(savedInstanceState != null){
			Toast.makeText(this, "saveInstanceNotNull", 2000).show();
			Bundle map = savedInstanceState.getBundle(DATA_NAME);
			if(map != null){
				Toast.makeText(this, "mapNotNull", 2000).show();
				my2048View.restoreDataAndState(map);
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		my2048View.saveMaxScore();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBundle(DATA_NAME, my2048View.saveDataAndState());
	}
}
