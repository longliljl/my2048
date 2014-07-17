package com.example.my2048;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 2048游戏
 * 
 * @author 阳光小强 CSDN博客：http://blog.csdn.net/dawanganban
 */
public class My2048View extends View {
	private enum State{
		FAILL,  //失败
		ANIMATION,  //合并动画
		RUNNING  //运行
	}
	private enum Directory {
		LEFT, RIGHT, BOTTOM, TOP, NONE
	}
	private static final int TOTAL_ROW = 4; // 行
	private static final int TOTAL_COL = 4; // 列
	private static final int SPACE = 15; // 行和列之间的间隙
	private static final int ANGLE_SPEED = 45;
	
	private int mViewWidth; // View的宽度
	private int mViewHeight; // View的高度
	private float cellSpace; // 每个格子的大小

	private Paint paint;
	private Paint textPaint;
	private RectF rectf;
	private Random random;
	private int touchSlop;
	private Directory currentDirectory = Directory.NONE; // 当前方向
	private Directory oldDirectory;  //上一次方向
	private boolean isLocked = true;
	private int count = 0;   //方格占用数
	private int score = 0;   //分数
	private boolean isMoved = false;
	private int angler = 0;
	private SharedPreferences sharedPreference;
	private GameChangeListener gameChangeListener;
	
	private State currentState = State.RUNNING;

	private int[] colors = { Color.rgb(204, 192, 178), // 1
			Color.rgb(253, 235, 213), // 2
			Color.rgb(252, 224, 174), // 4
			Color.rgb(255, 95, 95), // 8
			Color.rgb(255, 68, 68), // 16
			Color.rgb(248, 58, 58), // 32
			Color.rgb(240, 49, 49), // 64
			Color.rgb(233, 39, 39), // 128
			Color.rgb(226, 29, 29), // 256
			Color.rgb(219, 19, 19), // 562
			Color.rgb(211, 10, 10), // 1024
			Color.rgb(204, 0, 0) // 2048
	};

	private int[][] datas = new int[TOTAL_ROW][TOTAL_COL];
	private int[][] animationData = new int[TOTAL_ROW][TOTAL_COL];
	private RefreshHandler refreshHandler = new RefreshHandler();
	public interface GameChangeListener{
		public void onChangedGameOver(int score, int maxScore);
		public void onChangedScore(int score);
	}
	
