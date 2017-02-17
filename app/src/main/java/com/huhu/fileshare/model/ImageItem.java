package com.huhu.fileshare.model;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by Administrator on 2016/4/15.
 */
public class ImageItem extends BaseItem {

    private String mDate;

    public ImageItem(){
        super();
    }

    public ImageItem(String name,String path,long size,boolean selected,String cover,String date){
        super(name,path,size,selected,cover);
        mDate = date;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }
}
