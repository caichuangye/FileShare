package com.huhu.fileshare.model;

/**
 * Created by Administrator on 2016/10/20.
 */

public enum  DownloadStatus {
    INIT("init"),
    WAIT("wait"),
    DOWNLOADING("downloading"),
    PAUSED("paused"),
    SUCCESSED("successed");

    private String mStatus;

    DownloadStatus(String str){
        mStatus = str;
    }

    @Override
    public String toString(){
        return mStatus;
    }

    public static DownloadStatus getStatus(String str){
        DownloadStatus[] values = DownloadStatus.values();
        for (DownloadStatus status : values) {
            if(status.toString().toUpperCase().equals(str.toUpperCase())){
                return status;
            }
        }
        return null;
    }

}
