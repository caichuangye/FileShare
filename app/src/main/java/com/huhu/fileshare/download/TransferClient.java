package com.huhu.fileshare.download;

import android.os.Environment;
import android.util.Log;

import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by caichuangye on 2017-01-16.
 */

public class TransferClient {

    public static String TAG = TransferClient.class.getSimpleName();

    /**
     * 线程池，用户循环处理下载队列和开始每一个下载任务
     */
    private ExecutorService mWorkPool;

    /**
     *
     */
    private static TransferClient sInstance = null;

    /**
     * 下载队列
     */
    private BlockingQueue<ReceiveUnit> mReceiveList;

    /**
     * 是否停止循环处理下载队列
     */
    private Boolean mQuit;

    /**
     * 是否初始化
     */
    private boolean mInitial = false;

    /**
     * 下载端口
     */
    private int mPort;

    /**
     * 下载进度回调
     */
    private OnTransferDataListener mListener;

    /**
     * 单例
     */
    public static TransferClient getInstance() {
        if (sInstance == null) {
            sInstance = new TransferClient();
        }
        return sInstance;
    }

    /**
     * 初始化端口和下载进度回调
     */
    public void init(int port, OnTransferDataListener listener) {
        mListener = listener;
        mPort = port;
    }

    /**
     * 需要注意，线程池中只有2个线程，而且其中一个是用来循环处理下载队列，这也就意味着还剩余一个线程用来处理下载。
     * 也就是说，一个客户端同时只能进行一个下载任务，但可以同时请求多个，这些请求的任务会顺序执行
     */
    private TransferClient() {
        mWorkPool = Executors.newFixedThreadPool(GlobalParams.CLIENT_DOWNLOAD_THREAD_NUM+1);
        mReceiveList = new LinkedBlockingQueue<>();
        mQuit = false;
    }


    /**
     * 循环处理下载队列
     */
    private void startWorkThreadPool() {
        while (true) {
            synchronized (mQuit) {
                if (mQuit) {
                    break;
                }
            }
            try {
                    final ReceiveUnit unit = mReceiveList.take();
                    mWorkPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            boolean res = receiveFile(unit);
                            String str = res ? "success" : "failed";
                            HLog.d(TAG, "receive +" + unit.path + ": " + str);
                        }
                    });
            } catch (InterruptedException e) {
                e.printStackTrace();
                HLog.d(TAG,"run loop to receive: "+e.getMessage());
            }
        }
    }

    /**
     * 下载核心过程
     */
    private boolean receiveFile(ReceiveUnit unit) {
        try {
            InetAddress address = InetAddress.getByName(unit.ip);
            Socket socket = new Socket(address, mPort);
            socket.setSoTimeout(25000);
            InputStream inputStream = socket.getInputStream();
            String remotePath = unit.path;

            OutputStream output = socket.getOutputStream();
            String str = GlobalParams.REQUEST_TAG + remotePath;
            output.write(str.getBytes());

            long size = unit.totalSize;
            long offset = 0;
            String name = remotePath.substring(remotePath.lastIndexOf(File.separator) + 1);
            String localPath = Environment.getExternalStorageDirectory().getPath() + File.separator + GlobalParams.FOLDER;
            if (name.endsWith(".apk")) {
                localPath += File.separator + unit.desc + ".apk";
            } else {
                localPath += File.separator + name;
            }
            File file = new File(localPath);
            file.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(file);
            while (offset < size) {
                byte[] data = new byte[1024 * 4];
                int tmp = inputStream.read(data, 0, 1024 * 4);
                if (tmp == -1) {
                    break;
                }
                //没有权限下载文件
                if(tmp == GlobalParams.PERMISSION_DENIED.length()){
                    String info = new String(data,0,tmp,"utf-8");
                    if(info.equals(GlobalParams.PERMISSION_DENIED)){
                        HLog.e(TAG,"no permission to download: "+remotePath);
                        return false;
                    }
                }
                outputStream.write(data, 0, tmp);
                offset += tmp;
                if (mListener != null) {
                    mListener.onTransfer(unit.uuid, unit.totalSize, offset);
                } else {
                    Log.w(TAG, "mListener == null, can update");
                }
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            socket.close();
            Log.d(TAG, "end receive " + unit.path);
        } catch (Exception e) {
            Log.e(TAG, "receive err, " +unit.path+": "+e.getMessage());
        }
        return true;
    }


    /**
     * 向服务器请求一个文件
     */
    public void requestFile(DownloadItem info, String ip) {
        ReceiveUnit unit = new ReceiveUnit(ip,info.getFromPath(),info.getUUID(),info.getTotalSize());
        if(info.getFromPath().endsWith(".apk")){
            unit.desc = info.getDestName();
        }
        try {
            mReceiveList.put(unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    /**
     * 停止下载
     */
    public void quit() {
        synchronized (mQuit) {
            mQuit = true;
        }
        mWorkPool.shutdown();
        mReceiveList.clear();
    }

    /**
     * 下载进度回调
     */
    public interface OnTransferDataListener {
        void onTransfer(String uuid, long totalSize, long receiveSize);
    }

    /**
     * 用来描述一个下载信息
     */
    public class ReceiveUnit{

        public ReceiveUnit(String ip,String path,String uuid,long totalSize){
            this.ip = ip;
            this.path = path;
            this.uuid = uuid;
            this.totalSize = totalSize;
        }
        String ip;
        String path;
        String uuid;
        long totalSize;
        String desc;
    }

}
