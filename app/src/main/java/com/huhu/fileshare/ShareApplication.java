package com.huhu.fileshare;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.BaseAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.download.ServiceUtils;
import com.huhu.fileshare.model.BaseItem;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.model.ScanCollection;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.model.UpdateCommand;
import com.huhu.fileshare.util.ComClient;
import com.huhu.fileshare.util.ComServer;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.WiFiOperation;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huhu.fileshare.util.HLog.DD;

/**
 * Created by Administrator on 2016/4/11.
 */
public class ShareApplication extends Application {

    private String TAG = "ShareApplication";

    /**
     * 添加或删除共享文件的时间戳
     */
    private long mOperateTimeStamp = 0;

    /**
     * 上一次操作时的时间戳
     */
    private long mLastOperateTimeStamp = 0;

    /**
     * 共享文件的总数
     */
    private int mSharedFileSize = 0;

    private static ShareApplication sInstance;


    /**
     * 本机设置为共享的文件的集合
     */
    private SharedCollection mSharedCollection;

    /**
     * 客户端要向服务器下载的文件的集合
     */
    private ScanCollection mRequestCollection;

    /**
     * 所有目标设备的共享文件的集合
     */
    private Map<String, SharedCollection> mAllSharedCollection;

    private Map<String,List<DownloadItem>> mServerToSendList;

    /**
     * 监听网络连接状态的广播接收者
     */
    private NetReceiver mReceiver;

    private Handler mMainHandler;

