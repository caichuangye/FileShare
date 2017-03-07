package com.huhu.fileshare.databases;

import android.net.Uri;

/**
 * Created by Administrator on 2016/10/19.
 */

public class DatabaseUtils {

    public static String DATABASE_NAME = "fileshare";

    public static int DATABASE_VERSION = 1;

    public static final int DATA_COLLECTION = 1;

    public static final int DATA_ITEM = 2;

    public static final String SCHEME = "content://";

    public static final String AUTHORITIES = "com.huhu.fileshare.databases.ShareProvider";

    public static final String PATH_COLLECTION = "/fileshare";

    public static final String PATH_ITEM = "/fileshare/#";

    public static final String TABLE_NAME = "download_history";

    public static final Uri DOWNLOAD_HISTORY_URI = Uri.parse(SCHEME+AUTHORITIES+PATH_COLLECTION);

    public static String CREATE_TABLE_SQL = "create table if not exists download_history(" +
            "id text primary key not null," +
            "start_time text not null,"+
            "end_time text ,"+
            "from_path text not null,"+
            "from_username text not null,"+
            "from_ip text not null,"+
            "to_path text not null,"+
            "to_username text ,"+
            "total_size text not null,"+
            "recv_size text not null,"+
            "download_states text not null,"+
            "file_type text," +
            "dest_name text,"+
            "cover_path text)";

    public interface ColumnName{
        String ID = "id";
        String START_TIME = "start_time";
        String END_TIME = "end_time";
        String FROM_PATH = "from_path";
        String FROM_USERNAME = "from_username";
        String FROM_IP = "from_ip";
        String TO_PATH = "to_path";
        String TOTAL_SIZE = "total_size";
        String RECV_SIZE = "recv_size";
        String FILE_TYPE = "file_type";
        String DOWNLOAD_STATES = "download_states";
        String DEST_NAME = "dest_name";
        String COVER_PATH = "cover_path";
    }

}
