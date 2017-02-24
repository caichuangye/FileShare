package com.huhu.fileshare.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.HandlerThread;

import com.huhu.fileshare.de.greenrobot.event.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Administrator on 2017/2/24.
 */

public class ApkIconCacher implements Runnable {

    private Handler mWorkHandler;

    private static ApkIconCacher sInstance;

    private BlockingQueue<CacheItem> mList;

    private final String Folder = CommonUtil.getAppFolder() + File.separator + "apkicon" + File.separator;

    private volatile boolean mQuit = false;

    private ApkIconCacher() {
        mList = new LinkedBlockingDeque<>();
        File file = new File(Folder);
        if (!file.exists()) {
            file.mkdir();
        }
        HandlerThread thread = new HandlerThread("cache_drawable");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());
        mWorkHandler.post(this);
    }

    public static ApkIconCacher getInstance() {
        if (sInstance == null) {
            sInstance = new ApkIconCacher();
        }
        return sInstance;
    }

    public void cacheDrawable(String uuid, Drawable drawable) {
        try {
            mList.put(new CacheItem(uuid, drawable));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        mQuit = true;
    }

    @Override
    public void run() {
        while (!mQuit) {
            try {
                CacheItem item = mList.take();
                String path = getCoverPath(item.uuid);
                File file = new File(path);
                if (!file.exists()) {
                    Bitmap bitmap = drawable2Bitmap(item.drawable);
                    if (bitmap != null) {
                        byte[] data = bitmap2Bytes(bitmap);
                        FileOutputStream outputStream = new FileOutputStream(path);
                        outputStream.write(data);
                        outputStream.close();
                    }
                }
                EventBus.getDefault().post(new EventBusType.CacheApkIconComplete(item.uuid, path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getCoverPath(String path) {
        String tmp = path.replace('/', '_');
        tmp = tmp.replace('.', '_');
        return Folder + tmp;
    }

    public class CacheItem {
        public CacheItem(String uuid, Drawable drawable) {
            this.uuid = uuid;
            this.drawable = drawable;
        }

        String uuid;
        Drawable drawable;
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
