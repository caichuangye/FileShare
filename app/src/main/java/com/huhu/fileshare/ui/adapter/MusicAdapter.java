package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.ui.view.DownloadIcon;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;


/**
 * Created by Administrator on 2016/4/12.
 */
public class MusicAdapter extends HuhuBaseAdapter<MusicItem> {

    public MusicAdapter(Context context,int mode){
        super(context,mode);
    }

    @Override
    public GlobalParams.ShareType getSharedType() {
        return GlobalParams.ShareType.AUDIO;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item_layout,null);
            holder = new ViewHolder();
            holder.coverImageView = (ImageView)convertView.findViewById(R.id.file_cover);
            holder.titleTextView = (TextView)convertView.findViewById(R.id.file_name);
            holder.artistTextView = (TextView)convertView.findViewById(R.id.file_info1);
            holder.selectedImageView = (ImageView) convertView.findViewById(R.id.file_selected);
            holder.sizeTextView = (TextView)convertView.findViewById(R.id.file_info2);
            holder.downloadTextView = (DownloadIcon) convertView.findViewById(R.id.file_download);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        MusicItem item = mDataList.get(position);
        String uri = null;
        if(!TextUtils.isEmpty(item.getCoverBitMap())) {
            uri = "file://"+item.getCoverBitMap();
        }else{
            uri = "drawable://"+R.mipmap.mp3;
        }
        DisplayImageOptions options =  new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(15)).build();
        ImageLoader.getInstance().displayImage(uri,holder.coverImageView,options);

        holder.titleTextView.setText(item.getShowName());
        holder.artistTextView.setText(item.getArtist());
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
        TextView artistTextView;
        DownloadIcon downloadTextView;
        ImageView selectedImageView;
        TextView sizeTextView;
    }
}
