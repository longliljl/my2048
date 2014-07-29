package com.example.my2048;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my2048.My2048View.GameChangeListener;

public class MainActivity extends Activity {
	private TextView scoreText;
	private TextView maxScoreText;
	private ImageView soundButton;
	private ImageView shareButton;
	private My2048View my2048View;
	private SharedPreferences sharedPreference;
	private boolean soundOpened;
	private static final String DATA_NAME = "my2048Data";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sharedPreference = getSharedPreferences("my2048", MODE_PRIVATE);
		soundOpened = sharedPreference.getBoolean("soundOpend", true);
		scoreText = (TextView) findViewById(R.id.score);
		maxScoreText = (TextView) findViewById(R.id.maxScore);
		my2048View = (My2048View) findViewById(R.id.my2048view);
		my2048View.setSoundState(soundOpened);
		soundButton = (ImageView) findViewById(R.id.sound);
		shareButton = (ImageView) findViewById(R.id.share);
		soundButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Editor edit = sharedPreference.edit();
				edit.putBoolean("soundOpend", !soundOpened);
				edit.commit();
				soundOpened = !soundOpened;
				my2048View.setSoundState(soundOpened);
				if(soundOpened){
					soundButton.setImageResource(R.drawable.sound_opend);
				}else{
					soundButton.setImageResource(R.drawable.sound_closed);
				}
			}
		});
		shareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				my2048View.saveMaxScore();
				Intent intent=new Intent(Intent.ACTION_SEND);    
                intent.setType("image/*");    
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");    
                intent.putExtra(Intent.EXTRA_TEXT, "我的最高纪录是"+ sharedPreference.getInt("maxScore", 0) +"分，赶快来玩吧，LOL版2048好玩！有木有~~，进去看看：http://blog.csdn.net/dawanganban/article/details/37863693");            
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
                startActivity(Intent.createChooser(intent, getTitle()));    
			}
		});
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
