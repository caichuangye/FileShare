package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.model.SpecialFileItem;
import com.huhu.fileshare.ui.view.DownloadIcon;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/18.
 */
public class SpecialFileAdapter extends HuhuBaseAdapter<SpecialFileItem> {

    public SpecialFileAdapter(Context context,int mode){
        super(context,mode);
    }

    @Override
    public GlobalParams.ShareType getSharedType() {
        return GlobalParams.ShareType.FILE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item_layout,null);
            holder = new ViewHolder();
            holder.coverImageView = (ImageView)convertView.findViewById(R.id.file_cover);
            holder.titleTextView = (TextView)convertView.findViewById(R.id.file_name);
            holder.typeTextView = (TextView)convertView.findViewById(R.id.file_info1);
            holder.sizeTextView = (TextView)convertView.findViewById(R.id.file_info2);
            holder.selectedImageView = (ImageView) convertView.findViewById(R.id.file_selected);
            holder.downloadTextView = (DownloadIcon) convertView.findViewById(R.id.file_download);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        SpecialFileItem item = mDataList.get(position);
        holder.coverImageView.setImageBitmap(FileQueryHelper.getInstance(mContext).getSpecialFileCover(item.getType()));
        holder.titleTextView.setText(item.getShowName());
        holder.typeTextView.setText(item.getType().getTypeString());
        holder.sizeTextView.setText(Formatter.formatFileSize(mContext, item.getSize()));

        if (mMode == GlobalParams.SCAN_MODE) {
            holder.selectedImageView.setVisibility(View.GONE);
            holder.downloadTextView.setVisibility(View.VISIBLE);
            DownloadStatus status = ShareApplication.getInstance().getFileDownloadStatus(item.getPath());
            if(status != null) {
                holder.downloadTextView.setStatus(CommonUtil.getStatus(status));
            }
            if(holder.downloadTextView.getStatus() == DownloadIcon.Status.INIT) {
                EventBusType.SharedFileInfo info = new EventBusType.SharedFileInfo(item, getSharedType(), true);
                DownLoadListener listener = new DownLoadListener(info);
                holder.downloadTextView.setOnClickListener(listener);
            }
        }else{
            holder.downloadTextView.setVisibility(View.GONE);
            holder.selectedImageView.setVisibility(item.isSelected()?View.VISIBLE:View.GONE);
        }

        return convertView;
    }

    private class ViewHolder{
        ImageView coverImageView;
        TextView titleTextView;
        TextView typeTextView;
        DownloadIcon downloadTextView;
        ImageView selectedImageView;
        TextView sizeTextView;
    }
}
