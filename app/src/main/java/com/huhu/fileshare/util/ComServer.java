package com.huhu.fileshare.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.huhu.fileshare.ShareApplication;

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

    private static ComServer sInstance;

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
            HLog.d(TAG, e.getMessage());
        }
        HandlerThread thread = new HandlerThread("recv");
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
                HLog.e(TAG, e.getMessage());
            }
        }
    }

    private void handleSocket(Socket socket) {
        try {
            HLog.d(TAG, "server recv, from: " + socket.getInetAddress().getHostName());
            InputStream inputStream = socket.getInputStream();
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            String str = new String(data);
            handleRequest(str, socket);
            socket.close();
        } catch (IOException e) {
            HLog.e(TAG,e.getMessage());
        }
    }

    private void handleRequest(String request,Socket socket){
        HLog.d("shareinfo", "handleRequest: " + request);
        switch (request){
            case GlobalParams.REQUEST_SHARED_FILES:
                Log.d("shareinfo","1111111----");
                String reply = ShareApplication.getInstance().getAllSharedFiles();
                Log.d("shareinfo","json = "+reply);
                sendResponse(reply,socket);
                break;
            default:
                Log.d("shareinfo","pase request = "+request);
                parseData(request);
                break;
        }
    }

    private void parseData(String request){

    }

    private void sendResponse(String reply, Socket socket){
        HLog.d(TAG, "server build reply: " + reply);
        byte[] data = reply.getBytes();
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
        }catch (IOException e){
            HLog.e(TAG,e.getMessage());
        }
    }

    public boolean isQuit() {
        synchronized (ComServer.class) {
            return mIsQuit;
        }
    }

}
