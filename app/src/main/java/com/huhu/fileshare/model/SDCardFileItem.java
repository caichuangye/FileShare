package com.huhu.fileshare.model;

/**
 * Created by Administrator on 2016/4/10.
 */
public class SDCardFileItem extends BaseItem {

    public static final  int TYPE_FILE = 0;

    public static final int TYPE_FOLDER = 1;

    private int mType;

    public SDCardFileItem(int type, String path, long data){
        super(path.substring(path.lastIndexOf("/")+1),path,data,false,null);
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }


}
