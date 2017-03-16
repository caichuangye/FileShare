package com.huhu.fileshare;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.download.ServiceUtils;
import com.huhu.fileshare.model.BaseItem;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.model.ScanCollection;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.util.ComServer;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Map<String,Set<String>> mClientDeletedFiles;

    /**
     * 监听网络连接状态的广播接收者
     */
    private NetReceiver mReceiver;

    private Handler mMainHandler;

    private boolean mNeedRefreshIP = true;

    private class NetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WiFiOperation.getInstance(getApplicationContext()).isWiFiConnected()) {
                    EventBus.getDefault().post(new EventBusType.ConnectInfo(true, GlobalParams.WIFI_CONNECTED,
                            WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiBSSID(),
                            WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiSSID()));
                mNeedRefreshIP = true;
            } else {
                EventBus.getDefault().post(new EventBusType.ConnectInfo(false, GlobalParams.WIFI_NOT_CONNECTED, null, null));
                mNeedRefreshIP = false;
            }
        }
    }

    public boolean isNeedRefreshIP(){
        if(mNeedRefreshIP){
            mNeedRefreshIP = false;
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        FileQueryHelper.getInstance().init(getApplicationContext());
        initImageLoader(getApplicationContext());
        ComServer.getInstance().start();
        mSharedCollection = new SharedCollection();
        mRequestCollection = new ScanCollection();
        mAllSharedCollection = new HashMap<>();
        mClientDeletedFiles = new HashMap<>();
        sInstance = this;
        mReceiver = new NetReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        mMainHandler = new Handler(Looper.getMainLooper());
        initStrictMode();
    }

    private void initStrictMode(){
        ApplicationInfo info = getApplicationInfo();
        int flag = info.flags & ApplicationInfo.FLAG_DEBUGGABLE;
        if(flag == 2) {
            HLog.d(TAG,"init strict mode");
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    public static ShareApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mReceiver);
    }

    /**
     * 初始化imageloader
     */
    public void initImageLoader(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context,
                GlobalParams.FOLDER+"/cache");// 获取到缓存的目录地址
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
        operateShardFile(info);
    }


    private String mScanningServerIP;

    private String mScanningServerName;

    public void setServerInfo(String ip,String name){
        mScanningServerIP = ip;
        mScanningServerName = name;
    }


    public void requestFile(EventBusType.SharedFileInfo info){
        Set<String> tmp = mClientDeletedFiles.get(mScanningServerIP);
        if(tmp != null){
            boolean res = tmp.remove(info.getPath());
            if(res){
                HLog.d(TAG,"remove from deleted list: "+info.getPath());
            }
        }
        mClientDeletedFiles.put(mScanningServerIP,tmp);
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
            if(info.getType().equals(GlobalParams.ShareType.APK)){
                item.setDestName(item0.getShowName());
            }
            String name = item0.getPath().substring(item0.getPath().lastIndexOf(File.separator) + 1);
            String localPath = Environment.getExternalStorageDirectory().getPath() + File.separator + GlobalParams.FOLDER;
            if (name.endsWith(".apk")) {
                localPath += File.separator + item.getDestName() + ".apk";
            } else {
                localPath += File.separator + name;
            }
            if(item.getFileType().equals(GlobalParams.ShareType.IMAGE.toString())){
                item.setCoverPath(localPath);
            }else if(item.getFileType().equals(GlobalParams.ShareType.FILE.toString())){
                item.setCoverPath(String.valueOf(CommonUtil.getCommonFileCoverId(item.getFromPath())));
            }
            item.setToPath(localPath);
            File file = new File(localPath);
            if(file.exists()){
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    if(inputStream.available() < item0.getSize()) {
                        item.setRecvSize(inputStream.available());
                    }else{
                        /**
                         * 若本地已经存在了要下载的文件，并且文件大小也与服务器上文件大小相同，此时的策略是删除本地已存在的文件，从新从服务器上删除
                         */
                        item.setRecvSize(0);
                        file.delete();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException e){

                }
            }
            addScanFile(item);
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


    public byte getSharedType(){
        return mSharedCollection.getSharedType();
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

    public boolean isFileShared(String path){
        return mSharedCollection.isFileShared(path);
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
        int[] iconList = {
                R.mipmap.user_icon_0,
                R.mipmap.user_icon_1,
                R.mipmap.user_icon_2,
                R.mipmap.user_icon_3,
                R.mipmap.user_icon_4,
                R.mipmap.user_icon_5,
                R.mipmap.user_icon_6,
                R.mipmap.user_icon_7,
                R.mipmap.user_icon_8,
                R.mipmap.user_icon_9,
                R.mipmap.user_icon_10,
                R.mipmap.user_icon_11,};
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
    //    Log.d(TAG,"mRequestCollection add: "+item.getFromPath());
    //    item.setStatus(DownloadStatus.WAIT);
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
     * 获取所有要下载的信息
     */
    public List<DownloadItem> getWaitToDownloadingFiles() {
        return mRequestCollection.getWaitToDownloadingFiles();
    }

    public DownloadStatus getFileDownloadStatus(String path){
        DownloadItem item = mRequestCollection.getItemByPath(path);
        return item != null? item.getStatus() : DownloadStatus.INIT;
    }

    public void updateDownloadItem(final String uuid, final long total, final long recv) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mRequestCollection.updateProgress(uuid, total, recv);
            }
        });

    }

    /**
     * 在下载列表中删除一个下载后，服务器的共享页面上对对应的文件状态应被重置为初始化状态，即可再次点击下载
     * @param list
     */
    public void setDeletedFiles(List<DownloadItem> list){
        mClientDeletedFiles = CommonUtil.groupByIP(list);
        Map<GlobalParams.ShareType,List<String>> map = new HashMap<>();
        for(DownloadItem item : list){
            GlobalParams.ShareType type = GlobalParams.ShareType.valueOf(item.getFileType().toUpperCase());
            List<String> tmp = map.get(type);
            if(tmp == null){
                tmp = new ArrayList<>();
            }
            mRequestCollection.resetDownloadStatus(item.getFromPath());
            tmp.add(item.getFromPath());
            map.put(type,tmp);
        }
        if(map.size() > 0){
            EventBus.getDefault().post(new EventBusType.ResetDownloadStatus(map));
        }
    }

    public boolean isFileDeleted(String ip,String path){
        Set<String> list = mClientDeletedFiles.get(ip);
        if(list != null){
            return list.contains(path);
        }
        return false;
    }

}
