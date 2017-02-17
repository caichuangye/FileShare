package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.util.SystemSetting;

public class EditSettingItemActivity extends BaseActivity {

    private EditText mSettingInfo;
    private TextView mHintTextView;
    private String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_setting_item);

        mSettingInfo = (EditText)findViewById(R.id.setting_item);
        mHintTextView = (TextView)findViewById(R.id.setting_hint);

        initView();

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

    private void initView(){
        Intent intent = getIntent();
        String item = intent.getStringExtra("SETTING_ITEM");
        mType = item;
        String title = "";
        if(!TextUtils.isEmpty(item)){
            switch (item){
                case SystemSetting.USER_NICKNAME:
                    mSettingInfo.setText(SystemSetting.getInstance(this).getUserNickName());
                    mHintTextView.setText("请输入昵称");
                    title = getResources().getString(R.string.nick_name);
                    break;
                case SystemSetting.AP_NAME:
                    mSettingInfo.setText(SystemSetting.getInstance(this).getApName());
                    mHintTextView.setText("请无线热点名称");
                    title = getResources().getString(R.string.ap_name);
                    break;
                case SystemSetting.AP_PWD:
                    mSettingInfo.setText(SystemSetting.getInstance(this).getApPwd());
                    mHintTextView.setText("请无线热点密码");
                    title = getResources().getString(R.string.ap_pwd);
                    break;
            }
        }
        initToolbar(title,null);
        CharSequence text = mSettingInfo.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
        mSettingInfo.setFocusable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.edit_setting_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.save:
                saveSettingValues();
                finish();
                break;
        }
        return true;
    }

    private void saveSettingValues(){
        String values = mSettingInfo.getText().toString();
        switch (mType){
            case SystemSetting.USER_NICKNAME:
                SystemSetting.getInstance(this).setUserNickName(values);
                break;
            case SystemSetting.AP_NAME:
                SystemSetting.getInstance(this).setApName(values);
                break;
            case SystemSetting.AP_PWD:
                SystemSetting.getInstance(this).setApPwd(values);
                break;
        }
    }
}
