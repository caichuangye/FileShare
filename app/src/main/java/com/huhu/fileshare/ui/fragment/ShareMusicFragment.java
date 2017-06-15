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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
            mIP = getArguments().getString(IP);
        }
        mAdapter = new MusicAdapter(mContext, mType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_mediafiles, container, false);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);
        initEmptyView(view, "音乐");
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    public void onEventMainThread(EventBusType.QueryFiles info) {
        if(info.index == 1) {
            if (mType == GlobalParams.LOCAL_MODE) {
                FileQueryHelper.getInstance().scanFileByType(GlobalParams.ShareType.AUDIO);
            } else {
                setData();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void onEventMainThread(EventBusType.ShareMusicInfo info) {
        onQueryComplete();
        mAdapter.setData(info.getData());
    }

    public void onEventMainThread(EventBusType.ClearShared info) {
        mAdapter.updateSelectFiles();
    }

    public void onEventMainThread(EventBusType.UpdateSharedFiles info) {
        if (mType == GlobalParams.SERVER_MODE) {
            setData();
        }
    }

    public void onEventMainThread(EventBusType.NoLocalFiles info) {
        if(info.getType() == GlobalParams.ShareType.AUDIO && mType == GlobalParams.LOCAL_MODE) {
            onQueryComplete();
            mAdapter.setData(null);
        }
    }

    public void onEventMainThread(EventBusType.ResetDownloadStatus info) {
        if (mType == GlobalParams.SERVER_MODE) {
            List<String> list = info.map.get(GlobalParams.ShareType.AUDIO);
            if(list != null && list.size() > 0){
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info) {
        if (mType == GlobalParams.SERVER_MODE && (info.getOper() == GlobalParams.DownloadOper.UPDATE_END
                || info.getOper() == GlobalParams.DownloadOper.UPDATE_START
                || info.getOper() == GlobalParams.DownloadOper.ADD)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setData() {
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        onQueryComplete();
        if (collection != null && mAdapter != null) {
            List<MusicItem> list = collection.getMusicList();
            mAdapter.setData(list);
        }
    }

}
