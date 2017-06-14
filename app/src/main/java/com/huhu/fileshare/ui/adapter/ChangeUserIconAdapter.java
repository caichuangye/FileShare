package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;


/**
 * Created by Administrator on 2016/4/18.
 */
public class ChangeUserIconAdapter extends BaseAdapter {

    private Context mContext;

    private int mSelectedIndex = 0;

    public ChangeUserIconAdapter(Context context) {
        mContext = context;
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
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
        if (mSelectedIndex == position) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.image_cover));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white_0));
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.icon_imageview);
        imageView.setImageResource(((ShareApplication) mContext.getApplicationContext()).getUserIconList()[position]);
        return convertView;
    }
}
