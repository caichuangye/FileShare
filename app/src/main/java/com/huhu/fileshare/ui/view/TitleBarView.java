package com.huhu.fileshare.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.huhu.fileshare.R;

/**
 * Created by Administrator on 2016/4/23.
 */
public class TitleBarView extends View {
    public TitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflate.inflate(R.layout.title_bar_layout, null);
    }
}
