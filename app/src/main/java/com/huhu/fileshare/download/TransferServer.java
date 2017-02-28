package com.huhu.fileshare.download;


import android.content.Context;
import android.util.Log;

import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.util.HLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
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

public class TransferServer {

    public static String TAG = TransferServer.class.getSimpleName();

    private ExecutorService mWorkThreadPool;

    private ConcurrentMap<String, SendListItem> mSendMap;

    private Boolean mQuit;

    private int mPort;

    private ServerSocket mServerSocket;

    private Context mContext;

    private static TransferServer sInstance;

    public static TransferServer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TransferServer(context);
        }
        return sInstance;
    }

    private TransferServer(Context context) {
        mContext = context;
        mQuit = false;
        mWorkThreadPool = Executors.newCachedThreadPool();
        mSendMap = new ConcurrentHashMap<>();
    }

    /**
     * call this to start server
     */
    public void startServer(int port) {
        mPort = port;
        initServerSocket();
    }


    private List<DownloadItem> getSendFilesByIP(String ip) {
        return ShareApplication.getInstance().getSendList(ip);
    }

    private boolean sendFile(String path, Socket socket) {
        if (socket.isClosed()) {
            Log.e(TAG, "socket is closed, can not send file");
            return false;
        }
        long start = System.currentTimeMillis();
        long size = 0;
        boolean res = false;
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "file is no existed, can not send file");
            return res;
        }
        FileInputStream inputStream = null;
        OutputStream outputStream = null;
        Log.d(TAG, "start send: " + path);
        try {
            inputStream = new FileInputStream(file);
            size = file.length();
            long offset = 0;
            int length = 1024 * 4;
            outputStream = socket.getOutputStream();
            while (size > offset) {
                byte[] bytes = new byte[length];
                if (offset + length > size) {
                    length = (int) (size - offset);
                }
                int tmp = inputStream.read(bytes, 0, length);
                outputStream.write(bytes, 0, tmp);
                offset += tmp;
            }
            res = true;
            Log.d(TAG, "end send: " + path);
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
            Log.e(TAG, "err while sending, msg = " + e.getMessage());
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (Exception e) {

            }
        }
        long consume = System.currentTimeMillis() - start;
        float rate = size/consume;
        rate = rate*(1000/1024f);
        HLog.d(TAG,"rate = "+rate+"KB/s"+", size = "+size+", time = "+consume);
        return res;
    }

    public void quit() {
        synchronized (mQuit) {
            mQuit = true;
        }
        mWorkThreadPool.shutdown();
        for (Map.Entry<String, SendListItem> entry : mSendMap.entrySet()) {
            SendListItem info = entry.getValue();
            info.clear();
        }
        mSendMap.clear();
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pendingFiles(String ip, List<DownloadItem> list) {
        SendListItem info = mSendMap.get(ip);
        if (info != null) {
            info.appendFilesList(list);
            mSendMap.put(ip, info);
        }
    }

    public void pendingFiles(String ip, DownloadItem str) {
        List<DownloadItem> list = new ArrayList<>();
        list.add(str);
        pendingFiles(ip, list);
    }


    public void deleteFiles(String ip, List<String> list) {
        SendListItem info = mSendMap.get(ip);
        if (info != null && list != null) {
            info.deleteFileList(list);
            mSendMap.put(ip, info);
        }
    }

    private int mCurrentIndex = 0;

    private void initServerSocket() {
        try {
            mServerSocket = new ServerSocket(mPort);
            mWorkThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (mQuit == true) {
                            break;
                        }
                        try {
                            Log.d(TAG, "server waiting...");
                            final Socket socket = mServerSocket.accept();
                            Log.d(TAG, "socket = " + socket.toString());
                            String destIP = socket.getInetAddress().getHostAddress();
                            List<DownloadItem> list = getSendFilesByIP(destIP);

                            if (list == null) {
                                int times = 3;
                                while (times > 0) {
                                    list = getSendFilesByIP(destIP);
                                    if (list != null) {
                                        break;
                                    } else {
                                        times--;
                                        try {
                                            Thread.sleep(150);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            final String path = list.get(mCurrentIndex++).getFromPath();

                            mWorkThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    sendFile(path, socket);
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
