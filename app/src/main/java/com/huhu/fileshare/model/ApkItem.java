package com.huhu.fileshare.model;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2016/4/12.
 */
public class ApkItem extends BaseItem {

    private String mDesc;

  //  private Drawable mIcon;

    public ApkItem(){
        super();
    }

    public ApkItem(String name, String path, long size, boolean selected, String cover, String desc){
        super(name,path,size,selected,cover);
        mDesc = desc;
    }

    public String getDesc() {
        return mDesc;
    }


    public void setDesc(String desc) {
        this.mDesc = desc;
    }


//    public void setIcon(Drawable drawable){
//        mIcon = drawable;
//    }
//
//    public Drawable getIcon(){
//        return mIcon;
//    }

}
