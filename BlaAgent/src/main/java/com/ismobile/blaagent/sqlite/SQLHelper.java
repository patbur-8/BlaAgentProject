package com.ismobile.blaagent.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLHelper extends SQLiteOpenHelper {

    //Table and columns
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENTTEXT = "contenttext";
    public static final String COLUMN_LATITUDE = "lati";
    public static final String COLUMN_LONGITUDE = "longi";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_STOP = "stop";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DATE = "datecreated";

    //Database name
    private static final String DATABASE_NAME = "notifications.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NOTIFICATIONS + "(" + COLUMN_UID + " integer not null, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_CONTENTTEXT + " text not null, "
            + COLUMN_LATITUDE + " float not null, "
            + COLUMN_LONGITUDE + " float not null, "
            + COLUMN_DETAILS + " text not null, "
            + COLUMN_START + " text not null, "
            + COLUMN_STOP + " text not null, "
            + COLUMN_TYPE + " text not null, "
            + COLUMN_DATE + " integer not null);";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }

}