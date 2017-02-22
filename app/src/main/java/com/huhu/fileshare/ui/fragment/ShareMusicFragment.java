package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.ui.adapter.MusicAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.util.List;


public class ShareMusicFragment extends MediaFragment {

    private MusicAdapter mAdapter;

    public static ShareMusicFragment newInstance(int type, String data) {
        ShareMusicFragment fragment = new ShareMusicFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(IP, data);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareMusicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
            mIP = getArguments().getString(IP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share_mediafiles, container, false);
        mListView = (ListView)view.findViewById(R.id.listview);
        mAdapter = new MusicAdapter(mContext,mType);
        mListView.setAdapter(mAdapter);
        if(mType == GlobalParams.SHOW_MODE) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAdapter.handleClick(position);
                }
            });
        }

       initEmptyView(view,"音乐");

        if(mType == GlobalParams.SHOW_MODE) {
            FileQueryHelper.getInstance(mContext).scanFileByType(GlobalParams.ShareType.AUDIO);
        }else {
            setData();
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void onEventMainThread(EventBusType.ShareMusicInfo info){
        HLog.d("SHARECCY", "--------------:got data (GlobalParams.ShareType.AUDIO) ");
        getData();
        mAdapter.setData(info.getData());
    }

    public void onEventMainThread(EventBusType.ClearShared info){
        mAdapter.updateSelectFiles();
    }

    public void onEventMainThread(EventBusType.UpdateSharedFiles info){
        if(mType == GlobalParams.SCAN_MODE) {
            setData();
        }
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info){
        if(mType == GlobalParams.SCAN_MODE && (info.getOper() == GlobalParams.DownloadOper.UPDATE_END
                || info.getOper() == GlobalParams.DownloadOper.UPDATE_START
                || info.getOper() == GlobalParams.DownloadOper.ADD)) {
            Log.d("ooo","just change tag");
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setData(){
        HLog.d("RECCY", "---------------------in music fragment,  to update----------------------");
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        getData();
        if(collection != null && mAdapter != null){
            HLog.d("RECCY", "---------------------real to update----------------------");
            List<MusicItem> list = collection.getMusicList();
            mAdapter.setData(list);
        }
    }

}
