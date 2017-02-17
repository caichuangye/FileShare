package com.huhu.fileshare.model;

import com.huhu.fileshare.util.GlobalParams;

/**
 * Created by Administrator on 2016/4/26.
 */
public class ScanDeviceItem {
  //  (String uuid,String ip,String fromPath,long size,String fromUser,String type)
    private GlobalParams.ShareType mType;

    private String mFromPath;

    private String mName;

    private long mTotalSize;

    private long mRecvSize;

    private String mFromOwner;

    private String mFromIP;

    private long mDate;

    public ScanDeviceItem(GlobalParams.ShareType shareType,String path,long size,String owner,String ip){
        mType = shareType;
        mFromPath = path;
        mTotalSize = size;
        mRecvSize = 0;
        mName = mFromPath.substring(mFromPath.lastIndexOf("/")+1);
        mFromOwner = owner;
        mFromIP = ip;
        mDate = System.currentTimeMillis();
    }

    public String getFromPath(){
        return mFromPath;
    }

    public String getName() {
        return mName;
    }

    public GlobalParams.ShareType getType() {
        return mType;
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public long getRecvSize() {
        return mRecvSize;
    }

    public String getFromIP() {
        return mFromIP;
    }

    public void setFromIP(String mFromIP) {
        this.mFromIP = mFromIP;
    }

    public String getOwner() {
        return mFromOwner;
    }

    public void setOwner(String mFromOwner) {
        this.mFromOwner = mFromOwner;
    }
}
