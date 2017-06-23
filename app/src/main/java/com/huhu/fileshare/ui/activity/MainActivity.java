package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.download.ServiceUtils;
import com.huhu.fileshare.ui.fragment.MainFragment;
import com.huhu.fileshare.util.ComServer;
import com.huhu.fileshare.util.DevicesDetection;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.SystemSetting;
import com.huhu.fileshare.util.WiFiOperation;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {

    private DrawerLayout mDrawLayout;

    private long mLastPressBackTimeStamp;

    private boolean mHasPermissions = false;

    private TextView mUserNameTextView;

    private CircleImageView mUserIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        MainFragment mainFragment = new MainFragment();
        transaction.add(R.id.content_frame,mainFragment);
        transaction.commit();

        checkPermission();

    }

    private void init() {
        initToolbar(null,null);
        ServiceUtils.getInstance().connectDownloadService(getApplicationContext());
        DevicesDetection.getInstance(this).start();
        mDrawLayout = (DrawerLayout)findViewById(R.id.drawer);

        mUserNameTextView = (TextView)findViewById(R.id.username);
        mUserIconImageView = (CircleImageView)findViewById(R.id.user_icon);


        findViewById(R.id.user_icon).setOnClickListener(mMenuClickListener);
        findViewById(R.id.username).setOnClickListener(mMenuClickListener);
        findViewById(R.id.wifi_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.ap_name_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.ap_pwd_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.download_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.storagepath_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.checkversion_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.feedback_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.about_layout).setOnClickListener(mMenuClickListener);
        findViewById(R.id.quit_layout).setOnClickListener(mMenuClickListener);
    }

    private View.OnClickListener mMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String operation = null;
            switch (v.getId()){
                case R.id.user_icon:
                    startActivity(new Intent(MainActivity.this,UserIconActivity.class));
                    break;
                case R.id.username:
                    operation = SystemSetting.USER_NICKNAME;
                    break;
                case R.id.wifi_layout:
                    startActivity(new Intent(MainActivity.this,JoinWiFiActivity.class));
                    break;
                case R.id.download_layout:
                    startActivity(new Intent(MainActivity.this,DownloadActivity.class));
                    break;
                case R.id.storagepath_layout:
                    break;
                case R.id.ap_name_layout:
                    operation = SystemSetting.AP_NAME;
                    break;
                case R.id.ap_pwd_layout:
                    operation = SystemSetting.AP_PWD;
                    break;
                case R.id.checkversion_layout:
                    Toast.makeText(MainActivity.this,"已是最新版本",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.feedback_layout:
                    startActivity(new Intent(MainActivity.this,UserFeedbackActivity.class));
                    break;
                case R.id.about_layout:
                    startActivity(new Intent(MainActivity.this,AboutActivity.class));
                    break;
                case R.id.quit_layout:
                    exitApplication();
                    break;
            }
            if(!TextUtils.isEmpty(operation)){
                Intent intent = new Intent(MainActivity.this, EditSettingItemActivity.class);
                intent.putExtra("SETTING_ITEM", operation);
                startActivity(intent);
            }
        }
    };

    private void exitApplication(){
        ServiceUtils.getInstance().disConnected(getApplicationContext());
        DevicesDetection.getInstance(getApplicationContext()).stop();
        ComServer.getInstance().stop();
        Process.killProcess(Process.myPid());
    }

    @Override
    public void initToolbar(String title, String subtitle) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.mipmap.menu);
        mToolbar.setTitleTextColor(Color.parseColor("#82000000"));
        setSupportActionBar(mToolbar);
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


    @Override
    public void onResume() {
        super.onResume();
        if (mHasPermissions) {
            WiFiOperation.getInstance(getApplicationContext()).updateConnectionInfo();
        }
        mUserNameTextView.setText(SystemSetting.getInstance(this).getUserNickName());
        String path = SystemSetting.getInstance(this).getUserIconPath();
        if(!TextUtils.isEmpty(path)) {
            ImageLoader.getInstance().displayImage("file://" + path, mUserIconImageView);
        }else{
            mUserIconImageView.setImageResource(R.mipmap.default_icon);
        }
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
            case android.R.id.home:
                mDrawLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.create_ap:
                createAp();
                break;
        }
        return true;
    }

    private void createAp(){
        WiFiOperation.getInstance(this).closeWiFi();
        String name = SystemSetting.getInstance(this).getApName();
        String pwd = SystemSetting.getInstance(this).getApPwd();
        boolean res = WiFiOperation.getInstance(this).createAp(name,pwd,true);
        HLog.d(getClass(),HLog.L,"create ap res = "+res);
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - mLastPressBackTimeStamp < 2000) {
            exitApplication();
        } else {
            mLastPressBackTimeStamp = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
