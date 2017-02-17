package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.ui.adapter.DownloadListAdapter;
import com.huhu.fileshare.util.EventBusType;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DownloadListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DownloadListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadListFragment extends BaseFragment {
    private ListView mListView;

    private DownloadListAdapter mAdapter;

    public static DownloadListFragment newInstance(String param1, String param2) {
        DownloadListFragment fragment = new DownloadListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DownloadListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_list, container, false);
        mListView = (ListView)view.findViewById(R.id.listview);
        View empty = view.findViewById(R.id.emptyview);
        mListView.setEmptyView(empty);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        setData();
    }

    private void setData(){
        List<DownloadItem> list = ShareApplication.getInstance().getWaitToDownloadingFiles();
        mAdapter.setData(list);
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info) {
        setData();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new DownloadListAdapter(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
