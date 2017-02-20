package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.SystemSetting;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/4/28.
 */
public class DownloadHistoryAdapter extends BaseAdapter {

    private Context mContext;

    private List<ItemImpl> mDataList;

    private List<String> mSelectedList;

    private boolean mShowDeleteIcon = false;

    private OnSelectListener mListener;

    public void selectAll(boolean isSelectAll) {
        mSelectedList.clear();
        if (isSelectAll) {
            for (DownloadItem item : mDataList) {
                mSelectedList.add(item.getUUID());
            }
        }
        mListener.onHasSelected(mSelectedList.size() > 0);
        notifyDataSetChanged();
    }

    public void setShowDeleteIcon(boolean show) {
        mShowDeleteIcon = show;
        notifyDataSetChanged();
    }

    public void deleteSelected() {
        Iterator<ItemImpl> iterator = mDataList.iterator();
        while (iterator.hasNext()) {
            ItemImpl item = iterator.next();
            if (!item.isTitle() && mSelectedList.contains(item.getUUID())) {
                iterator.remove();
            }
        }
        setGroup(mFlag);

    }

    public DownloadHistoryAdapter(Context context, OnSelectListener listener) {
        mContext = context;
        mDataList = new ArrayList<>();
        mSelectedList = new ArrayList<>();
        mListener = listener;
        mFlag = CommonUtil.getFlag(SystemSetting.getInstance(mContext).getGroupFlag());
    }

    public List<DownloadItem> getSelectedItem() {
        List<DownloadItem> list = new ArrayList<>();
        Log.d("cccc", "selected size = " + mSelectedList.size());
        for (ItemImpl item : mDataList) {
            Log.d("cccc","item uuid: "+item.getUUID());
            if (mSelectedList.contains(item.getUUID())) {
                Log.d("cccc", "prepare to delete: " + item.getToPath());
                list.add(item);
            }
        }
        return list;
    }

    public void setData(List<DownloadItem> list) {
        mDataList.clear();
        if (list != null) {
            for (DownloadItem item : list) {
                Log.d("cccc","---: "+item.getUUID());
                mDataList.add(ItemImpl.get(item));
            }
        }
        setGroup(mFlag);
    }

