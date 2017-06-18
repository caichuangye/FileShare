package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.util.SystemSetting;

public class SettingActivity extends BaseActivity {

    private ImageView mIconImageView;
    private TextView mNameTextView;
    private TextView mStorageEditText;
    private TextView mVersionEditText;

    private RelativeLayout mUserIconLayout;
    private RelativeLayout mUserNameLayout;
    private RelativeLayout mVersionLayout;
    private RelativeLayout mFeedbackLayout;
    private RelativeLayout mAboutLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initToolbar("设置",null);

        mIconImageView = (ImageView)findViewById(R.id.user_icon);
        mNameTextView = (TextView)findViewById(R.id.user_nickname);
        mStorageEditText = (TextView)findViewById(R.id.storage_path);
        mVersionEditText = (TextView)findViewById(R.id.app_version);

        mUserIconLayout = (RelativeLayout)findViewById(R.id.usericon_layout);
        mUserNameLayout = (RelativeLayout)findViewById(R.id.username_layout);
        mVersionLayout = (RelativeLayout)findViewById(R.id.checkversion_layout);
        mFeedbackLayout = (RelativeLayout)findViewById(R.id.feedback_layout);
        mAboutLayout = (RelativeLayout)findViewById(R.id.about_layout);
        initClickEvent();

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

    private void initSettingValues(){
        int index = 1;//SystemSetting.getInstance(getApplicationContext()).getUserIconPath();
        mIconImageView.setImageResource(((ShareApplication)getApplicationContext()).getUserIconList()[index]);
        mNameTextView.setText(SystemSetting.getInstance(getApplicationContext()).getUserNickName());
        mStorageEditText.setText(SystemSetting.getInstance(getApplicationContext()).getStoragePath());
        mVersionEditText.setText(SystemSetting.getInstance(getApplicationContext()).getVersionName());
    }


    private void initClickEvent(){
        SettingItemClickListener listener = new SettingItemClickListener();
        mUserIconLayout.setOnClickListener(listener);
        mUserNameLayout.setOnClickListener(listener);
        mVersionLayout.setOnClickListener(listener);
        mFeedbackLayout.setOnClickListener(listener);
        mAboutLayout.setOnClickListener(listener);
    }

    @Override
    public void onResume(){
        super.onResume();
        initSettingValues();
    }

    private class SettingItemClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            String item = null;
            switch (v.getId()){
                case R.id.usericon_layout:
                    goActivity(UserIconActivity.class);
                    return;
                case R.id.username_layout:
                    item = SystemSetting.USER_NICKNAME;
                    break;
                case R.id.checkversion_layout:
                    checkNewVersion();
                    return;
                case R.id.feedback_layout:
                    goActivity(UserFeedbackActivity.class);
                    return;
                case R.id.about_layout:
                    goActivity(AboutActivity.class);
                    return;
            }
            goEditSettingItemActivity(item);
        }
    }

    private void goActivity(Class c){
        Intent intent = new Intent(this,c);
        startActivity(intent);
    }

    private void goEditSettingItemActivity(String item){
        Intent intent = new Intent(this, EditSettingItemActivity.class);
        intent.putExtra("SETTING_ITEM",item);
        this.startActivity(intent);

    }

    private void checkNewVersion(){
        Toast.makeText(this,"已是最新版本",Toast.LENGTH_SHORT).show();
    }
}
