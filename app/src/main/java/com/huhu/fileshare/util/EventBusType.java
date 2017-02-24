package com.huhu.fileshare.util;

import com.huhu.fileshare.model.BaseItem;
import com.huhu.fileshare.model.DeviceItem;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.SDCardFileItem;
import com.huhu.fileshare.model.ImageFolderItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.CommonFileItem;
import com.huhu.fileshare.model.VideoItem;

import java.util.List;

/**
 * Created by Administrator on 2016/4/9.
 */
public class EventBusType {

    public static final int FIND_FRAGMENT = 0;

    public static final int DEVICES_FRAGMENT = 1;

    public static final int SHARE_FRAGMENT = 2;

    public static final int DOWNLOAD_FRAGMENT = 3;

    public static final int SETTING_FRAGMENT = 4;

    public static final int SHARE_IMAGE_FRAGMENT = 5;

    public static final int SHARE_MUSIC_FRAGMENT = 6;

    public static final int SHARE_VIDEO_FRAGMENT = 7;

    public static final int SHARE_FILE_FRAGMENT = 8;

    public static final int SHARE_STORAGE_FRAGMENT = 9;

    /**
     *
     */
    public static class ChangeMainFragment{

        private String mTitle;

        private int mId;

        public ChangeMainFragment(int id,String title){
            this.mId = id;
            mTitle = title;
        }

        public int getId(){
            return mId;
        }

        public String getTitle(){
            return mTitle;
        }
    }


    /**
     *
     */
    public static class FileItemsInfo{

        private List<SDCardFileItem> mDataList;

        public FileItemsInfo(List<SDCardFileItem> list){
            mDataList = list;
        }

        public List<SDCardFileItem> getData(){
            return mDataList;
        }
    }

    /**
     *
     */
    public static class SharedFileInfo{

        private Object mData;

        private GlobalParams.ShareType mType;

        private boolean mIsAdd;

        public SharedFileInfo(Object data,GlobalParams.ShareType type,boolean isAdd){
            mData = data;
            mType = type;
            mIsAdd = isAdd;
        }

        public String getPath(){
            return ((BaseItem)mData).getPath();
        }

        public GlobalParams.ShareType getType(){
            return mType;
        }

        public boolean isAdd(){
            return mIsAdd;
        }

        public Object getData(){
            return mData;
        }

    }

    /**
     *
     */
    public static class ShareMusicInfo{
        private List<MusicItem> mData;

        public ShareMusicInfo(List<MusicItem> items){
            mData = items;
        }

        public List<MusicItem> getData(){
            return mData;
        }
    }

    /**
     *
     */
    public static class ShareVideoInfo{
        private List<VideoItem> mData;

        public ShareVideoInfo(List<VideoItem> items){
            mData = items;
        }

        public List<VideoItem> getData(){
            return mData;
        }
    }

    /**
     *
     */
    public static class ShareImageFolderInfo{
        private List<ImageFolderItem> mData;

        public ShareImageFolderInfo(List<ImageFolderItem> items){
            mData = items;
        }

        public List<ImageFolderItem> getData(){
            return mData;
        }
    }


    /**
     *
     */
    public static class ShareSpecialFileInfo{
        private List<CommonFileItem> mData;

        public ShareSpecialFileInfo(List<CommonFileItem> items){
            mData = items;
        }

        public List<CommonFileItem> getData(){
            return mData;
        }
    }

    /**
     *
     */
    public static class OnlineDevicesInfo{
        private List<DeviceItem> mData;

        public OnlineDevicesInfo(List<DeviceItem> items){
            mData = items;
        }

        public List<DeviceItem> getData(){
            return mData;
        }
    }

    /**
     *
     */
    public static class SharedFilesReply{
        private String mData;

        private String mIP;

        public SharedFilesReply(String str,String ip){
            mData = str;
            mIP = ip;
        }

        public String getData(){
            return mData;
        }

        public String getIP(){
            return mIP;
        }
    }

    /**
     *
     */
    public static class SharedFilesCount{
        private int mCount;

        public SharedFilesCount(int count){
            mCount = count;
        }

        public int getCount(){
            return mCount;
        }
    }

    /**
     *
     */
    public static class WiFiStatus{
        private boolean mIsConnected;

        public WiFiStatus(boolean connect){
            mIsConnected = connect;
        }

        public boolean isConnected(){
            return mIsConnected;
        }
    }

    /**
     *
     */
    public static class ConnectInfo{
        private int mStatus;

        private boolean mIsWiFiAvailble;

        private String mSSID;

        private String mBSSID;

        public ConnectInfo(boolean enable, int status,String bssid,String ssid){
            mIsWiFiAvailble = enable;
            mStatus = status;
            mBSSID = bssid;
            mSSID = ssid;
        }

        public int getStatus(){
            return mStatus;
        }

        public String getBSSID(){
            return mBSSID;
        }

        public String getSSID(){
            return mSSID;
        }

        public boolean wifiAvailvle(){
            return mIsWiFiAvailble;
        }

        @Override
        public String toString(){
            return "["+mStatus+" "+mSSID+" "+mBSSID+"]";
        }
    }

    /**
     *
     */
    public static class ClearShared{

        public ClearShared(){
        }

    }

    /**
     *
     */
    public static class DeleteDownloadFile{
        private DownloadItem mData;

        public DeleteDownloadFile(DownloadItem item){
            mData = item;
        }

        public DownloadItem getData(){
            return mData;
        }
    }

    /**
     *
     */
    public static class UpdateDownloadFile{
        private DownloadItem mData;

        private GlobalParams.DownloadOper mOper;

        public UpdateDownloadFile(DownloadItem item,GlobalParams.DownloadOper oper){
            mData = item;
            mOper = oper;
        }

        public GlobalParams.DownloadOper getOper(){
            return mOper;
        }

        public DownloadItem getData(){
            return mData;
        }
    }

    /**
     *
     */
    public static class UpdateSharedFiles{

        public UpdateSharedFiles(){

        }

    }

    public static class CacheApkIconComplete{

        public String path;

        public String coverPath;

        public CacheApkIconComplete(String path,String coverPath){
            this.path = path;
            this.coverPath = coverPath;
        }

    }

}
