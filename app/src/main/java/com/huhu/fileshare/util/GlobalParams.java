package com.huhu.fileshare.util;

/**
 * Created by Administrator on 2016/4/9.
 */
public class GlobalParams {

    /**
     * 浏览服务器端共享资源页面的中的各个fragment与设置本地共享资源的各个fragment是相同的，但内部逻辑不一样
     * SERVER_MODE：表明该fragment是用来显示本地共享资源
     * LOCAL_MODE：表明该fragment是用来显示服务器端的共享资源
     */
    public static int SERVER_MODE = 0;

    public static int LOCAL_MODE = 1;

    /**
     * 网络已连接
     */
    public static int NET_CONNECTED = 0;

    /**
     * 网络未连接
     */
    public static int NET_NOT_CONNECTED = 1;

    /**
     * 每个设备都要定时向所处的局域网发送广播，告知自己的共享信息，该端口就是发送广播的端口
     */
    public static final int DETECT_PORT = 25301;

    /**
     * 设备直接需要交互信息，包括请求与应答共享信息等，SEND_PORT就是用来交换信息的端口
     */
    public static final int SEND_PORT = 34052;

    /**
     * 服务器下载监听的端口
     */
    public static final int DOWNLOAD_PORT = 10935;

    /**
     * 客户端向服务器请求数据时，在请求的文件路径前加上REQUEST_TAG发送给服务器
     */
    public static final String REQUEST_TAG = "PATH:";

    /**
     * 服务器检测到客户端没有权限下载文件时，将通过下载端口将 "PERMISSION_DENIED"发送给客户端
     */
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";

    /**
     * 本应用的文件夹名称，在外置存储的根路径下
     */
    public static final String FOLDER = "fileshare";

    public static int CLIENT_DOWNLOAD_THREAD_NUM = 1;

    /**
     * 用来控制下载按钮的显示状态
     */
    public enum DownloadOper{
        ADD,         //添加一个下载
        DELETE,      //删除下载
        UPDATE_START,//下载刚开始
        UPDATE_ING,  //下载正在进行
        UPDATE_END   //下载结束
    }

    /**
     * 共享文件的类型
     */
    public enum ShareType{
        IMAGE("IMAGE"),//图片
        AUDIO("AUDIO"),//音频
        VIDEO("VIDEO"),//视频
        FILE("FILE"),  //文件
        APK("APK");    //应用

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

    /**
     * 点击设备列表中的设备时，将向目标设备发送请求，请求目标设备的共享文件信息
     */
    public static final String REQUEST_SHARED_FILES = "request_shared_files";

}
