package com.huhu.fileshare.download;

import android.util.Log;

import com.huhu.fileshare.model.DownloadItem;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by caichuangye on 2017-01-16.
 */

public class SendListItem extends TransferItem<DownloadItem>{

    private Socket mSocket;

    public SendListItem(Socket socket, List<DownloadItem> list){
        super();
        mSocket = socket;
        if(list != null && list.size() > 0) {
            mSourceFileList.addAll(list);
            for(DownloadItem i : list){
                Log.d("testt","-----: "+i.getFromPath());
            }
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    @Override
    public void clear(){
        super.clear();
        if(mSocket != null){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void out(){
        if(mSourceFileList == null){
            Log.d("transfer-s", "getFile.out: ---------send list is null---------");
        }else {
            Log.d("transfer-s", "getFile.out: ---------" + mIndex + "---------");
            for (int i = 0; i < mSourceFileList.size(); i++) {
                Log.d("transfer-s", i+": "+mSourceFileList.get(i).getFromPath());
            }
        }
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
}
