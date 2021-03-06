package com.huhu.fileshare.model;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/4/18.
 */
public class SpecialFileItem extends BaseItem {

    public enum FileType{
        APK("apk"),PDF("pdf"),ZIP("zip"),DOC("doc"),XLS("xls"),PPT("ppt"),TXT("txt"),UNKNOWN("?");

        private String mType;

        private FileType(String str){
            mType = str;
        }

        public String getTypeString(){
            return mType;
        }

        public static FileType valueOfString(String str){
            for(FileType type : FileType.values()){
                if(str.equals(type.getTypeString())){
                    return type;
                }
            }
            return FileType.UNKNOWN;
        }

    }

    private FileType mType;

    public SpecialFileItem(){
        super();
    }

    public SpecialFileItem(String name,String path,long size,boolean selected,String cover, FileType type){
        super(name,path,size,selected,cover);
        mType = type;
    }

    public FileType getType(){
        return mType;
    }
}
