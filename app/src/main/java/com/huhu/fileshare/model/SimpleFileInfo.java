package com.huhu.fileshare.model;

import com.huhu.fileshare.util.GlobalParams;

/**
 * Created by Administrator on 2017/3/3.
 */

public class SimpleFileInfo {

    public String path;

    public long size;

    public GlobalParams.ShareType fileType;

    public SimpleFileInfo(String path, long size, GlobalParams.ShareType type){
        this.path = path;
        this.size = size;
        this.fileType = type;
    }

}
