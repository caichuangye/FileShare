package com.huhu.fileshare.model;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/4/12.
 */
public class MusicItem extends BaseItem {

    private String mArtist;

    public MusicItem(){
        super();
    }

    public MusicItem(String name,String path,long size,boolean selected,String cover,String artist){
        super(name,path,size,selected,cover);
        mArtist = artist;
    }

    public String getArtist() {
        return mArtist;
    }


    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }



}
