package com.huhu.fileshare.util;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.huhu.fileshare.model.ApkItem;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.model.VideoItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huhu on 2017/6/25.
 */

public class PreviewIconManager {

    private static volatile PreviewIconManager sInstance;

    private Map<String,List<PreviewItem>> mPrepareToRequestedPreviewMap;

    /**
     * key: ip+path
     */
    private LruCache<String,Bitmap> mPreviewIconsMap;

    public class PreviewItem{

        public PreviewItem(String path){
            this.path = path;
            this.status = Status.INIT;
        }

        public String path;

        public Status status;

    }

    public enum Status{
        INIT,
        ING,
        SUCCESS,
        FAILED
    }

    private PreviewIconManager(){
        mPrepareToRequestedPreviewMap = new HashMap<>();
        mPreviewIconsMap = new LruCache<String,Bitmap>(50*1024*1024){

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getAllocationByteCount();
            }
        };

    }

    public static PreviewIconManager getInstance(){
        if(sInstance == null){
            synchronized (PreviewIconManager.class){
                if(sInstance == null){
                    sInstance = new PreviewIconManager();
                }
            }
        }
        return sInstance;
    }

    public List<PreviewItem> setCurrentScanedFiles(String ip, SharedCollection collection){
        HLog.d(getClass(),HLog.P,"-----------setCurrentScanedFiles-------------");
        List<PreviewItem> currentItems = getList(collection);
        List<PreviewItem> addedList = new ArrayList<>();
        if(currentItems.size() == 0){
            return addedList;
        }
        List<PreviewItem> previewItems = mPrepareToRequestedPreviewMap.get(ip);
        if(previewItems == null){
            for(PreviewItem item : currentItems){
                    HLog.d(getClass(),HLog.P,"1 add a preview request, server ip = "+ip+", path = "+item.path);
            }
            mPrepareToRequestedPreviewMap.put(ip,currentItems);
        }else{
            for(PreviewItem item : currentItems){
                boolean has = false;
                for(PreviewItem oldItem : previewItems) {
                    if (item.path.equals(oldItem.path)) {
                        has = true;
                        break;
                    }
                }
                if(!has){
                    HLog.d(getClass(), HLog.P, "2 add a preview request, server ip = " + ip + ", path = " + item.path);
                    addedList.add(item);
                }
            }
            currentItems.addAll(addedList);
            mPrepareToRequestedPreviewMap.put(ip,currentItems);
        }
        return addedList;
    }

    public void setStatus(String ip, String path,Status status,Bitmap bitmap){

        List<PreviewItem> previewItems = mPrepareToRequestedPreviewMap.get(ip);
        if(previewItems == null || previewItems.size() == 0){
            return;
        }

        for(PreviewItem item : previewItems){
            if(item.path.equals(path)){
                item.status = status;
                break;
            }
        }

        if(status == Status.SUCCESS && bitmap != null){
            HLog.d(getClass(),HLog.P,"get a preview icon, server ip = "+ip+", path = "+path);
            mPreviewIconsMap.put(ip+path,bitmap);
        }
    }

    public Bitmap getBitMap(String ip, String serverPath){
        return mPreviewIconsMap.get(ip+serverPath);
    }

    private List<PreviewItem> getList(SharedCollection collection){

        List<PreviewItem> resultList = new ArrayList<>();

        List<ImageItem> imageItems = collection.getImageList();
        for(ImageItem item : imageItems){
            resultList.add(new PreviewItem(item.getPath()));
        }

        List<MusicItem> musicItems = collection.getMusicList();
        for(MusicItem item : musicItems){
            resultList.add(new PreviewItem(item.getPath()));
        }

        List<VideoItem> videoItems = collection.getVideoList();
        for(VideoItem item : videoItems){
            resultList.add(new PreviewItem(item.getPath()));
        }

        List<ApkItem> apkItems = collection.getApkList();
        for(ApkItem item : apkItems){
            resultList.add(new PreviewItem(item.getPath()));
        }

        return resultList;
    }

}
