package com.huhu.fileshare.util;

/**
 * Created by Administrator on 2016/4/9.
 */
public class GlobalParams {

    public static int SHOW_MODE = 0;

    public static int SCAN_MODE = 1;

    public static int NET_CONNECTED = 0;

    public static int WIFI_OPEN_CONNECTED = 0;

    public static int WIFI_OPEN_DISCONNECTED = 1;

    public static int WIFI_CLOSED = 2;

    public static int NET_NOT_CONNECTED = 1;

    public static final int DETECT_PORT = 25012;

    public static final int SEND_PORT = 34012;

    public static final int RECV_PORT = 34012;

    public enum ScanType{
        SET_SHARED_FILES,
        SCAN_SHARED_FILES
    }

    public enum RequestType{
        GET_SHARED_FILES,
        REQUEST_DOWNLOAD_FILES
    }

    public enum DownloadOper{
        ADD,
        DELETE,
        UPDATE_START,
        UPDATE_ING,
        UPDATE_END
    }

    public enum ShareType{
        IMAGE("IMAGE"),
        AUDIO("AUDIO"),
        VIDEO("VIDEO"),
        FILE("FILE"),
        SD_FILE("SD_FILE");

        private String mString;

        private ShareType(String str){
            mString = str;
        }

        @Override
        public String toString(){
            return mString;
        }

        public static ShareType getType(String str){
            for(ShareType type : ShareType.values()){
                if(type.toString().toUpperCase().equals(str.toUpperCase())){
                    return type;
                }
            }
            return null;
        }
    }

    public static final String REQUEST_SHARED_FILES = "request_shared_files";

}
