package com.huhu.fileshare.model;

/**
 * Created by Administrator on 2016/4/9.
 */
public class DeviceItem {
    private int mIconIndex;

    private String mUserName;

    private String mIP;

    private boolean mHasShared;

    private long mTimeStamp;

    private boolean mNeedRefresh;

    private boolean mIsScanning;

    private byte mSharedType;

    public DeviceItem(int index,String name,String ip,boolean has,boolean refresh,long time,byte type){
        mIconIndex = index;
        mUserName = name;
        mIP = ip;
        mHasShared = has;
        mTimeStamp = time;
        mNeedRefresh = refresh;
        mIsScanning = false;
        mSharedType = type;
    }

    public byte getSharedType(){
        return mSharedType;
    }

    public void setSharedType(byte flag){
        mSharedType = flag;
    }

    public int getIconIndex() {
        return mIconIndex;
    }

    public void setIconIndex(int iconIndex) {
        this.mIconIndex = iconIndex;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getIP() {
        return mIP;
    }

    public void setIP(String ip) {
        this.mIP = ip;
    }

    public boolean hasShared(){
        return mHasShared;
    }

    public void setHasShared(boolean has){
        mHasShared = has;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }

    public  boolean isNeedRefresh(){
        return mNeedRefresh;
    }

    public void setRefresh(boolean refresh){
        mNeedRefresh = refresh;
    }

    public boolean isScaning(){
        return mIsScanning;
    }

    public void setIsScaning(boolean scaning){
        mIsScanning = scaning;
    }
}
