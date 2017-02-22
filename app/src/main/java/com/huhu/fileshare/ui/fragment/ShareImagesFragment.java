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
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.ui.adapter.ImageAdapter;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShareImagesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShareImagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareImagesFragment extends BaseFragment {

    private static final String TYPE = "type";
    private static final String IP = "IP";

    private String mIP;

    private OnFragmentInteractionListener mListener;

    private GridView mGridView;

    private ImageAdapter mAdapter;

    public static ShareImagesFragment newInstance(int type, String data) {
        ShareImagesFragment fragment = new ShareImagesFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(IP, data);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareImagesFragment() {
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
        View view = inflater.inflate(R.layout.fragment_share_images, container, false);
        mGridView = (GridView)view.findViewById(R.id.gridview);
        mGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(),true,true));
        mAdapter = new ImageAdapter(mContext,mType);
        mGridView.setAdapter(mAdapter);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        if(mType == GlobalParams.SHOW_MODE) {
            ProgressBar progressBar = new ProgressBar(mContext);
            ViewGroup parent = (ViewGroup)mGridView.getParent();
            parent.addView(progressBar,params);
            mGridView.setEmptyView(progressBar);
            mAdapter.setData(CommonUtil.getImageItemsList(mContext, mIP));
        }else{
            TextView textView = new TextView(mContext);
            textView.setTextColor(mContext.getResources().getColor(R.color.black_57));
            textView.setText("暂无共享的图片");
            ViewGroup parent = (ViewGroup)mGridView.getParent();
            parent.addView(textView,params);
            mGridView.setEmptyView(textView);
            setData();
        }
        if(mType == GlobalParams.SHOW_MODE) {
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAdapter.handleClick(position);
                }
            });
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
        if(collection != null && mAdapter != null){
            List<ImageItem> list = collection.getImageList();
            mAdapter.setData(list);
        }
    }

}
