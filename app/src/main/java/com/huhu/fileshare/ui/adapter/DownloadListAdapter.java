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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.model.ScanDeviceItem;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;

import static com.huhu.fileshare.util.HLog.DD;

/**
 * Created by Administrator on 2016/4/28.
 */
public class DownloadListAdapter extends BaseAdapter {

    private Context mContext;

    private List<DownloadItem> mList;

    public DownloadListAdapter(Context context){
        mContext = context;
        mList = new ArrayList<>();
    }

    public void setData(List<DownloadItem> list){
        mList.clear();
        if(list != null) {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        for(DownloadItem item : mList){
            if(item.getStatus() == DownloadStatus.SUCCESSED){
                HLog.d(DD,"download success, remove from list: "+item.getFromPath());
                mList.remove(item);
            }
        }
        super.notifyDataSetChanged();
    }

    public void deleteItem(DownloadItem item){
        for(DownloadItem item1 : mList){
            if(item.getUUID().equals(item1.getUUID())){
                mList.remove(item1);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.download_item_layout,null);
            holder.imageView = (ImageView)convertView.findViewById(R.id.type_icon);
            holder.nameTextView = (TextView)convertView.findViewById(R.id.file_name);
            holder.ownerTextView = (TextView)convertView.findViewById(R.id.file_owner);
            holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            holder.downloadTextView = (TextView)convertView.findViewById(R.id.download_info);
            holder.progressTextView = (TextView)convertView.findViewById(R.id.progress);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        final DownloadItem item = mList.get(position);
        holder.imageView.setImageBitmap(getCover(GlobalParams.ShareType.getType(item.getFileType())));
        String name = item.getFromPath().substring(item.getFromPath().lastIndexOf("/")+1);
        holder.nameTextView.setText(name);
        holder.ownerTextView.setText(item.getFromUserName());
        int progress = (int) (item.getRecvSize()*100/item.getTotalSize());
        holder.progressBar.setProgress(progress);
        holder.progressTextView.setText(progress+"%");
        String info = Formatter.formatFileSize(mContext, item.getTotalSize());
        holder.downloadTextView.setText(info);
        convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.remove(position);
                notifyDataSetChanged();
                EventBus.getDefault().post(new EventBusType.DeleteDownloadFile(item));
            }
        });
        return convertView;
    }

    private Bitmap getCover(GlobalParams.ShareType shareType){
        int id = R.mipmap.file;
        if(shareType == GlobalParams.ShareType.IMAGE){
            id = R.mipmap.image;
        }else if(shareType == GlobalParams.ShareType.AUDIO){
            id = R.mipmap.mp3;
        }else if(shareType == GlobalParams.ShareType.VIDEO){
            id = R.mipmap.video;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),id);
        return CommonUtil.roundBitmap(bitmap,bitmap.getWidth()/10,bitmap.getHeight()/10);
    }

    private class ViewHolder{
        ImageView imageView;
        TextView nameTextView;
        ProgressBar progressBar;
        TextView progressTextView;
        TextView ownerTextView;
        TextView downloadTextView;
    }
}
