package com.huhu.fileshare.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;

import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.SpecialFileItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/18.
 */
public class ScanSpecialFiles implements Runnable {

    private static ScanSpecialFiles sInstance;

    private Handler mHandler;

    private Context mContext;

    private List<SpecialFileItem> mFileList;

    private ScanSpecialFiles(Context context) {
        mContext = context;
        HandlerThread handlerThread = new HandlerThread("scanspecialfile");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public static ScanSpecialFiles getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ScanSpecialFiles.class) {
                if (sInstance == null) {
                    sInstance = new ScanSpecialFiles(context);
                }
            }
        }
        return sInstance;
    }

    public void start() {
        mHandler.post(this);
    }


    @Override
    public void run() {
        if (mFileList == null) {
            mFileList = new ArrayList<>();
            String[] tmp = new String[]{"apk", "pdf", "zip", "xls", "ppt", "doc", "txt"};
            String[] columns = new String[]{
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.MIME_TYPE
            };
            String[] types = new String[]{
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("zip"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
            };

            String selectType = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
            Cursor cursor = null;
            for (int i = 0; i < types.length; i++) {
                String t = types[i];
                String typeStr = tmp[i];
                try {
                    ContentResolver resolver = mContext.getContentResolver();
                    if (typeStr.equals("apk1")) {
                        selectType = MediaStore.Files.FileColumns.DATA + " like \'%.apk\' ";
                        cursor = resolver.query(MediaStore.Files.getContentUri("external"), columns, selectType, null, null);
                    } else {
                        cursor = resolver.query(MediaStore.Files.getContentUri("external"), columns, selectType, new String[]{t},
                                MediaStore.Files.FileColumns.DATE_ADDED + " desc");
                    }
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            int pathIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                            int titleIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
                            int sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                            String path = cursor.getString(pathIndex);
                            String title = cursor.getString(titleIndex);
                            long size = cursor.getLong(sizeIndex);
                            SpecialFileItem item = new SpecialFileItem(title, path, size, false, null, SpecialFileItem.FileType.valueOfString(typeStr));
                            mFileList.add(item);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        EventBus.getDefault().post(new EventBusType.ShareSpecialFileInfo(mFileList));
    }
}
