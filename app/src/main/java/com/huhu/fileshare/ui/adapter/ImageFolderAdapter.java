package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.model.ImageFolderItem;
import com.huhu.fileshare.ui.view.DownloadIcon;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.GlobalParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;


/**
 * Created by Administrator on 2016/4/15.
 */
public class ImageFolderAdapter extends HuhuBaseAdapter<ImageFolderItem> {

    public ImageFolderAdapter(Context context){
        super(context,GlobalParams.SHOW_MODE);
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
            holder.downloadTextView = (DownloadIcon)convertView.findViewById(R.id.download_icon);
            holder.selectedImage = (ImageView)convertView.findViewById(R.id.selected_icon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        ImageFolderItem item = mDataList.get(position);
        holder.folderInfoTextView.setText(item.getShowName()+"("+item.getImageCount()+")");
        ImageLoader.getInstance().displayImage("file://" + item.getCoverImagePath(), holder.image,
                new ImageSize((int) mContext.getResources().getDimension(R.dimen.huhu_174_dp),
                        (int) mContext.getResources().getDimension(R.dimen.huhu_174_dp)));
        holder.selectedImage.setVisibility(View.GONE);
        holder.downloadTextView.setVisibility(View.GONE);
        return convertView;
    }

    public String getFolderPath(int pos){
        return CommonUtil.getFolderByPath(mDataList.get(pos).getCoverImagePath());
    }

    private static class ViewHolder{
        ImageView image;
        TextView folderInfoTextView;
        DownloadIcon downloadTextView;
        ImageView selectedImage;
    }

}
