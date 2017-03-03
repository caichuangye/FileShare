package com.huhu.fileshare.model;

import com.huhu.fileshare.util.GlobalParams;

/**
 * Created by Administrator on 2017/3/3.
 */

public class OperationInfo {

    public OperationInfo(GlobalParams.OperationType oper, String path, long total) {
        this.oper = oper;
        this.totalSize =total;
        this.path = path;
        this.start = 0;
        this.end = -1;
    }

    public GlobalParams.OperationType oper;
    public long totalSize;
    public long start;
    public long end;
    public String path;
}