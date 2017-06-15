package com.huhu.fileshare.download;

import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.google.gson.Gson;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.OperationInfo;
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
    private static volatile TransferClient sInstance = null;

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
            synchronized (TransferClient.class) {
                if(sInstance == null) {
                    sInstance = new TransferClient();
                }
            }
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
                    if(!ShareApplication.getInstance().isFileDeleted(unit.ip,unit.serverPath)) {
                        mWorkPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                                boolean res = receiveFile(unit);
                                String str = res ? "success" : "failed";
                            }
                        });
                    }else{
                    }
                    Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            String remotePath = unit.serverPath;

            Gson gson = new Gson();
            OperationInfo operInfo = new OperationInfo(GlobalParams.OperationType.REQUEST,unit.serverPath,unit.totalSize);
            operInfo.start = unit.recvSize;
            String str = gson.toJson(operInfo);
            OutputStream output = socket.getOutputStream();
            output.write(str.getBytes());

            long size = unit.totalSize;
            long offset = unit.recvSize;
            String name = remotePath.substring(remotePath.lastIndexOf(File.separator) + 1);
            String localPath = Environment.getExternalStorageDirectory().getPath() + File.separator + GlobalParams.FOLDER;
            if (name.endsWith(".apk")) {
                localPath += File.separator + unit.desc + ".apk";
            } else {
                localPath += File.separator + name;
            }
            FileOutputStream outputStream = new FileOutputStream(localPath,true);
            while (offset < size) {

                if(ShareApplication.getInstance().isFileDeleted(unit.ip,unit.serverPath)){
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                    socket.close();
                    return false;
                }

                byte[] data = new byte[1024 * 8];
                int tmp = inputStream.read(data, 0, 1024 * 8);
                if (tmp == -1) {
                    break;
                }
                //没有权限下载文件
                if(tmp == GlobalParams.PERMISSION_DENIED.length()){
                    String info = new String(data,0,tmp,"utf-8");
                    if(info.equals(GlobalParams.PERMISSION_DENIED)){
                        HLog.w(getClass(),HLog.T,"no permission to download: "+remotePath);
                        return false;
                    }
                }
                outputStream.write(data, 0, tmp);
                offset += tmp;
                if (mListener != null) {
                    mListener.onTransfer(unit.uuid, localPath,unit.totalSize, offset);
                } else {
                    Log.w(TAG, "mListener == null, can update");
                }
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            socket.close();
            Log.d(TAG, "end receive " + unit.serverPath);
        } catch (Exception e) {
            Log.e(TAG, "receive err, " +unit.serverPath +": "+e.getMessage());
        }
        return true;
    }


    /**
     * 向服务器请求一个文件
     */
    public void requestFile(DownloadItem info, String ip) {
        ReceiveUnit unit = new ReceiveUnit(ip,info.getFromPath(),info.getUUID(),info.getTotalSize(),info.getRecvSize());
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
            if(mWorkPool.isShutdown()){
                mWorkPool = Executors.newFixedThreadPool(GlobalParams.CLIENT_DOWNLOAD_THREAD_NUM+1);
            }
            mWorkPool.execute(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
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
        void onTransfer(String uuid,String path, long totalSize, long receiveSize);
    }

    /**
     * 用来描述一个下载信息
     */
    public class ReceiveUnit{

        public ReceiveUnit(String ip, String serverPath, String uuid, long totalSize, long recvSize){
            this.ip = ip;
            this.serverPath = serverPath;
            this.uuid = uuid;
            this.totalSize = totalSize;
            this.recvSize = recvSize;
        }
        String ip;
        String serverPath;
        String uuid;
        long totalSize;
        long recvSize;
        String desc;
    }

}
