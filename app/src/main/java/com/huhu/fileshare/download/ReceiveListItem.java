package com.huhu.fileshare.download;

import com.huhu.fileshare.model.DownloadItem;

import java.util.List;


/**
 * Created by caichuangye on 2017-01-16.
 */

public class ReceiveListItem extends TransferItem<DownloadItem>{

    private String mIP;

    public ReceiveListItem(String ip, List<DownloadItem> list){
        mIP = ip;
        if(list != null && list.size() > 0) {
            mSourceFileList.addAll(list);
        }
    }

    public String getIP(){
        return mIP;
    }

//    public static class FileInfo{
//
//        public FileInfo(String path,long size){
//            this.path = path;
//            this.size = size;
//        }
//
//        public String path;
//        public long size;
//    }

}
