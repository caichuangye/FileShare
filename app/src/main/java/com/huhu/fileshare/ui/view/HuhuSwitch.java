package com.huhu.fileshare.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.huhu.fileshare.R;

/**
 * Created by Administrator on 2016/4/20.
 */
public class HuhuSwitch extends View implements View.OnTouchListener{
    private static final int MODE_CLICK = 0;
    private static final int MODE_MOVE = 1;

    private static final int POS_LEFT = 0;
    private static final int POS_RIGHT = 1;
    private static final int POS_MOVE = 2;

    private int mMode = MODE_CLICK;

    private int mMargin;

    private Paint mPaint;

    private int mWidth;
    private int mHeight;

    private int mStatus;

    private int mLastX;
    private int mDownX;

    private int mLastPos;

    private int mTouchSlop;

    private int mCheckedColor = getResources().getColor(R.color.title_color);
    private int mUnCheckedColor = Color.argb(255, 190, 194, 200);
    private int mSlideCircleColor = Color.WHITE;

    private OnChangedListener mListener;

    public HuhuSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Color.BLUE);
        setOnTouchListener(this);
        mStatus = 0;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mLastX = mDownX = 0;
    }

    @Override
    public void onDraw(Canvas canvas){
        int w = getWidth();
        int h = getHeight();
        mWidth = w;
        mHeight = h;
        mMargin = 6;
        mPaint.setAntiAlias(true);
        if(mStatus == POS_LEFT) {
            mPaint.setColor(mUnCheckedColor);

            canvas.drawCircle(h / 2, h / 2, h / 2, mPaint);
            canvas.drawRect(h / 2, 0, w - h / 2, h, mPaint);
            canvas.drawCircle(w - h / 2, h / 2, h / 2, mPaint);

            mPaint.setColor(mSlideCircleColor);
            canvas.drawCircle(h / 2, h / 2, h / 2 - mMargin / 2, mPaint);
            mLastPos = POS_LEFT;
            if(mListener != null){
                mListener.onChange(false);
            }


        }else if(mStatus == POS_RIGHT){
            mPaint.setColor(mCheckedColor);

            canvas.drawCircle(h / 2, h / 2, h / 2, mPaint);
            canvas.drawRect(h / 2, 0, w - h / 2, h, mPaint);
            canvas.drawCircle(w - h / 2, h / 2, h / 2, mPaint);

            mPaint.setColor(mSlideCircleColor);
            canvas.drawCircle(w - h / 2, h / 2, h / 2 - mMargin / 2, mPaint);
            mLastPos = POS_RIGHT;
            if(mListener != null){
                mListener.onChange(true);
            }

        }else if(mStatus == POS_MOVE){
            if(mDownX < mWidth/2) {
                mPaint.setColor(mUnCheckedColor);
            }else{
                mPaint.setColor(mCheckedColor);
            }

            canvas.drawCircle(h / 2, h / 2, h / 2, mPaint);
            canvas.drawRect(h / 2, 0, w - h / 2, h, mPaint);
            canvas.drawCircle(w - h / 2, h / 2, h / 2, mPaint);

            mPaint.setColor(mSlideCircleColor);
            canvas.drawCircle(mDownX,h/2,h/2 - mMargin/2,mPaint);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int status =  mStatus;
        boolean move = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                mDownX = (int) event.getX();
                if (mDownX - mLastX > mTouchSlop) {
                    mStatus = POS_MOVE;
                    if (mDownX < mHeight / 2) {
                        mDownX = mHeight / 2;
                    } else if (mDownX > mWidth - mHeight / 2) {
                        mDownX = mWidth - mHeight / 2;
                    }
                    move = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                int x = (int) event.getX();
                int y = (int) event.getY();
                if(mMode == MODE_MOVE) {
                    mStatus = getPosition(x, y);
                }else{
                    if(mLastPos == POS_LEFT){
                        mStatus = POS_RIGHT;
                    }else if(mLastPos == POS_RIGHT){
                        mStatus = POS_LEFT;
                    }
                }
                break;
        }

        if (status != mStatus || move) {
            invalidate();
        }

        return true;
    }


    private int getPosition(int x ,int y) {
        if (x < mWidth / 2) {
            return POS_LEFT;
        } else {
            return POS_RIGHT;
        }
    }

    public interface OnChangedListener{
        void onChange(boolean checked);
    }

    public void setChecked(boolean checked){
        if(checked){
            mStatus = POS_RIGHT;
        }else{
            mStatus = POS_LEFT;
        }
        invalidate();
    }

    public void setCheckedColor(int color){
        mCheckedColor = color;
    }

    public void setUnCheckedColor(int color){
        mUnCheckedColor = color;
    }

    public void setSlideCircleColor(int color){
        mSlideCircleColor = color;
    }

    public void setOnChangedListener(OnChangedListener listener){
        mListener = listener;
    }

    public boolean isChecked(){
        return mLastPos == POS_RIGHT? true:false;
    }

}







/*

 public static class DevicesItem{
        String ip;
        long timeStamp;
    }

    private List<DevicesItem> mList = new ArrayList<>();

    public void update(DevicesItem item) {
        long now = item.timeStamp;
        boolean isSame = false;
        for (DevicesItem devicesItem : mList) {
            if (devicesItem.ip.equals(item.ip)) {//same device
                devicesItem.timeStamp = now;
                isSame = true;
            } else {
                if (now - devicesItem.timeStamp > 1500) {
                    mList.remove(devicesItem);
                } else {
                    devicesItem.timeStamp = now;
                }
            }
        }
        if(!isSame){
            mList.add(item);
        }
    }

*/




