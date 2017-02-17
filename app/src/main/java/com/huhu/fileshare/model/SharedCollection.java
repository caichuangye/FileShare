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

    private List<SpecialFileItem> mSpecialFileList;

    private List<FileItem> mSDFileList;

    public SharedCollection(){
        mMusicList = new ArrayList<>();
        mVideoList = new ArrayList<>();
        mImagesList = new ArrayList<>();
        mSpecialFileList = new ArrayList<>();
        mSDFileList = new ArrayList<>();
    }

    public List<ImageItem> getImageList(){ return mImagesList;  }

    public List<MusicItem> getMusicList(){
        return mMusicList;
    }

    public List<VideoItem> getVideoList(){
        return mVideoList;
    }

    public List<SpecialFileItem> getSpecialFileList(){
        return mSpecialFileList;
    }

    public List<FileItem> getSDFileList(){return mSDFileList; }


    public void clear(){
        mMusicList.clear();
        mVideoList.clear();
        mImagesList.clear();
        mSpecialFileList.clear();
        mSDFileList.clear();
    }

    public void mergeSpecialAndSDFiles(){
        for(FileItem fileItem : mSDFileList){
            if(fileItem.getType() == FileItem.TYPE_FILE){
                int index = fileItem.getPath().lastIndexOf(".");
                String type = fileItem.getPath().substring(index+1);
                SpecialFileItem item = new SpecialFileItem(fileItem.getShowName(),fileItem.getPath(),fileItem.getSize(),
                        false,null, SpecialFileItem.FileType.valueOfString(type));
                HLog.d("MERGE","item name = "+fileItem.getShowName()+"; type = "+type);
                mSpecialFileList.add(item);
            }
        }
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
                for(SpecialFileItem item : mSpecialFileList){
                    list.add(item.getPath());
                }
                break;
            case SD_FILE:
                for(FileItem item : mSDFileList){
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
                SpecialFileItem item3 = (SpecialFileItem)object;
                if(!mSpecialFileList.contains(item3)) {
                    mSpecialFileList.add(item3);
                }
                break;
            case SD_FILE:
                FileItem item4 = (FileItem)object;
                if(!mSDFileList.contains(item4)) {
                    mSDFileList.add(item4);
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
                for(int i = 0 ; i < mSpecialFileList.size(); i++){
                    if(mSpecialFileList.get(i).getPath().equals(path)){
                        mSpecialFileList.remove(i);
                        break;
                    }
                }
                break;
            case SD_FILE:
                for(int i = 0 ; i < mSDFileList.size(); i++){
                    if(mSDFileList.get(i).getPath().equals(path)){
                        mSDFileList.remove(i);
                        break;
                    }
                }
                break;
        }
    }

}
