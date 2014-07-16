package com.example.my2048;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.my2048.My2048View.GameChangeListener;

public class MainActivity extends Activity {
	private TextView scoreText;
	private TextView maxScoreText;
	private My2048View my2048View;
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
	}
}
