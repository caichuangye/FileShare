package com.huhu.fileshare.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huhu.fileshare.R;

/**
 * Created by Administrator on 2017/2/21.
 */

public class LoadingView extends View {

    private TextView mInfoTextView;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_layout,null);
        mInfoTextView = (TextView)view.findViewById(R.id.info);
    }

    public void setInfo(String info){
        mInfoTextView.setText(info);
    }
}
