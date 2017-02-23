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
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.ui.view.DownloadIcon;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Created by Administrator on 2016/4/16.
 */
public class ImageAdapter extends FileBaseAdapter<ImageItem> {

    private DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.ic_launcher) // 在ImageView加载过程中显示图片
    .showImageForEmptyUri(R.mipmap.ic_launcher) // image连接地址为空时
    .showImageOnFail(R.mipmap.ic_launcher) // image加载失败
    .cacheInMemory(true) // 加载图片时会在内存中加载缓存
    .cacheOnDisk(true) // 加载图片时会在磁盘中加载缓存
    .build();


    public ImageAdapter(Context context,int mode) {
        super(context,mode);
    }

    @Override
    public GlobalParams.ShareType getSharedType() {
        return GlobalParams.ShareType.IMAGE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.image_folder_item_layout,null);
            holder.image = (ImageView)convertView.findViewById(R.id.image);
            holder.folderInfoTextView = (TextView)convertView.findViewById(R.id.folder_info);
            holder.dateTextView = (TextView)convertView.findViewById(R.id.image_date);
            holder.coverView = convertView.findViewById(R.id.selected_cover);
            holder.selectedImageView = (ImageView)convertView.findViewById(R.id.selected_icon);
            holder.downloadTextView = (DownloadIcon) convertView.findViewById(R.id.download_icon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        ImageItem item = mDataList.get(position);
        ImageAware aware = new ImageViewAware(holder.image,false);
        ImageLoader.getInstance().displayImage("file://" + item.getPath(), aware ,mOptions,new ImageSize(174*3,174*3),null,null);
        holder.folderInfoTextView.setText(Formatter.formatFileSize(mContext, item.getSize()));
        holder.dateTextView.setText(item.getDate());
        holder.dateTextView.setVisibility(View.VISIBLE);
        holder.coverView.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);

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

    private static class ViewHolder{
        ImageView image;
        TextView folderInfoTextView;
        TextView dateTextView;
        View coverView;
        ImageView selectedImageView;
        DownloadIcon downloadTextView;
    }
}
