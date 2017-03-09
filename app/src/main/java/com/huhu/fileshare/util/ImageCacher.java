package com.huhu.fileshare.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.huhu.fileshare.R;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.CommonFileItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Administrator on 2017/2/24.
 */

public class ImageCacher implements Runnable {

    public enum Type {APK, VIDEO, COMMON_FILE}

    private Handler mWorkHandler;

    private Handler mVideoCoverHandler;

    private static ImageCacher sInstance;

    private BlockingQueue<CacheItem> mList;

    private final String APK_Folder = CommonUtil.getAppFolder() + File.separator + "ApkIcon" + File.separator;

    private final String VIDEO_Folder = CommonUtil.getAppFolder() + File.separator + "VideoIcon" + File.separator;

    private final String COMMON_Folder = CommonUtil.getAppFolder() + File.separator + "CommonIcon" + File.separator;

    private volatile boolean mQuit = false;

    private ImageCacher() {
        mList = new LinkedBlockingDeque<>();
        File fileApk = new File(APK_Folder);
        if (!fileApk.exists()) {
            fileApk.mkdir();
        }

        File fileVideo = new File(VIDEO_Folder);
        if (!fileVideo.exists()) {
            fileVideo.mkdir();
        }

        File fileCommon = new File(COMMON_Folder);
        if (!fileCommon.exists()) {
            fileCommon.mkdir();
        }

        mQuit = false;
        HandlerThread thread = new HandlerThread("cache_drawable");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());
        mWorkHandler.post(this);
    }

    public static ImageCacher getInstance() {
        if (sInstance == null) {
            sInstance = new ImageCacher();
        }
        return sInstance;
    }

    public void cacheDrawable(String path, Drawable drawable, Type type) {
        if(checkExist(path,type)){
            return;
        }
        try {
            Bitmap bitmap = drawable2Bitmap(drawable);
            if (bitmap != null) {
                mList.put(new CacheItem(path, bitmap, type));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cacheBitmap(String path, Bitmap bitmap, Type type) {
        if(checkExist(path,type)){
            return;
        }
        try {
            mList.put(new CacheItem(path, bitmap, type));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cacheVideo(final String path, final int w, final int h) {
        if(checkExist(path,Type.VIDEO)){
            return;
        }
        if (mVideoCoverHandler == null) {
            HandlerThread thread = new HandlerThread("video_cover");
            thread.start();
            mVideoCoverHandler = new Handler(thread.getLooper());
        }

        mVideoCoverHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, w, h, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                if (bitmap != null) {
                    cacheBitmap(path, bitmap, Type.VIDEO);
                }
            }
        });

    }

    public void cacheCommonFileIcon(CommonFileItem.FileType type, int w, int h) {
        if(checkExist(type.toString(),Type.COMMON_FILE)){
            return;
        }
        String str = type.getTypeString().toUpperCase();
        int color = Color.argb(255, 45, 53, 60);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);

        Paint paint = new Paint();
        paint.setTextSize(60);
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);

        paint.setColor(Color.parseColor("#24b7a4"));
        canvas.drawText(str, w / 2 - rect.width() / 2, h / 2 + rect.height() / 2, paint);
        cacheBitmap(type.toString(), bitmap, Type.COMMON_FILE);
    }

    public void exit() {
        mQuit = true;
        sInstance = null;
    }

    private boolean checkExist(String path, Type type) {
        String filePath = getCoverPath(path, type);
        File file = new File(filePath);
        if(file.exists()) {
            EventBus.getDefault().post(new EventBusType.CacheImageComplete(new CacheResult(path, filePath, type)));
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        while (!mQuit) {
            try {
                CacheItem item = mList.take();
                String path = getCoverPath(item.path, item.type);
                Bitmap bitmap = item.bitmap;
                if (bitmap != null) {
                    byte[] data = bitmap2Bytes(bitmap);
                    FileOutputStream outputStream = new FileOutputStream(path);
                    outputStream.write(data);
                    outputStream.close();
                }
                HLog.d("filequeryhelper",item.path+": "+path);
                FileQueryHelper.getInstance().saveCoverImage(item.path,path);
                EventBus.getDefault().post(new EventBusType.CacheImageComplete(new CacheResult(item.path, path, item.type)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getCoverPath(String path, Type type) {
        String tmp = path.replace('/', '_');
        tmp = tmp.replace('.', '_');
        if (type == Type.APK) {
            return APK_Folder + tmp;
        } else if (type == Type.VIDEO) {
            return VIDEO_Folder + tmp;
        } else {
            return COMMON_Folder + tmp;
        }
    }

    public class CacheItem {
        public CacheItem(String path, Bitmap bitmap, Type type) {
            this.path = path;
            this.bitmap = bitmap;
            this.type = type;
        }

        String path;
        Bitmap bitmap;
        Type type;
    }

    public class CacheResult {

        public CacheResult(String filePath, String coverPath, Type type) {
            this.filePath = filePath;
            this.coverPath = coverPath;
            this.type = type;
        }

        public String filePath;
        public String coverPath;
        public Type type;
    }

    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    private byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
