package com.huhu.fileshare.util;

import android.net.Uri;

import com.huhu.fileshare.model.ApkItem;
import com.huhu.fileshare.model.BaseItem;
import com.huhu.fileshare.model.DeviceItem;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.SDCardFileItem;
import com.huhu.fileshare.model.ImageFolderItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.CommonFileItem;
import com.huhu.fileshare.model.VideoItem;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/9.
 */
public class EventBusType {



    /**
     *
     */
    public static class FileItemsInfo {

        private List<SDCardFileItem> mDataList;

        public FileItemsInfo(List<SDCardFileItem> list) {
            mDataList = list;
        }

        public List<SDCardFileItem> getData() {
            return mDataList;
        }
    }

    /**
     *
     */
    public static class SharedFileInfo {

        private Object mData;

        private GlobalParams.ShareType mType;

        private boolean mIsAdd;

        public SharedFileInfo(Object data, GlobalParams.ShareType type, boolean isAdd) {
            mData = data;
            mType = type;
            mIsAdd = isAdd;
        }

        public String getPath() {
            return ((BaseItem) mData).getPath();
        }

        public GlobalParams.ShareType getType() {
            return mType;
        }

        public boolean isAdd() {
            return mIsAdd;
        }

        public Object getData() {
            return mData;
        }

    }

    /**
     *
     */
    public static class ShareMusicInfo {

        private List<MusicItem> mData;

        public ShareMusicInfo(List<MusicItem> item) {
            mData = item;
        }

        public List<MusicItem> getData() {
            return mData;
        }
    }

    /**
     *
     */
    public static class ShareApkInfo {
        private List<ApkItem> mData;

        public ShareApkInfo(List<ApkItem> item) {
            mData = item;
        }

        public List<ApkItem> getData() {
            return mData;
        }
    }

    /**
     *
     */
    public static class ShareVideoInfo {
        private List<VideoItem> mData;

        public ShareVideoInfo(List<VideoItem> item) {
            mData = item;
        }

        public List<VideoItem> getData() {
            return mData;
        }
    }

    /**
     *
     */
    public static class ShareImageFolderInfo {
        private List<ImageFolderItem> mData;

        public ShareImageFolderInfo(List<ImageFolderItem> items) {
            mData = items;
        }

        public List<ImageFolderItem> getData() {
            return mData;
        }
    }


    /**
     *
     */
    public static class ShareCommonFileInfo {
        private List<CommonFileItem> mData;

        public ShareCommonFileInfo(List<CommonFileItem> items) {
            mData = items;
        }

        public List<CommonFileItem> getData() {
            return mData;
        }
    }


    /**
     *
     */
    public static class OnlineDevicesInfo {
        private List<DeviceItem> mData;

        public OnlineDevicesInfo(List<DeviceItem> items) {
            mData = items;
        }

        public List<DeviceItem> getData() {
            return mData;
        }
    }

    /**
     *
     */
    public static class SharedFilesReply {
        private String mData;

        private String mIP;

        public SharedFilesReply(String str, String ip) {
            mData = str;
            mIP = ip;
        }

        public String getData() {
            return mData;
        }

        public String getIP() {
            return mIP;
        }
    }

    /**
     *
     */
    public static class SharedFilesCount {
        private int mCount;

        public SharedFilesCount(int count) {
            mCount = count;
        }

        public int getCount() {
            return mCount;
        }
    }


    /**
     *
     */
    public static class ConnectInfo {
        private int mStatus;

        private boolean mIsWiFiAvailble;

        private String mSSID;

        private String mBSSID;

        public ConnectInfo(boolean enable, int status, String bssid, String ssid) {
            mIsWiFiAvailble = enable;
            mStatus = status;
            mBSSID = bssid;
            mSSID = ssid;
        }

        public int getStatus() {
            return mStatus;
        }

        public String getBSSID() {
            return mBSSID;
        }

        public String getSSID() {
            return mSSID;
        }

        public boolean wifiAvailvle() {
            return mIsWiFiAvailble;
        }

        @Override
        public String toString() {
            return "[" + mStatus + " " + mSSID + " " + mBSSID + "]";
        }
    }

    /**
     *
     */
    public static class ClearShared {

        public ClearShared() {
        }

    }

    /**
     *
     */
    public static class QueryFiles {

        public int index;

        public QueryFiles(int index) {
            this.index = index;
        }

    }

    /**
     *
     */
    public static class DeleteDownloadFile {
        private DownloadItem mData;

        public DeleteDownloadFile(DownloadItem item) {
            mData = item;
        }

        public DownloadItem getData() {
            return mData;
        }
    }

    /**
     *
     */
    public static class UpdateDownloadFile {
        private DownloadItem mData;

        private GlobalParams.DownloadOper mOper;

        public UpdateDownloadFile(DownloadItem item, GlobalParams.DownloadOper oper) {
            mData = item;
            mOper = oper;
        }

        public GlobalParams.DownloadOper getOper() {
            return mOper;
        }

        public DownloadItem getData() {
            return mData;
        }
    }

    /**
     *
     */
    public static class UpdateSharedFiles {

        public UpdateSharedFiles() {

        }

    }

    /**
     *
     */
    public static class NoLocalFiles {

        private GlobalParams.ShareType mType;

        public NoLocalFiles(GlobalParams.ShareType type) {
            mType = type;
        }

        public GlobalParams.ShareType getType(){
            return mType;
        }

    }

    /**
     * 浏览服务端共享页时，点击已近点击下载的按钮时，跳转到下载页面
     * 即第一次点击时开始下载，再次点击时跳到下载页
     */
    public static class GoToDownloadActivity {

        public GoToDownloadActivity() {

        }
    }

    public static class CacheImageComplete {

        public ImageCacher.CacheResult result;

        public CacheImageComplete(ImageCacher.CacheResult result) {
            this.result = result;
        }

    }

    public static class ResetDownloadStatus{
        public Map<GlobalParams.ShareType,List<String>> map;

        public ResetDownloadStatus(Map<GlobalParams.ShareType,List<String>> map){
            this.map = map;
        }
    }

    /**
     * 文件下载到本地后需要通过MediaScannerService进行扫描，否则系统的媒体数据库无法识别到这些文件
     */
    public static class ScanDownloadFileComplete{
        /**
         * 文件的本地路径
         */
        public String path;

        /**
         * 文件封面地址
         */
        public String uri;
        public ScanDownloadFileComplete(String path,String uri){
            this.path = path;
            this.uri = uri;
        }
    }

    public static class DownloadList{
        public List<DownloadItem> list;

        public DownloadList(List<DownloadItem> list){
            this.list = list;
        }
    }

    public static class StartViewAction {
        public String path;
        public StartViewAction(String path){
            this.path = path;
        }
    }


}
