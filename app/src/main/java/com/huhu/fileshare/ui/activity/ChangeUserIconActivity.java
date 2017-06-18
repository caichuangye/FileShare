package com.huhu.fileshare.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.ui.adapter.ChangeUserIconAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.FileQueryHelper;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.SystemSetting;

import java.util.ArrayList;
import java.util.List;

public class ChangeUserIconActivity extends BaseActivity {

    private List<String> mDataList;

    private ChangeUserIconAdapter mAdapter;

    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_icon);
        initToolbar("更改头像",null);
        mAdapter = new ChangeUserIconAdapter(this);
        mGridView = (GridView)findViewById(R.id.icon_gridview);
        mGridView.setAdapter(mAdapter);
        List<ImageItem> list = FileQueryHelper.getInstance().getAllImages();
        if(list != null){
           setData(list);
        }else{
            FileQueryHelper.getInstance().scanFileByType(GlobalParams.ShareType.IMAGE);
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = mDataList.get(position);
                SystemSetting.getInstance(ChangeUserIconActivity.this).setUserIconPath(path);
                finish();
            }
        });
    }

    private void setData(List<ImageItem> list){
       mDataList = new ArrayList<>();
        for(ImageItem imageItem : list){
            mDataList.add(imageItem.getPath());
        }
        mAdapter.setData(mDataList);
    }

    public void onEventMainThread(EventBusType.AllImages info) {
        setData(info.getData());
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
