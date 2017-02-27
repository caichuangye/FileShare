package com.huhu.fileshare.model;

import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/24.
 */
public class SharedCollection {

    private List<ImageItem> mImagesList;

    private List<VideoItem> mVideoList;

    private List<MusicItem> mMusicList;

    private List<CommonFileItem> mCommonFileList;

    private List<ApkItem> mApkList;

    public SharedCollection(){
        mMusicList = new ArrayList<>();
        mVideoList = new ArrayList<>();
        mImagesList = new ArrayList<>();
        mCommonFileList = new ArrayList<>();
        mApkList = new ArrayList<>();
    }

    public List<ImageItem> getImageList(){ return mImagesList;  }

    public List<MusicItem> getMusicList(){
        return mMusicList;
    }

    public List<VideoItem> getVideoList(){
        return mVideoList;
    }

    public List<CommonFileItem> getCommonFileList(){
        return mCommonFileList;
    }

    public List<ApkItem> getApkList(){return mApkList; }

    public byte getSharedType(){
        byte b = 0x00;
        if(mImagesList.size() > 0){
            b |= 0x01;
        }
        if(mMusicList.size() > 0){
            b |= 0x02;
        }
        if(mVideoList.size() > 0){
            b |= 0x04;
        }
        if(mApkList.size() > 0){
            b |= 0x08;
        }
        if(mCommonFileList.size() > 0){
            b |= 0x10;
        }
        return b;
    }

    public void clear(){
        mMusicList.clear();
        mVideoList.clear();
        mImagesList.clear();
        mCommonFileList.clear();
        mApkList.clear();
    }

    public List<String> getSharedPathByType(GlobalParams.ShareType type){
        List<String> list = new ArrayList<>();
        switch (type){
            case IMAGE:
                for(ImageItem item : mImagesList){
                    list.add(item.getPath());
                }
                break;
            case AUDIO:
                for(MusicItem item : mMusicList){
                    list.add(item.getPath());
                }
                break;
            case VIDEO:
                for(VideoItem item : mVideoList){
                    list.add(item.getPath());
                }
                break;
            case FILE:
                for(CommonFileItem item : mCommonFileList){
                    list.add(item.getPath());
                }
                break;
            case APK:
                for(ApkItem item : mApkList){
                    list.add(item.getPath());
                }
                break;
        }
        return list;
    }

    public void addShared(GlobalParams.ShareType type,Object object){
        switch (type){
            case IMAGE:
                ImageItem item = (ImageItem)object;
                if(!mImagesList.contains(item)) {
                    mImagesList.add(item);
                }
                break;
            case AUDIO:
                MusicItem item1 = (MusicItem)object;
                if(!mMusicList.contains(item1)) {
                    mMusicList.add(item1);
                }
                break;
            case VIDEO:
                VideoItem item2 = (VideoItem)object;
                if(!mVideoList.contains(item2)) {
                    mVideoList.add(item2);
                }
                break;
            case FILE:
                CommonFileItem item3 = (CommonFileItem)object;
                if(!mCommonFileList.contains(item3)) {
                    mCommonFileList.add(item3);
                }
                break;
            case APK:
                ApkItem item4 = (ApkItem)object;
                if(!mApkList.contains(item4)) {
                    mApkList.add(item4);
                }
                break;
        }
    }

    public void deleteShared(GlobalParams.ShareType type,String path){
        switch (type){
            case IMAGE:
                for(int i = 0 ; i < mImagesList.size(); i++){
                    if(mImagesList.get(i).getPath().equals(path)){
                        mImagesList.remove(i);
                        break;
                    }
                }
                break;
            case AUDIO:
                for(int i = 0 ; i < mMusicList.size(); i++){
                    if(mMusicList.get(i).getPath().equals(path)){
                        mMusicList.remove(i);
                        break;
                    }
                }
                break;
            case VIDEO:
                for(int i = 0 ; i < mVideoList.size(); i++){
                    if(mVideoList.get(i).getPath().equals(path)){
                        mVideoList.remove(i);
                        break;
                    }
                }
                break;
            case FILE:
                for(int i = 0; i < mCommonFileList.size(); i++){
                    if(mCommonFileList.get(i).getPath().equals(path)){
                        mCommonFileList.remove(i);
                        break;
                    }
                }
                break;
            case APK:
                for(int i = 0 ; i < mApkList.size(); i++){
                    if(mApkList.get(i).getPath().equals(path)){
                        mApkList.remove(i);
                        break;
                    }
                }
                break;
        }
    }

}
