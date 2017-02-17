package com.huhu.fileshare.model;


/**
 * Created by Administrator on 2016/4/15.
 */
public class BaseItem {

    protected String mCoverUrl;

    protected String mShowName;

    protected String mPath;

    protected long mSize;

    protected boolean mIsSelected;

    public BaseItem(String name,String path,long size,boolean selected,String cover){
        mShowName = name;
        mPath = path;
        mSize = size;
        mIsSelected = selected;
        mCoverUrl = cover;
    }

    public BaseItem(){

    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public String getShowName() {
        return mShowName;
    }

    public void setShowName(String mShowName) {
        this.mShowName = mShowName;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long mSize) {
        this.mSize = mSize;
    }

    public boolean isSelected(){
        return mIsSelected;
    }

    public void setSelected(boolean selected){
        mIsSelected = selected;
    }

    public String getCoverBitMap() {
        return mCoverUrl;
    }

    public void setCoverBitMap(String mCoverBitMap) {
        this.mCoverUrl = mCoverBitMap;
    }

}
