package com.huhu.fileshare.ui.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.download.ServiceUtils;
import com.huhu.fileshare.util.DevicesDetection;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.WiFiOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class MainActivity extends BaseActivity {

    private long mLastPressBackTimeStamp;

    private TextView mWiFiTextView;


    private boolean mHasPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        checkPermission();

    }

    private void init() {
        initToolbar("文件分享", null);

        mWiFiTextView = (TextView) findViewById(R.id.wifi);

        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BrowserLocalFilesActivity.class);
                intent.putExtra("type", "mine");
                startActivity(intent);
            }
        });

        findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WiFiOperation.getInstance(getApplicationContext()).isWiFiConnected()) {
                    startActivity(new Intent(MainActivity.this, UsersListActivity.class));
                } else {
                    showJoinWiFiNotice();
                }
            }
        });

        ServiceUtils.getInstance().connectDownloadService(getApplicationContext());
        DevicesDetection.getInstance(this).start();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            mHasPermissions = true;
            init();
        } else {
            ArrayList<String> permissions = new ArrayList<>(Arrays.asList("android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.ACCESS_COARSE_LOCATION"));
            Iterator<String> iter = permissions.iterator();
            boolean hasAll = true;
            while (iter.hasNext()) {
                iter.next();
                if (ContextCompat.checkSelfPermission(MainActivity.this, iter.next()) == PackageManager.PERMISSION_GRANTED) {
                    iter.remove();
                }else{
                    hasAll = false;
                }
            }
            if(hasAll){
                mHasPermissions = true;
                init();
            }else if (permissions.size() > 0) {
                String[] requestList = new String[permissions.size()];
                for (int i = 0; i < permissions.size(); i++) {
                    requestList[i] = permissions.get(i);
                }
                ActivityCompat.requestPermissions(MainActivity.this, requestList, 0x34);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0x34 &&
                permissions != null &&
                grantResults != null) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    finish();
                }
            }
            mHasPermissions = true;
            init();
        }

    }


    public void onEventMainThread(EventBusType.ConnectInfo info) {
        if (info.wifiAvailvle()) {
            if (!TextUtils.isEmpty(info.getSSID())) {
                mWiFiTextView.setText("WiFi: " + info.getSSID());
            } else {
                mWiFiTextView.setText("WiFi未连接");
            }
        } else {
            mWiFiTextView.setText("WiFi已关闭");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHasPermissions) {
            boolean available = WiFiOperation.getInstance(getApplicationContext()).isWiFiAvailable();
            int status = WiFiOperation.getInstance(getApplicationContext()).isWiFiConnected() ?
                    GlobalParams.WIFI_CONNECTED : GlobalParams.WIFI_NOT_CONNECTED;
            String bssid = WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiBSSID();
            String ssid = WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiSSID();
            onEventMainThread(new EventBusType.ConnectInfo(available, status, bssid, ssid));
        }
    }

    @Override
    public void initToolbar(String title, String subtitle) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        mToolbar.setLogo(R.mipmap.ic_launcher);
        mToolbar.setLogoDescription(title);
        setSupportActionBar(mToolbar);
    }

    private void showJoinWiFiNotice() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        builder.setTitle(R.string.join_wifi_title);
        builder.setPositiveButton("确    定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(MainActivity.this, JoinWiFiActivity.class));
            }
        });
        builder.setNegativeButton("取    消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            case R.id.joinwifi:
                startActivity(new Intent(MainActivity.this, JoinWiFiActivity.class));
                break;
            case R.id.download:
                startActivity(new Intent(MainActivity.this, DownloadActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - mLastPressBackTimeStamp < 3000) {
            finish();
        } else {
            mLastPressBackTimeStamp = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ServiceUtils.getInstance().disConnected(getApplicationContext());
    }
}
