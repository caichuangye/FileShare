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
import com.huhu.fileshare.model.CommonFileItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.ui.view.DownloadIcon;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.ImageCacher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * Created by Administrator on 2016/4/18.
 */
public class CommonFileAdapter extends FileBaseAdapter<CommonFileItem> {

    public CommonFileAdapter(Context context, int mode) {
        super(context, mode);
    }

    public void updateCover(String path, String cover) {
        boolean hit = false;
        for (CommonFileItem item : mDataList) {
            if (path.equals(item.getType().toString())) {
                item.setCoverBitMap(cover);
                hit = true;
                //     break;
            }
        }
        if (hit) {
            notifyDataSetChanged();
        }
    }

    @Override
    public GlobalParams.ShareType getSharedType() {
        return GlobalParams.ShareType.FILE;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item_layout, null);
            holder = new ViewHolder();
            holder.coverImageView = (ImageView) convertView.findViewById(R.id.file_cover);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.file_name);
            holder.typeTextView = (TextView) convertView.findViewById(R.id.file_info1);
            holder.sizeTextView = (TextView) convertView.findViewById(R.id.file_info2);
            holder.selectedCheckbox = (CheckBox) convertView.findViewById(R.id.file_selected);
            holder.downloadTextView = (DownloadIcon) convertView.findViewById(R.id.file_download);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CommonFileItem item = mDataList.get(position);

        if (TextUtils.isEmpty(item.getCoverBitMap())) {
            item.setCoverBitMap(ImageCacher.getInstance().getCoverPath(item.getType().toString(), ImageCacher.Type.COMMON_FILE));
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(
                new RoundedBitmapDisplayer(15))
                .showImageOnFail(R.mipmap.file)
                .build();
        ImageLoader.getInstance().displayImage("file://" + item.getCoverBitMap(), holder.coverImageView, options);

        holder.selectedCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(position);
            }
        });

        holder.titleTextView.setText(item.getShowName());
        holder.typeTextView.setText(item.getType().getTypeString());
        holder.sizeTextView.setText(Formatter.formatFileSize(mContext, item.getSize()));

        if (mMode == GlobalParams.SCAN_MODE) {
            holder.selectedCheckbox.setVisibility(View.GONE);
            holder.downloadTextView.setVisibility(View.VISIBLE);
            DownloadStatus status = ShareApplication.getInstance().getFileDownloadStatus(item.getPath());
            if (status != null) {
                holder.downloadTextView.setStatus(CommonUtil.getStatus(status));
            }
            EventBusType.SharedFileInfo info = new EventBusType.SharedFileInfo(item, getSharedType(), true);
            DownLoadListener listener = new DownLoadListener(info);
            holder.downloadTextView.setOnClickListener(listener);
        } else {
            holder.selectedCheckbox.setVisibility(View.VISIBLE);
            holder.downloadTextView.setVisibility(View.GONE);
            holder.selectedCheckbox.setChecked(item.isSelected());
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView coverImageView;
        TextView titleTextView;
        TextView typeTextView;
        DownloadIcon downloadTextView;
        CheckBox selectedCheckbox;
        TextView sizeTextView;
    }
}
