package com.huhu.fileshare.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/4/16.
 */
public class SystemSetting {

    private final String FILE_SHARE_SETTING = "FILE_SHARE_SETTING";

    public static final String USER_ICON_INDEX ="USER_ICON_INDEX";
    public static final String USER_NICKNAME   = "USER_NICKNAME";
    public static final String AP_NAME         = "AP_NAME";
    public static final String AP_PWD          = "AP_PWD";
    public static final String STORAGE_PATH    = "STORAGE_PATH";
    public static final String AUTO_REFRESH    = "AUTO_REFRESH";

    private SharedPreferences mSharedPreferences;

    private static SystemSetting sInstance;

    private Context mContext;

    private SystemSetting(Context context){
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(FILE_SHARE_SETTING,Context.MODE_PRIVATE);
    }

    public static SystemSetting getInstance(Context context){
        if(sInstance == null){
            synchronized (SystemSetting.class){
                if(sInstance == null){
                    sInstance = new SystemSetting(context);
                }
            }
        }
        return sInstance;
    }

    public int getUserIconIndex(){
        return mSharedPreferences.getInt(USER_ICON_INDEX,0);
    }

    public void setUserIconIndex(int index){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(USER_ICON_INDEX,index);
        editor.commit();
    }

    public String getUserNickName(){
        return mSharedPreferences.getString(USER_NICKNAME,"user");
    }

    public void setUserNickName(String name){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(USER_NICKNAME,name);
        editor.commit();
    }

    public String getApName(){
        return mSharedPreferences.getString(AP_NAME,"file_share_wifi");
    }

    public void setApName(String ap){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(AP_NAME,ap);
        editor.commit();
    }

    public String getApPwd(){
        return mSharedPreferences.getString(AP_PWD,"12345678");
    }

    public void setApPwd(String pwd){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(AP_PWD,pwd);
        editor.commit();
    }

    public String getStoragePath(){
        return mSharedPreferences.getString(STORAGE_PATH,getDefaultStotagePath());
    }

    public void setStoragePath(String path){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(STORAGE_PATH,path);
        editor.commit();
    }

    public boolean getAutoRefresh(){
        return mSharedPreferences.getBoolean(AUTO_REFRESH,false);
    }

    public void setAutoRefresh(boolean auto){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(AUTO_REFRESH, auto);
        editor.commit();
    }

    public String getVersionName(){
        PackageManager manager = mContext.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
           return null;
        }
        return info.versionName;
    }

    public String getDefaultStotagePath(){
        String path = Environment.getExternalStorageDirectory().getPath()+"/fileshare";
        File file = new File(path);
        if(file != null && !file.exists()){
            file.mkdir();
        }
        return path;
    }

}
