package com.huhu.fileshare.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.ApkItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.ui.adapter.ApkAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.ImageCacher;

import java.util.List;


public class ShareApkFragment extends MediaFragment {

    private ApkAdapter mAdapter;

    public static ShareApkFragment newInstance(int type, String data) {
        ShareApkFragment fragment = new ShareApkFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(IP, data);
        fragment.setArguments(args);
        return fragment;
    }


    public ShareApkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
            mIP = getArguments().getString(IP);
        }
        mAdapter = new ApkAdapter(mContext, mType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share_mediafiles, container, false);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);

        initEmptyView(view, "应用");


        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void onEventMainThread(EventBusType.QueryFiles info) {
        if(info.index == 3) {
            if (mType == GlobalParams.LOCAL_MODE) {
                FileQueryHelper.getInstance().scanFileByType(GlobalParams.ShareType.APK);
            } else {
                setData();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void onEventMainThread(EventBusType.ShareApkInfo info) {
        onQueryComplete();
        mAdapter.setData(info.getData());
    }


    public void onEventMainThread(EventBusType.ClearShared info) {
        mAdapter.updateSelectFiles();
    }

    public void onEventMainThread(EventBusType.CacheImageComplete info) {
        if (info.result.type == ImageCacher.Type.APK) {
            mAdapter.updateCover(info.result.filePath, info.result.coverPath);
        }
    }

    public void onEventMainThread(EventBusType.UpdateSharedFiles info) {
        if (mType == GlobalParams.SERVER_MODE) {
            setData();
        }
    }

    public void onEventMainThread(EventBusType.ResetDownloadStatus info) {
        if (mType == GlobalParams.SERVER_MODE) {
            List<String> list = info.map.get(GlobalParams.ShareType.APK);
            if(list != null && list.size() > 0){
               mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info) {
        HLog.d("ccrd","oper = "+info.getOper().toString());
        if (mType == GlobalParams.SERVER_MODE && (info.getOper() == GlobalParams.DownloadOper.UPDATE_END
                || info.getOper() == GlobalParams.DownloadOper.UPDATE_START
                || info.getOper() == GlobalParams.DownloadOper.ADD)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新目标机器上共享的apk文件
     */
    private void setData() {
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        onQueryComplete();
        if (collection != null && mAdapter != null) {
            List<ApkItem> list = collection.getApkList();
            mAdapter.setData(list);
        }
    }

}
