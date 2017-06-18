package com.huhu.fileshare.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.text.TextUtils;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.CommonFileItem;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.ui.view.DownloadIcon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2016/4/15.
 */
public class CommonUtil {

    public static String formatSeconds(long second) {
        long h = 0;
        long d = 0;
        long s = 0;
        long temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        String strH = String.valueOf(h);
        String strM = String.valueOf(d);
        String strS = String.valueOf(s);
        if (h < 10) {
            strH = "0" + h;
        }
        if (d < 10) {
            strM = "0" + d;
        }
        if (s < 10) {
            strS = "0" + s;
        }
        return strH + ":" + strM + ":" + strS;
    }

    public static String getFolderByPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            return path.substring(0, path.lastIndexOf("/"));
        } else {
            return null;
        }
    }

    public static boolean isDirectFolder(String file, String folder) {
        if (TextUtils.isEmpty(file) || TextUtils.isEmpty(folder)) {
            return false;
        }
        if (!file.startsWith(folder)) {
            return false;
        }
        int folderLen = folder.length();
        String tmp = file.substring(folderLen + 1);
        return !tmp.contains("/");
    }

    public static List<ImageItem> getImageItemsList(Context context, String folderPath) {
        List<ImageItem> list = new ArrayList<>();
        List<ImageItem> allList = FileQueryHelper.getInstance().getAllImages();
        if (allList != null) {
            for (ImageItem item : allList) {
                //   if(item.getPath().startsWith(folderPath)){
                if (CommonUtil.isDirectFolder(item.getPath(), folderPath)) {
                    list.add(item);
                }
            }
        }
        return list;
    }


    private static boolean sIsSendIconPath = true;

    public static byte[] buildSendData(Context context) {
        ShareApplication shareApplication = (ShareApplication) context.getApplicationContext();
        byte b = 0;
        if (shareApplication.getSharedFilesCount() > 0) {
            b |= 0x80;
        } else {
            b &= 0x7f;
        }
        if (shareApplication.needRefresh()) {
            b |= 0x40;
        } else {
            b &= 0xbf;
        }
        String name = SystemSetting.getInstance(context).getUserNickName();
        String iconPath = SystemSetting.getInstance(context).getUserIconPath();
        String size = String.valueOf(UserIconManager.getInstance().getSelfIconBitmapSize(context));
        if(TextUtils.isEmpty(iconPath) || Long.parseLong(size) <= 0){
            sIsSendIconPath = false;
        }
        int length = name.length() + 2;
        if (sIsSendIconPath) {
            length += iconPath.length() + 1 + size.length() + 1;
        }

        byte[] data = new byte[length];
        data[0] = b;
        data[1] = shareApplication.getSharedType();
        System.arraycopy(name.getBytes(), 0, data, 2, name.length());
        if (sIsSendIconPath) {
            data[2 + name.length()] = '|';
            System.arraycopy(iconPath.getBytes(), 0, data, 2 + name.length() + 1, iconPath.length());
            data[2 + name.length() + iconPath.length()+1] = '|';
            System.arraycopy(size.getBytes(), 0, data, 2 + name.length() + 1 +iconPath.length()+ 1, size.length());
            HLog.d(CommonUtil.class,HLog.S,"send str = "+new String(data)+", size = "+size);
            sIsSendIconPath = false;
        } else {
            sIsSendIconPath = true;
        }
        return data;
    }

    public static String parseUserName(byte[] data) {
        if (data != null) {
            String str = new String(data);
            if (!str.contains("|")) {
                return new String(data, 2, data.length - 2);
            } else {
                return str.substring(2, str.indexOf('|'));
            }
        }
        return null;
    }

    public static String parseIconPath(byte[] data) {
        String str = new String(data);
        if(str.contains("|")){
            int first = str.indexOf("|");
            int last = str.lastIndexOf("|");
            if(first > 2 && last > first){
                return str.substring(first+1,last);
            }
        }
        return null;
    }

    public static long parseIconSize(byte[] data) {
        String str = new String(data);
        if(str.contains("|")){
            int last = str.lastIndexOf("|");
            if(last > 2){
                return Long.parseLong(str.substring(last+1));
            }
        }
        return 0;
    }

    public static boolean parseNeedRefresh(byte[] data) {
        byte b = data[0];
        b &= 0x40;
        return b != 0;
    }

    public static boolean parseHasSharedFiles(byte[] data) {
        byte b = data[0];
        b &= 0x80;
        return b != 0;
    }

    public static boolean parseHasImages(byte data) {
        data &= 0x01;
        return data != 0;
    }

    public static boolean parseHasMusics(byte data) {
        data &= 0x02;
        return data != 0;
    }

    public static boolean parseHasVideos(byte data) {
        data &= 0x04;
        return data != 0;
    }

    public static boolean parseHasApks(byte data) {
        data &= 0x08;
        return data != 0;
    }

    public static boolean parseHascommonFiles(byte data) {
        data &= 0x10;
        return data != 0;
    }


    public static Bitmap roundBitmap(Bitmap src, int rx, int ry) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setAntiAlias(true);
        canvas.drawRoundRect(0, 0, w, h, rx, ry, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, p);
        return bitmap;
    }

    private static String sFolderName;

    public static String getAppFolder() {
        if (TextUtils.isEmpty(sFolderName)) {
            sFolderName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/fileshare";
            File file = new File(sFolderName);
            if (!file.exists()) {
                file.mkdir();
            }
        }
        return sFolderName;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static DownloadIcon.Status getStatus(DownloadStatus status) {
        if (status == DownloadStatus.INIT) {
            return DownloadIcon.Status.INIT;
        } else if (status == DownloadStatus.WAIT) {
            return DownloadIcon.Status.WAIT;
        } else if (status == DownloadStatus.DOWNLOADING) {
            return DownloadIcon.Status.DOWNLOADING;
        } else if (status == DownloadStatus.SUCCESSED) {
            return DownloadIcon.Status.COMPLETE;
        } else {
            return null;
        }
    }

    public enum Flag {
        NONE,//0
        TYPE,//1
        DATE,//2
        OWNER//3
    }

    public static Flag getFlag(int i) {
        if (i == 1) {
            return Flag.TYPE;
        } else if (i == 2) {
            return Flag.DATE;
        } else if (i == 3) {
            return Flag.OWNER;
        } else {
            return Flag.NONE;
        }
    }

    public static int getFlagValue(Flag flag) {
        if (flag == Flag.TYPE) {
            return 1;
        } else if (flag == Flag.DATE) {
            return 2;
        } else if (flag == Flag.OWNER) {
            return 3;
        } else {
            return 0;
        }
    }


    public static Map<String, Set<String>> groupByIP(List<DownloadItem> list) {
        Map<String, Set<String>> map = new HashMap<>();
        if (list != null) {
            for (DownloadItem item : list) {
                Set<String> tmp = map.get(item.getFromIP());
                if (tmp == null) {
                    tmp = new HashSet<>();
                }
                tmp.add(item.getFromPath());
                map.put(item.getFromIP(), tmp);
            }
        }
        return map;
    }

    public static int getCommonFileCoverId(String path) {
        String typeStr = path.substring(path.lastIndexOf('.') + 1);
        CommonFileItem.FileType type = CommonFileItem.FileType.valueOfString(typeStr);
        switch (type) {
            case PDF:
                return R.mipmap.pdf;
            case TXT:
                return R.mipmap.txt;
            case DOC:
                return R.mipmap.doc;
            case ZIP:
                return R.mipmap.zip;
            case PPT:
                return R.mipmap.ppt;
            case APK:
                return R.mipmap.apk1;
            case XLS:
                return R.mipmap.excel;
            default:
                UNKNOWN:
                return R.mipmap.file;
        }
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
