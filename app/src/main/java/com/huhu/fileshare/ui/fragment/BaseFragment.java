package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.huhu.fileshare.de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/4/10.
 */
public class BaseFragment extends Fragment {

    protected Context mContext;

    protected int mType;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mContext = activity;
    }
}
