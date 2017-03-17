package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;

import com.huhu.fileshare.R;
import com.huhu.fileshare.model.BaseItem;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.ui.adapter.ServerViewPagerAdapter;
import com.huhu.fileshare.ui.view.PagerSlidingTabStrip;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.HLog;

import java.util.ArrayList;
import java.util.List;

public class BrowserServerFilesActivity extends BaseActivity {

    private String mIP;
    private String mOwner;

    private ServerViewPagerAdapter mAdapter;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mTabs;

    private List<DownloadItem> mSelectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_shared);
        int index = 0;
        if (getIntent() != null) {
            mIP = getIntent().getStringExtra("IP");
            mOwner = getIntent().getStringExtra("USER_NAME");
            index = getIntent().getIntExtra("INDEX", 0);
            initToolbar(mOwner, mIP);
        } else {
            initToolbar(null, null);
        }

        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.share_viewpager);
        mAdapter = new ServerViewPagerAdapter(getSupportFragmentManager(), mIP);
        mViewPager.setAdapter(mAdapter);
        mTabs.setViewPager(mViewPager);
        mViewPager.setCurrentItem(index);
    }

    @Override
    public void initToolbar(String title, String subtitle) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
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
                startActivity(new Intent(BrowserServerFilesActivity.this, DownloadActivity.class));
                break;
        }
        return true;
    }

    public void onEventMainThread(EventBusType.StartViewAction info){
        HLog.d("ccstatus","start view: "+info.path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + info.path);
        String ext = info.path.substring(info.path.lastIndexOf('.') + 1);
        intent.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        startActivity(intent);
    }

    public void onEventMainThread(EventBusType.GoToDownloadActivity info) {
        startActivity(new Intent(this,DownloadActivity.class));
    }

    public void onEventMainThread(EventBusType.SharedFileInfo info) {
        BaseItem item0 = (BaseItem) info.getData();
        if (mSelectedList == null) {
            mSelectedList = new ArrayList<>();
        }
        if (info.isAdd()) {
            DownloadItem item = new DownloadItem();
            item.setUUID(CommonUtil.getUUID());
            item.setStatus(DownloadStatus.WAIT);
            item.setFileType(info.getType().toString());
            item.setFromPath(item0.getPath());
            item.setTotalSize(item0.getSize());
            item.setFromUserName(mOwner);
            item.setIP(mIP);
            mSelectedList.add(item);
        } else {
            for (DownloadItem item : mSelectedList) {
                if (item.getFromIP().equals(mIP) && item.getFromPath().equals(item0.getPath())) {
                    mSelectedList.remove(item);
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mSelectedList != null) {
            mSelectedList.clear();
        }
        finish();
    }
}