    public void addItem(DownloadItem item) {
        boolean has = false;
        for (DownloadItem ele : mDataList) {
            if (item.getUUID().equals(ele.getUUID())) {
                has = true;
                ele.setRecvSize(item.getRecvSize());
                ele.setStatus(item.getStatus());
                break;
            }
        }
        if (!has) {
            mDataList.add(0, ItemImpl.get(item));
        }
        setGroup(mFlag);
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
        ItemViewHolder itemHolder = new ItemViewHolder();
        TitleViewHolder titleHolder = new TitleViewHolder();
        final ItemImpl item = mDataList.get(position);
        if (convertView == null) {
            if (!item.isTitle()) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.download_history_layout, null);
                itemHolder.imageView = (ImageView) convertView.findViewById(R.id.type_icon);
                itemHolder.nameTextView = (TextView) convertView.findViewById(R.id.file_name);
                itemHolder.ownerTextView = (TextView) convertView.findViewById(R.id.file_owner);
                itemHolder.sizeTextView = (TextView) convertView.findViewById(R.id.file_size);
                itemHolder.dateTextView = (TextView) convertView.findViewById(R.id.date);
                itemHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
                itemHolder.progressLabelTextView = (TextView) convertView.findViewById(R.id.progress_label);
                itemHolder.deleteLayout = (RelativeLayout) convertView.findViewById(R.id.delete);
                itemHolder.deleteImageView = (ImageView) convertView.findViewById(R.id.deleteImageView);
                convertView.setTag(itemHolder);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.title_layout, null);
                titleHolder.titleTextView = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(titleHolder);
            }
        } else {
            if (!item.isTitle()) {
                itemHolder = (ItemViewHolder) convertView.getTag();
            } else {
                titleHolder = (TitleViewHolder) convertView.getTag();
            }
        }
        if (!item.isTitle()) {
            DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(15)).build();
            ImageLoader.getInstance().displayImage("drawable://" + getCoverId(GlobalParams.ShareType.getType(item.getFileType())),
                    itemHolder.imageView, options);
            String name = item.getFromPath().substring(item.getFromPath().lastIndexOf("/") + 1);
            itemHolder.nameTextView.setText(name);
            itemHolder.ownerTextView.setText(item.getFromUserName());
            String info = Formatter.formatFileSize(mContext, item.getTotalSize());
            itemHolder.sizeTextView.setText(info);
            itemHolder.dateTextView.setText(item.getStartTime());

            int resID = mSelectedList.contains(item.getUUID()) ? R.mipmap.select : R.mipmap.unselect;
            itemHolder.deleteImageView.setImageDrawable(mContext.getDrawable(resID));

            final ImageView imageView = itemHolder.deleteImageView;
            itemHolder.deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedList.contains(item.getUUID())) {
                        mSelectedList.remove(item.getUUID());
                        Log.d("cccc", "remove: " + item.getUUID());
                        imageView.setImageDrawable(mContext.getDrawable(R.mipmap.unselect));
                        mListener.onHasSelected(mSelectedList.size() > 0);
                    } else {
                        Log.d("cccc", "add: " + item.getUUID());
                        mSelectedList.add(item.getUUID());
                        imageView.setImageDrawable(mContext.getDrawable(R.mipmap.select));
                        mListener.onHasSelected(true);
                    }
                }
            });

            if (mShowDeleteIcon) {
                itemHolder.sizeTextView.setVisibility(View.GONE);
                itemHolder.dateTextView.setVisibility(View.GONE);
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.deleteLayout.setVisibility(View.VISIBLE);
            } else {
                itemHolder.sizeTextView.setVisibility(View.VISIBLE);
                itemHolder.dateTextView.setVisibility(View.VISIBLE);
                itemHolder.progressBar.setVisibility(View.VISIBLE);
                itemHolder.deleteLayout.setVisibility(View.GONE);
            }

            if (item.getStatus() != DownloadStatus.SUCCESSED) {
                int progress = (int) (item.getRecvSize() * 100 / item.getTotalSize());
                itemHolder.progressBar.setProgress(progress);
                itemHolder.progressLabelTextView.setText(progress + "%");
                itemHolder.progressBar.setVisibility(View.VISIBLE);
                itemHolder.progressLabelTextView.setVisibility(View.VISIBLE);
            } else {
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.progressLabelTextView.setVisibility(View.GONE);
            }

        } else {
            if (mFlag == CommonUtil.Flag.DATE) {
                titleHolder.titleTextView.setText(item.getDate());
            } else if (mFlag == CommonUtil.Flag.OWNER) {
                titleHolder.titleTextView.setText(item.getFromUserName());
            } else if (mFlag == CommonUtil.Flag.TYPE) {
                titleHolder.titleTextView.setText(getTypeDesc(item.getFileType()));
            }
        }
        return convertView;
    }

    private int getCoverId(GlobalParams.ShareType shareType) {
        int id = R.mipmap.file;
        if (shareType == GlobalParams.ShareType.IMAGE) {
            id = R.mipmap.image;
        } else if (shareType == GlobalParams.ShareType.AUDIO) {
            id = R.mipmap.mp3;
        } else if (shareType == GlobalParams.ShareType.VIDEO) {
            id = R.mipmap.video;
        }
        return id;
    }

    private CommonUtil.Flag mFlag;

    public CommonUtil.Flag getFlag() {
        return mFlag;
    }

    public void setGroup(final CommonUtil.Flag flag) {
        mFlag = flag;
        Iterator<ItemImpl> iterator = mDataList.iterator();
        //删除之前添加的标题项
        while (iterator.hasNext()) {
            if (iterator.next().isTitle()) {
                iterator.remove();
            }
        }
        if (mFlag == CommonUtil.Flag.NONE) {
            notifyDataSetChanged();
            return;
        }
        //重新排序，排序的目的是为了分组
        Collections.sort(mDataList, new Comparator<ItemImpl>() {
            @Override
            public int compare(ItemImpl o1, ItemImpl o2) {
                if (flag == CommonUtil.Flag.TYPE) {
                    return o1.getFileType().compareTo(o2.getFileType());
                } else if (flag == CommonUtil.Flag.DATE) {
                    return o1.getDate().compareTo(o2.getDate());
                } else {
                    return o1.getFromUserName().compareTo(o2.getFromUserName());
                }
            }
        });

        //每个分组添加一个标题
        for (int i = 0; i < mDataList.size(); ) {
            if (i == mDataList.size() - 1) {
                break;
            }
            ItemImpl item = mDataList.get(i);
            ItemImpl next = mDataList.get(i + 1);
            if (flag == CommonUtil.Flag.TYPE) {
                if (!item.getFileType().equals(next.getFileType())) {
                    ItemImpl tmp = ItemImpl.get(next);
                    tmp.setIsTitle(true);
                    mDataList.add(i + 1, tmp);
                    i += 2;
                } else {
                    i++;
                }
            } else if (flag == CommonUtil.Flag.DATE) {
                if (!item.getDate().equals(next.getDate())) {
                    ItemImpl tmp = ItemImpl.get(next);
                    tmp.setIsTitle(true);
                    mDataList.add(i + 1, tmp);
                    i += 2;
                } else {
                    i++;
                }
            } else if (flag == CommonUtil.Flag.OWNER) {
                if (!item.getFromUserName().equals(next.getFromUserName())) {
                    ItemImpl tmp = ItemImpl.get(next);
                    tmp.setIsTitle(true);
                    mDataList.add(i + 1, tmp);
                    i += 2;
                } else {
                    i++;
                }
            }
        }
        ItemImpl firstTitle = ItemImpl.get(mDataList.get(0));
        firstTitle.setIsTitle(true);
        mDataList.add(0, firstTitle);
        notifyDataSetChanged();
    }

    private class ItemViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView ownerTextView;
        TextView dateTextView;
        TextView sizeTextView;
        ProgressBar progressBar;
        TextView progressLabelTextView;
        RelativeLayout deleteLayout;
        ImageView deleteImageView;
    }

    private class TitleViewHolder {
        TextView titleTextView;
    }


    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).isTitle() ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private String getTypeDesc(String type) {
        if (type.toUpperCase().equals("IMAGE")) {
            return "图片";
        } else if (type.toUpperCase().equals("AUDIO")) {
            return "音乐";
        } else if (type.toUpperCase().equals("FILE")) {
            return "文件";
        } else if (type.toUpperCase().equals("VIDEO")) {
            return "视频";
        } else {
            return "其他";
        }
    }

    public interface OnSelectListener {
        void onHasSelected(boolean selected);
    }

//    public enum Flag {
//        NONE,//0
//        TYPE,//1
//        DATE,//2
//        OWNER//3
//    }

    public static class ItemImpl extends DownloadItem {

        private boolean mIsTitle = false;

        public ItemImpl() {
            mIsTitle = false;
        }

        public static ItemImpl get(DownloadItem item) {
            ItemImpl impl = new ItemImpl();
            impl.setStatus(item.getStatus());
            impl.setStartTime(item.getStartTime());
            impl.setRecvSize(item.getRecvSize());
            impl.setEndTime(item.getEndTime());
            impl.setFileType(item.getFileType());
            impl.setFromPath(item.getFromPath());
            impl.setFromUserName(item.getFromUserName());
            impl.setIP(item.getFromIP());
            impl.setUUID(item.getUUID());
            impl.setToPath(item.getToPath());
            impl.setTotalSize(item.getTotalSize());
            return impl;
        }

        public boolean isTitle() {
            return mIsTitle;
        }

        public void setIsTitle(boolean isTitle) {
            mIsTitle = isTitle;
        }

        public String getDate() {
            String str = getStartTime();
            return str.substring(0, 10);
        }
    }
}
