package com.huhu.fileshare.download;

import android.util.Log;

import com.huhu.fileshare.model.DownloadItem;

import java.util.List;


/**
 * Created by caichuangye on 2017-01-16.
 */

public class ReceiveListItem extends TransferItem<DownloadItem>{

    private String mIP;

    public ReceiveListItem(String ip, List<DownloadItem> list){
        super();
        mIP = ip;
        if(list != null && list.size() > 0) {
            mSourceFileList.addAll(list);
        }
    }

    public String getIP(){
        return mIP;
    }

    @Override
    protected boolean hasSame(DownloadItem t){
        for(DownloadItem item : mSourceFileList){
            if(item.getFromPath().equals(t.getFromPath())){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void out(){
        if(mSourceFileList == null){
      //      Log.d("transfer-c", "getFile.out: ---------send list is null---------");
        }else {
      //      Log.d("transfer-c", "getFile.out: ---------" + mIndex + "---------");
            for (int i = 0; i < mSourceFileList.size(); i++) {
            //    Log.d("transfer-c", i+": "+mSourceFileList.get(i).getFromPath());
            }
        }
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
