package com.huhu.fileshare.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/4.
 */

public class MediaScanner {

    public static final String TAG = MediaScanner.class.getSimpleName();

    private MediaScannerConnection mMediaScannerConnection;

    private Context mContext;

    private static volatile MediaScanner sInstance;

    private List<String> mPendingList;

    private MediaScanner(Context context){
        mContext = context;
        mPendingList = new ArrayList<>();
        mMediaScannerConnection = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                synchronized (mPendingList) {
                    for (String path : mPendingList) {
                        String ext = path.substring(path.lastIndexOf('.') + 1);
                        mMediaScannerConnection.scanFile(path, MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
                    }
                    mPendingList.clear();
                }
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                mMediaScannerConnection.disconnect();
                HLog.d(TAG,path+": "+uri);
                FileQueryHelper.getInstance().parseCoverImage(path,uri);
            }
        });

    }

    public static MediaScanner getInstance(Context context){
        if(sInstance == null){
            synchronized (MediaScanner.class) {
                if(sInstance == null) {
                    sInstance = new MediaScanner(context);
                }
            }
        }
        return sInstance;
    }

    public void scanFile(String path){
        if(TextUtils.isEmpty(path)){
            return;
        }
        File file = new File(path);
        if(!file.exists()){
            HLog.d(TAG,"begin to scan,not f: "+path);
            return;
        }
        HLog.d(TAG,"begin to scan: "+path);
        if(mMediaScannerConnection.isConnected()){
            String ext = path.substring(path.lastIndexOf('.')+1);
            mMediaScannerConnection.scanFile(path, MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        }else {
            synchronized (mPendingList) {
                mPendingList.add(path);
            }
            mMediaScannerConnection.connect();
        }
    }

}
