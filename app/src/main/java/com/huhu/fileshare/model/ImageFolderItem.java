package com.huhu.fileshare.model;

/**
 * Created by Administrator on 2016/4/15.
 */
public class ImageFolderItem extends BaseItem {

    private String mCoverImagePath;

    private long mImageCount;

    public ImageFolderItem(){
        super();
    }

    public ImageFolderItem(String path,String name,long count){
        super(name,path,0,false,null);
        mCoverImagePath = path;
        mImageCount = count;
    }

    public long getImageCount() {
        return mImageCount;
    }

    public void setImageCount(long mImageCount) {
        this.mImageCount = mImageCount;
    }

    public String getCoverImagePath() {
        return mCoverImagePath;
    }

    public void setCoverImagePath(String mCoverImagePath) {
        this.mCoverImagePath = mCoverImagePath;
    }


    @Override
    public String toString(){
        return "image count = "+mImageCount+"; name = "+mShowName+"; path = "+mCoverImagePath;
    }

}
