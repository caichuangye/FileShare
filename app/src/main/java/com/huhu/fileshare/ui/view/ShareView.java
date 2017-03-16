package com.huhu.fileshare.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2017/1/14.
 */

public class ShareView extends View implements Runnable {

    private int mRefreshInternal = 200;

    private Paint mPaint;

    private Handler mWorkHandler;

    private int mCenterColor;

    private int mLittleCircleColor;

    private int mLineColor;

    private int mLineWidth = 5;

    private Boolean mUpdateFlag = true;

    public ShareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        HandlerThread thread = new HandlerThread("refresh-share-view");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());
        mPaint = new Paint(Color.CYAN);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mCenterColor = Color.rgb(36, 183, 164);
        mLittleCircleColor = Color.rgb(144, 198, 82);
        mLineColor = Color.rgb(36, 183, 164);
        mWorkHandler.postDelayed(this, mRefreshInternal);
    }

    @Override
    public void run() {
        postInvalidate();
      //  mWorkHandler.postDelayed(this, mRefreshInternal);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        int radius = Math.min(w, h);
        PointF pLittle1 = new PointF(w * 0.75f, h * 0.25f);
        PointF pLittle2 = new PointF(w * 0.75f, h * 0.75f);
        PointF pLittle3 = new PointF(w * 0.25f, h * 0.5f);
        float r1 = radius * 0.125f;
        float r2 = radius * 0.125f;
        float r3 = radius * 0.25f * 0.75f;

        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(mLineWidth);
        canvas.drawLine(pLittle3.x, pLittle3.y, pLittle1.x, pLittle1.y, mPaint);
        canvas.drawLine(pLittle3.x, pLittle3.y, pLittle2.x, pLittle2.y, mPaint);

        if (mUpdateFlag) {
            mPaint.setColor(mLittleCircleColor);
        } else {
            mPaint.setColor(mCenterColor);
        }
        canvas.drawCircle(pLittle1.x, pLittle1.y, r1, mPaint);
        canvas.drawCircle(pLittle2.x, pLittle2.y, r2, mPaint);

        if (mUpdateFlag) {
            mPaint.setColor(mCenterColor);
        } else {
            mPaint.setColor(mLittleCircleColor);
        }
        canvas.drawCircle(pLittle3.x, pLittle3.y, r3, mPaint);
        mUpdateFlag = !mUpdateFlag;
    }
}


