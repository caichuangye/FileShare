package com.huhu.fileshare.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
        HLog.d(getClass(),HLog.S,"request msg = "+msg);
        mSendMsg = msg;
        mHandler.post(this);
    }

    @Override
    public void run() {
        Socket socket;
        try{
            socket = new Socket(mIP,GlobalParams.SEND_PORT);
            socket.setSoTimeout(3000*10);
        }catch (UnknownHostException e){
            HLog.e(getClass(),HLog.S,e.getMessage());
            return;
        }catch (IOException e){
            HLog.e(getClass(),HLog.S,e.getMessage());
            return;
        }

        byte[] data = mSendMsg.getBytes();
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            if(mSendMsg.equals(GlobalParams.REQUEST_SHARED_FILES)) {
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                String line = null;
                while((line = bReader.readLine()) != null){
                    stringBuilder.append(line);
                }
                HLog.d(getClass(),HLog.S,"recv: "+stringBuilder.toString());
                EventBus.getDefault().post(new EventBusType.SharedFilesReply(stringBuilder.toString(),mIP));
            }else if(mSendMsg.equals(GlobalParams.REQUEST_ICON_PATH)){
                InputStream inputStream = socket.getInputStream();
                HLog.d(getClass(),HLog.S,"REQUEST_ICON_PATH: get reply, inputStream.size = "+inputStream.available());
                int iconSize = (int)UserIconManager.getInstance().getServerIconSize(mIP);
                if(iconSize > 0) {
                    byte[] img = new byte[iconSize];
                    inputStream.read(img);
                    HLog.d(getClass(), HLog.S, "REQUEST_ICON_PATH: get reply, record.size = " + img.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                    UserIconManager.getInstance().setBitMap(mIP, bitmap);
                }
            }
        }catch (SocketTimeoutException out){
            HLog.e(getClass(),HLog.T,"client read reply timeout");
        }catch (IOException e){
            HLog.e(getClass(),HLog.T, TextUtils.isEmpty(e.getMessage()) ? "-send msg occur unknown error" : e.getMessage());
        }

        try {
            socket.close();
        }catch (IOException e){
            HLog.e(getClass(),HLog.S,e.getMessage());
        }
    }
}
