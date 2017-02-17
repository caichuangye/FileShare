package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DeviceItem;
import com.huhu.fileshare.util.ComClient;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/9.
 */
public class DevicesAdapter extends BaseAdapter {

    private Context mContext;

    private List<DeviceItem> mDataList;

    public DevicesAdapter(Context context){
        mContext = context;
        mDataList = new ArrayList<>();
    }

    public void setData(List<DeviceItem> list){
        mDataList.clear();
        if(list != null){
            for(DeviceItem item : list){
                mDataList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public boolean hasShared(int pos){
        return mDataList.get(pos).hasShared();
    }

    public String getIP(int pos){
        return mDataList.get(pos).getIP();
    }

    public String getName(int pos){
        return mDataList.get(pos).getUserName();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.devices_item_layout,null);
            viewHolder.iconImageView = (ImageView)convertView.findViewById(R.id.user_icon);
            viewHolder.nameTextView = (TextView)convertView.findViewById(R.id.user_name);
            viewHolder.ipTextView = (TextView)convertView.findViewById(R.id.user_ip);
            viewHolder.hasSharedImageView = (ImageView)convertView.findViewById(R.id.have_shared);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        DeviceItem item = mDataList.get(position);
        viewHolder.iconImageView.setImageResource(((ShareApplication) mContext.getApplicationContext()).getUserIconList()[item.getIconIndex()]);
        viewHolder.nameTextView.setText(mDataList.get(position).getUserName());
        viewHolder.ipTextView.setText(mDataList.get(position).getIP());
        viewHolder.hasSharedImageView.setVisibility(mDataList.get(position).hasShared()? View.VISIBLE:View.INVISIBLE);
        return convertView;
    }

    private class ViewHolder{
        ImageView iconImageView;
        TextView nameTextView;
        TextView ipTextView;
        ImageView hasSharedImageView;
    }
}