	class RefreshHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			My2048View.this.update();
			My2048View.this.invalidate();
		}
		
		public void sleep(long delayMillis){
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
		
	}
	
	public My2048View(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		textPaint = new Paint();
		rectf = new RectF();
		random = new Random();
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		sharedPreference = context.getSharedPreferences("my2048", context.MODE_PRIVATE);
		initData();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		count = 0;
		score = 0;
		isMoved = false;
		for (int i = 0; i < TOTAL_ROW; i++) {
			for (int j = 0; j < TOTAL_COL; j++) {
				datas[i][j] = 0;
			}
		}
		randomOneOrTwo();
	}
	
	private void update(){
		if(currentState == State.ANIMATION){
			angler = angler + ANGLE_SPEED;
			System.out.println("angler = " + angler);
			if(angler > 180){
				angler = 0;
				currentState = State.RUNNING;
				clearAnimationData();
			}else{
				refreshHandler.sleep(100);
			}
		}
	}
	
	private void clearAnimationData(){
		for (int i = 0; i < TOTAL_ROW; i++) {
			for (int j = 0; j < TOTAL_COL; j++) {
				animationData[i][j] = 0;
			}
		}
	}
	
	public void setOnGameChangeListener(GameChangeListener gameChangeListener){
		this.gameChangeListener = gameChangeListener;
		gameChangeListener.onChangedGameOver(score, sharedPreference.getInt("maxScore", 0));
		gameChangeListener.onChangedScore(score);
	}

	/**
	 * 随机的产生1或者2
	 */
	private void randomOneOrTwo() {
		if(count >= TOTAL_COL * TOTAL_ROW){
			int maxScore = sharedPreference.getInt("maxScore", 0);
				if(score > maxScore){
				Editor edit = sharedPreference.edit();
				edit.putInt("maxScore", score);
				edit.commit();
			}
			gameChangeListener.onChangedGameOver(score, maxScore);
			currentState = State.FAILL;
			return;
		}
		int row = random.nextInt(TOTAL_ROW);
		int col = random.nextInt(TOTAL_COL);

		// 判断在该位置是否已存在数据
		if (datas[row][col] != 0) {
			randomOneOrTwo();
		} else {
			setIsLocked(row, col, random.nextInt(2) + 1);
		}
	}
	
	private void setIsLocked(int row, int col, int randomData){
		isLocked = true;
		switch (currentDirectory) {
		case LEFT:
			if(col != 0 && datas[row][col - 1] == randomData){
				isLocked = false;
			}
			for(int i=0; i<col; i++){
				if(datas[row][i] == 0){
					isLocked = false;
					break;
				}
			}
			for(int i=0; i< TOTAL_ROW; i++){
				for(int j = 0; j < TOTAL_COL -1; j++){
					if(datas[i][j] != 0 && datas[i][j] == datas[i][j+1]){
						isLocked = false;
						break;
					}
				}
			}
			break;
		case RIGHT:
			if(col != TOTAL_COL - 1 && datas[row][col + 1] == randomData){
				isLocked = false;
			}
			for(int i= col + 1; i < TOTAL_COL; i++){
				if(datas[row][i] == 0){
					isLocked = false;
					break;
				}
			}
			for(int i=0; i< TOTAL_ROW; i++){
				for(int j = 0; j < TOTAL_COL - 1; j++){
					if(datas[i][j] != 0 && datas[i][j] == datas[i][j+1]){
						isLocked = false;
						break;
					}
				}
			}
			break;
		case TOP:
			if(row != 0 && datas[row - 1][col] == randomData){
				isLocked = false;
			}
			for(int i=0; i<row; i++){
				if(datas[i][col] == 0){
					isLocked = false;
					break;
				}
			}
			for(int i=0; i< TOTAL_COL; i++){
				for(int j=0; j<TOTAL_ROW  - 1; j++){
					if(datas[j][i] != 0 && datas[j][i] == datas[j+1][i]){
						isLocked = false;
						break;
					}
				}
			}
			break;
		case BOTTOM:
			if(row != TOTAL_ROW - 1 && datas[row + 1][col] == randomData){
				isLocked = false;
			}
			for(int i= row + 1; i < TOTAL_ROW; i++){
				if(datas[i][col] == 0){
					isLocked = false;
					break;
				}
			}
			for(int i=0; i< TOTAL_COL; i++){
				for(int j=0; j<TOTAL_ROW  - 1; j++){
					if(datas[j][i] != 0 && datas[j][i] == datas[j+1][i]){
						isLocked = false;
						break;
					}
				}
			}
			break;
		}
		datas[row][col] = randomData;
		count++;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.mViewWidth = w;
		this.mViewHeight = h;
		cellSpace = ((float) mViewWidth - (TOTAL_COL + 1) * SPACE) / TOTAL_COL;
		textPaint.setTextSize(cellSpace / 3);
	}

	private float mDownX;
	private float mDownY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//如果当前状态是动画状态则返回
		if(currentState == State.ANIMATION){
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			oldDirectory = currentDirectory;
			mDownX = event.getX();
			mDownY = event.getY();
			if(currentState == State.FAILL){
				if(mDownY < mViewHeight && mDownY > mViewHeight - cellSpace){
					currentState = State.RUNNING;
					initData();
					invalidate();
				}
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			float disX = event.getX() - mDownX;
			float disY = event.getY() - mDownY;
			if (Math.abs(disX) > touchSlop || Math.abs(disY) > touchSlop) {
				isMoved = true;
				if (Math.abs(disX) > Math.abs(disY)) {
					if (disX > 0) {
						currentDirectory = Directory.RIGHT;
					} else {
						currentDirectory = Directory.LEFT;
					}
				} else {
					if (disY > 0) {
						currentDirectory = Directory.BOTTOM;
					} else {
						currentDirectory = Directory.TOP;
					}
				}
			}
			return true;
		case MotionEvent.ACTION_UP:
			if (isMoved) {
				System.out.println(oldDirectory);
				System.out.println(currentDirectory);
				if(oldDirectory != currentDirectory){
					changeState();
					randomOneOrTwo();
					invalidate();
					isMoved = false;
				}else if(!isLocked){
					changeState();
					randomOneOrTwo();
					invalidate();
					isMoved = false;
				}
			}
		}
		return super.onTouchEvent(event);
	}

	private void changeState() {
		switch (currentDirectory) {
		case TOP:
			toTop();
			break;
		case BOTTOM:
			toBottom();
			break;
		case LEFT:
			toLeft();
			break;
		case RIGHT:
			toRight();
			break;
		}
		if(currentState == State.ANIMATION){
			update();
		}
	}

	/*
	 * 向上移动
	 */
	private void toTop() {
		moveTop();
		for (int i = 0; i < TOTAL_COL; i++) {
			for (int j = 0; j < TOTAL_ROW; j++) {
				for (int k = TOTAL_ROW - 1; k > j; k--) {
					if (datas[k][i] != 0 && datas[k][i] == datas[k - 1][i]) {
						datas[k - 1][i] = datas[k - 1][i] + 1;
						datas[k][i] = 0;
						score = score + (int)Math.pow(2, datas[k - 1][i]);
						gameChangeListener.onChangedScore(score);
						count--;
						animationData[k - 1][i] = datas[k - 1][i];
						currentState = State.ANIMATION; //设置当前状态为动画
						//退出最内两层循环
						k = j + 1;
						j = TOTAL_ROW;
					}
				}
			}
		}
		moveTop();
	}
	
	private void moveTop(){
		int temp;
		for (int i = 0; i < TOTAL_COL; i++) {
			for (int j = 0; j < TOTAL_ROW; j++) {
				for (int k = 0; k < TOTAL_ROW - j - 1; k++) {
					if (datas[k][i] == 0) {
						temp = datas[k][i];
						datas[k][i] = datas[k + 1][i];
						datas[k + 1][i] = temp;
					}
				}
			}
		}
	}

	/*
	 * 向下移动
	 */
	private void toBottom() {
		moveBottom();

		for (int i = 0; i < TOTAL_COL; i++) {
			for (int j = 0; j < TOTAL_ROW; j++) {
				for (int k = 0; k < TOTAL_ROW - j - 1; k++) {
					if (datas[k][i] != 0 && datas[k][i] == datas[k + 1][i]) {
						datas[k + 1][i] = datas[k + 1][i] + 1;
						datas[k][i] = 0;
						score = score + (int)Math.pow(2, datas[k + 1][i]);
						animationData[k + 1][i] = datas[k + 1][i];
						gameChangeListener.onChangedScore(score);
						count--;	
						currentState = State.ANIMATION; //设置当前状态为动画
						k = TOTAL_ROW - j - 2;
						j = TOTAL_ROW;
					}
				}
			}
		}
		moveBottom();
	}
	
	private void moveBottom(){
		int temp;
		for (int i = 0; i < TOTAL_COL; i++) {
			for (int j = 0; j < TOTAL_ROW; j++) {
				for (int k = TOTAL_ROW - 1; k > j; k--) {
					if (datas[k][i] == 0) {
						temp = datas[k][i];
						datas[k][i] = datas[k - 1][i];
						datas[k - 1][i] = temp;
					}
				}
			}
		}
	}

	private void toLeft() {
		moveLeft();
		for (int i = 0; i < TOTAL_ROW; i++) {
			for (int j = 0; j < TOTAL_COL; j++) {
				for (int k = TOTAL_ROW - 1; k > j; k--) {
					if (datas[i][k] != 0 && datas[i][k] == datas[i][k - 1]) {
						datas[i][k - 1] = datas[i][k - 1] + 1;
						datas[i][k] = 0;
						score = score + (int)Math.pow(2, datas[i][k - 1]);
						gameChangeListener.onChangedScore(score);
						count--;
						animationData[i][k - 1] = datas[i][k - 1];
						currentState = State.ANIMATION; //设置当前状态为动画
						//退出最内两层循环
						k = j + 1;
						j = TOTAL_ROW;
					}
				}
			}
		}
		moveLeft();
	}
	
	private void moveLeft(){
		int temp;
		// 向左移动
		for (int i = 0; i < TOTAL_ROW; i++) {
			for (int j = 0; j < TOTAL_COL; j++) {
				for (int k = 0; k < TOTAL_COL - j - 1; k++) {
					if (datas[i][k] == 0) {
						temp = datas[i][k];
						datas[i][k] = datas[i][k + 1];
						datas[i][k + 1] = temp;
					}
				}
			}
		}
	}

	private void toRight() {
		moveRight();

		for (int i = 0; i < TOTAL_ROW; i++) {
			for (int j = 0; j < TOTAL_COL; j++) {
				for (int k = 0; k < TOTAL_COL - j - 1; k++) {
					if (datas[i][k] != 0 && datas[i][k] == datas[i][k + 1]) {
						datas[i][k + 1] = datas[i][k + 1] + 1;
						datas[i][k] = 0;
						score = score + (int)Math.pow(2, datas[i][k + 1]);
						gameChangeListener.onChangedScore(score);
						count--;
						animationData[i][k + 1] = datas[i][k + 1];
						currentState = State.ANIMATION; //设置当前状态为动画
						k = TOTAL_ROW - j - 2;
						j = TOTAL_ROW;
					}
				}
			}
		}
		moveRight();
	}
	
	private void moveRight(){
		int temp;
		for (int i = 0; i < TOTAL_COL; i++) {
			for (int j = 0; j < TOTAL_ROW; j++) {
				for (int k = TOTAL_ROW - 1; k > j; k--) {
					if (datas[i][k] == 0) {
						temp = datas[i][k];
						datas[i][k] = datas[i][k - 1];
						datas[i][k - 1] = temp;
					}
				}
			}
		}
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if(visibility != View.VISIBLE){
			int maxScore = sharedPreference.getInt("maxScore", 0);
			if(score > maxScore){
				Editor edit = sharedPreference.edit();
				edit.putInt("maxScore", score);
				edit.commit();
			}
		}
	}

	private float pointX;
	private float pointY;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		String showNum;
		if(currentState == State.RUNNING || currentState == State.ANIMATION){
			for (int i = 0; i < TOTAL_ROW; i++) {
				for (int j = 0; j < TOTAL_COL; j++) {
					pointX = SPACE * (j + 1) + j * cellSpace;
					pointY = SPACE * (i + 1) + i * cellSpace;
					// 绘制背景
					rectf.set(pointX, pointY, pointX + cellSpace, pointY
							+ cellSpace);
					paint.setColor(colors[datas[i][j]]);
					if(currentState == State.ANIMATION && datas[i][j] != 0 && animationData[i][j] != 0){
						canvas.save();
						canvas.rotate(angler, pointX + cellSpace / 2, pointY + cellSpace / 2);
						canvas.drawRect(rectf, paint);
						canvas.restore();
					}else{
						canvas.drawRect(rectf, paint);
					}
	
					if (datas[i][j] != 0) {
						// 绘制数字
						if (datas[i][j] == 1 || datas[i][j] == 2) {
							textPaint.setColor(Color.rgb(0, 0, 0));
						} else {
							textPaint.setColor(Color.rgb(255, 255, 255));
						}
						showNum = (int) Math.pow(2, datas[i][j]) + "";
						canvas.drawText(
								showNum,
								pointX + (cellSpace - textPaint.measureText(showNum)) / 2,
								pointY+ (cellSpace + textPaint.measureText(showNum, 0, 1)) / 2, textPaint);
					}
				}
			}
		}
		if(currentState == State.FAILL){
			rectf.set(0 , mViewHeight - cellSpace, mViewWidth, mViewHeight);
			paint.setColor(colors[5]);
			canvas.drawRect(rectf, paint);
			textPaint.setColor(Color.rgb(255, 255, 255));
			canvas.drawText("游戏结束", (mViewWidth - textPaint.measureText("游戏结束")) / 2, mViewHeight / 2, textPaint);
			canvas.drawText("重新开始", (mViewWidth - textPaint.measureText("游戏结束")) / 2, 
					mViewHeight - textPaint.measureText("游戏结束", 0, 1), textPaint);
		}
	}

}
