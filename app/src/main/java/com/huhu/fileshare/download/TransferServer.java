package com.huhu.fileshare.download;


import android.util.Log;

import com.google.gson.Gson;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.OperationInfo;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by caichuangye on 2017-01-16.
 */

public class TransferServer {

    public static String TAG = TransferServer.class.getSimpleName();

    /**
     * 线程池，用户向客户端发送文件
     */
    private ExecutorService mWorkThreadPool;

    /**
     * 标志是否循环监听客户端的连接
     */
    private Boolean mQuit;

    /**
     * 服务器监听的端口
     */
    private int mPort;

    /**
     * 服务端socket
     */
    private ServerSocket mServerSocket;

    /**
     * 服务端对象，服务端为单例模式
     */
    private static TransferServer sInstance;

    public static TransferServer getInstance() {
        if (sInstance == null) {
            sInstance = new TransferServer();
        }
        return sInstance;
    }

    private TransferServer() {
        mQuit = false;
        mWorkThreadPool = Executors.newFixedThreadPool(5);
    }

    /**
     * 启动服务器监听
     */
    public void startServer(int port) {
        mPort = port;
        initServerSocket();
    }

    /**
     * 服务器接收到客户端发来的请求的文件路径时，需要检测该文件是否被共享了。
     * 只有被共享的文件才向客户端发送
     *
     * @param path
     * @return
     */
    private boolean checkSendPermission(String path) {
        return ShareApplication.getInstance().isFileShared(path);
    }

    /**
     * 核心发送函数
     * 客户端向服务器请求文件时，首先现将要请求的文件路径发送过来，消息的格式为：serverPath:+文件路径
     * @param socket
     * @return true表示发送成功，false表示发送失败
     */
    private boolean sendFile(Socket socket) {
        if (socket.isClosed()) {
            HLog.e(TAG, "socket is closed, can not send file");
            return false;
        }
        Log.d(TAG, "socket = " + socket.toString());
        String path = "";
        long startPos = 0;
        long endPos = -1;
        //读取客户端请求的文件名
        try {
            InputStream readStream = socket.getInputStream();
            byte[] name = new byte[2048];
            int count = readStream.read(name);
            String info = new String(name,0,count);
            Gson gson = new Gson();
            OperationInfo operInfo = gson.fromJson(info,OperationInfo.class);
            if (operInfo.oper.equals(GlobalParams.OperationType.REQUEST)){
                path = operInfo.path;
                startPos = operInfo.start;
                endPos = operInfo.end;
            } else {
                HLog.e(TAG, "read serverPath format wrong: " + path);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            HLog.e(TAG, "read snd serverPath: " + e.getMessage());
            return false;
        }

        boolean res = false;
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "file is no existed, can not send file");
            return res;
        }
        long start = System.currentTimeMillis();
        long size = 0;
        FileInputStream inputStream = null;
        OutputStream outputStream = null;
        Log.d(TAG, "start send: " + path);
        try {
            outputStream = socket.getOutputStream();
            /**
             * 检查客户端请求的文件是否在服务端允许的范围内,若客户端没有权限请求该文件，则向客户端发送"NOPERMISSION"
             */
            if (!checkSendPermission(path)) {
                outputStream.write(GlobalParams.PERMISSION_DENIED.getBytes());
                outputStream.flush();
                HLog.e(TAG, socket.getInetAddress().getHostAddress()+" have no permission to request: " + path);
                outputStream.close();
                socket.close();
                return false;
            }
            inputStream = new FileInputStream(file);
            inputStream.skip(startPos);
            size = file.length();
            long offset = startPos;
            int length = 1024 * 4;
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
        float rate = size / consume;
        rate = rate * (1000 / 1024f);
        HLog.d(TAG, "rate = " + rate + "KB/s" + ", size = " + size + ", time = " + consume);
        return res;
    }

    public void quit() {
        synchronized (mQuit) {
            mQuit = true;
        }
        mWorkThreadPool.shutdown();
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void deleteFiles(String ip, List<String> list) {

    }


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
                            mWorkThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    sendFile(socket);
                                }
                            });

                        } catch (IOException e) {
                            HLog.e(TAG,e.getMessage());
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
