package com.huhu.fileshare.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.huhu.fileshare.ShareApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016/4/21.
 */
public class ComServer implements Runnable {

    public static final String TAG = ComServer.class.getSimpleName();

    private static volatile ComServer sInstance;

    private Handler mHandler;

    private ServerSocket mServerSocket;

    private boolean mIsQuit;

    public static ComServer getInstance() {
        if (sInstance == null) {
            synchronized (ComServer.class) {
                if (sInstance == null) {
                    sInstance = new ComServer();
                }
            }
        }
        return sInstance;
    }

    private ComServer() {
        try {
            mServerSocket = new ServerSocket(GlobalParams.SEND_PORT);
        } catch (IOException e) {
            HLog.e(getClass(), HLog.S, e.getMessage());
        }
        HandlerThread thread = new HandlerThread("common-server-recv");
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mIsQuit = false;
    }

    public void start() {
        mHandler.post(this);
    }

    @Override
    public void run() {
        while (!isQuit()) {
            try {
                final Socket socket = mServerSocket.accept();
                new Thread((new Runnable() {
                    @Override
                    public void run() {
                        HLog.d(ComServer.class, HLog.S, "server get conn from: " + socket.getInetAddress().getHostAddress());
                        handleSocket(socket);
                    }
                })).start();
            } catch (IOException e) {
                HLog.e(getClass(), HLog.S, e.getMessage());
            }
        }
    }

    private void handleSocket(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            String str = new String(data);
            handleRequest(str, socket);
            socket.close();
        } catch (IOException e) {
            HLog.e(getClass(), HLog.S, e.getMessage());
        }
    }

    private void handleRequest(String request, Socket socket) {
        switch (request) {
            case GlobalParams.REQUEST_SHARED_FILES:
                String reply = ShareApplication.getInstance().getAllSharedFiles();
                HLog.d(getClass(), HLog.S, "server reply = " + reply);
                sendResponse(reply, socket);
                break;
            case GlobalParams.REQUEST_ICON_PATH:
                sendIconData(socket);
                break;
            default:
                parseData(request);
                break;
        }
    }

    private void parseData(String request) {

    }

    private void sendIconData(Socket socket) {
        String path = SystemSetting.getInstance(ShareApplication.getInstance()).getUserIconPath();
        if (!TextUtils.isEmpty(path)) {
            HLog.d(getClass(), HLog.S, "server send icon, size = " + UserIconManager.getInstance().getSelfIconBitmapSize(ShareApplication.getInstance()) +
                    ", to " + socket.getInetAddress().getHostAddress());
            sendSelfIcon(path, socket);
        } else {
            HLog.d(getClass(), HLog.S, "server to send icon, path is not exists = " + path);
        }

    }

    private void sendResponse(String reply, Socket socket) {
        byte[] data = reply.getBytes();
        sendReplay(data, socket);
    }

    private void sendSelfIcon(String path, Socket socket) {
        byte[] data = CommonUtil.compressImage(path,8);
        if(data == null || data.length <= 0){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return;
        }
        long start = System.currentTimeMillis();
        String ip = socket.getInetAddress().getHostAddress();
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
            int unit = 1024 * 4;
            int offset  = 0;
            int total = data.length;
            while (true) {
                int size = total - offset >= unit ? unit : total - offset;
                outputStream.write(data, offset, size);
                offset += size;
                if (size < unit) {
                    break;
                }
            }
            HLog.d(getClass(), HLog.S, "server send icon to " + ip + " success！");
        } catch (FileNotFoundException e) {
            HLog.e(getClass(), HLog.S, "server send icon to " + ip + " failed: " + e.getMessage());
        } catch (IOException e) {
            HLog.e(getClass(), HLog.S, "server send icon to " + ip + " failed: " + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }
        long time = System.currentTimeMillis() - start;
        if(time <= 0){
            time = 1;
        }
        float rate = data.length/time;// b/ms
        HLog.d(getClass(),HLog.S,"--------------sent to "+ip+" rate = "+rate*(1000f/(1024*1024))+" MB/s, size = "+data.length/1024+" kb, consume "+time+" ms--------------");
    }

    private void sendReplay(byte[] data, Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            HLog.e(getClass(), HLog.S, e.getMessage());
        } finally {

        }
    }

    public boolean isQuit() {
        synchronized (ComServer.class) {
            return mIsQuit;
        }
    }

}
