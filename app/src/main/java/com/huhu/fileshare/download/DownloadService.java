package com.huhu.fileshare.download;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.huhu.fileshare.IDownloadListenerInterface;
import com.huhu.fileshare.IDownloadServicelInterface;
import com.huhu.fileshare.databases.DownloadHistory;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.huhu.fileshare.databases.DownloadHistory.ADD_ITEM;
import static com.huhu.fileshare.databases.DownloadHistory.DELETE_ITEM;
import static com.huhu.fileshare.databases.DownloadHistory.UPDATE_ITEM;
import static com.huhu.fileshare.util.HLog.DD;

/**
 * Created by Administrator on 2016/5/10.
 */
public class DownloadService extends Service{

    public static final String TAG = "CCYS";

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
        TransferClient.getInstance().init(10935, new TransferClient.OnTransferData() {
            @Override
            public void onTransfer(String uuid, long total, long recv) {
                if(mListener != null){
                    try {
                        Log.d(DD,uuid+"="+total+"->"+recv);
                        mListener.onProgress(uuid,total,recv);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e("transfer",e.getMessage());
                    }
                }else{
                    Log.e("transfer","download listener == null");
                }
            }
        });
        TransferServer.getInstance(getApplicationContext()).startServer(10935);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.d("transfer","-----------destroy service-----------");
        TransferServer.getInstance(getApplicationContext()).quit();
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
        public void addDownloadItem(String uuid,String ip,String fromPath,long size,String fromUser,String type,String destName)
                throws RemoteException {
            HLog.d(DD,"Service:addDownloadItem, ip = "+ip+", from path = "+fromPath);
            DownloadItem item = new DownloadItem(ip,size,fromPath,fromUser,uuid,type);
            item.setDestName(destName);
            mDownloadHistory.operateDatabases(item, ADD_ITEM);
            TransferClient.getInstance().requestFiles(item,ip);
        }

//        @Override
//        public void addDownloadItem(DownloadItem item)
//                throws RemoteException {
//
//        }

        @Override
        public void deleteDownloadItem(String uuid) throws RemoteException {
            DownloadItem item = new DownloadItem();
            item.setUUID(uuid);
            mDownloadHistory.operateDatabases(item,DELETE_ITEM);
            mRemovedItemList.add(uuid);
        }
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info) {
        if(info.getData().getStatus() == DownloadStatus.SUCCESSED) {
            mDownloadHistory.operateDatabases(info.getData(), UPDATE_ITEM);
        }
    }
}
