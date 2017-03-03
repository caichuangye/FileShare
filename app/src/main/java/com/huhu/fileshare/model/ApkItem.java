package com.huhu.fileshare.model;

/**
 * Created by Administrator on 2016/4/12.
 */
public class ApkItem extends BaseItem {

    private String mDesc;

    public ApkItem() {
        super();
    }

    public ApkItem(String name, String path, long size, boolean selected, String cover, String desc) {
        super(name, path, size, selected, cover);
        mDesc = desc;
    }

    public String getDesc() {
        return mDesc;
    }


    public void setDesc(String desc) {
        this.mDesc = desc;
    }

}
