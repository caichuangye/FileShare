package com.huhu.fileshare.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.huhu.fileshare.com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huhu.fileshare.model.WiFiItem;
import com.huhu.fileshare.ui.adapter.WiFiAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.WiFiOperation;

import java.util.List;

public class JoinWiFiActivity extends BaseActivity implements WiFiOperation.IOnWiFiListScanListener {

    private String TAG = JoinWiFiActivity.class.getSimpleName();

    private static final int ANIM_DURATION = 1500;

    private static final int JOIN_WIFI_TIMEOUT = 5000;

    private int LABEL_HEIGHT = 150;

    private String mJoiningSSID;

    private Handler mMainHandler;

    private WiFiAdapter mAdapter;

    private PullToRefreshListView mPullToRefreshListView;

    private ListView mListView;

    private RelativeLayout mJoiningLayout;

    private ProgressBar mProgressBar;

    private TextView mJoiningTextView;

    private WiFiOperation mWiFiOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_wi_fi);

        initToolbar("无线网络",null);
        mMainHandler = new Handler(Looper.getMainLooper());
        mWiFiOperation = WiFiOperation.getInstance(getApplicationContext());
        mWiFiOperation.setListener(this);
        mAdapter = new WiFiAdapter(getApplicationContext());
        mPullToRefreshListView = (PullToRefreshListView)findViewById(R.id.find_listview);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWiFiOperation.scanWiFi();
            }
        });
        mJoiningLayout = (RelativeLayout)findViewById(R.id.joining_label);
        mJoiningTextView = (TextView)findViewById(R.id.joining_textview);
        mProgressBar = (ProgressBar)findViewById(R.id.progressbar);
        RelativeLayout emptyView = (RelativeLayout)findViewById(R.id.emptyview);
        TextView setWiFiTextView = (TextView)emptyView.findViewById(R.id.set_wifi);
        setWiFiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                startActivity(intent);
            }
        });
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setEmptyView(emptyView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String bssid = WiFiOperation.getInstance(getApplicationContext()).getConnectedWiFiBSSID();
                if (!TextUtils.isEmpty(bssid) && bssid.equals(mAdapter.getBSSID(position - 1))) {
                    setJoiningViewVisible(null);
                    disappearJoiningView("即将返回上一页");
                    finishDelay(ANIM_DURATION);
                } else {
                    showInputPasswordDialog(mAdapter.getSSID(position - 1),bssid);
                }
            }
        });
        mWiFiOperation.scanWiFi();
    }

    @Override
    public void initToolbar(String title, String subtitle){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void showInputPasswordDialog(final String ssid,final String bssid){
        mJoiningSSID = ssid;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.input_pwd_layout,null);
        final EditText editText = (EditText)view.findViewById(R.id.pwd);
        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(ssid);
        editText.setHint("请输入密码");
        editText.setText("chuangqianmingyueguang");
        builder.setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pwd = editText.getText().toString();
                        if(TextUtils.isEmpty(pwd.trim())){
                            Toast.makeText(JoinWiFiActivity.this,"请输入密码", Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d(TAG,"begin to join: "+ssid);
                            setJoiningViewVisible("正在加入: "+ssid);
                            mWiFiOperation.joinWiFi(ssid, pwd, 1);
                            checkConnectDelay(JOIN_WIFI_TIMEOUT);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onChanged(List<WiFiItem> list) {
        mPullToRefreshListView.onRefreshComplete();
        mAdapter.setData(list);
    }

    public void onEventMainThread(EventBusType.ConnectInfo info){
        Log.d(TAG, info.toString()+": "+mJoiningSSID);
        String str = null;
        if(info.wifiAvailvle()){
            if(info.getStatus() == GlobalParams.WIFI_CONNECTED){
                if(info.getSSID().equals(mJoiningSSID)){
                    str = "已加入：" + mJoiningSSID;
                    Log.d(TAG,mJoiningSSID+": connected, after 500ms finish");
                    finishDelay(ANIM_DURATION);
                }
            }else if(!TextUtils.isEmpty(mJoiningSSID)){
                str = mJoiningSSID+": 连接失败";
            }
            Log.d(TAG,"set mJoiningSSID = null");
            mJoiningSSID = null;
            mWiFiOperation.scanWiFi();
        }else{
            Log.d(TAG,"wifi disabled");
            clearDelay(0);
        }
        disappearJoiningView(str);
    }

    private void finishDelay(int ms){
       mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"finishDelay");
                finish();
            }
        },ms);
    }

    private void clearDelay(int ms){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"clearDelay");
                mAdapter.clear();
            }
        },ms);
    }

    private void checkConnectDelay(int ms){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!TextUtils.isEmpty(mJoiningSSID)){
                    Log.d(TAG,"checkConnectDelay, mJoiningBSSID = "+mJoiningSSID+", conn failed");
                    disappearJoiningView(mJoiningSSID+": 连接超时");
                    mJoiningSSID = null;
                }else{
                    Log.d(TAG,"checkConnectDelay, mJoiningSSID == null, joined or failed");
                }
            }
        },ms);
    }


    private void disappearJoiningView(String str){
        if(mJoiningLayout.getVisibility() != View.VISIBLE){
            return;
        }
        if(!TextUtils.isEmpty(str)){
            mJoiningTextView.setText(str);
        }
        mProgressBar.setVisibility(View.GONE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(mJoiningLayout, "alpha", 1f, 0f);
        animator.setDuration(ANIM_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float)animation.getAnimatedValue();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mJoiningLayout.getLayoutParams();
                params.height = (int)(150*val);
                mJoiningLayout.setLayoutParams(params);
                if(val < 0.01f){
                    mJoiningLayout.setVisibility(View.GONE);
                }
            }
        });
        animator.start();
    }

    private void setJoiningViewVisible(String str){
        if(!TextUtils.isEmpty(str)){
            mJoiningTextView.setText(str);
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mJoiningLayout.setVisibility(View.VISIBLE);
        mJoiningLayout.setAlpha(1f);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mJoiningLayout.getLayoutParams();
        params.height = LABEL_HEIGHT;
        mJoiningLayout.setLayoutParams(params);
    }
}
