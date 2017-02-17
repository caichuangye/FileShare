package com.huhu.fileshare.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huhu.fileshare.model.FileItem;
import com.huhu.fileshare.model.ImageItem;
import com.huhu.fileshare.model.MusicItem;
import com.huhu.fileshare.model.SharedCollection;
import com.huhu.fileshare.model.SpecialFileItem;
import com.huhu.fileshare.model.VideoItem;
import com.huhu.fileshare.ui.fragment.ShareImageFolderFragment;
import com.huhu.fileshare.ui.fragment.ShareImagesFragment;
import com.huhu.fileshare.ui.fragment.ShareMusicFragment;
import com.huhu.fileshare.ui.fragment.ShareSpecialFileFragment;
import com.huhu.fileshare.ui.fragment.ShareVideoFragment;
import com.huhu.fileshare.util.GlobalParams;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Administrator on 2016/4/24.
 */
public class ScanSharedViewPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] mFragmentList = new Fragment[4];

    private String[] mTitles = {"图片","音乐","视频","文件"};

    public ScanSharedViewPagerAdapter(FragmentManager fm,String ip) {
        super(fm);
        for(int i = 0; i < 5; i++) {
            if(i == 0){
                mFragmentList[i] = ShareImagesFragment.newInstance(GlobalParams.SCAN_MODE,ip);
            }else if(i == 1){
                mFragmentList[i] = ShareMusicFragment.newInstance(GlobalParams.SCAN_MODE,ip);
            }else if(i == 2){
                mFragmentList[i] = ShareVideoFragment.newInstance(GlobalParams.SCAN_MODE, ip);
            } else if(i == 3){
                mFragmentList[i] = ShareSpecialFileFragment.newInstance(GlobalParams.SCAN_MODE, ip);
            }
        }
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList[i];
    }

    @Override
    public int getCount() {
        return mFragmentList.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
