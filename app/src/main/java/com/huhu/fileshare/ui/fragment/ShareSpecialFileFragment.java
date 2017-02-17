package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.model.SpecialFileItem;
import com.huhu.fileshare.model.VideoItem;
import com.huhu.fileshare.ui.adapter.SpecialFileAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.ScanSpecialFiles;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShareSpecialFileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShareSpecialFileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareSpecialFileFragment extends BaseFragment {
    private static final String TYPE = "type";
    private static final String IP = "ip";

    private String mIP;

    private OnFragmentInteractionListener mListener;

    private SpecialFileAdapter mAdapter;
    private ListView mListView;

    public static ShareSpecialFileFragment newInstance(int type, String data) {
        ShareSpecialFileFragment fragment = new ShareSpecialFileFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(IP, data);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareSpecialFileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_share_special_file, container, false);
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
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        if(mType == GlobalParams.SHOW_MODE) {
            ProgressBar progressBar = new ProgressBar(mContext);
            ViewGroup parent = (ViewGroup)mListView.getParent();
            parent.addView(progressBar,params);
            mListView.setEmptyView(progressBar);
            ScanSpecialFiles.getInstance(mContext).start();
        }else{
            TextView textView = new TextView(mContext);
            textView.setTextColor(mContext.getResources().getColor(R.color.black_57));
            textView.setText("暂无共享的文件");
            ViewGroup parent = (ViewGroup)mListView.getParent();
            parent.addView(textView,params);
            mListView.setEmptyView(textView);
            setData();
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
         void onFragmentInteraction(Uri uri);
    }

    public void onEventMainThread(EventBusType.ShareSpecialFileInfo info){
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
        if(mType == GlobalParams.SCAN_MODE && !info.isUpdateProgress()) {
            Log.d("ooo","just change tag");
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setData(){
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        if(collection != null && mAdapter != null){
            List<SpecialFileItem> list = collection.getSpecialFileList();
            mAdapter.setData(list);
        }
    }

}
