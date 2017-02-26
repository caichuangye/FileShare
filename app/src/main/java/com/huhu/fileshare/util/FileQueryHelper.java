package com.huhu.fileshare.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

import com.huhu.fileshare.R;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.ImageFolderItem;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.CommonFileItem;
import com.huhu.fileshare.model.VideoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/4/12.
 */
public class FileQueryHelper {

    private Handler mHandler;

    private static FileQueryHelper sInstance;

    private Context mContext;

    private List<ImageItem> mAllImagesList;

    private List<MusicItem> mMusicList;

    private List<VideoItem> mVideoList;


    public static FileQueryHelper getInstance(Context context){
        if(sInstance == null){
            synchronized (FileQueryHelper.class){
                if(sInstance == null){
                    sInstance = new FileQueryHelper(context);
                }
            }
        }
        return sInstance;
    }

    private FileQueryHelper(Context context){
        mContext = context;
        HandlerThread thread = new HandlerThread("scan1");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public void scanFileByType(final GlobalParams.ShareType type){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Uri uri = buildUri(type);
                if(uri != null){
                    Cursor cursor = mContext.getContentResolver().query(uri,null,null,null,null);
                    buildResultList(type,cursor);
                }
            }
        });

    }

    private Uri buildUri(GlobalParams.ShareType type){
        Uri uri = null;
        switch (type){
            case IMAGE:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case AUDIO:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case VIDEO:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case FILE:

                break;
        }
        return uri;
    }

    private void buildResultList(GlobalParams.ShareType type,Cursor cursor){
        int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
        int pathIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        int sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
        if(type == GlobalParams.ShareType.AUDIO){
            if(mMusicList == null) {
                mMusicList  = new ArrayList<>();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                        String url = getAlbumArt(cursor.getInt(albumIndex));
                        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                        MusicItem item = new MusicItem(cursor.getString(titleIndex), cursor.getString(pathIndex),
                                cursor.getLong(sizeIndex), false, url, cursor.getString(artistIndex));
                        mMusicList.add(item);
                    }
                }
            }
            EventBus.getDefault().post(new EventBusType.ShareMusicInfo(mMusicList));
        }else if(type == GlobalParams.ShareType.VIDEO){
            if(mVideoList == null) {
                mVideoList = new ArrayList<>();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                        VideoItem item = new VideoItem(cursor.getString(titleIndex), cursor.getString(pathIndex),
                                cursor.getLong(sizeIndex), false, null, cursor.getLong(durationIndex));
                        mVideoList.add(item);
                    }
                }
            }
            EventBus.getDefault().post(new EventBusType.ShareVideoInfo(mVideoList));
        }else if(type == GlobalParams.ShareType.IMAGE){
            if(mAllImagesList == null) {
                mAllImagesList = new ArrayList<>();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                        SimpleDateFormat sDateFormat = new SimpleDateFormat("yy-MM-dd");
                        String d = sDateFormat.format(new Date(cursor.getLong(dateIndex) * 1000));
                        String path = cursor.getString(pathIndex);
                        ImageItem item = new ImageItem(cursor.getString(titleIndex), path,
                                cursor.getLong(sizeIndex), false, null, d);
                        mAllImagesList.add(item);
                    }
                }
            }
            EventBus.getDefault().post(new EventBusType.ShareImageFolderInfo(convert(mAllImagesList)));
        }
    }

    private List<ImageFolderItem> convert(final List<ImageItem> imageList){
        List<ImageFolderItem> folderList = new ArrayList<>();
        Map<String,Long> map = new HashMap<>();
        List<String> coverList = new ArrayList<>();
        if(imageList != null){
            for(int i = 0 ; i < imageList.size(); i++){
                ImageItem item = imageList.get(i);
                String foldPath = getFolderPath(item.getPath());
                Long count = map.get(foldPath);
                if (count == null){
                    count = new Long(0);
                    coverList.add(item.getPath());
                }
                map.put(foldPath,count+1);
            }
        }
        Set<String> set = map.keySet();
        for(String str : set){
            String folderName = getFolderName(str);
            long count = map.get(str);
            String coverPath = null;
            for(String p : coverList){
                if(CommonUtil.isDirectFolder(p,str)){
                    coverPath = p;
                    break;
                }
            }
            ImageFolderItem folderItem = new ImageFolderItem(coverPath,folderName,count);
            folderList.add(folderItem);

        }
        return folderList;
    }

    private String getFolderName(String path){
        int index1 = path.lastIndexOf("/");
        return path.substring(index1+1);
    }

    private String getFolderPath(String path){
        int index1 = path.lastIndexOf("/");
        return path.substring(0,index1);
    }

    public List<ImageItem> getAllImages(){
        return mAllImagesList;
    }


    private String getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cur = mContext.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        return album_art;
    }


}
