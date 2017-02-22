package com.huhu.fileshare.download;

import android.os.Environment;
import android.util.Log;


import com.huhu.fileshare.model.DownloadItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by caichuangye on 2017-01-16.
 */

public class TransferClient {

    public static String TAG = TransferClient.class.getSimpleName();

    private ExecutorService mWorkPool;

    private static TransferClient sInstance = null;

    private ConcurrentMap<String, ReceiveListItem> mFilesMap;

    private Boolean mQuit;

    private boolean mInitial = false;

    private int mPort = 10235;

    private OnTransferData mListener;

    public static TransferClient getInstance() {
        if (sInstance == null) {
            sInstance = new TransferClient();
        }
        return sInstance;
    }

    public void init(int port,OnTransferData listener){
        mListener = listener;
        mPort = port;
    }

    private TransferClient() {
        mWorkPool = Executors.newCachedThreadPool();
        mFilesMap = new ConcurrentHashMap<>();
        mQuit = false;
    }

    private void startWorkThreadPool() {
        while (true) {
            synchronized (mQuit) {
                if (mQuit) {
                    break;
                }
                for (final Map.Entry<String, ReceiveListItem> entry : mFilesMap.entrySet()) {
                 //   Log.d(TAG,"--begin iter--");
                    if (!entry.getValue().isHandleOver()) {
                        Log.d(TAG,"begin recv from "+entry.getKey());
                      //  entry.getValue().setHandle();
                        mWorkPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                receiveFiles(entry.getValue());
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                   //     Log.d(TAG,"recv over from "+entry.getKey());
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void receiveFiles(ReceiveListItem item) {
        Log.d(TAG,"receiveFiles: "+item.getIP());
        try {
            InetAddress address = InetAddress.getByName(item.getIP());
            Socket socket = new Socket(address, mPort);
            socket.setSoTimeout(25000);
            InputStream inputStream = socket.getInputStream();
            while (true) {
                DownloadItem info = item.getFile();
                if (info == null) {
                    Log.d("transfer-c","recv: null ");
                    break;
                }else{
                    Log.d("transfer-c","recv: "+info.getFromPath());
                }
                String remotePath = info.getFromPath();
                long size = info.getTotalSize();
                long offset = 0;
                String name = remotePath.substring(remotePath.lastIndexOf(File.separator) + 1);
                String localPath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"fileshare";
                localPath += File.separator + name;
                File file = new File(localPath);
                file.deleteOnExit();
                FileOutputStream outputStream = new FileOutputStream(file);
                while (offset < size) {
                    byte[] data = new byte[1024 * 4];
                    int tmp = inputStream.read(data, 0, 1024 * 4);
                    if(tmp == -1){
                        break;
                    }
                    outputStream.write(data, 0, tmp);
                    offset += tmp;
                    if(mListener != null){
                     //   Log.d(TAG,info.getFromPath()+": "+info.getTotalSize()+", tmp = "+tmp+", offset = "+offset);
                        mListener.onTransfer(info.getUUID(),info.getTotalSize(),offset);
                    }else{
                        Log.w(TAG,"mListener == null, can update");
                    }
                }
                outputStream.flush();
            //    outputStream.close();
                Log.d(TAG,"end recv "+info.getFromPath());
            }
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        } catch (IOException e) {
            Log.e(TAG,e.toString());
        }

    }

    public void requestFiles(List<DownloadItem> list, String ip) {
        ReceiveListItem item = mFilesMap.get(ip);
        if (item == null) {
            item = new ReceiveListItem(ip, list);
        } else {
            item.appendFilesList(list);
        }
        mFilesMap.put(ip, item);
        Log.d(TAG,"request files from "+ip+", count = "+list.size()+", map = "+mFilesMap.size());
        if (!mInitial) {
            mInitial = true;
            mWorkPool.execute(new Runnable() {
                @Override
                public void run() {
                    startWorkThreadPool();
                }
            });
        }
    }

    public void requestFiles(DownloadItem info, String ip) {
        List<DownloadItem> list = new ArrayList<>();
        list.add(info);
        requestFiles(list,ip);
    }

    public void quit() {
        synchronized (mQuit) {
            mQuit = true;
        }
        mWorkPool.shutdown();
        for (Map.Entry<String, ReceiveListItem> entry : mFilesMap.entrySet()) {
            ReceiveListItem info = entry.getValue();
            info.clear();
        }
        mFilesMap.clear();
    }

    public interface OnTransferData{
        void onTransfer(String uuid, long total, long recv);
    }

}
