package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.SDCardFileItem;
import com.huhu.fileshare.ui.view.DownloadIcon;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.ScanFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/10.
 */
public class FileAdapter extends BaseAdapter {

    private String TAG = "ShareApplication";

    public static String ROOT_PATH = Environment.getExternalStorageDirectory().getPath();

    private Context mContext;

    private List<SDCardFileItem> mDataList;

    private Bitmap mFileCover;

    private Bitmap mFolderCover;

//    private int mIndex;

    public FileAdapter(Context context){
        mContext = context;
        mDataList = new ArrayList<>();
        Bitmap file = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.file);
        mFileCover = CommonUtil.roundBitmap(file,file.getWidth()/10,file.getHeight()/10);
        Bitmap folder = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.folder);
        mFolderCover = CommonUtil.roundBitmap(folder,folder.getWidth()/10,folder.getHeight()/10);
    }

    public void setData(List<SDCardFileItem> list, List<String> selectedList){
        mDataList.clear();
        if(list != null){
            for(SDCardFileItem item : list){
                if(selectedList != null && selectedList.contains(item.getPath())){
                    item.setSelected(true);
                }
                mDataList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public final void updateSelectFiles(){
//        List<String> list = ((ShareApplication)mContext.getApplicationContext()).
//                getSharedFileByType(GlobalParams.ShareType.SD_FILE);
//        if(list == null){
//            for(SDCardFileItem t : mDataList){
//                t.setSelected(false);
//            }
//        }else{
//            for(SDCardFileItem t : mDataList){
//                if(list.contains(t.getPath())){
//                    t.setSelected(true);
//                }else{
//                    t.setSelected(false);
//                }
//            }
//        }
//        notifyDataSetChanged();
    }

    public String handleClick(int pos){
        SDCardFileItem item = mDataList.get(pos);
        if(item.getType() == SDCardFileItem.TYPE_FOLDER){
            if(item.getSize() == 0) {
                return null;
            }else{
                ScanFiles.getInstance().scan(item.getPath());
                return item.getPath();
            }
        }else{
            operateFile(item);
            return null;
        }
    }

    private void operateFile(SDCardFileItem item){
        boolean res = item.isSelected();
        item.setSelected(!res);
        notifyDataSetChanged();
        String oper = res? "delete":"add";
        HLog.d(TAG,"post: "+oper+"; path = "+item.getPath());
     //   EventBus.getDefault().post(new EventBusType.SharedFileInfo(item,GlobalParams.ShareType.SD_FILE,!res));
    }

    public String getParentFolderPath(){
        if(mDataList.size() > 0) {
            SDCardFileItem item = mDataList.get(0);
            int index = item.getPath().lastIndexOf("/");
            if (index > 0) {
                String str = item.getPath().substring(0, index);
                int tmp = str.lastIndexOf("/");
                if(tmp > 0){
                    return str.substring(0,tmp);
                }
            }
        }
        return null;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item_layout,null);
            holder.iconImageView = (ImageView)convertView.findViewById(R.id.file_cover);
            holder.nameTextView = (TextView)convertView.findViewById(R.id.file_name);
            holder.sizeTextView = (TextView)convertView.findViewById(R.id.file_info1);
            holder.selectedImageView = (CheckBox) convertView.findViewById(R.id.file_selected);
            holder.downloadTextView = (DownloadIcon) convertView.findViewById(R.id.file_download);
            holder.file2TextView = (TextView)convertView.findViewById(R.id.file_info2);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        SDCardFileItem item = mDataList.get(position);
        if(item.getType() == SDCardFileItem.TYPE_FILE){
            holder.iconImageView.setImageBitmap(mFileCover);
        }else{
            holder.iconImageView.setImageBitmap(mFolderCover);
        }
        holder.file2TextView.setVisibility(View.INVISIBLE );
        holder.nameTextView.setText(item.getShowName());
        if(item.getType() == SDCardFileItem.TYPE_FILE) {
            String sizeStr = Formatter.formatFileSize(mContext, item.getSize());
            holder.sizeTextView.setText(sizeStr);
        }else{
            String sizeStr = item.getSize()+mContext.getResources().getString(R.string.file_unit);
            holder.sizeTextView.setText(sizeStr);
        }
        if(item.getType() == SDCardFileItem.TYPE_FOLDER){
            holder.selectedImageView.setVisibility(View.INVISIBLE);
        }else{
            if(item.isSelected()) {
                holder.selectedImageView.setVisibility(View.VISIBLE);
            }else{
                holder.selectedImageView.setVisibility(View.INVISIBLE);
            }
        }
        holder.downloadTextView.setVisibility(View.GONE);
        return convertView;
    }

    private class ViewHolder{
        ImageView iconImageView;
        TextView nameTextView;
        TextView sizeTextView;
        TextView file2TextView;
        DownloadIcon downloadTextView;
        CheckBox selectedImageView;
    }
}
