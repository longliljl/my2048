package com.example.my2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class My2048View extends View{
	private static final int TOTAL_ROW = 4; //行
	private static final int TOTAL_COL = 4; //列
	private static final int SPACE = 10;  //行和列之间的间隙
	
	private int mViewWidth;  //View的宽度
	private int mViewHeight;  //View的高度
	private float cellSpace;   //每个格子的大小
	
	private Paint paint;
	private Paint textPaint;
	private RectF rectf;
	
	private int[] colors = {
			Color.rgb(204, 192, 178), //1
			Color.rgb(251, 233, 213),  //2
			Color.rgb(252, 224, 174),  //4
			Color.rgb(255, 95, 95),   //8
			Color.rgb(255, 68, 68), //16
			Color.rgb(248, 58, 58), //32
			Color.rgb(240, 49, 49), //64
			Color.rgb(233, 39, 39),  //128
			Color.rgb(226, 29, 29),  //256
			Color.rgb(219, 19, 19),  //562
			Color.rgb(211, 10, 10),  //1024
			Color.rgb(204, 0, 0)   //2048
			};
	
	private int[][] datas = new int[TOTAL_ROW][TOTAL_COL];
	

	public My2048View(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		textPaint = new Paint();
		rectf = new RectF();
		
		initData();
	}
	
	private void initData(){
		for(int i=0; i<TOTAL_ROW; i++){
			for(int j=0; j<TOTAL_COL; j++){
				int a =  (i+1) * (j+1);
				if(a < 12){
					datas[i][j] = a;
				}else{
					datas[i][j] = 0;
				}
			}
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
		cellSpace = ((float)mViewWidth - (TOTAL_COL + 1) * SPACE) / TOTAL_COL;
		textPaint.setTextSize(cellSpace / 2);
	}

	private float pointX;
	private float pointY;
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		String showNum;
		for(int i=0; i<TOTAL_ROW; i++){
			for(int j=0; j<TOTAL_COL; j++){
				pointX = SPACE * (j + 1) + j * cellSpace;
				pointY = SPACE * (i + 1) + i * cellSpace;
				//绘制背景
				rectf.set(pointX, pointY, pointX + cellSpace, pointY + cellSpace);
				paint.setColor(colors[datas[i][j]]);
				canvas.drawRect(rectf, paint);
				if(datas[i][j] != 0){
					//绘制数字
					if(datas[i][j] == 1 || datas[i][j] == 2){
						textPaint.setColor(Color.rgb(0, 0, 0));
					}else{
						textPaint.setColor(Color.rgb(255, 255, 255));
					}
					showNum = (int)Math.pow(2, datas[i][j]) + "";
					canvas.drawText(showNum, pointX + (cellSpace - textPaint.measureText(showNum)) / 2,
							pointY + (cellSpace + textPaint.measureText(showNum, 0, 1)) / 2, textPaint);
				}
			}
		}
	}
}
