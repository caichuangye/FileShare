package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.ui.adapter.FileAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.ScanFiles;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShareSDCardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShareSDCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareSDCardFragment extends BaseFragment {

    private  final String TAG = this.getClass().getCanonicalName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ListView mListView;

    private FileAdapter mAdapter;

//    private Context mContext;

    private TextView mPathTextView;

    private TextView mReturnTextView;

    private RelativeLayout mLayout;

    private ProgressBar mProgressBar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShareSDCardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShareSDCardFragment newInstance(String param1, String param2) {
        ShareSDCardFragment fragment = new ShareSDCardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareSDCardFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_share_file, container, false);
        mLayout = (RelativeLayout)view.findViewById(R.id.share_file_layout);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        mPathTextView = (TextView)view.findViewById(R.id.cur_path);
        mReturnTextView = (TextView)view.findViewById(R.id.return_back);
        mListView = (ListView)view.findViewById(R.id.listview);
        mAdapter = new FileAdapter(mContext);
        mListView.setAdapter(mAdapter);
        mPathTextView.setText(FileAdapter.ROOT_PATH);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = mAdapter.handleClick(position);
                if (!TextUtils.isEmpty(str)) {
                    mPathTextView.setText(str);
                    mReturnTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        mReturnTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = mAdapter.getParentFolderPath();
                HLog.d(TAG, "parent path = " + str);
                if (!TextUtils.isEmpty(str)) {
                    mLayout.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    ScanFiles.getInstance().scan(str);
                    mPathTextView.setText(str);
                    if (str.equals(FileAdapter.ROOT_PATH)) {
                        mReturnTextView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        mReturnTextView.setVisibility(View.INVISIBLE);
        mLayout.setVisibility(View.GONE);
    //    ScanFiles.getInstance().scan(FileAdapter.ROOT_PATH);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        HLog.d(TAG,"---onResume, to scanFile---");
        ScanFiles.getInstance().scan(FileAdapter.ROOT_PATH);
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
    //    mContext = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
         //   throw new ClassCastException(activity.toString()
         //           + " must implement OnFragmentInteractionListener");
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

    public void onEventMainThread(EventBusType.FileItemsInfo info) {
//        List<String> list = ((ShareApplication)((Activity)(mContext)).getApplication()).getSharedFileByType(GlobalParams.ShareType.SD_FILE);
//                mLayout.setVisibility(View.VISIBLE);
//        mProgressBar.setVisibility(View.GONE);
//        mAdapter.setData(info.getData(),list);
    }

    public void onEventMainThread(EventBusType.ClearShared info){
        mAdapter.updateSelectFiles();
    }

}
