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
	private RectF rectf;
	

	public My2048View(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		rectf = new RectF();
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
	}

	private float pointX;
	private float pointY;
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint.setColor(Color.rgb(204, 192, 178));
		for(int i=0; i<TOTAL_ROW; i++){
			for(int j=0; j<TOTAL_COL; j++){
				pointX = SPACE * (j + 1) + j * cellSpace;
				pointY = SPACE * (i + 1) + i * cellSpace;
				rectf.set(pointX, pointY, pointX + cellSpace, pointY + cellSpace);
				canvas.drawRect(rectf, paint);
			}
		}
	}
}
