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
import com.huhu.fileshare.ui.adapter.MusicAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.lang.reflect.Type;
import java.util.List;


public class ShareMusicFragment extends BaseFragment {

    private static final String TYPE = "type";
    private static final String IP = "ip";

    private String mIP;

    private OnFragmentInteractionListener mListener;

    private ListView mListView;

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
        View view = inflater.inflate(R.layout.fragment_share_music, container, false);
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
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        if(mType == GlobalParams.SHOW_MODE) {
            ProgressBar progressBar = new ProgressBar(mContext);
            ViewGroup parent = (ViewGroup)mListView.getParent();
            parent.addView(progressBar,params);
            mListView.setEmptyView(progressBar);
            FileQueryHelper.getInstance(mContext).scanFileByType(GlobalParams.ShareType.AUDIO);
        }else {
            TextView textView = new TextView(mContext);
            textView.setTextColor(mContext.getResources().getColor(R.color.black_57));
            textView.setText("暂无共享的音乐");
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
      //      throw new ClassCastException(activity.toString()
       //             + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
         void onFragmentInteraction(Uri uri);
    }

    public void onEventMainThread(EventBusType.ShareMusicInfo info){
        HLog.d("SHARECCY", "--------------:got data (GlobalParams.ShareType.AUDIO) ");
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
        HLog.d("RECCY", "---------------------in music fragment,  to update----------------------");
        SharedCollection collection = ShareApplication.getInstance().getDestAllSharedFiles(mIP);
        if(collection != null && mAdapter != null){
            HLog.d("RECCY", "---------------------real to update----------------------");
            List<MusicItem> list = collection.getMusicList();
            mAdapter.setData(list);
        }
    }

}
