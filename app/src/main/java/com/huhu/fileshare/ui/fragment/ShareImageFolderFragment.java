package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ui.activity.ShareImagesActivity;
import com.huhu.fileshare.ui.adapter.ImageFolderAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;


public class ShareImageFolderFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private GridView mGridView;

    private ImageFolderAdapter mAdapter;

    protected RelativeLayout mLoadingLayout;

    public static ShareImageFolderFragment newInstance(String param1, String param2) {
        ShareImageFolderFragment fragment = new ShareImageFolderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareImageFolderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_image_folder, container, false);
        mGridView = (GridView)view.findViewById(R.id.gridview);
        mAdapter = new ImageFolderAdapter(mContext);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String folder = mAdapter.getFolderPath(position);
                if(!TextUtils.isEmpty(folder)) {
                    HLog.d("SSSS", "pos = "+position+"; folder = " + folder);
                    Intent intent = new Intent(mContext, ShareImagesActivity.class);
                    intent.putExtra("FOLDER", folder);
                    mContext.startActivity(intent);
                }
            }
        });

        initEmptyView(view,"图片");

        FileQueryHelper.getInstance(mContext).scanFileByType(GlobalParams.ShareType.IMAGE);
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

    public void onEventMainThread(EventBusType.ShareImageFolderInfo info){
        getData();
        mAdapter.setData(info.getData());
    }

    public void initEmptyView(View root,String msg){
        mLoadingLayout = (RelativeLayout)root.findViewById(R.id.loading_view);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        TextView textView = new TextView(mContext);
        textView.setTextColor(mContext.getResources().getColor(R.color.black_57));
        if(mType == GlobalParams.SHOW_MODE) {
            textView.setText("暂无"+msg);
        }else {
            textView.setText("暂无共享的"+msg);
        }
        ViewGroup parent = (ViewGroup)mGridView.getParent();
        parent.addView(textView,params);
        mGridView.setEmptyView(textView);
        mGridView.setVisibility(View.GONE);
        mGridView.getEmptyView().setVisibility(View.GONE);
    }

    public void getData(){
        mLoadingLayout.setVisibility(View.GONE);
        mGridView.getEmptyView().setVisibility(View.VISIBLE  );
        mGridView.setVisibility(View.VISIBLE);
    }

}