    private class NetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WiFiOperation.getInstance(getApplicationContext()).isWiFiAvailable()) {
                if (WiFiOperation.getInstance(getApplicationContext()).isWiFiConnected()) {
                    EventBus.getDefault().post(new EventBusType.ConnectInfo(true, GlobalParams.NET_CONNECTED,
                            WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiBSSID(),
                            WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiSSID()));
                } else {
                    EventBus.getDefault().post(new EventBusType.ConnectInfo(true, GlobalParams.NET_NOT_CONNECTED, null, null));
                }
            } else {
                EventBus.getDefault().post(new EventBusType.ConnectInfo(false, GlobalParams.NET_NOT_CONNECTED, null, null));
            }
        }
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        initImageLoader(getApplicationContext());
        ComServer.getInstance(this).start();
        mSharedCollection = new SharedCollection();
        mRequestCollection = new ScanCollection();
        mAllSharedCollection = new HashMap<>();
        mServerToSendList = new HashMap<>();
        sInstance = this;
        mReceiver = new NetReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public static ShareApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(mReceiver);
    }

    /**
     * 初始化imageloader
     */
    public void initImageLoader(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context,
                "fileshare/cache");// 获取到缓存的目录地址
        Log.e("cacheDir", cacheDir.getPath());
        // 创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                //硬盘缓存50MB
                .diskCacheSize(50 * 1024 * 1024)
                //将保存的时候的URI名称用MD5
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                // 加密
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())//将保存的时候的URI名称用HASHCODE加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheFileCount(100) //缓存的File数量
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * 添加或者删除共享文件
     */
    public void onEventMainThread(EventBusType.SharedFileInfo info) {
    //    if (info.getMode() == GlobalParams.SHOW_MODE) {
            operateShardFile(info);
        /*}else if(info.getMode() == GlobalParams.SCAN_MODE){
            tellServerRequestFiles(info);
        }*/
    }


    private String mScanningServerIP;

    private String mScanningServerName;

    public void setServerInfo(String ip,String name){
        mScanningServerIP = ip;
        mScanningServerName = name;
    }

    public void tellServerRequestFiles(EventBusType.SharedFileInfo info){
        boolean isAdd = info.isAdd();
        String path = info.getPath();
        String oper = isAdd?"add" : "delete";
        Log.d("upf","c: "+mScanningServerIP+": "+oper+": "+path);
        UpdateCommand command = new UpdateCommand();
        command.ip = mScanningServerIP;
        command.oper = oper;
        command.path = path;
        Gson gson = new Gson();
        Type type = new TypeToken<UpdateCommand>() {
        }.getType();
        ComClient.getInstance(mScanningServerIP).sendMessage(gson.toJson(command, type));

        BaseItem item0 = (BaseItem)info.getData();
        if(info.isAdd()) {
            DownloadItem item = new DownloadItem();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            item.setStartTime(format.format(date));
            item.setUUID(CommonUtil.getUUID());
            item.setStatus(DownloadStatus.WAIT);
            item.setFileType(info.getType().toString());
            item.setFromPath(item0.getPath());
            item.setTotalSize(item0.getSize());
            item.setFromUserName(mScanningServerName);
            item.setIP(mScanningServerIP);
            addScanFile(item);
        }
    }






    public void updateSendList(UpdateCommand command){
        synchronized (mServerToSendList) {
            List<DownloadItem> list = mServerToSendList.get(command.ip);
            if (list == null) {
                list = new ArrayList<>();
            }
            if(command.oper.toUpperCase().equals("DELETE")){
                for(DownloadItem item : list){
                    if(item.getFromPath().toUpperCase().equals(command.path.toLowerCase())){
                        list.remove(item);
                        break;
                    }
                }
            }else if(command.oper.toUpperCase().equals("ADD")){
                DownloadItem item = new DownloadItem();
                item.setFromPath(command.path);
                list.add(item);
            }
            mServerToSendList.put(command.ip, list);
            for(DownloadItem i : list){
                Log.d("transferu","-----: "+i.getFromPath());
            }
        }
    }

    /**
     * 添加或者删除共享文件
     */
    private void operateShardFile(EventBusType.SharedFileInfo info) {
        mOperateTimeStamp = System.currentTimeMillis();
        if (info.isAdd()) {
            mSharedCollection.addShared(info.getType(), info.getData());
            mSharedFileSize++;
        } else {
            mSharedCollection.deleteShared(info.getType(), info.getPath());
            mSharedFileSize--;
        }
        EventBus.getDefault().post(new EventBusType.SharedFilesCount(mSharedFileSize));
    }


    /**
     * 获取所有共享资源的json字符串
     */
    public String getAllSharedFiles() {
        Gson gson = new Gson();
        Type type = new TypeToken<SharedCollection>() {
        }.getType();
        return gson.toJson(mSharedCollection, type);
    }

    /**
     * 判断当前设备的共享文件有没有被更新
     */
    public boolean needRefresh() {
        boolean res = mOperateTimeStamp != mLastOperateTimeStamp;
        mLastOperateTimeStamp = mOperateTimeStamp;
        return res;
    }

    /**
     * 根据文件类型获取共享的文件
     *
     * @param type 文件类型
     * @return
     */
    public List<String> getSharedFileByType(GlobalParams.ShareType type) {
        return mSharedCollection.getSharedPathByType(type);
    }

    /**
     * 获取共享的文件总数
     */
    public int getSharedFilesCount() {
        return mSharedFileSize;
    }

    /**
     * 获取用户头像的图标的集合
     */
    public int[] getUserIconList() {
        int[] iconList = {R.mipmap.user_icon_0, R.mipmap.user_icon_1, R.mipmap.user_icon_2, R.mipmap.user_icon_3,
                R.mipmap.user_icon_4, R.mipmap.user_icon_5, R.mipmap.user_icon_6, R.mipmap.user_icon_7, R.mipmap.user_icon_8,};
        return iconList;
    }

    /**
     * 获取所有共享的文件
     */
    public SharedCollection getSharedCollection() {
        return mSharedCollection;
    }

    /**
     * 移除所有共享
     */
    public void removeAllShared() {
        mOperateTimeStamp = System.currentTimeMillis();
        mSharedFileSize = 0;
        mSharedCollection.clear();
        EventBus.getDefault().post(new EventBusType.ClearShared());
    }


    /**
     * 获取到目标设备的共享信息后，通知浏览界面刷新目标设备的共享信息
     */
    public void onEventMainThread(EventBusType.SharedFilesReply reply) {
        Gson gson = new Gson();
        Type type = new TypeToken<SharedCollection>() {
        }.getType();
        SharedCollection collection = gson.fromJson(reply.getData(), type);
        collection.mergeSpecialAndSDFiles();
        mAllSharedCollection.put(reply.getIP(), collection);
        EventBus.getDefault().post(new EventBusType.UpdateSharedFiles());
        HLog.d("RECCY", "---------------------in application, notice fragment to update----------------------");
    }

    /**
     * 根据ip获取该设备的所有共享信息
     */
    public SharedCollection getDestAllSharedFiles(String ip) {
        return mAllSharedCollection.get(ip);
    }

    /**
     * 添加一个要下载的文件
     */
    public void addScanFile(DownloadItem item) {
        Log.d(DD,"mRequestCollection add: "+item.getFromPath());
        item.setStatus(DownloadStatus.WAIT);
        mRequestCollection.addFile(item);
        ServiceUtils.getInstance().addDownloadItem(item);
    }

    /**
     * 在下载队列中删除一个文件
     */
    public void deleteScanFile(String uuid) {
        mRequestCollection.deleteFile(uuid);
    }


    /**
     * 在下载队列中删除一个文件
     */
    public void onEventMainThread(EventBusType.DeleteDownloadFile info) {
        deleteScanFile(info.getData().getUUID());
        ServiceUtils.getInstance().deleteDownloadItem(info.getData().getUUID());
    }

    /**
     * 获取本机将要想目标ip发送的文件的集合
     */
    public List<DownloadItem> getSendList(String ip) {
        synchronized (mServerToSendList){
            return mServerToSendList.get(ip);
        }
    }

    /**
     * 获取所有要下载的信息
     */
    public List<DownloadItem> getWaitToDownloadingFiles() {
        return mRequestCollection.getWaitToDownloadingFiles();
    }

    public DownloadStatus getFileDownloadStatus(String path){
        DownloadItem item = mRequestCollection.getItemByPath(path);
        return item != null? item.getStatus() : null;
    }

    public void updateDownloadItem(final String uuid, final long total, final long recv) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mRequestCollection.updateProgress(uuid, total, recv);
            }
        });

    }

}
