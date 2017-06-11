package com.huhu.fileshare.ui.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ui.fragment.MainFragment;

public class NewMainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        MainFragment mainFragment = new MainFragment();
        transaction.add(R.id.content_frame,mainFragment);
        transaction.commit();

    }
}
