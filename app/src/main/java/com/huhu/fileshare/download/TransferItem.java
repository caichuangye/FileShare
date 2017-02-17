package com.huhu.fileshare.download;

import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by caichuangye on 2017-01-17.
 */

public abstract class TransferItem<T> {

    protected CopyOnWriteArrayList<T> mSourceFileList = new CopyOnWriteArrayList<>();

    protected int mIndex = -1;


    public boolean isHandleOver() {
        return mSourceFileList.size() <= mIndex+1;
    }


    public void appendFilesList(List<T> sourceFileList) {
        if (sourceFileList != null && sourceFileList.size() > 0) {
            this.mSourceFileList.addAll(sourceFileList);
        }
    }

    //// TODO: 2017/1/17 fix this
    public void deleteFileList(List<String> sourceFileList) {
    }

    public T getFile() {
        Log.d("transfer","mSourceFileList.size = "+mSourceFileList.size());
        if(mIndex < mSourceFileList.size()-1 && mSourceFileList.size() > 0){
            mIndex++;
            return mSourceFileList.get(mIndex);
        }else{
            return null;
        }
    }

    public void clear() {
        mSourceFileList.clear();
    }

}
