package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.ApkItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.ui.adapter.ApkAdapter;
import com.huhu.fileshare.ui.adapter.MusicAdapter;
import com.huhu.fileshare.util.ApkIconCacher;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;


public class ShareApkFragment extends MediaFragment {

    private ApkAdapter mAdapter;

    private List<ApkItem> mApkList;

    private Handler mWorkHandler;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share_mediafiles, container, false);
        mListView = (ListView)view.findViewById(R.id.listview);
        mAdapter = new ApkAdapter(mContext,mType);
        mListView.setAdapter(mAdapter);
        if(mType == GlobalParams.SHOW_MODE) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAdapter.handleClick(position);
                }
            });
        }

       initEmptyView(view,"应用");

        HandlerThread thread = new HandlerThread("getapp");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());
        mApkList = new ArrayList<>();
        if(mType == GlobalParams.SHOW_MODE) {
            queryAllApks();
        }else {
            setData();
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void queryAllApks(){
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                PackageManager packageManager = mContext.getPackageManager();
                List<ApplicationInfo> list = packageManager.getInstalledApplications(GET_UNINSTALLED_PACKAGES);
                if(list != null){
                    for(ApplicationInfo info : list){
                        String path = info.sourceDir;
                        if(!TextUtils.isEmpty(path) && path.startsWith("/data/app")){
                            String name = String.valueOf(info.loadLabel(packageManager));
                            try {
                                FileInputStream inputStream = new FileInputStream(path);
                                long size = inputStream.available();
                                PackageInfo pi = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
                                final ApkItem item = new ApkItem(name, path, size, false, null, pi.versionName);
                                ApkIconCacher.getInstance().cacheDrawable(path,info.loadIcon(packageManager));
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getData();
                                        mAdapter.addItem(item);
                                    }
                                });
                            }catch (Exception e){

                            }
                        }
                    }
                    ApkIconCacher.getInstance().exit();
                }
            }
        });
    }


    public void onEventMainThread(EventBusType.ClearShared info){
        mAdapter.updateSelectFiles();
    }

    public void onEventMainThread(EventBusType.CacheApkIconComplete info){
        HLog.d("ccdr",info.path+": "+info.coverPath);
        mAdapter.updateCover(info.path,info.coverPath);
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

    /**
     * 更新目标机器上共享的apk文件
     */
    private void setData(){
        HLog.d("RECCY", "---------------------in music fragment,  to update----------------------");
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        getData();
        if(collection != null && mAdapter != null){
            HLog.d("RECCY", "---------------------real to update----------------------");
            List<ApkItem> list = collection.getApkList();
            mAdapter.setData(list);
        }
    }

}
