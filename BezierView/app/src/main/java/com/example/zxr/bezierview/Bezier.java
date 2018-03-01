package com.example.zxr.bezierview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Eastime on 2018/2/28.
 */

public class Bezier extends View {

    private Paint mPaint;
    private PointF start, end, control, firstControl;
    private int centerX, centerY;
    private int mAllCount = 10;//一共弹几次
    private int mCount = 0;//当前正在弹第几次
    private boolean isUp = false;//是否在松开状态
    private boolean isMove = false;//是否线周边触摸
    private static int mDirection_Up = 1;
    private static int mDirection_Down = 2;
    private int mDirection = 0;//最后手松开的点方向，1线中心的上方，2下方
    private float mOffset_m = 0.5f;//每次震动偏移系数
    private float mOffset = 0;//每次震动偏移量
    private long mSpeed = 50;//回弹速度

    public Bezier(Context context) {
        this(context, null);
    }

    public Bezier(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Bezier(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initPointF();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(60);
    }

    private void initPointF() {
        start = new PointF(0, 0);
        end = new PointF(0, 0);
        control = new PointF(0, 0);
        firstControl = new PointF(0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        // 初始化数据点和控制点的位置
        start.x = centerX - 400;
        start.y = centerY;
        end.x = centerX + 400;
        end.y = centerY;
        control.x = centerX;
        control.y = centerY;
        firstControl.x = control.x;
        firstControl.y = control.y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCount = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://0
                isUp = false;
                RectF rect = new RectF(start.x - 20, start.y - 20, end.x + 20, end.y + 20);
                isMove = rect.contains(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP://1
                isUp = true;
                float x = event.getX();
                float y = event.getY();
                if (y > centerY) {
                    mDirection = mDirection_Down;
                    mOffset = y - centerY;
                } else if (y < centerY) {
                    mDirection = mDirection_Up;
                    mOffset = centerY - y;
                }
//                control.x = firstControl.x;
//                control.y = firstControl.y;
                break;
            case MotionEvent.ACTION_MOVE://2
                // 根据触摸位置更新控制点，并提示重绘
                control.x = event.getX();
                control.y = event.getY();
                isUp = false;
                break;
        }
        if (isMove) {
            invalidate();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制数据点和控制点
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(20);
        canvas.drawPoint(start.x, start.y, mPaint);
        canvas.drawPoint(end.x, end.y, mPaint);
//        canvas.drawPoint(control.x, control.y, mPaint);

        // 绘制辅助线
//        mPaint.setStrokeWidth(4);
//        canvas.drawLine(start.x, start.y, control.x, control.y, mPaint);
//        canvas.drawLine(end.x, end.y, control.x, control.y, mPaint);

        // 绘制贝塞尔曲线
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(8);

        Path path = new Path();

        path.moveTo(start.x, start.y);
        path.quadTo(control.x, control.y, end.x, end.y);
        canvas.drawPath(path, mPaint);

        if (isUp && mCount < mAllCount) {
            control.x = firstControl.x;
            mCount += 1;
            mOffset = mOffset_m * mOffset;
            if (mDirection == mDirection_Down) {
                if (mCount % 2 == 0) {
                    control.y = firstControl.y - mOffset;
                } else {
                    control.y = firstControl.y + mOffset;
                }
            } else if (mDirection == mDirection_Up) {
                if (mCount % 2 == 0) {
                    control.y = firstControl.y + mOffset;
                } else {
                    control.y = firstControl.y - mOffset;
                }
            }
            postInvalidateDelayed(mSpeed);
        } else if (mCount == mAllCount) {
            control.x = firstControl.x;
            control.y = firstControl.y;
            postInvalidateDelayed(mSpeed);
        }
    }
}
