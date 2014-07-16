package com.example.my2048;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 2048��Ϸ
 * 
 * @author ����Сǿ CSDN���ͣ�http://blog.csdn.net/dawanganban
 */
public class My2048View extends View {
	private enum State{
		FAILL,  //ʧ��
		RUNNING  //����
	}
	private enum Directory {
		LEFT, RIGHT, BOTTOM, TOP
	}
	private static final int TOTAL_ROW = 4; // ��
	private static final int TOTAL_COL = 4; // ��
	private static final int SPACE = 15; // �к���֮��ļ�϶
	
	private int mViewWidth; // View�Ŀ��
	private int mViewHeight; // View�ĸ߶�
	private float cellSpace; // ÿ�����ӵĴ�С

	private Paint paint;
	private Paint textPaint;
	private RectF rectf;
	private Random random;
	private int touchSlop;
	private Directory currentDirectory; // ��ǰ����
	private int count = 0;   //����ռ����
	private int score = 0;   //����
	private boolean isMoved = false;
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

	public interface GameChangeListener{
		public void onChangedGameOver(int score, int maxScore);
		public void onChangedScore(int score);
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
	 * ��ʼ������
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
	
	public void setOnGameChangeListener(GameChangeListener gameChangeListener){
		this.gameChangeListener = gameChangeListener;
		gameChangeListener.onChangedGameOver(score, sharedPreference.getInt("maxScore", 0));
		gameChangeListener.onChangedScore(score);
	}

	/**
	 * ����Ĳ���1����2
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

		// �ж��ڸ�λ���Ƿ��Ѵ�������
		if (datas[row][col] != 0) {
			randomOneOrTwo();
		} else {
			datas[row][col] = random.nextInt(2) + 1;
			count++;
		}
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
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
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
				System.out.println("isMove");
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
			if (isMoved == true) {
				changeState();
				randomOneOrTwo();
				invalidate();
				isMoved = false;
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
	}

	/*
	 * �����ƶ�
	 */
	private void toTop() {
		int temp;
		// �����ƶ�
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
		// �ϲ�����
		for (int i = 0; i < TOTAL_COL; i++) {
			for (int j = 0; j < TOTAL_ROW; j++) {
				for (int k = 0; k < TOTAL_ROW - j - 1; k++) {
					if (datas[k][i] != 0 && datas[k][i] == datas[k + 1][i]) {
						datas[k][i] = datas[k][i] + 1;
						datas[k + 1][i] = 0;
						score = score + (int)Math.pow(2, datas[k][i]);
						gameChangeListener.onChangedScore(score);
						count--;
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

	/*
	 * �����ƶ�
	 */
	private void toBottom() {
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
		// �ϲ�����
		for (int i = 0; i < TOTAL_COL; i++) {
			for (int j = 0; j < TOTAL_ROW; j++) {
				for (int k = TOTAL_ROW - 1; k > j; k--) {
					if (datas[k][i] != 0 && datas[k][i] == datas[k - 1][i]) {
						datas[k][i] = datas[k][i] + 1;
						datas[k - 1][i] = 0;
						score = score + (int)Math.pow(2, datas[k][i]);
						gameChangeListener.onChangedScore(score);
						count--;
					}
				}
			}
		}
	}

	private void toLeft() {
		int temp;
		// �����ƶ�
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
		// �ϲ�����
		for (int i = 0; i < TOTAL_ROW; i++) {
			for (int j = 0; j < TOTAL_COL; j++) {
				for (int k = 0; k < TOTAL_COL - j - 1; k++) {
					if (datas[i][k] != 0 && datas[i][k] == datas[i][k + 1]) {
						datas[i][k] = datas[i][k] + 1;
						datas[i][k + 1] = 0;
						score = score + (int)Math.pow(2, datas[i][k]);
						gameChangeListener.onChangedScore(score);
						count--;
					}
				}
			}
		}
	}

	private void toRight() {
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
		// �ϲ�����
		for (int i = 0; i < TOTAL_ROW; i++) {
			for (int j = 0; j < TOTAL_COL; j++) {
				for (int k = TOTAL_ROW - 1; k > j; k--) {
					if (datas[i][k] != 0 && datas[i][k] == datas[i][k - 1]) {
						datas[i][k] = datas[i][k] + 1;
						datas[i][k - 1] = 0;
						score = score + (int)Math.pow(2, datas[i][k]);
						gameChangeListener.onChangedScore(score);
						count--;
					}
				}
			}
		}
	}

	private float pointX;
	private float pointY;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		String showNum;
		if(currentState == State.RUNNING){
			for (int i = 0; i < TOTAL_ROW; i++) {
				for (int j = 0; j < TOTAL_COL; j++) {
					pointX = SPACE * (j + 1) + j * cellSpace;
					pointY = SPACE * (i + 1) + i * cellSpace;
					// ���Ʊ���
					rectf.set(pointX, pointY, pointX + cellSpace, pointY
							+ cellSpace);
					paint.setColor(colors[datas[i][j]]);
					canvas.drawRect(rectf, paint);
	
					if (datas[i][j] != 0) {
						// ��������
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
			canvas.drawText("��Ϸ����", (mViewWidth - textPaint.measureText("��Ϸ����")) / 2, mViewHeight / 2, textPaint);
			canvas.drawText("���¿�ʼ", (mViewWidth - textPaint.measureText("��Ϸ����")) / 2, 
					mViewHeight - textPaint.measureText("��Ϸ����", 0, 1), textPaint);
		}
	}

}
