package com.huhu.fileshare.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.text.TextUtils;

import com.huhu.fileshare.R;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.ApkItem;
import com.huhu.fileshare.model.ImageFolderItem;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.CommonFileItem;
import com.huhu.fileshare.model.VideoItem;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

/**
 * Created by Administrator on 2016/4/12.
 */
public class FileQueryHelper {

    private static FileQueryHelper sInstance;

    private Context mContext;

    private List<ImageItem> mAllImagesList;

    private Executor mThreadPool;

    public static FileQueryHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (FileQueryHelper.class) {
                if (sInstance == null) {
                    sInstance = new FileQueryHelper(context);
                }
            }
        }
        return sInstance;
    }

    private FileQueryHelper(Context context) {
        mContext = context;
        mThreadPool = Executors.newFixedThreadPool(5);
    }

    public void scanFileByType(final GlobalParams.ShareType type) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Uri uri = buildUri(type);
                if (uri != null) {
                    String order = null;
                    if (type == GlobalParams.ShareType.AUDIO) {
                        order = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " asc";
                    } else if (type == GlobalParams.ShareType.VIDEO) {
                        order = MediaStore.Video.Media.DEFAULT_SORT_ORDER + " desc";
                    }
                    Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, order);
                    buildResultList(type, cursor);
                } else if (type == GlobalParams.ShareType.APK) {
                    parseInstalledApp();
                }
            }
        });

    }

    private Uri buildUri(GlobalParams.ShareType type) {
        Uri uri = null;
        switch (type) {
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


    private void parseInstalledApp() {
        PackageManager packageManager = mContext.getPackageManager();
        List<ApplicationInfo> list = packageManager.getInstalledApplications(GET_UNINSTALLED_PACKAGES);
        if (list != null) {
            for (ApplicationInfo info : list) {
                String path = info.sourceDir;
                if (!TextUtils.isEmpty(path) && path.startsWith("/data/app")) {
                    String name = String.valueOf(info.loadLabel(packageManager));
                    try {
                        FileInputStream inputStream = new FileInputStream(path);
                        long size = inputStream.available();
                        PackageInfo pi = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
                        final ApkItem item = new ApkItem(name, path, size, false, null, pi.versionName);
                        ImageCacher.getInstance().cacheDrawable(path, info.loadIcon(packageManager), ImageCacher.Type.APK);
                        EventBus.getDefault().post(new EventBusType.ShareApkInfo(item));
                    } catch (Exception e) {

                    }
                }
            }
            ImageCacher.getInstance().exit();
        }
    }

    private void buildResultList(GlobalParams.ShareType type, Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            EventBus.getDefault().post(new EventBusType.NoLocalFiles(type));
            return;
        }
        int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
        int pathIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        int sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
        if (type == GlobalParams.ShareType.AUDIO) {
            while (cursor.moveToNext()) {
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                String url = getAlbumArt(cursor.getInt(albumIndex));
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                MusicItem item = new MusicItem(cursor.getString(titleIndex), cursor.getString(pathIndex),
                        cursor.getLong(sizeIndex), false, url, cursor.getString(artistIndex));
                EventBus.getDefault().post(new EventBusType.ShareMusicInfo(item));
            }
        } else if (type == GlobalParams.ShareType.VIDEO) {
            while (cursor.moveToNext()) {
                int durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                VideoItem item = new VideoItem(cursor.getString(titleIndex), cursor.getString(pathIndex),
                        cursor.getLong(sizeIndex), false, null, cursor.getLong(durationIndex));
                EventBus.getDefault().post(new EventBusType.ShareVideoInfo(item));
            }

        } else if (type == GlobalParams.ShareType.IMAGE) {
            if (mAllImagesList == null) {
                mAllImagesList = new ArrayList<>();
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
            EventBus.getDefault().post(new EventBusType.ShareImageFolderInfo(convert(mAllImagesList)));
        }
    }

    private List<ImageFolderItem> convert(final List<ImageItem> imageList) {
        List<ImageFolderItem> folderList = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        List<String> coverList = new ArrayList<>();
        if (imageList != null) {
            for (int i = 0; i < imageList.size(); i++) {
                ImageItem item = imageList.get(i);
                String foldPath = getFolderPath(item.getPath());
                Long count = map.get(foldPath);
                if (count == null) {
                    count = new Long(0);
                    coverList.add(item.getPath());
                }
                map.put(foldPath, count + 1);
            }
        }
        Set<String> set = map.keySet();
        for (String str : set) {
            String folderName = getFolderName(str);
            long count = map.get(str);
            String coverPath = null;
            for (String p : coverList) {
                if (CommonUtil.isDirectFolder(p, str)) {
                    coverPath = p;
                    break;
                }
            }
            ImageFolderItem folderItem = new ImageFolderItem(coverPath, folderName, count);
            folderList.add(folderItem);

        }
        return folderList;
    }

    private String getFolderName(String path) {
        int index1 = path.lastIndexOf("/");
        return path.substring(index1 + 1);
    }

    private String getFolderPath(String path) {
        int index1 = path.lastIndexOf("/");
        return path.substring(0, index1);
    }

    public List<ImageItem> getAllImages() {
        return mAllImagesList;
    }


    private String getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
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
