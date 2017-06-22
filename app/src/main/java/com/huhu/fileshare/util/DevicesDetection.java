package com.huhu.fileshare.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;

import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.DeviceItem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/4/9.
 */
public class DevicesDetection {

    private String TAG = this.getClass().getSimpleName();

    private Handler mSendHandler;

    private Handler mRecvHandler;

    private static volatile DevicesDetection sInstance;

    private Context mContext;

    private boolean mIsStart;

    private boolean mQuit;

    private int mSendInternal;

    private DatagramSocket mSocket;

    private List<DeviceItem> mDevicesList;

    private String mIP;

    public static DevicesDetection getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DevicesDetection.class) {
                if (sInstance == null) {
                    sInstance = new DevicesDetection(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private DevicesDetection(Context context) {
        mContext = context;
        HandlerThread threadSend = new HandlerThread("DevicesDetection_send",Process.THREAD_PRIORITY_BACKGROUND);
        threadSend.start();
        mSendHandler = new Handler(threadSend.getLooper());

        HandlerThread threadRecv = new HandlerThread("DevicesDetection_recv");
        threadRecv.start();
        mRecvHandler = new Handler(threadRecv.getLooper());

        mDevicesList = new ArrayList<>();

        try {
            mSocket = new DatagramSocket(GlobalParams.DETECT_PORT);
        } catch (Exception e) {
            HLog.d(getClass(),HLog.D, e.getMessage());
        }

        mIsStart = false;
        mQuit = false;
        mSendInternal = 500;
    }

    public void start() {
        if (!mIsStart) {
            mQuit = false;
            mIsStart = true;
            receiveOnlineMessage();
            broadcastOnlineMessage();
        }
    }

    public void stop() {
        synchronized (DevicesDetection.class) {
            mQuit = true;
            mIsStart = false;
        }
    }


    private void broadcastOnlineMessage() {
        mSendHandler.post(new Runnable() {
            @Override
            public void run() {
                while (!isQuit() && mSocket != null) {
                    try {
                        if(WiFiOperation.getInstance(mContext).isWiFiConnected()) {
                            byte[] data = buildOnlineMessage();
                            InetAddress address = InetAddress.getByName(getBroadcastIP());
                            DatagramPacket dp = new DatagramPacket(data, data.length, address, GlobalParams.DETECT_PORT);
                            mSocket.send(dp);
                            Thread.sleep(mSendInternal);
                        }else{
                            Thread.sleep(mSendInternal*2);
                        }
                        Thread.sleep(mSendInternal);
                    } catch (IOException e) {
                        HLog.e(getClass(),HLog.D ,e.getMessage());
                    } catch (InterruptedException e) {
                        HLog.e(getClass(),HLog.D, e.getMessage());
                    }
                }
            }
        });
    }

    private String getBroadcastIP() {
        if(TextUtils.isEmpty(mIP) || ShareApplication.getInstance().isNeedRefreshIP()) {
            String ip = WiFiOperation.getInstance(mContext).getIP();
            mIP = ip.substring(0, ip.lastIndexOf(".")) + ".255";
        }
        return mIP;
    }

    private void receiveOnlineMessage() {
        mRecvHandler.post(new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[1024];
                while (!isQuit() && mSocket != null) {
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                    try {
                        mSocket.receive(datagramPacket);
                        int length = datagramPacket.getLength();
                        byte[] buf = new byte[length];
                        for (int i = 0; i < length; i++) {
                            buf[i] = data[i];
                        }
                        parseMessage(buf, datagramPacket.getAddress().getHostAddress());
                    } catch (IOException e) {
                        HLog.e(getClass(),HLog.D, e.getMessage());
                    }
                }
            }
        });
    }


    private void parseMessage(byte[] data, String ip) {
        long now = System.currentTimeMillis();
        boolean isSame = false;
        boolean has = CommonUtil.parseHasSharedFiles(data);
        boolean refresh = CommonUtil.parseNeedRefresh(data);
        String path = CommonUtil.parseIconPath(data);
        long iconSize = CommonUtil.parseIconSize(data);
        boolean needRefreshIcon = UserIconManager.getInstance().setServerIconPath(ip,new UserIconManager.ServerIconItem(path,iconSize));
        if(needRefreshIcon){
            HLog.d(getClass(),HLog.S,"---needRefreshIcon----");
            ComClient.getInstance(ip).sendMessage(GlobalParams.REQUEST_ICON_PATH);
        }
        String name = CommonUtil.parseUserName(data);
        DeviceItem item = new DeviceItem(path, name, ip, has, refresh, now,data[1]);
        List<String> tmp = new ArrayList<>();
        synchronized (DevicesDetection.class) {
            for (DeviceItem devicesItem : mDevicesList) {
                if (devicesItem.getIP().equals(ip)) {
                    devicesItem.setTimeStamp(now);
                    devicesItem.setHasShared(has);
                    devicesItem.setIconPath(path);
                    devicesItem.setUserName(name);
                    devicesItem.setRefresh(refresh);
                    devicesItem.setSharedType(data[1]);
                    isSame = true;
                } else {
                    if (now - devicesItem.getTimeStamp() > 1500) {
                        tmp.add(devicesItem.getIP());
                    } else {
                        devicesItem.setTimeStamp(now);
                    }
                }
            }
            if (!isSame) {
                mDevicesList.add(item);
            }
        }
        for (String ip1 : tmp) {
            for (DeviceItem item1 : mDevicesList) {
                if (ip1.equals(item1.getIP())) {
                    mDevicesList.remove(item1);
                    break;
                }
            }
        }
        EventBus.getDefault().post(new EventBusType.OnlineDevicesInfo(mDevicesList));
        if (refresh) {
            ComClient.getInstance(ip).sendMessage(GlobalParams.REQUEST_SHARED_FILES);
        }
    }

    private boolean isQuit() {
        synchronized (DevicesDetection.class) {
            return mQuit;
        }
    }

    private byte[] buildOnlineMessage() {
        return CommonUtil.buildSendData(mContext);
    }
}
