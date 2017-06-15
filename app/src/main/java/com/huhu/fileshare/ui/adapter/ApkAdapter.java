package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.ApkItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.ui.view.DownloadIcon;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.ImageCacher;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;


/**
 * Created by Administrator on 2016/4/12.
 */
public class ApkAdapter extends FileBaseAdapter<ApkItem> {

    public ApkAdapter(Context context, int mode) {
        super(context, mode);
    }

    public void addItem(ApkItem item) {
        for (ApkItem apkItem : mDataList) {
            if (item.getPath().equals(apkItem.getPath())) {
                return;
            }
        }
        mDataList.add(item);
        notifyDataSetChanged();
    }

    public void updateCover(String path, String cover) {
        boolean hit = false;
        for (ApkItem item : mDataList) {
            if (path.equals(item.getPath())) {
                item.setCoverBitMap(cover);
                hit = true;
                break;
            }
        }
        if (hit) {
            notifyDataSetChanged();
        }
    }

    @Override
    public GlobalParams.ShareType getSharedType() {
        return GlobalParams.ShareType.APK;
    }
    private int mTimes = 0;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mTimes++;
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item_layout, null);
            holder = new ViewHolder();
            holder.coverImageView = (ImageView) convertView.findViewById(R.id.file_cover);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.file_name);
            holder.descTextView = (TextView) convertView.findViewById(R.id.file_info1);
            holder.selectedCheckbox = (CheckBox) convertView.findViewById(R.id.file_selected);
            holder.sizeTextView = (TextView) convertView.findViewById(R.id.file_info2);
            holder.downloadIcon = (DownloadIcon) convertView.findViewById(R.id.file_download);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ApkItem item = mDataList.get(position);

        if (TextUtils.isEmpty(item.getCoverBitMap())) {
            item.setCoverBitMap(ImageCacher.getInstance().getCoverPath(item.getPath(), ImageCacher.Type.APK));
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(
                new RoundedBitmapDisplayer(15))
                .showImageOnFail(R.mipmap.apk)
                .build();
        ImageLoader.getInstance().displayImage("file://" + item.getCoverBitMap(), holder.coverImageView, options);

        holder.titleTextView.setText(item.getShowName());
        holder.descTextView.setText(item.getDesc());
        holder.sizeTextView.setText(Formatter.formatFileSize(mContext, item.getSize()));

        holder.selectedCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(position);
            }
        });

        if (mMode == GlobalParams.SERVER_MODE) {
            holder.selectedCheckbox.setVisibility(View.GONE);
            holder.downloadIcon.setVisibility(View.VISIBLE);
            DownloadStatus status = ShareApplication.getInstance().getFileDownloadStatus(item.getPath());
            if (status != null) {
                holder.downloadIcon.setStatus(CommonUtil.getStatus(status));
            }
            EventBusType.SharedFileInfo info = new EventBusType.SharedFileInfo(item, getSharedType(), true);
            DownLoadListener listener = new DownLoadListener(info);
            holder.downloadIcon.setOnClickListener(listener);
        } else {
            holder.selectedCheckbox.setVisibility(View.VISIBLE);
            holder.downloadIcon.setVisibility(View.GONE);
            holder.selectedCheckbox.setChecked(item.isSelected());
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView coverImageView;
        TextView titleTextView;
        TextView descTextView;
        DownloadIcon downloadIcon;
        CheckBox selectedCheckbox;
        TextView sizeTextView;
    }
}
