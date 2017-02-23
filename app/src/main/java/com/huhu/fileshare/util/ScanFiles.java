package com.huhu.fileshare.util;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.SDCardFileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/4/10.
 */
public class ScanFiles {

    private static String TAG = "ScanFiles";

    private Handler mWorkerHandler;

    private static ScanFiles sInstance;

    public static ScanFiles getInstance(){
        if(sInstance == null){
            synchronized (ScanFiles.class){
                if(sInstance == null){
                    sInstance = new ScanFiles();
                }
            }
        }
        return sInstance;
    }

    private ScanFiles(){
        HandlerThread handlerThread = new HandlerThread("scan");
        handlerThread.start();
        mWorkerHandler = new Handler(handlerThread.getLooper());
    }

    public String getStoragePath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public void scan(final String path){
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!TextUtils.isEmpty(path)) {
                    List<SDCardFileItem> list = new ArrayList<SDCardFileItem>();
                    File file = new File(path);
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            String p = f.getAbsolutePath();
                            HLog.d(TAG,"path = "+f.getAbsolutePath());
                            if (f.isFile()) {
                                SDCardFileItem item = new SDCardFileItem(SDCardFileItem.TYPE_FILE, p, f.length());
                                list.add(item);
                            } else if (f.isDirectory()) {
                                File[] t = f.listFiles();
                                int s = t == null ? 0 : t.length;
                                SDCardFileItem item = new SDCardFileItem(SDCardFileItem.TYPE_FOLDER, p, s);
                                list.add(item);
                            }
                        }
                        EventBus.getDefault().post(new EventBusType.FileItemsInfo(list));
                    }else{
                        HLog.d(TAG,"path = "+path+" ---------is null");
                    }
                }
            }
        });
    }

}
