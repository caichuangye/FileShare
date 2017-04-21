package com.huhu.fileshare.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.WiFiItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caichuangye on 2016-04-07.
 * http://www.easyicon.net/iconsearch/play/
 * http://www.easyicon.net/iconsearch/iconset:File-Format-icons/
 */

public class WiFiOperation {

    private  final String TAG = this.getClass().getSimpleName();

    private static final int SCAN_DONE = 0;

    private List<ScanResult> mScanList;

    private WifiManager mWiFiManager;

    private int mScanInterval = 1000;

    private Handler mRefreshHandler;

    private Handler mMainHandler;

    private static WiFiOperation sInstance = new WiFiOperation();

    private IOnWiFiListScanListener mWifiListChangedListener;

    private Context mContext;

    private boolean mIsAutoRefreshWiFi;

    private AutoRefreshRunnable mAutoRefreshRunnable;

    public static WiFiOperation getInstance(Context context){
        sInstance.init(context);
        return sInstance;
    }

    private void init(Context context){
        if(mWiFiManager == null) {
            mContext = context;
            mWiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
    }

    private WiFiOperation(){
        mIsAutoRefreshWiFi = false;
        mAutoRefreshRunnable = new AutoRefreshRunnable();
        mScanList = new ArrayList<>();
        HandlerThread handlerThread = new HandlerThread("wifi-work-handler");
        handlerThread.start();
        HandlerThread handlerThread1 = new HandlerThread("wifi-refresh-handler");
        handlerThread1.start();
        mRefreshHandler = new Handler(handlerThread1.getLooper());
        mMainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message message){
                if(mWifiListChangedListener != null && message.what == SCAN_DONE){
                    HLog.d(TAG, "begin to update");
                    mWifiListChangedListener.onChanged(convertScanResults(mScanList));
                }
            }
        };
    }

    public void setAutoScanInterval(int interval){
        if(interval >= 0){
            mScanInterval = interval;
        }
    }

    public void setAutoRefreshWiFi(final boolean auto){
        if(auto && mIsAutoRefreshWiFi){
            return;
        }
        mIsAutoRefreshWiFi = auto;
        if(auto){
            mRefreshHandler.post(mAutoRefreshRunnable);
        }else{
            mRefreshHandler.removeCallbacks(mAutoRefreshRunnable);
        }
    }

    private class AutoRefreshRunnable implements Runnable{
        @Override
        public void run() {
            while (mIsAutoRefreshWiFi){
                HLog.d(TAG,"begin scan");
                scanWiFi();
                try {
                    Thread.sleep(mScanInterval);
                }catch (Exception e){
                }
            }
        }
    }

    public void updateConnectionInfo(){
        mRefreshHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean available = isWiFiAvailable();
                int status = isWiFiConnected() ?
                        GlobalParams.WIFI_CONNECTED : GlobalParams.WIFI_NOT_CONNECTED;
                String bssid = getConnectedWiFiBSSID();
                String ssid = getConnectedWiFiSSID();
                EventBus.getDefault().post(new EventBusType.ConnectInfo(available, status, bssid, ssid));
            }
        });
    }

    public void setListener(IOnWiFiListScanListener listener){
        mWifiListChangedListener = listener;
    }

    public void scanWiFi() {
        HLog.d(TAG, "in scan");
        boolean res = mWiFiManager.startScan();
        if (res) {
            HLog.d(TAG, "scan done");
            mScanList.clear();
            mScanList = mWiFiManager.getScanResults();
            HLog.d(TAG, "size = "+mScanList.size());
            mMainHandler.sendEmptyMessage(SCAN_DONE);
        } else {
            HLog.d(TAG, "scan failed");
        }
    }

    public String getConnectedWiFiBSSID(){
        if(isWiFiConnected()){
            WifiInfo info = mWiFiManager.getConnectionInfo();
            return info == null? null : info.getBSSID();
        }else {
            return null;
        }
    }

    public String getConnectedWiFiSSID(){
        if(isWiFiConnected()){
            WifiInfo info = mWiFiManager.getConnectionInfo();
            if(info != null ){
                String ssid = info.getSSID();
                if(!TextUtils.isEmpty(ssid) && ssid.length() > 2){
                    return ssid.substring(1,ssid.length()-1);
                }
            }
        }
        return null;
    }

    public String getIP(){
        WifiInfo info = mWiFiManager.getConnectionInfo();
        return intToIp(info.getIpAddress());
    }

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }


    public boolean isWiFiConnected(){
        NetworkInfo info = ((ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).
                getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isConnected();
    }

    public boolean isWiFiAvailable(){
        NetworkInfo info = ((ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).
                getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isAvailable();
    }

    public boolean joinWiFi(String ssid,String pwd,int type){
        WifiConfiguration configuration = new WifiConfiguration();
        if(type == 1){//has pwd
            configuration.SSID = "\""+ssid+"\"";
            configuration.preSharedKey = "\""+pwd+"\"";
            configuration.hiddenSSID = false;
            configuration.status = WifiConfiguration.Status.ENABLED;
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            configuration.status = WifiConfiguration.Status.ENABLED;
        }else if(type == 2){//no pwd
            configuration.SSID = "\""+ssid+"\"";
            configuration.hiddenSSID = true;
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }else{
            return false;
        }
        int id = mWiFiManager.addNetwork(configuration);
        mWiFiManager.disconnect();
        boolean join = mWiFiManager.enableNetwork(id, true);
        mWiFiManager.reconnect();
        return join;
    }

    public boolean createAp(String ssid,String pwd,int type,boolean enable){
        WifiConfiguration configuration = createWiFiConfiguration(ssid,pwd,type);
        try{
            Class localClass = mWiFiManager.getClass();
            Class[] classes = new Class[2];
            classes[0] = WifiConfiguration.class;
            classes[1] = Boolean.TYPE;
            Method method = localClass.getMethod("setWifiApEnabled",classes);
            Object[] objects = new Object[2];
            objects[0] = configuration;
            objects[1] = Boolean.valueOf(enable);
            return (boolean)method.invoke(mWiFiManager,objects);
        }catch (InvocationTargetException e){
            HLog.d(TAG, e.getTargetException().toString());
        }catch (Exception e){
            HLog.d(TAG, e.getMessage());
        }
        return false;
    }

    private WifiConfiguration createWiFiConfiguration(String ssid,String pwd,int type){
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = ssid;
        if(type == 1){//no pwd
            configuration.wepKeys[0] = "";
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.wepTxKeyIndex = 0;
        }else if(type == 2){//wep
            configuration.preSharedKey = pwd;
            configuration.hiddenSSID = true;
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.wepTxKeyIndex = 0;
        }else if(type == 3){//wpa
            configuration.preSharedKey = pwd;
            configuration.allowedAuthAlgorithms.set(0);
            configuration.allowedProtocols.set(1);
            configuration.allowedProtocols.set(0);
            configuration.allowedKeyManagement.set(1);
            configuration.allowedPairwiseCiphers.set(2);
            configuration.allowedPairwiseCiphers.set(1);
        }
        return configuration;
    }

    public void dimissAp(String ssid,String pwd,int type){
        createAp(ssid,pwd,type,false);
    }

    public void closeWiFi(){
        mWiFiManager.setWifiEnabled(false);
    }

    public void openWiFi(){
        mWiFiManager.setWifiEnabled(true);
    }

    public interface IOnWiFiListScanListener{
        void onChanged(List<WiFiItem> list);
    }

    private List<WiFiItem> convertScanResults(List<ScanResult> list){
        List<WiFiItem> wiFiItems = new ArrayList<>();
        if(list != null){
            for(ScanResult result : list){
                WiFiItem item = new WiFiItem(result.SSID, result.BSSID,WifiManager.calculateSignalLevel(result.level,4));
                wiFiItems.add(item);
            }
        }
        return wiFiItems;
    }

}
