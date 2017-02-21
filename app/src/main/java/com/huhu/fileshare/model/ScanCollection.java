package com.huhu.fileshare.model;

import android.util.Log;

import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/26.
 */
public class ScanCollection {

    private List<DownloadItem> mDataList;

    public ScanCollection(){
        mDataList = new ArrayList<>();
    }

    private void notifyDownloadListChanged(DownloadItem item,GlobalParams.DownloadOper oper){
        EventBusType.UpdateDownloadFile file = new EventBusType.UpdateDownloadFile(item,oper);
        EventBus.getDefault().post(file);
    }

    public void addFile(DownloadItem item){
        for(DownloadItem tmp : mDataList){
            if(tmp.getFromIP().equals(item.getFromIP()) && tmp.getFromPath().equals(item.getFromPath())){
                return;
            }
        }
        mDataList.add(item);
        notifyDownloadListChanged(item,GlobalParams.DownloadOper.ADD);
    }

    public DownloadItem getItemByUUID(String uuid){
        for (DownloadItem item : mDataList){
            if(item.getUUID().equals(uuid)){
                return item;
            }
        }
        return null;
    }

    public DownloadItem getItemByPath(String path){
        for (DownloadItem item : mDataList){
            if(item.getFromPath().equals(path)){
                return item;
            }
        }
        return null;
    }

    public void deleteFile(String uuid){
        for( int i = 0 ; i < mDataList.size(); i++){
            DownloadItem item = mDataList.get(i);
            if(item.getUUID().equals(uuid)){
                mDataList.remove(i);
                break;
            }
        }
        notifyDownloadListChanged(null,GlobalParams.DownloadOper.DELETE);
    }

    public List<DownloadItem> getScanListByIP(String ip){
        List<DownloadItem> list = new ArrayList<>();
        for(DownloadItem item : mDataList){
            if(item.getFromIP().equals(ip)){
                list.add(item);
            }
        }
        return list;
    }

    public void deleteFiles(String ip){
        List<DownloadItem> list = new ArrayList<>();
        for(DownloadItem item : mDataList){
            if(!item.getFromIP().equals(ip)){
                list.add(item);
            }
        }
        mDataList.clear();
        mDataList.addAll(list);
    }

    public void updateProgress(String uuid,long total, long recv){
        for(DownloadItem item : mDataList){
            if(item.getUUID().equals(uuid)){
                long diff = recv - item.getRecvSize();
                item.setRecvSize(recv);
                if(total > recv && item.getStatus() == DownloadStatus.WAIT){
                    item.setStatus(DownloadStatus.DOWNLOADING);
                    notifyDownloadListChanged(item,GlobalParams.DownloadOper.UPDATE_START);
                }else if(recv >= total){
                    item.setStatus(DownloadStatus.SUCCESSED);
                    item.setEndTime(String.valueOf(System.currentTimeMillis()));
                    notifyDownloadListChanged(item,GlobalParams.DownloadOper.UPDATE_END);
                }else{
                    long per = recv*100/total;
                    Long last = mUpdateProgressMap.get(item.getUUID());
                    if(last == null && per >= 1){
                        mUpdateProgressMap.put(item.getUUID(),per);
                        notifyDownloadListChanged(item, GlobalParams.DownloadOper.UPDATE_ING);
                    }else{
                        if(last != null && per > last){
                            mUpdateProgressMap.put(item.getUUID(),per);
                            notifyDownloadListChanged(item, GlobalParams.DownloadOper.UPDATE_ING);
                        }
                    }
                }
                break;
            }
        }
    }

    private Map<String, Long> mUpdateProgressMap = new HashMap<>();

    public List<DownloadItem> getWaitToDownloadingFiles(){
        List<DownloadItem> items = new ArrayList<>();
        for(DownloadItem item : mDataList){
            if(item.getStatus() != DownloadStatus.SUCCESSED){
                items.add(item);
            }
        }
        return items;
    }


}
