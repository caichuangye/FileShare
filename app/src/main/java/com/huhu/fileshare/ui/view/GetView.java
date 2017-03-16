package com.huhu.fileshare.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.huhu.fileshare.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/16.
 */
public class GetView extends View implements Runnable {

    private int mRefreshInternal = 200;

    private Handler mWorkHandler;

    private int mLineWidth = 5;

    private int mLineColor;

    private int mCenterCircleColor;

    private float mLittleRadius;

    private float mCenterRadius;

    private int mLittleColor1;

    private int mLittleColor2;

    private int mLittleColor3;

    private int mLittleColor4;

    private Paint mPaint;

    private List<Integer> mColorList;

    public GetView(Context context, AttributeSet set) {
        super(context, set);

        HandlerThread thread = new HandlerThread("refresh-get-view");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mLineColor = Color.rgb(36, 183, 164);
        mCenterCircleColor = Color.rgb(36, 183, 164);

        mLittleColor1 = Color.rgb(221, 80, 68);
        mLittleColor2 = Color.rgb(255, 206, 67);
        mLittleColor3 = Color.rgb(23, 160, 94);
        mLittleColor4 = Color.rgb(76, 139, 245);
        mColorList = new ArrayList<>();
        mColorList.add(mLittleColor1);
        mColorList.add(mLittleColor2);
        mColorList.add(mLittleColor3);
        mColorList.add(mLittleColor4);
        mWorkHandler.postDelayed(this, mRefreshInternal);
    }

    @Override
    public void run() {
        postInvalidate();
     //   mWorkHandler.postDelayed(this, mRefreshInternal);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        int radius = Math.min(w, h);

        float r = radius * 0.5f * 0.85f;

        PointF pCenter = new PointF(w / 2f, h / 2f);

        PointF p1 = new PointF(w / 2f, h / 2f - r);
        PointF p12 = new PointF(w / 2f, h / 2f + r);

        float tmp = (float) (r / Math.sqrt(2.0));

        PointF p2 = new PointF(w / 2f + tmp, h / 2 - tmp);
        PointF p22 = new PointF(w / 2f - tmp, h / 2 + tmp);

        PointF p3 = new PointF(w / 2f + r, h / 2);
        PointF p32 = new PointF(w / 2f - r, h / 2);

        PointF p4 = new PointF(w / 2f + tmp, h / 2 + tmp);
        PointF p42 = new PointF(w / 2f - tmp, h / 2 - tmp);

        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(mLineWidth);
        canvas.drawLine(p1.x, p1.y, p12.x, p12.y, mPaint);
        canvas.drawLine(p2.x, p2.y, p22.x, p22.y, mPaint);
        canvas.drawLine(p3.x, p3.y, p32.x, p32.y, mPaint);
        canvas.drawLine(p4.x, p4.y, p42.x, p42.y, mPaint);

        mCenterRadius = radius * 0.25f * 0.75f;
        mPaint.setColor(mCenterCircleColor);
        canvas.drawCircle(pCenter.x, pCenter.y, mCenterRadius, mPaint);

        mLittleRadius = radius * 0.125f * 0.5f;

        mPaint.setColor(mColorList.get(0));
        canvas.drawCircle(p1.x, p1.y, mLittleRadius, mPaint);
        canvas.drawCircle(p12.x, p12.y, mLittleRadius, mPaint);

        mPaint.setColor(mColorList.get(1));
        canvas.drawCircle(p2.x, p2.y, mLittleRadius, mPaint);
        canvas.drawCircle(p22.x, p22.y, mLittleRadius, mPaint);

        mPaint.setColor(mColorList.get(2));
        canvas.drawCircle(p3.x, p3.y, mLittleRadius, mPaint);
        canvas.drawCircle(p32.x, p32.y, mLittleRadius, mPaint);

        mPaint.setColor(mColorList.get(3));
        canvas.drawCircle(p4.x, p4.y, mLittleRadius, mPaint);
        canvas.drawCircle(p42.x, p42.y, mLittleRadius, mPaint);

        int last = mColorList.remove(mColorList.size() - 1);
        mColorList.add(0, last);
    }

}
