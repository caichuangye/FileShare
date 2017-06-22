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
import com.huhu.fileshare.util.HLog;
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

    private Context mContext;

    private List<String> mImagePathList;

    public ChangeUserIconAdapter(Context context) {
        mImagePathList = new ArrayList<>();
        mContext = context;
    }

    public void setData(List<String> list) {
        mImagePathList.clear();
        if(list != null){
            mImagePathList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mImagePathList.size();
    }

    @Override
    public Object getItem(int position) {
        return mImagePathList.get(position);
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
            ImageLoader.getInstance().displayImage("file://" + mImagePathList.get(position), imageView);
        }
        return convertView;
    }
}
