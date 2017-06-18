package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DeviceItem;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.UserIconManager;

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
        Bitmap bitmap = UserIconManager.getInstance().getIconBitMap(item.getIP());
        viewHolder.iconImageView.setImageBitmap(bitmap != null ? bitmap : BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.default_icon));
        viewHolder.nameTextView.setText(mDataList.get(position).getUserName());
        viewHolder.ipTextView.setText(getShardDesc(mDataList.get(position).getSharedType()));
        viewHolder.hasSharedImageView.setVisibility(mDataList.get(position).hasShared()? View.VISIBLE:View.INVISIBLE);
        return convertView;
    }

    private String getShardDesc(byte flag){
        String desc = "";
        if(CommonUtil.parseHasImages(flag)){
            desc += "图片|";
        }
        if(CommonUtil.parseHasMusics(flag)){
            desc += "音乐|";
        }
        if(CommonUtil.parseHasVideos(flag)){
            desc += "视频|";
        }
        if(CommonUtil.parseHasApks(flag)){
            desc += "应用|";
        }
        if(CommonUtil.parseHascommonFiles(flag)){
            desc += "文件|";
        }
        if(!TextUtils.isEmpty(desc)){
            desc = desc.substring(0,desc.length()-1);
        }else{
            desc = "暂无共享";
        }
        return desc;
    }

    public int getFirstSharedFileIndex(int pos){
        byte flag = mDataList.get(pos).getSharedType();
        if(CommonUtil.parseHasImages(flag)){
            return 0;
        }
        if(CommonUtil.parseHasMusics(flag)){
            return 1;
        }
        if(CommonUtil.parseHasVideos(flag)){
            return 2;
        }
        if(CommonUtil.parseHasApks(flag)){
            return 3;
        }
        if(CommonUtil.parseHascommonFiles(flag)){
            return 4;
        }
        return 0;
    }

    private class ViewHolder{
        ImageView iconImageView;
        TextView nameTextView;
        TextView ipTextView;
        ImageView hasSharedImageView;
    }
}
