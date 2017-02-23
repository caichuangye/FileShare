package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.model.CommonFileItem;
import com.huhu.fileshare.ui.adapter.SpecialFileAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.ScanSpecialFiles;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShareCommonFileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareCommonFileFragment extends MediaFragment {

    private SpecialFileAdapter mAdapter;

    public static ShareCommonFileFragment newInstance(int type, String data) {
        ShareCommonFileFragment fragment = new ShareCommonFileFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(IP, data);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareCommonFileFragment() {
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
        mAdapter = new SpecialFileAdapter(mContext,mType);
        mListView.setAdapter(mAdapter);
        if(mType == GlobalParams.SHOW_MODE) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAdapter.handleClick(position);
                }
            });
        }

        initEmptyView(view,"文件");

        if(mType == GlobalParams.SHOW_MODE) {
            ScanSpecialFiles.getInstance(mContext).start();
        }else{
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


    public void onEventMainThread(EventBusType.ShareSpecialFileInfo info){
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
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        getData();
        if(collection != null && mAdapter != null){
            List<CommonFileItem> list = collection.getCommonFileList();
            mAdapter.setData(list);
        }
    }

}
