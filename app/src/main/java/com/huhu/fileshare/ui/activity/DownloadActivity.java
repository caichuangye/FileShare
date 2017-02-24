package com.huhu.fileshare.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.databases.DatabaseUtils;
import com.huhu.fileshare.databases.DownloadHistory;
import com.huhu.fileshare.model.DownloadItem;
import com.huhu.fileshare.model.DownloadStatus;
import com.huhu.fileshare.ui.adapter.DownloadAdapter;
import com.huhu.fileshare.util.CommonUtil;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.SystemSetting;

import java.io.File;
import java.util.List;

public class DownloadActivity extends BaseActivity implements DownloadAdapter.OnSelectListener {

    private DownloadAdapter mAdapter;

    private DownloadHistory mDownloadHistory;

    private ListView mListView;

    private MenuItem mDeleteItem;

    private MenuItem mSelectAllItem;

    private MenuItem mCancelItem;

    private MenuItem mGroupItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initToolbar("下载列表", null);


        View empty = findViewById(R.id.emptyview);
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setEmptyView(empty);

        mAdapter = new DownloadAdapter(this, this);
        mDownloadHistory = new DownloadHistory(this);
        mAdapter.setData(mDownloadHistory.getAllItems());
        mListView.setAdapter(mAdapter);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                setShowMenu(true);
                mAdapter.setShowDeleteIcon(true);
                return true;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            handleClickItem(position);
            }
        });

    }

    private void handleClickItem(int position){
        DownloadItem  item = (DownloadItem) mAdapter.getItem(position);
        if(item.getStatus() != DownloadStatus.SUCCESSED){
            String name = item.getToPath().substring(item.getToPath().lastIndexOf("/")+1);
            Toast.makeText(this,name+": 未下载完成",Toast.LENGTH_SHORT).show();
            return;
        }
        String path = item.getToPath();
        if(!TextUtils.isEmpty(path)){
            File file = new File(path);
            if(!file.exists()) {
                Toast.makeText(this, path + ": 不存在", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            Toast.makeText(this, "文件路径格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://"+path);
        String ext = path.substring(path.lastIndexOf('.')+1);
        intent.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        Log.d("view-c",path+": "+ext+": "+MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        startActivity(intent);
    }

    @Override
    public void initToolbar(String title, String subtitle) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_57));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onEventMainThread(EventBusType.UpdateDownloadFile info) {
        DownloadItem item = info.getData();
        GlobalParams.DownloadOper oper = info.getOper();
        if(oper == GlobalParams.DownloadOper.DELETE){
            mAdapter.deleteItem(item);
        }else if(oper == GlobalParams.DownloadOper.ADD){
            Log.d("cctype","add: "+item.toString());
            mAdapter.addItem(item);
        }else{
            mAdapter.updateItem(item);
        }
    }

    private void setShowMenu(boolean show) {
        mGroupItem.setVisible(!show);
        mCancelItem.setVisible(show);
        mSelectAllItem.setVisible(show);
        if (show == false) {
            mDeleteItem.setVisible(show);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.download_menu, menu);
        mSelectAllItem = menu.findItem(R.id.all_select);
        mCancelItem = menu.findItem(R.id.cancel);
        mDeleteItem = menu.findItem(R.id.delete);
        mGroupItem = menu.findItem(R.id.group);
        setShowMenu(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.all_select:
                if (mSelectAllItem.getTitle().equals("全选")) {
                    mAdapter.selectAll(true);
                    mSelectAllItem.setTitle("全不选");
                } else {
                    mAdapter.selectAll(false);
                    mSelectAllItem.setTitle("全选");
                }
                break;
            case R.id.cancel:
                setShowMenu(false);
                mSelectAllItem.setTitle("全选");
                mAdapter.setShowDeleteIcon(false);
                mAdapter.selectAll(false);
                break;
            case R.id.delete:
                showDeleteConfirmDialog();
                break;
            case R.id.group:
                showGroupDialog();
                break;
        }
        return true;
    }


    @Override
    public void onHasSelected(boolean selected) {
        mDeleteItem.setVisible(selected);
    }

    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(this).inflate(R.layout.confirm_delete_download_layout, null);
        final CheckBox checkBox = (CheckBox)view.findViewById(R.id.delete_checkbox);
        builder.setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      //  showDeletingDialog();
                        deleteSelectedItems(checkBox.isChecked());
                        mAdapter.deleteSelected();

                        setShowMenu(false);
                        mSelectAllItem.setTitle("全选");
                        mAdapter.setShowDeleteIcon(false);
                        mAdapter.selectAll(false);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private void showGroupDialog(){
        CommonUtil.Flag flag = mAdapter.getFlag();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.group_show_layout,null);
        final RadioButton allRB = (RadioButton)view.findViewById(R.id.rb_all);
        final RadioButton dateRB = (RadioButton)view.findViewById(R.id.rb_date);
        RadioButton typeRB = (RadioButton)view.findViewById(R.id.rb_type);
        final RadioButton ownerRB = (RadioButton)view.findViewById(R.id.rb_owner);
        if(flag == CommonUtil.Flag.NONE){
            allRB.toggle();
        }else if(flag == CommonUtil.Flag.OWNER){
            ownerRB.toggle();
        }else if(flag == CommonUtil.Flag.DATE){
            dateRB.toggle();
        }else{
            typeRB.toggle();
        }
        builder.setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonUtil.Flag res;
                        if(allRB.isChecked()){
                            res = CommonUtil.Flag.NONE;
                        }else if(ownerRB.isChecked()){
                            res = CommonUtil.Flag.OWNER;
                        }else if(dateRB.isChecked()){
                            res = CommonUtil.Flag.DATE;
                        }else{
                            res = CommonUtil.Flag.TYPE;
                        }
                        SystemSetting.getInstance(getApplicationContext()).setGroupFlag(CommonUtil.getFlagValue(res));
                        mAdapter.setGroup(res);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private void deleteSelectedItems(boolean isDeleteResources){
        List<DownloadItem> list = mAdapter.getSelectedItem();
        StringBuilder where = new StringBuilder(DatabaseUtils.ColumnName.ID+" in (");
        for(DownloadItem item : list){
            String path = item.getToPath();
            String uuid = item.getUUID();
            if(!TextUtils.isEmpty(path)) {
                where.append("'" + uuid + "',");
                if(isDeleteResources) {
                    if(item.getFileType().equals(GlobalParams.ShareType.APK.toString())){
                        String localPath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"fileshare";
                        path = localPath + File.separator + item.getDestName()+".apk";
                    }
                    File file = new File(path);
                    if(file.exists()) {
                        boolean res = file.delete();
                        Log.d("ccdelete", path+": "+res);
                    }
                }
            }

        }
        if(!where.toString().endsWith("(")) {
            where.deleteCharAt(where.length() - 1);
            where.append(")");
            int count = getContentResolver().delete(DatabaseUtils.DOWNLOAD_HISTORY_URI, where.toString(), null);
        }
    }


}
