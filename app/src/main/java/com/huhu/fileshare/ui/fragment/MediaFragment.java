package com.huhu.fileshare.ui.fragment;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.util.GlobalParams;

/**
 * Created by Administrator on 2017/2/20.
 */

public class MediaFragment extends BaseFragment {

    public static final String TYPE = "type";
    public static final String IP = "ip";

    protected String mIP;

    protected ListView mListView;

    protected ProgressBar mProgressBar;

    public void initEmptyView(View root,String msg){
        mProgressBar = (ProgressBar)root.findViewById(R.id.progressBar);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        TextView textView = new TextView(mContext);
        textView.setTextColor(mContext.getResources().getColor(R.color.black_57));
        if(mType == GlobalParams.SHOW_MODE) {
            textView.setText("暂无"+msg);
        }else {
            textView.setText("暂无共享的"+msg);
        }
        ViewGroup parent = (ViewGroup)mListView.getParent();
        parent.addView(textView,params);
        mListView.setEmptyView(textView);
    }

    public void getData(){
        mProgressBar.setVisibility(View.GONE);
    }

}
