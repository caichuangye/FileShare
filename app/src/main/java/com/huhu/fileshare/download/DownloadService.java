package com.huhu.fileshare.download;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.huhu.fileshare.IDownloadListenerInterface;
import com.huhu.fileshare.IDownloadServicelInterface;
import com.huhu.fileshare.databases.DownloadHistory;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;

import static com.huhu.fileshare.databases.DownloadHistory.ADD_ITEM;
import static com.huhu.fileshare.databases.DownloadHistory.DELETE_ITEM;
import static com.huhu.fileshare.databases.DownloadHistory.UPDATE_ITEM;
import static com.huhu.fileshare.util.HLog.DD;

/**
 * Created by Administrator on 2016/5/10.
 */
public class DownloadService extends Service{

    public static final String TAG = DownloadService.class.getSimpleName();

    private DownloadBinder mBinder;

    private IDownloadListenerInterface mListener;

    private DownloadHistory mDownloadHistory;

    private List<String> mRemovedItemList;

    @Override
    public void onCreate(){
        super.onCreate();
        EventBus.getDefault().register(this);
        mBinder = new DownloadBinder();
        mDownloadHistory = new DownloadHistory(getApplicationContext());
        mRemovedItemList = new ArrayList<>();
        TransferClient.getInstance().init(GlobalParams.DOWNLOAD_PORT, new TransferClient.OnTransferDataListener() {
            @Override
            public void onTransfer(String uuid, long total, long recv) {
                if(mListener != null){
                    try {
                        Log.d(TAG,uuid+"="+total+"->"+recv);
                        DownloadItem item = new DownloadItem();
                        item.setUUID(uuid);
                        if(total == recv){
                            item.setStatus(DownloadStatus.SUCCESSED);
                        }else{
                            item.setStatus(DownloadStatus.DOWNLOADING);
                        }
                        item.setEndTime(String.valueOf(System.currentTimeMillis()));
                        item.setRecvSize(recv);
                    //    mDownloadHistory.operateDatabases(item,UPDATE_ITEM);
                        mListener.onProgress(uuid,total,recv);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
                    }
                }else{
                    Log.e(TAG,"download listener == null");
                }
            }
        });
        TransferServer.getInstance().startServer(GlobalParams.DOWNLOAD_PORT);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.d(TAG,"-----------destroy service-----------");
        TransferServer.getInstance().quit();
        TransferClient.getInstance().quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle bundle = intent.getBundleExtra("data");
        mListener = IDownloadListenerInterface.Stub.asInterface(bundle.getBinder("listener"));
        return mBinder;
    }

    private class DownloadBinder extends IDownloadServicelInterface.Stub{

        @Override
        public void addDownloadItem(String uuid,String ip,String fromPath,long size,long recv,String fromUser,String type,String destName)
                throws RemoteException {
            HLog.d(TAG,"Service:addDownloadItem, ip = "+ip+", from path = "+fromPath);
            DownloadItem item = new DownloadItem(ip,size,fromPath,fromUser,uuid,type);
            item.setRecvSize(recv);
            item.setDestName(destName);
            mDownloadHistory.operateDatabases(item, ADD_ITEM);
            TransferClient.getInstance().requestFile(item,ip);
        }

        @Override
        public void deleteDownloadItem(String uuid) throws RemoteException {
            DownloadItem item = new DownloadItem();
            item.setUUID(uuid);
            mDownloadHistory.operateDatabases(item,DELETE_ITEM);
            mRemovedItemList.add(uuid);
        }
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info) {
        mDownloadHistory.operateDatabases(info.getData(), UPDATE_ITEM);
    }
}
