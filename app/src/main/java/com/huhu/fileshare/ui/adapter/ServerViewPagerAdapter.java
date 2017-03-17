package com.huhu.fileshare.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.huhu.fileshare.ui.fragment.ShareApkFragment;
import com.huhu.fileshare.ui.fragment.ShareImagesFragment;
import com.huhu.fileshare.ui.fragment.ShareMusicFragment;
import com.huhu.fileshare.ui.fragment.ShareCommonFileFragment;
import com.huhu.fileshare.ui.fragment.ShareVideoFragment;
import com.huhu.fileshare.util.GlobalParams;

/**
 * Created by Administrator on 2016/4/24.
 */
public class ServerViewPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] mFragmentList = new Fragment[5];

    private String[] mTitles = {"图片","音乐","视频","应用","文件"};

    public ServerViewPagerAdapter(FragmentManager fm, String ip) {
        super(fm);
        for(int i = 0; i < 5; i++) {
            if(i == 0){
                mFragmentList[i] = ShareImagesFragment.newInstance(GlobalParams.SERVER_MODE,ip);
            }else if(i == 1){
                mFragmentList[i] = ShareMusicFragment.newInstance(GlobalParams.SERVER_MODE,ip);
            }else if(i == 2){
                mFragmentList[i] = ShareVideoFragment.newInstance(GlobalParams.SERVER_MODE, ip);
            } else if(i == 3){
                mFragmentList[i] = ShareApkFragment.newInstance(GlobalParams.SERVER_MODE, ip);
            }else{
                mFragmentList[i] = ShareCommonFileFragment.newInstance(GlobalParams.SERVER_MODE, ip);
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
