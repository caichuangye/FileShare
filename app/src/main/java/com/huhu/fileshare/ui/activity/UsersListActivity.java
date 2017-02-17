package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.ui.adapter.DevicesAdapter;
import com.huhu.fileshare.util.ComClient;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.WiFiOperation;

public class UsersListActivity extends BaseActivity {

    private ListView mListView;

    private DevicesAdapter mAdapter;

    private String mUserName;

    private String mSelectedIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        initToolbar(WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiSSID(),null);

        View empty = findViewById(R.id.progressBar);
        mListView = (ListView)findViewById(R.id.devices_listview);
        mListView.setEmptyView(empty);
        mAdapter = new DevicesAdapter(getApplicationContext());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUserName = mAdapter.getName(position);
                if (mAdapter.hasShared(position)) {
                    mSelectedIP = mAdapter.getIP(position);
                    ShareApplication.getInstance().setServerInfo(mSelectedIP,mUserName);
                    Intent intent = new Intent(UsersListActivity.this, ScanSharedFilesActivity.class);
                    intent.putExtra("USER_NAME", mUserName);
                    intent.putExtra("IP", mSelectedIP);
                    startActivity(intent);
                    ComClient.getInstance(mSelectedIP).sendMessage(GlobalParams.REQUEST_SHARED_FILES);
                } else {
                    mSelectedIP = null;
                    Toast.makeText(UsersListActivity.this, mUserName + "无共享文件", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void initToolbar(String title, String subtitle){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        mToolbar.setSubtitle(subtitle);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onEventMainThread(EventBusType.OnlineDevicesInfo info){
        mAdapter.setData(info.getData());
    }
}
