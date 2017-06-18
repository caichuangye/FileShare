package com.huhu.fileshare.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

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
                sendIconPath(socket);
                break;
            default:
                parseData(request);
                break;
        }
    }

    private void parseData(String request) {

    }

    private void sendIconPath(Socket socket) {
        Bitmap bitmap = UserIconManager.getInstance().getSelfIconBitmap(ShareApplication.getInstance());
        if(bitmap != null) {
            byte[] data = CommonUtil.bitmap2Bytes(bitmap);
            sendReplay(data, socket);
        }

    }

    private void sendResponse(String reply, Socket socket) {
        byte[] data = reply.getBytes();
        sendReplay(data, socket);
    }

    private void sendReplay(byte[] data, Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            HLog.e(getClass(), HLog.S, e.getMessage());
        }
    }

    public boolean isQuit() {
        synchronized (ComServer.class) {
            return mIsQuit;
        }
    }

}
