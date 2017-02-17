package com.huhu.fileshare.model;

/**
 * Created by Administrator on 2016/4/6.
 */
public class WiFiItem {

    private String mName;

    private String mMAC;

    private int mLevel;

    private boolean mIsConnected;

    public WiFiItem(String name, String mac,int level){
        this(name,mac,level,false);
    }

    public WiFiItem(String name, String mac,int level,boolean isConnected){
        mName = name;
        mMAC = mac;
        mLevel = level;
        mIsConnected = isConnected;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getMAC() {
        return mMAC;
    }

    public void setMAC(String MAC) {
        this.mMAC = MAC;
    }

    public boolean getIsConnected(){
        return mIsConnected;
    }

    public void setIsConnected(boolean isConnected){
        mIsConnected = isConnected;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        this.mLevel = level;
    }
}
