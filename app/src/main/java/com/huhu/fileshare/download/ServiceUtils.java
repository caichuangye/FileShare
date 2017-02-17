package com.huhu.fileshare.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.huhu.fileshare.IDownloadListenerInterface;
import com.huhu.fileshare.IDownloadServicelInterface;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.ScanDeviceItem;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.HLog;

/**
 * Created by Administrator on 2016/10/20.
 */

public class ServiceUtils {

    private static ServiceUtils sUtils;

    private ServiceConnection mDownloadConnection;

    private IDownloadServicelInterface mService;

    private boolean mIsConnected = false;

    public static ServiceUtils getInstance(){
        if(sUtils == null){
            sUtils = new ServiceUtils();
        }
        return sUtils;
    }

    private ServiceUtils(){
        mDownloadConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IDownloadServicelInterface.Stub.asInterface(service);
                mIsConnected = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                mIsConnected = false;
            }
        };
    }

    public  void addDownloadItem(DownloadItem item){
        if(mIsConnected){
            try {
                mService.addDownloadItem(item.getUUID(),item.getFromIP(),item.getFromPath(),item.getTotalSize(),
                        item.getFromUserName(),item.getFileType());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public  void deleteDownloadItem(String uuid){
        if(mIsConnected){
            try {
                mService.deleteDownloadItem(uuid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    private Context mContext;
    public void connectDownloadService(Context context){
        if(!mIsConnected){
            mContext = context;
            Intent intent = new Intent(context,DownloadService.class);
            Bundle bundle = new Bundle();
            bundle.putBinder("listener",new DownloadListener());
            intent.putExtra("data",bundle);
            mContext.bindService(intent,mDownloadConnection,Context.BIND_AUTO_CREATE);
        }
    }

    public void disConnected(Context context){
        if(mIsConnected && context == mContext){
            context.unbindService(mDownloadConnection);
        }
    }

    public class DownloadListener extends IDownloadListenerInterface.Stub{

        @Override
        public void onProgress(String uuid,long total,long recv) throws RemoteException {
          //  HLog.d(DownloadService.TAG,"onProgress, uuid = "+uuid+", total = "+total+", recv = "+recv);
            ShareApplication.getInstance().updateDownloadItem(uuid,total,recv);
        }
    }

}
