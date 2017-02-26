package com.huhu.fileshare.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.ui.adapter.MusicAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;

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
        mListView = (ListView) view.findViewById(R.id.listview);
        mAdapter = new MusicAdapter(mContext, mType);
        mListView.setAdapter(mAdapter);

        initEmptyView(view, "音乐");

        if (mType == GlobalParams.SHOW_MODE) {
            FileQueryHelper.getInstance(mContext).scanFileByType(GlobalParams.ShareType.AUDIO);
        } else {
            setData();
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void onEventMainThread(EventBusType.ShareMusicInfo info) {
        getData();
        mAdapter.setData(info.getData());
    }

    public void onEventMainThread(EventBusType.ClearShared info) {
        mAdapter.updateSelectFiles();
    }

    public void onEventMainThread(EventBusType.UpdateSharedFiles info) {
        if (mType == GlobalParams.SCAN_MODE) {
            setData();
        }
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info) {
        if (mType == GlobalParams.SCAN_MODE && (info.getOper() == GlobalParams.DownloadOper.UPDATE_END
                || info.getOper() == GlobalParams.DownloadOper.UPDATE_START
                || info.getOper() == GlobalParams.DownloadOper.ADD)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setData() {
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        getData();
        if (collection != null && mAdapter != null) {
            List<MusicItem> list = collection.getMusicList();
            mAdapter.setData(list);
        }
    }

}
