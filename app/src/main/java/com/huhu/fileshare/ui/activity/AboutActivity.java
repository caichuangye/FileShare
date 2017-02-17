package com.huhu.fileshare.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.util.SystemSetting;

public class AboutActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initToolbar("关于",null);
        TextView versionTextView = (TextView)findViewById(R.id.version);
        versionTextView.setText(SystemSetting.getInstance(getApplicationContext()).getVersionName());
    }

    @Override
    public void initToolbar(String title, String subtitle){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        if(!TextUtils.isEmpty(subtitle)){
            mToolbar.setSubtitle(subtitle);
        }
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
