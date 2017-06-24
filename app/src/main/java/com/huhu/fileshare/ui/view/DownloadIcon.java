package com.huhu.fileshare.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.huhu.fileshare.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/14.
 */

public class DownloadIcon extends View {

    private Status mStatus;

    private Map<Status,Info> mInfoMap;

    private int mTextSize = 36;

    private int mPercent;

    public enum Status{
        INIT,
        WAIT,
        DOWNLOADING,
        COMPLETE
    }

    public class Info{
        public Info(String info,int color){
            this.info = info;
            this.color = color;
        }
        String info;
        int color;
    }

    public DownloadIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInfoMap = new HashMap<>();
        mInfoMap.put(Status.INIT,new Info("下载",Color.WHITE));
        mInfoMap.put(Status.WAIT,new Info("等待中",Color.WHITE));
        mInfoMap.put(Status.DOWNLOADING,new Info("正在下载",Color.WHITE));
        mInfoMap.put(Status.COMPLETE,new Info("打开",Color.WHITE));
        mStatus = Status.INIT;
    }

    public void setStatus(Status status){
        if(status != null) {
            mStatus = status;
            invalidate();
        }
    }

    public void setStatus(int percent) {
        mPercent = percent;
        mStatus = Status.DOWNLOADING;
        invalidate();
    }

    public Status getStatus(){
        return mStatus;
    }


    @Override
    protected void onDraw(Canvas canvas){
        int w = getWidth();
        int h= getHeight();
        String info = mInfoMap.get(mStatus).info;
        if(mStatus == Status.DOWNLOADING){
            info = mPercent+"%";
        }
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.download_color));
        p.setAntiAlias(true);
        canvas.drawRoundRect(0,0,w,h,h/2,h/2,p);

        p.setColor(mInfoMap.get(mStatus).color);
        p.setTextSize(mTextSize);
        Rect bound = new Rect();
        p.getTextBounds(info,0,info.length(),bound);
        canvas.drawText(info,0,info.length(),w/2 - bound.width()/2,h/2+bound.height()/2,p);
    }
}
