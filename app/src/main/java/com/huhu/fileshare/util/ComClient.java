package com.huhu.fileshare.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.huhu.fileshare.de.greenrobot.event.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2016/4/20.
 */
public class ComClient implements Runnable{

    private Handler mHandler;

    public static final String TAG = "CCOM_CLIENT";

    private static ComClient sComClient;

    private String mSendMsg;

    private String mIP;

    private ComClient(String ip){
        mIP = ip;
        HandlerThread thread = new HandlerThread("common-client");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public static ComClient getInstance(String ip){
        if(sComClient == null){
            synchronized (ComClient.class){
                if(sComClient == null){
                    sComClient = new ComClient(ip);
                }
            }
        }else{
            sComClient.setIP(ip);
        }
        return sComClient;
    }

    private void setIP(String ip){
        mIP = ip;
    }

    public void sendMessage(String msg){
        mSendMsg = msg;
        mHandler.post(this);
    }

    @Override
    public void run() {
        Socket socket;
        try{
            socket = new Socket(mIP,GlobalParams.SEND_PORT);
            socket.setSoTimeout(3000*10);
            HandlerThread thread = new HandlerThread("common-client-send");
            thread.start();
            mHandler = new Handler(thread.getLooper());
        }catch (UnknownHostException e){
            HLog.e(TAG,e.getMessage());
            return;
        }catch (IOException e){
            HLog.e(TAG,e.getMessage());
            return;
        }

        HLog.d(TAG, "send:" + mSendMsg);
        byte[] data = mSendMsg.getBytes();
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();

            HLog.d(TAG, "after send, then to recv reply");


            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            String line = null;
            while((line = bReader.readLine()) != null){
                stringBuilder.append(line);
            }

            HLog.d(TAG, "after send to "+mIP+", recv:" + stringBuilder.toString());
            if(mSendMsg.equals(GlobalParams.REQUEST_SHARED_FILES)) {
                HLog.d("RECCY","---------------------got reply REQUEST_SHARED_FILES, then post----------------------");
                EventBus.getDefault().post(new EventBusType.SharedFilesReply(stringBuilder.toString(),mIP));
            }
        }catch (SocketTimeoutException eout){
            eout.printStackTrace(System.out);
            HLog.e(TAG,"client read reply timeout");
        }catch (IOException e){
            e.printStackTrace(System.out);
            HLog.e(TAG, TextUtils.isEmpty(e.getMessage()) ? "-send msg occur unknown error" : e.getMessage());
        }

        try {
            socket.close();
        }catch (IOException e){
            HLog.e(TAG,e.getMessage());
        }
    }
}
