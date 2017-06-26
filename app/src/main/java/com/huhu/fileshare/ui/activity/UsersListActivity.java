package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.DeviceItem;
import com.huhu.fileshare.ui.adapter.DevicesAdapter;
import com.huhu.fileshare.util.ComClient;
import com.huhu.fileshare.util.DevicesDetection;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.SystemSetting;
import com.huhu.fileshare.util.WiFiOperation;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RunnableFuture;

public class UsersListActivity extends BaseActivity {

    private ListView mListView;

    private DevicesAdapter mAdapter;

    private String mUserName;

    private String mSelectedIP;

    private RelativeLayout mLoadingView;

    private Handler mCheckHandler;

    private TextView mEmptyView;

    private boolean mHasDevices = false;

    private boolean mHasChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        initToolbar(WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiSSID(), null);
        mLoadingView = (RelativeLayout) findViewById(R.id.loading_view);
        mEmptyView = (TextView) findViewById(R.id.emptyview);
        mListView = (ListView) findViewById(R.id.devices_listview);
        mAdapter = new DevicesAdapter(getApplicationContext());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUserName = mAdapter.getName(position);
                if (mAdapter.hasShared(position)) {
                    mSelectedIP = mAdapter.getIP(position);
                    ShareApplication.getInstance().setServerInfo(mSelectedIP, mUserName);
                    Intent intent = new Intent(UsersListActivity.this, BrowserServerFilesActivity.class);
                    intent.putExtra("USER_NAME", mUserName);
                    intent.putExtra("IP", mSelectedIP);
                    intent.putExtra("INDEX", mAdapter.getFirstSharedFileIndex(position));
                    startActivity(intent);
                } else {
                    mSelectedIP = null;
                    Toast.makeText(UsersListActivity.this, mUserName + "无共享文件", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mLoadingView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        List<DeviceItem> list = DevicesDetection.getInstance(this).getDevices();
        if (list.size() > 0) {
            checkSelfDevice(list);
        }
        initCheckHandler();
    }

    private void initCheckHandler() {
        HandlerThread handlerThread = new HandlerThread("check_net_available");
        handlerThread.start();
        mCheckHandler = new Handler(handlerThread.getLooper());
        mCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHasChecked = true;
                if (!mHasDevices) {
                    Toast.makeText(getBaseContext(), "当前网络不可用！", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if (mAdapter.getCount() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingView.setVisibility(View.GONE);
                                mEmptyView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }
        }, GlobalParams.CHECK_NET_AVAILABLE_INTERNAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void initToolbar(String title, String subtitle) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        mToolbar.setSubtitle(subtitle);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onEventMainThread(EventBusType.OnlineDevicesInfo info) {
        checkSelfDevice(info.getData());
    }

    private void checkSelfDevice(List<DeviceItem> list) {

        mHasDevices = list != null && list.size() > 0;

        if (!SystemSetting.getInstance(ShareApplication.getInstance()).getShowSelf()) {
            Iterator<DeviceItem> itemIterator = list.iterator();
            String selfIP = WiFiOperation.getInstance(ShareApplication.getInstance()).getIP();
            while (itemIterator.hasNext()) {
                DeviceItem deviceItem = itemIterator.next();
                if (selfIP.equals(deviceItem.getIP())) {
                    itemIterator.remove();
                    break;
                }
            }
        }
        if (list != null && list.size() > 0) {
            HLog.d(getClass(), HLog.P, "has devices");
            mLoadingView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }else if(mHasChecked){
            mEmptyView.setVisibility(View.VISIBLE);
        }
        mAdapter.setData(list);
    }

    public void onEventMainThread(EventBusType.UpdateUserIcon info) {
        mAdapter.notifyDataSetChanged();
    }

    public void onEventMainThread(EventBusType.ConnectInfo info) {
        if (!info.wifiAvailvle()) {
            Toast.makeText(getBaseContext(), "当前无线网络已断开！", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
