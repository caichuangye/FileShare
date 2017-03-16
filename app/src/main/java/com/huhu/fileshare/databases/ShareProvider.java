package com.huhu.fileshare.databases;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Administrator on 2016/10/19.
 */

public class ShareProvider extends ContentProvider {

    private ShareDatabaseHelper mDatabaseHelper;

    public static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(DatabaseUtils.AUTHORITIES, DatabaseUtils.PATH_COLLECTION, DatabaseUtils.DATA_COLLECTION);
        sUriMatcher.addURI(DatabaseUtils.AUTHORITIES, DatabaseUtils.PATH_ITEM, DatabaseUtils.DATA_ITEM);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new ShareDatabaseHelper(getContext(), DatabaseUtils.DATABASE_NAME, null, DatabaseUtils.DATABASE_VERSION);
        return mDatabaseHelper != null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case DatabaseUtils.DATA_COLLECTION:
                return "vnd.android.cursor.dir/fileshare";
            case DatabaseUtils.DATA_ITEM:
                return "vnd.android.cursor.item/fileshare";
            default:
                throw new IllegalArgumentException("unknown uri code: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            case DatabaseUtils.DATA_COLLECTION:
                return db.query(DatabaseUtils.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case DatabaseUtils.DATA_ITEM:
                long id = ContentUris.parseId(uri);
                String where = "id = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                return db.query(DatabaseUtils.TABLE_NAME, projection, where, selectionArgs, null,
                        null, sortOrder);
            default:
                throw new IllegalArgumentException("unknown uri:" + uri.toString());
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case DatabaseUtils.DATA_COLLECTION:
                long id = db.insert(DatabaseUtils.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(uri, id);
            case DatabaseUtils.DATA_ITEM:
                db.insert(DatabaseUtils.TABLE_NAME, null, values);
                return uri;
            default:
                throw new IllegalArgumentException("insert: " + sUriMatcher.match(uri) + ", unknown uri:" + uri.toString());

        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case DatabaseUtils.DATA_COLLECTION:
                return db.delete(DatabaseUtils.TABLE_NAME, selection, selectionArgs);
            case DatabaseUtils.DATA_ITEM:
                long id = ContentUris.parseId(uri);
                String where = "id = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                return db.delete(DatabaseUtils.TABLE_NAME, where, selectionArgs);
            default:
                throw new IllegalArgumentException("unknown uri:" + uri.toString());
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case DatabaseUtils.DATA_COLLECTION:
                return db.update(DatabaseUtils.TABLE_NAME, values, selection, selectionArgs);
            case DatabaseUtils.DATA_ITEM:
                long id = ContentUris.parseId(uri);
                String where = " id = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                return db.update(DatabaseUtils.TABLE_NAME, values, where, selectionArgs);
            default:
                throw new IllegalArgumentException("unknown uri:" + uri.toString());
        }
    }
}
