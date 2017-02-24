package com.huhu.fileshare.databases;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/21.
 */

public class DownloadHistory {

    public final static int ADD_ITEM = 0;

    public final static int UPDATE_ITEM = 1;

    public final static int DELETE_ITEM = 2;

    private ContentResolver mContentResolver;

    public DownloadHistory(Context context){
        mContentResolver = context.getContentResolver();
    }


    public void operateDatabases(DownloadItem item, int oper){
        switch (oper){
            case ADD_ITEM:
                addItem(item);
                break;
            case UPDATE_ITEM:
                updateItem(item);
                break;
            case DELETE_ITEM:
                deleteItem(item);
                break;
        }

    }

    public List<DownloadItem> getAllItems(){
        List<DownloadItem> list = new ArrayList<>();
        String order = " end_time desc";
        Cursor cursor = mContentResolver.query(DatabaseUtils.DOWNLOAD_HISTORY_URI,null,null,null,order);
        if(cursor != null){
            while (cursor.moveToNext()){
                list.add(parseCursor(cursor));
            }
        }
        cursor.close();
        return list;
    }

    private DownloadItem parseCursor(Cursor cursor){
        DownloadItem item = new DownloadItem();
        String uuid = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.ID));
        String startTime = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.START_TIME));
        String endTime = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.END_TIME));
        String fromPath = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.FROM_PATH));
        String fromUserName = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.FROM_USERNAME));
        String ip = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.FROM_IP));
        String toPath = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.TO_PATH));
        String fileType = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.FILE_TYPE));
        String str = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.DOWNLOAD_STATES));
        String totalSize = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.TOTAL_SIZE));
        String recvSize = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.RECV_SIZE));
        String destName = cursor.getString(cursor.getColumnIndex(DatabaseUtils.ColumnName.DEST_NAME));
        DownloadStatus status = DownloadStatus.getStatus(str);
        item.setUUID(uuid);
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setFromPath(fromPath);
        item.setFromUserName(fromUserName);
        item.setIP(ip);
        item.setToPath(toPath);
        item.setFileType(fileType);
        item.setStatus(status);
        item.setTotalSize(Long.parseLong(totalSize));
        item.setRecvSize(Long.parseLong(recvSize));
        item.setDestName(destName);
        HLog.d("ccyd","parse curor: "+item.toString());
        return item;
    }

    private void addItem(DownloadItem item){
        HLog.d("ccyd","add item: "+item.getStartTime());
        ContentValues values = new ContentValues();
        values.put(DatabaseUtils.ColumnName.ID,item.getUUID());
        values.put(DatabaseUtils.ColumnName.START_TIME,item.getStartTime());
        values.put(DatabaseUtils.ColumnName.END_TIME,item.getEndTime());
        values.put(DatabaseUtils.ColumnName.FROM_PATH,item.getFromPath());
        values.put(DatabaseUtils.ColumnName.FROM_USERNAME,item.getFromUserName());
        values.put(DatabaseUtils.ColumnName.FROM_IP,item.getFromIP());
        values.put(DatabaseUtils.ColumnName.TO_PATH,item.getToPath());
        values.put(DatabaseUtils.ColumnName.TOTAL_SIZE,String.valueOf(item.getTotalSize()));
        values.put(DatabaseUtils.ColumnName.RECV_SIZE,String.valueOf(item.getRecvSize()));
        values.put(DatabaseUtils.ColumnName.FILE_TYPE,item.getFileType());
        values.put(DatabaseUtils.ColumnName.DOWNLOAD_STATES,item.getStatus().toString());
        values.put(DatabaseUtils.ColumnName.DEST_NAME,item.getDestName());
        mContentResolver.insert(DatabaseUtils.DOWNLOAD_HISTORY_URI,values);
    }

    private void updateItem(DownloadItem item){
        HLog.d("ccyd","update item: "+item.toString());
        String where = " id = '"+item.getUUID()+"'";
        ContentValues values = new ContentValues();
        values.put("download_states",item.getStatus().toString());
        values.put("end_time",item.getEndTime());
        mContentResolver.update(DatabaseUtils.DOWNLOAD_HISTORY_URI,values,where,null);
    }

    private void deleteItem(DownloadItem item){
        String where = " id = '"+item.getUUID()+"'";
        mContentResolver.delete(DatabaseUtils.DOWNLOAD_HISTORY_URI,where,null);
    }
}
