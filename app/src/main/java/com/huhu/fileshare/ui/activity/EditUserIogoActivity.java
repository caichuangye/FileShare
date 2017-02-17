package com.huhu.fileshare.ui.activity;

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
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.ui.adapter.ChangeUserIconAdapter;
import com.huhu.fileshare.util.SystemSetting;

public class EditUserIogoActivity extends BaseActivity {

    private ImageView mSelectedIcon;

    private GridView mIconGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_logo);
        initToolbar("编辑头像",null);

        mSelectedIcon = (ImageView)findViewById(R.id.user_icon);
        mIconGridView = (GridView)findViewById(R.id.gridview);
        init();
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
                finish();
                break;
        }
        return true;
    }

    private void init(){
        int index = SystemSetting.getInstance(this).getUserIconIndex();
        mSelectedIcon.setImageResource(((ShareApplication) this.getApplicationContext()).getUserIconList()[index]);
        ChangeUserIconAdapter adapter = new ChangeUserIconAdapter(this);
        mIconGridView.setAdapter(adapter);
        mIconGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SystemSetting.getInstance(getApplicationContext()).setUserIconIndex(position);
                mSelectedIcon.setImageResource(((ShareApplication)getApplicationContext()).getUserIconList()[position]);
            }
        });
    }

}
