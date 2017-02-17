package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.model.WiFiItem;
import com.huhu.fileshare.util.WiFiOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/4/6.
 */


public class WiFiAdapter extends BaseAdapter {

    private Context mContext;

    private List<WiFiItem> mDataList;

    private String mJoiningBssid;

    public WiFiAdapter(Context context){
        mDataList = new ArrayList<>();
        mContext = context;
    }

    public void clear(){
        mDataList.clear();
        notifyDataSetChanged();
    }

    public void setData(List<WiFiItem> list){
        mDataList.clear();
        if(list != null){
            for (WiFiItem item:list){
                mDataList.add(item);
            }
        }
        Collections.sort(mDataList, new Comparator<WiFiItem>() {
            @Override
            public int compare(WiFiItem lhs, WiFiItem rhs) {
               if(lhs.getLevel() > rhs.getLevel()){
                   return -1;
               }else if(lhs.getLevel() < rhs.getLevel()){
                   return 1;
               }else{
                   return 0;
               }
            }
        });
        notifyDataSetChanged();
    }

    public void setJoiningWiFi(String pos){
        mJoiningBssid = pos;
        notifyDataSetChanged();
    }

    public String getSSID(int index){
        return mDataList.get(index).getName();
    }

    public String getBSSID(int index){
        return mDataList.get(index).getMAC();
    }

    @Override
    public int getCount() {
        return mDataList == null? 0:mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList == null? null:mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_item_layout,null);
            holder.wifiNameTextView = (TextView)convertView.findViewById(R.id.wifi_name);
            holder.wifiStrengthImageView = (ImageView)convertView.findViewById(R.id.wifi_strength);
            holder.connectedIconImageView = (TextView)convertView.findViewById(R.id.joined_icon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        int[] icons = {R.mipmap.signal_wifi_1,R.mipmap.signal_wifi_2,R.mipmap.signal_wifi_3,R.mipmap.signal_wifi_4};
        holder.wifiNameTextView.setText(mDataList.get(position).getName());
        holder.wifiStrengthImageView.setImageResource(icons[mDataList.get(position).getLevel()]);
        if(mDataList.get(position).getMAC().equals(WiFiOperation.getInstance(mContext).getConnectedWiFiBSSID())){
            holder.connectedIconImageView.setVisibility(View.VISIBLE);
        }else{
            holder.connectedIconImageView.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private class ViewHolder{
        TextView wifiNameTextView;
        ImageView wifiStrengthImageView;
        TextView connectedIconImageView;
    }
}
