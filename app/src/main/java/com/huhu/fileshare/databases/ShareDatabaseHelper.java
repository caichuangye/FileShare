package com.huhu.fileshare.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.huhu.fileshare.util.HLog;

/**
 * Created by Administrator on 2016/10/19.
 */

public class ShareDatabaseHelper extends SQLiteOpenHelper {



    public ShareDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HLog.d("huhudb","create table");
        db.execSQL(DatabaseUtils.CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
