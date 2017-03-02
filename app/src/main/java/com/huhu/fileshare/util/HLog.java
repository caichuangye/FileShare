package com.huhu.fileshare.util;

import android.util.Log;

/**
 * Created by Administrator on 2016/4/7.
 */
public class HLog {
    private static final boolean sDebug = true;

    public static String DD = "tran_d";

    public static void d(String tag,String msg){
        if(sDebug){
            Log.d(tag,msg);
        }
    }

    public static void e(String tag,String msg){
        if(sDebug){
            Log.e(tag,msg);
        }
    }

    public static void w(String tag,String msg){
        if(sDebug){
            Log.w(tag,msg);
        }
    }

}
