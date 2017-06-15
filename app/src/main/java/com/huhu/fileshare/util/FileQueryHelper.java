package com.huhu.fileshare.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.huhu.fileshare.databases.DownloadHistory;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.ApkItem;
import com.huhu.fileshare.model.ImageFolderItem;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.VideoItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/4/12.
 */
public class FileQueryHelper {

    public static final String TAG = FileQueryHelper.class.getSimpleName();

    private static volatile FileQueryHelper sInstance;

    private Context mContext;

    private List<ImageItem> mAllImagesList;

    private Executor mThreadPool;

    private List<ApkItem> mApkList;
    private List<MusicItem> mMusicList;
    private List<VideoItem> mVideoList;

    public static FileQueryHelper getInstance() {
        if (sInstance == null) {
            synchronized (FileQueryHelper.class) {
                if (sInstance == null) {
                    sInstance = new FileQueryHelper();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mThreadPool = Executors.newFixedThreadPool(5);
    }

    private FileQueryHelper() {

    }

    public void scanFileByType(final GlobalParams.ShareType type) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                String name = Thread.currentThread().getName();
                name += ": " + type.toString();
                Thread.currentThread().setName(name);

                if (type == GlobalParams.ShareType.AUDIO) {
                    if (mMusicList != null) {
                        EventBus.getDefault().post(new EventBusType.ShareMusicInfo(mMusicList));
                        return;
                    } else {
                        mMusicList = new ArrayList<>();
                    }
                } else if (type == GlobalParams.ShareType.VIDEO) {
                    if (mVideoList != null) {
                        EventBus.getDefault().post(new EventBusType.ShareVideoInfo(mVideoList));
                        return;
                    } else {
                        mVideoList = new ArrayList<>();
                    }
                } else if (type == GlobalParams.ShareType.APK) {
                    if (mApkList != null) {
                        EventBus.getDefault().post(new EventBusType.ShareApkInfo(mApkList));
                        return;
                    } else {
                        mApkList = new ArrayList<>();
                    }
                } else if (type == GlobalParams.ShareType.IMAGE) {
                    if (mAllImagesList != null) {
                        EventBus.getDefault().post(new EventBusType.ShareImageFolderInfo(convert(mAllImagesList)));
                        return;
                    } else {
                        mAllImagesList = new ArrayList<>();
                    }
                }

                if (type == GlobalParams.ShareType.APK) {
                    parseInstalledApp();
                }else {
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
                        cursor.close();
                    }
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
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
        if (list != null) {
            for (PackageInfo info : list) {
                String path = info.applicationInfo.sourceDir;
                if (!TextUtils.isEmpty(path) && path.startsWith("/data/app")) {
                    String name = String.valueOf(info.applicationInfo.loadLabel(packageManager));
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(path);
                        long size = inputStream.available();
                        final ApkItem item = new ApkItem(name, path, size, false, null, info.versionName);
                        ImageCacher.getInstance().cacheDrawable(path, info.applicationInfo.loadIcon(packageManager), ImageCacher.Type.APK);
                        mApkList.add(item);
                    } catch (Exception e) {

                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            EventBus.getDefault().post(new EventBusType.ShareApkInfo(mApkList));
            ImageCacher.getInstance().exit();
        }
    }

    private void buildResultList(GlobalParams.ShareType type, Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            cursor.close();
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
                mMusicList.add(item);
            }
            EventBus.getDefault().post(new EventBusType.ShareMusicInfo(mMusicList));
        } else if (type == GlobalParams.ShareType.VIDEO) {
            while (cursor.moveToNext()) {
                int durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                VideoItem item = new VideoItem(cursor.getString(titleIndex), cursor.getString(pathIndex),
                        cursor.getLong(sizeIndex), false, null, cursor.getLong(durationIndex));
                mVideoList.add(item);
            }
            EventBus.getDefault().post(new EventBusType.ShareVideoInfo(mVideoList));

        } else if (type == GlobalParams.ShareType.IMAGE) {
            while (cursor.moveToNext()) {
                int dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yy-MM-dd");
                String d = sDateFormat.format(new Date(cursor.getLong(dateIndex) * 1000));
                String path = cursor.getString(pathIndex);
                ImageItem item = new ImageItem(cursor.getString(titleIndex), path,
                        cursor.getLong(sizeIndex), false, null, d);
                mAllImagesList.add(item);
            }
            EventBus.getDefault().post(new EventBusType.ShareImageFolderInfo(convert(mAllImagesList)));
        }
        cursor.close();
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
                    count = Long.valueOf(0);
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

    private Map<Long, String> mCoverMap = null;

    private String getAlbumArt(int album_id) {
        if (mCoverMap == null) {
            mCoverMap = new HashMap<>();
            String[] projection = new String[]{"album_art", "_id"};
            String uri = "content://media/external/audio/albums";
            Cursor cursor = mContext.getContentResolver().query(
                    Uri.parse(uri),
                    projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex("_id"));
                    String path = cursor.getString(cursor.getColumnIndex("album_art"));
                    mCoverMap.put(id, path);
                }
                cursor.close();
            }
        }
        return mCoverMap.get(Long.valueOf(album_id));
    }

    private Map<String, String> mCoverImageMap = new HashMap<>();

    public String getCoverImage(String path) {
        return mCoverImageMap.get(path);
    }

    public void parseCoverImage(String path, Uri uri) {
        if (path.endsWith(".apk")) {
            parseApkCoverImage(path);
        } else if (path.endsWith(".mp4")) {//// TODO: 2017/3/12 not only mp4
            parseVideoCoverImage(path);
        } else if (path.endsWith(".mp3")) {//// TODO: 2017/3/12 not only mp3
            parseMusicCoverImage(path, uri);
        } else {
        }
    }

    private void parseMusicCoverImage(String path, Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{MediaStore.Audio.Media.ALBUM_ID}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int id = cursor.getInt(albumIndex);
                String coverPath = getAlbumArt(id);
                saveCoverImage(path, coverPath);
                break;
            }
            cursor.close();
        } else {
        }
    }

    public void saveCoverImage(final String path, final String coverPath) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                String pre = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + GlobalParams.FOLDER;
                if (path.startsWith(pre)) {
                    mCoverImageMap.put(path, coverPath);
                    DownloadHistory.getInstance(mContext).updateFileCoverImage(path, coverPath);
                    EventBus.getDefault().post(new EventBusType.ScanDownloadFileComplete(path, coverPath));
                }
            }
        });

    }

    private void parseApkCoverImage(String path) {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo pi = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        pi.applicationInfo.sourceDir = path;
        pi.applicationInfo.publicSourceDir = path;
        Drawable drawable = pi.applicationInfo.loadIcon(packageManager);
        ImageCacher.getInstance().cacheDrawable(path, drawable, ImageCacher.Type.APK);
    }

    private void parseVideoCoverImage(String path) {
        ImageCacher.getInstance().cacheVideo(path, 150, 150);
    }


}
