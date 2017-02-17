package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.BaseItem;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.ui.adapter.ScanSharedViewPagerAdapter;
import com.huhu.fileshare.ui.view.PagerSlidingTabStrip;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;

public class ScanSharedFilesActivity extends BaseActivity {

    private String mIP;
    private String mOwner;

    private ScanSharedViewPagerAdapter mAdapter;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mTabs;

    private List<DownloadItem> mSelectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_sdcard);
        if(getIntent() != null){
            mIP = getIntent().getStringExtra("IP");
            mOwner = getIntent().getStringExtra("USER_NAME");
            initToolbar(mOwner,mIP);
        }else {
            initToolbar(null,null);
        }

        mTabs = (PagerSlidingTabStrip)findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.share_viewpager);
        mAdapter = new ScanSharedViewPagerAdapter(getSupportFragmentManager(),mIP);
        mViewPager.setAdapter(mAdapter);
        mTabs.setViewPager(mViewPager);
    }

    @Override
    public void initToolbar(String title, String subtitle){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.scan_files_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.download:
                startActivity(new Intent(ScanSharedFilesActivity.this,DownloadActivity.class));
                break;
        }
        return true;
    }

    public void onEventMainThread(EventBusType.SharedFileInfo info){
            BaseItem item0 = (BaseItem)info.getData();
            if(mSelectedList == null){
                mSelectedList = new ArrayList<>();
            }
            if(info.isAdd()) {
                DownloadItem item = new DownloadItem();//info.getType(), item0.getPath(), item0.getSize(),mOwner,mIP);
                item.setUUID(CommonUtil.getUUID());
                item.setStatus(DownloadStatus.WAIT);
                item.setFileType(info.getType().toString());
                item.setFromPath(item0.getPath());
                item.setTotalSize(item0.getSize());
                item.setFromUserName(mOwner);
                item.setIP(mIP);
                mSelectedList.add(item);
                HLog.d("CCYSC","add: "+item0.getPath());
            }else{
                for(DownloadItem item : mSelectedList){
                    if(item.getFromIP().equals(mIP) && item.getFromPath().equals(item0.getPath())){
                        mSelectedList.remove(item);
                        break;
                    }
                }
                HLog.d("CCYSC", "delete: " + item0.getPath());
            }
    }

    @Override
    public void onBackPressed(){
        if(mSelectedList != null) {
            mSelectedList.clear();
        }
        finish();
    }
}
