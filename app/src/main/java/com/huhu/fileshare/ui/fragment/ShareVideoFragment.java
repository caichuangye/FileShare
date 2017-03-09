package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.model.VideoItem;
import com.huhu.fileshare.ui.adapter.VideoAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.ImageCacher;

import java.util.List;


public class ShareVideoFragment extends MediaFragment {

    private VideoAdapter mAdapter;
    public static ShareVideoFragment newInstance(int type, String data) {
        ShareVideoFragment fragment = new ShareVideoFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(IP, data);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
            mIP = getArguments().getString(IP);
        }
        mAdapter = new VideoAdapter(mContext, mType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_mediafiles, container, false);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);

        initEmptyView(view, "视频");

        if (mType == GlobalParams.LOCAL_MODE && mAdapter.getCount() == 0) {
            FileQueryHelper.getInstance().scanFileByType(GlobalParams.ShareType.VIDEO);
        } else {
            setData();
        }
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void onEventMainThread(EventBusType.NoLocalFiles info) {
        HLog.d("ccload",info.getType().toString()+", fragment: "+mType);
        if(info.getType() == GlobalParams.ShareType.VIDEO && mType == GlobalParams.LOCAL_MODE) {
            HLog.d("ccload","no video");
            getData();
            mAdapter.setData(null);
        }
    }

    public void onEventMainThread(EventBusType.CacheImageComplete info){
        if(info.result.type == ImageCacher.Type.VIDEO) {
            mAdapter.updateCover(info.result.filePath, info.result.coverPath);
        }
    }

    public void onEventMainThread(EventBusType.ShareVideoInfo info){
        getData();
        VideoItem item = info.getData();
        ImageCacher.getInstance().cacheVideo(item.getPath(),150,150);
        mAdapter.addItem(item);
    }

    public void onEventMainThread(EventBusType.ClearShared info){
        mAdapter.updateSelectFiles();
    }

    public void onEventMainThread(EventBusType.UpdateSharedFiles info){
        if(mType == GlobalParams.SERVER_MODE) {
            setData();
        }
    }

    public void onEventMainThread(EventBusType.ResetDownloadStatus info) {
        if (mType == GlobalParams.SERVER_MODE) {
            List<String> list = info.map.get(GlobalParams.ShareType.VIDEO);
            if(list != null && list.size() > 0){
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info){
        if(mType == GlobalParams.SERVER_MODE && (info.getOper() == GlobalParams.DownloadOper.UPDATE_END
                || info.getOper() == GlobalParams.DownloadOper.UPDATE_START
                || info.getOper() == GlobalParams.DownloadOper.ADD)) {
            Log.d("ooo","just change tag");
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setData(){
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        getData();
        if(collection != null && mAdapter != null){
            List<VideoItem> list = collection.getVideoList();
            mAdapter.setData(list);
        }
    }

}
