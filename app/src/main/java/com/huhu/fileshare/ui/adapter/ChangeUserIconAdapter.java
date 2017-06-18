package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.ImageItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/4/18.
 */
public class ChangeUserIconAdapter extends BaseAdapter {

    private DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.ic_launcher) // 在ImageView加载过程中显示图片
            .showImageForEmptyUri(R.mipmap.ic_launcher) // image连接地址为空时
            .showImageOnFail(R.mipmap.ic_launcher) // image加载失败
            .cacheInMemory(true) // 加载图片时会在内存中加载缓存
            .cacheOnDisk(true) // 加载图片时会在磁盘中加载缓存
            .build();

    private Context mContext;

    private List<String> mImagePathList;

    public ChangeUserIconAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<String> list) {
        if(mImagePathList == null){
            mImagePathList = new ArrayList<>();
        }else{
            mImagePathList.clear();
        }
        if(list != null){
            mImagePathList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return ((ShareApplication) mContext.getApplicationContext()).getUserIconList().length;
    }

    @Override
    public Object getItem(int position) {
        return ((ShareApplication) mContext.getApplicationContext()).getUserIconList()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.change_usericon_item_layout, null);
        }
        if(mImagePathList != null && position < mImagePathList.size()) {
            ImageView imageView = (ImageView) convertView.findViewById(R.id.icon_imageview);
            ImageAware aware = new ImageViewAware(imageView, false);
            ImageLoader.getInstance().displayImage("file://" + mImagePathList.get(position), aware, mOptions, new ImageSize(174 * 3, 174 * 3), null, null);
        }
        return convertView;
    }
}
