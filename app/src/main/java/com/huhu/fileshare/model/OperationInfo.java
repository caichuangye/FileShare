package com.huhu.fileshare.model;

import com.huhu.fileshare.util.GlobalParams;

import java.util.List;

/**
 * Created by Administrator on 2017/3/3.
 */

public class OperationInfo {

    public OperationInfo(GlobalParams.OperationType oper, List<SimpleFileInfo> list) {
        this.oper = oper;
        this.fileList = list;
    }

    public GlobalParams.OperationType oper;
    public List<SimpleFileInfo> fileList;
}