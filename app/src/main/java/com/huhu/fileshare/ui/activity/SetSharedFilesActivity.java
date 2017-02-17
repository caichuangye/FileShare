package com.huhu.fileshare.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.ui.adapter.ShareViewPagerAdapter;
import com.huhu.fileshare.ui.view.PagerSlidingTabStrip;

public class SetSharedFilesActivity extends BaseActivity {

    private ShareViewPagerAdapter mAdapter;

    private ViewPager mViewPager;

    private PagerSlidingTabStrip mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources_scan);
        if (getIntent() != null) {
            initToolbar("设置共享", null);
        }

        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.share_viewpager);
        mAdapter = new ShareViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                //     EventBus.getDefault().post(new EventBusType.ChangeShareTab(i));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mTabs.setViewPager(mViewPager);

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
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.set_files_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.see_share_info:
                showSharedFilesInfoDialog(ShareApplication.getInstance().getSharedCollection());
                break;
        }
        return true;
    }

    private void showSharedFilesInfoDialog(SharedCollection collection){

        int imageSize = collection.getImageList().size();
        int audioSize = collection.getMusicList().size();
        int videoSize = collection.getVideoList().size();
        int fileSize = collection.getSDFileList().size()+collection.getSpecialFileList().size();
        int total = imageSize+audioSize+videoSize+fileSize;
        if(total == 0){
            Toast.makeText(this,"暂无共享的文件",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(this).inflate(R.layout.selected_shared_layout,null);
        TextView title = (TextView)view.findViewById(R.id.title);
        final CheckBox checkBox = (CheckBox)view.findViewById(R.id.unselect_all);
        TextView image = (TextView)view.findViewById(R.id.image_count);
        TextView audio = (TextView)view.findViewById(R.id.music_count);
        TextView video = (TextView)view.findViewById(R.id.video_count);
        TextView file = (TextView)view.findViewById(R.id.file_count);
        title.setText("已共享"+total+"个文件");
        image.setText("图片：" + imageSize );
        audio.setText("音乐：" + audioSize);
        video.setText("视频："+ videoSize);
        file.setText("文件：" + fileSize);
        builder.setView(view)
                .setPositiveButton("确    定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkBox.isChecked()) {
                            ShareApplication.getInstance().removeAllShared();
                        }
                    }
                }).show();
    }



}
