package com.huhu.fileshare.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.huhu.fileshare.de.greenrobot.event.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huhu on 2017/6/18.
 */

public class UserIconManager {

    private static volatile UserIconManager sInstance;

    private Map<String, ServerIconItem> mServerIconPathMap;

    private Map<String, Bitmap> mServerIconBitmapMap;

    private UserIconManager(){

    }

    public static UserIconManager getInstance(){
        if(sInstance == null){
            synchronized (UserIconManager.class){
                if(sInstance == null){
                    sInstance = new UserIconManager();
                }
            }
        }
        return sInstance;
    }


    public static class ServerIconItem{

        public ServerIconItem(String path,long size){
            this.path = path;
            this.size = size;
        }

        public String path;
        public long size;
    }

    /**
     * @param ip
     * @param
     * @return true: 表示需要更新服务端头像； false表示不需要更新服务端头像
     */
    public  boolean setServerIconPath(String ip, ServerIconItem item) {
        if (mServerIconPathMap == null) {
            mServerIconPathMap = new HashMap<>();
        }
        ServerIconItem iconItem = mServerIconPathMap.get(ip);
        if(iconItem == null){
            mServerIconPathMap.put(ip, item);
            return true;
        }
        String oldPath = iconItem.path;
        if (!TextUtils.isEmpty(oldPath) && !TextUtils.isEmpty(item.path) && oldPath.equals(item.path)) {
            return false;
        } else {
            mServerIconPathMap.put(ip, item);
            return true;
        }
    }

    public long getServerIconSize(String ip){
        if(mServerIconPathMap != null){
            return mServerIconPathMap.get(ip).size;
        }
        return 0;
    }

    public void setBitMap(String ip, Bitmap bitmap){
        if(mServerIconBitmapMap == null){
            mServerIconBitmapMap = new HashMap<>();
        }
        if(bitmap != null) {
            HLog.d(getClass(),HLog.S,"setBitMap, ip = "+ip+", bitmap = "+bitmap.toString());
            mServerIconBitmapMap.put(ip, bitmap);
            EventBus.getDefault().post(new EventBusType.UpdateUserIcon());
        }else{
            HLog.d(getClass(),HLog.S,"setBitMap, ip = "+ip+", bitmap = null");
        }
    }

    public Bitmap getIconBitMap(String ip){
        return mServerIconBitmapMap == null ? null : mServerIconBitmapMap.get(ip);
    }

    public Bitmap getSelfIconBitmap(Context context){
        String path = SystemSetting.getInstance(context).getUserIconPath();
        if(!TextUtils.isEmpty(path)) {
            File file = new File(path);
            return file.exists() ? BitmapFactory.decodeFile(path) : null;
        }
        return null;
    }

    public long getSelfIconBitmapSize(Context context){
        String path = SystemSetting.getInstance(context).getUserIconPath();
        if(!TextUtils.isEmpty(path)) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(path);
                return inputStream.available();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }
}
