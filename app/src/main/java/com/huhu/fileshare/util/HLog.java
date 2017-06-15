package com.huhu.fileshare.util;

import android.util.Log;

/**
 * Created by Administrator on 2016/4/7.
 */
public class HLog {
    private static final boolean sDebug = true;

    private static final String PRE_TAG = "com_huhu_fileshare";

    public static String T = "transfer";

    public static String S = "socket";

    public static String D = "detection";

    public static String L = "temp";

    public static void d(Class cls,String model, String msg){
        if(sDebug){
            Log.d("["+PRE_TAG+"_"+cls.getSimpleName()+": "+model+"] ",msg);
        }
    }

    public static void w(Class cls,String model, String msg){
        if(sDebug){
            Log.w("["+PRE_TAG+"_"+cls.getSimpleName()+"]: "+model,msg);
        }
    }

    public static void e(Class cls,String model, String msg){
        if(sDebug){
            Log.e("["+PRE_TAG+"_"+cls.getSimpleName()+"]: "+model,msg);
        }
    }



}
