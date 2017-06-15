package com.huhu.fileshare.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.huhu.fileshare.download.DownloadService;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.SystemSetting;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Administrator on 2016/10/20.
 */

/**
 *  String ID = "id";
 String START_TIME = "start_time";
 String END_TIME = "end_time";
 String FROM_PATH = "from_path";
 String FROM_USERNAME = "from_username";
 String FROM_IP = "from_ip";
 String TO_PATH = "to_path";
 String TO_USERNAME = "to_username";
 String FILE_TYPE = "file_type";
 String DOWNLOAD_STATES = "download_states";
 */

public class DownloadItem implements Parcelable {
    private String mUUID;
    private String mStartTime;
    private String mEndTime;
    private String mFileType;
    private String mIP;
    private long mTotalSize;
    private String mFromPath;
    private String mToPath;
    private DownloadStatus mStates;
    private long mRecvSize;
    private String mFromUser;
    private String mDestName;
    private String mCoverPath;

    @Override
    public String toString(){
        String str = "uuid = "+mUUID+"; startTime = "+mStartTime+"; endTime = "+mEndTime+"; fileType = "+mFileType+
                "; ip = "+mIP+"; totalSize = "+mTotalSize+"; recvSize = "+mRecvSize+"; fromPath = "+mFromPath+"; toPath = "+mToPath+"; status = "+
                mStates.toString()+"; fromUser = "+mFromUser+", dest name = "+mDestName+", cover = "+mCoverPath;
        return str;
    }

    public String getStartTime(){
        return mStartTime;
    }

    public void setUUID(String id){
        mUUID = id;
    }

    public String getUUID(){
        return mUUID;
    }

    public long getTotalSize(){
        return mTotalSize;
    }

    public void setTotalSize(long size){
        mTotalSize = size;
    }

    public long getRecvSize(){
        return mRecvSize;
    }

    public void setRecvSize(long size){
        mRecvSize  = size;
    }

    public void setStartTime(String time){
        mStartTime = time;
    }

    public String getEndTime(){
        return mEndTime;
    }

    public String getToPath(){
        return mToPath;
    }

    public void setToPath(String path){
        mToPath = path;
    }

    public void setEndTime(String end){
        mEndTime = end;
    }

    public String getFileType(){
        return mFileType;
    }

    public void setFileType(String type){
        mFileType = type;
    }

    public String getFromUserName(){
        return mFromUser;
    }

    public void setFromUserName(String name){
        mFromUser = name;
    }

    public DownloadStatus getStatus(){
        return mStates;
    }

    public void setStatus(DownloadStatus status){
        mStates = status;
    }

    public static final Parcelable.Creator<DownloadItem> CREATOR = new Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Object[] newArray(int size) {
            return new Object[0];
        }
    };

    public DownloadItem(){

    }

    public DownloadItem(String ip,long size,String fromPath,String fromUser,String uuid,String type){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        mStartTime = format.format(date);
        mUUID = uuid;
        mIP = ip;
        mTotalSize = size;
        mFromPath = fromPath;
        mFromUser = fromUser;
        mRecvSize = 0;
        mFileType = type;
        mStates = DownloadStatus.WAIT;
        int index = mFromPath.lastIndexOf("/");
        mToPath = CommonUtil.getAppFolder()+ mFromPath.substring(index);
    }

    public String getFromIP(){
        return mIP;
    }

    public void setIP(String ip){
        mIP = ip;
    }

    public String getFromPath(){
        return mFromPath;
    }

    public void setFromPath(String fromPath){
        mFromPath = fromPath;
    }

    public String getDestName() {
        return mDestName;
    }

    public void setDestName(String destName) {
        this.mDestName = destName;
        if(mFileType.equals(GlobalParams.ShareType.APK.toString())) {
            mToPath = CommonUtil.getAppFolder() + File.separator + mDestName + ".apk";
        }
    }

    public String getCoverPath() {
        return mCoverPath;
    }

    public void setCoverPath(String coverPath) {
        this.mCoverPath = coverPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
