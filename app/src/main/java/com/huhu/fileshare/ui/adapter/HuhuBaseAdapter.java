package com.huhu.fileshare.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.BaseItem;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/12.
 */
public abstract class HuhuBaseAdapter<T extends BaseItem> extends BaseAdapter {

    protected Context mContext;

    protected List<T> mDataList;

    protected int mMode;

    public HuhuBaseAdapter(Context context,int mode){
        mMode = mode;
        mContext = context;
        mDataList = new ArrayList<>();
    }

    public  void setData(List<T> list){
        List<String> selectedList = ((ShareApplication)mContext.getApplicationContext()).
                getSharedFileByType(getSharedType());
        mDataList.clear();
        if(list != null){
            for(T t : list){
                if(selectedList != null && selectedList.contains(t.getPath())){
                    t.setSelected(true);
                }else{
                    t.setSelected(false);
                }
                mDataList.add(t);
            }
        }
        notifyDataSetChanged();
    }

    public final void updateSelectFiles(){
        List<String> list = ((ShareApplication)mContext.getApplicationContext()).
                getSharedFileByType(getSharedType());
        if(list == null){
            for(T t : mDataList){
                t.setSelected(false);
            }
        }else{
            for(T t : mDataList){
                if(list.contains(t.getPath())){
                    t.setSelected(true);
                }else{
                    t.setSelected(false);
                }
            }
        }
        notifyDataSetChanged();
    }

    public String handleClick(int pos){
        Object item = getItem(pos);
        boolean res = ((BaseItem)item).isSelected();
        ((BaseItem)item).setSelected(!res);
        notifyDataSetChanged();
        EventBus.getDefault().post(new EventBusType.SharedFileInfo(item, getSharedType(), !res));
        return null;
    }

    public abstract GlobalParams.ShareType getSharedType();

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
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public class DownLoadListener implements View.OnClickListener{

        public DownLoadListener(EventBusType.SharedFileInfo info){
            mInfo = info;
        }

        private EventBusType.SharedFileInfo mInfo;

        @Override
        public void onClick(View v){
            ShareApplication.getInstance().tellServerRequestFiles(mInfo);
        }

    }
}
