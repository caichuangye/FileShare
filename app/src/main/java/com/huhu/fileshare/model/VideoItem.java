package com.huhu.fileshare.model;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/4/15.
 */
public class VideoItem extends BaseItem {

    private long mDuration;

    public VideoItem(){
        super();
    }

    public VideoItem(String name,String path,long size,boolean selected,String cover,long duration){
        super(name,path,size,selected,cover);
        mDuration = duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long mDuration) {
        this.mDuration = mDuration;
    }
}
