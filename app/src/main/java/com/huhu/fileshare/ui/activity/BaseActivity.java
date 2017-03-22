package com.huhu.fileshare.ui.activity;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.util.HLog;

/**
 * Created by Administrator on 2016/4/9.
 */
public class BaseActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        EventBus.getDefault().register(this);
        initStrictMode();
    }

    private void initStrictMode(){
     //   ApplicationInfo info = getApplicationInfo();
      //  int flag = info.flags & ApplicationInfo.FLAG_DEBUGGABLE;
      //  if(flag == 2) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
   //     }
    }

    public void initToolbar(String title, String subtitle){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
