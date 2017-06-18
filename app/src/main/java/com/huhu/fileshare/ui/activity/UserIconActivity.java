package com.huhu.fileshare.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ui.adapter.ChangeUserIconAdapter;
import com.huhu.fileshare.util.SystemSetting;

public class UserIconActivity extends BaseActivity {

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_logo);
        initToolbar("头像", null);
        findViewById(R.id.change_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserIconActivity.this, ChangeUserIconActivity.class));
            }
        });
        mImageView = (ImageView) findViewById(R.id.user_icon);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bitmap bitmap = BitmapFactory.decodeFile(SystemSetting.getInstance(UserIconActivity.this).getUserIconPath());
        if(bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }else{
            mImageView.setImageResource(R.mipmap.default_icon);
        }
    }

    @Override
    public void initToolbar(String title, String subtitle) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        if (!TextUtils.isEmpty(subtitle)) {
            mToolbar.setSubtitle(subtitle);
        }
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void init() {

    }


}
