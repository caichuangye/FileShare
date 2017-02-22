package com.huhu.fileshare.download;

import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by caichuangye on 2017-01-17.
 */

public abstract class TransferItem<T> {

    protected CopyOnWriteArrayList<T> mSourceFileList = new CopyOnWriteArrayList<>();

    protected int mIndex;

    public TransferItem(){
        Log.d("transfer-s","***************init index = -1*****************");
        mIndex = -1;
    }

    public boolean isHandleOver() {
        return mSourceFileList.size() <= mIndex+1;
    }


    public void appendFilesList(List<T> sourceFileList) {
        if (sourceFileList != null && sourceFileList.size() > 0) {
            for(T t : sourceFileList){
                if(!hasSame(t)){
                    this.mSourceFileList.add(t);
                }
            }
        }
    }

    protected boolean hasSame(T t){
        return false;
    }

    //// TODO: 2017/1/17 fix this
    public void deleteFileList(List<String> sourceFileList) {
    }

    public T getFile() {
        Log.d("transfer-s","getFile: mIndex = "+mIndex+", list size = "+mSourceFileList.size());
        if(mIndex < mSourceFileList.size()-1 && mSourceFileList.size() > 0){
            mIndex = mIndex+1;
            out();
            return mSourceFileList.get(mIndex);
        }else{
            Log.d("transfer-s","getFile: no handled item");
            return null;
        }
    }

    public void clear() {
        mSourceFileList.clear();
    }

    protected void out(){

    }

}
