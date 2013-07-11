package com.ismobile.blaagent.sqlite;

/**
 * Created by pbm on 2013-07-10.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NotificationItemsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private SQLHelper dbHelper;
    private String[] allColumns = { SQLHelper.COLUMN_UID, SQLHelper.COLUMN_TITLE,
            SQLHelper.COLUMN_CONTENTTEXT, SQLHelper.COLUMN_LATITUDE, SQLHelper.COLUMN_LONGITUDE,
            SQLHelper.COLUMN_DETAILS, SQLHelper.COLUMN_START, SQLHelper.COLUMN_STOP,
            SQLHelper.COLUMN_TYPE, SQLHelper.COLUMN_DATE};

    public NotificationItemsDataSource(Context context) {
        dbHelper = new SQLHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public NotificationItem createNotificationItem(String title, String uid, String contentText,
                                                    String start, String stop, float lati,
                                                    float longi, String[] details, String type) {
        String detailString = convertArrayToString(details);

        ContentValues values = new ContentValues();
        values.put(SQLHelper.COLUMN_UID, uid);
        values.put(SQLHelper.COLUMN_TITLE, title);
        values.put(SQLHelper.COLUMN_CONTENTTEXT, contentText);
        values.put(SQLHelper.COLUMN_LATITUDE, lati);
        values.put(SQLHelper.COLUMN_LONGITUDE, longi);
        values.put(SQLHelper.COLUMN_DETAILS, detailString);
        values.put(SQLHelper.COLUMN_START, start);
        values.put(SQLHelper.COLUMN_STOP, stop);
        values.put(SQLHelper.COLUMN_TYPE, type);

        long insertId = database.insert(SQLHelper.TABLE_NOTIFICATIONS, null,
                values);

        Cursor cursor = database.query(SQLHelper.TABLE_NOTIFICATIONS,
                allColumns, SQLHelper.COLUMN_UID + " = '" + uid + "'"  + " AND "
                + SQLHelper.COLUMN_TYPE + " = '" + type + "'", null, null, null, null);

        cursor.moveToFirst();
        NotificationItem newNoti = cursorToNotification(cursor);
        cursor.close();
        return newNoti;
    }

    public void deleteNotificationItem(NotificationItem noti) {
        String uid = noti.getUid();
        System.out.println("Comment deleted with id: " + uid);
        database.delete(SQLHelper.TABLE_NOTIFICATIONS, SQLHelper.COLUMN_UID
                + " = " + uid, null);
    }

    public List<NotificationItem> getAllNotificationItems() {
        List<NotificationItem> notiList = new ArrayList<NotificationItem>();

        Cursor cursor = database.query(SQLHelper.TABLE_NOTIFICATIONS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            NotificationItem noti = cursorToNotification(cursor);
            notiList.add(noti);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return notiList;
    }

    private NotificationItem cursorToNotification(Cursor cursor) {
        NotificationItem noti = new NotificationItem();
        noti.setUid(cursor.getString(0));
        noti.setTitle(cursor.getString(1));
        return noti;
    }

    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            // Do not append comma at the end of last element
            if(i<array.length-1){
                str = str+"#!%";
            }
        }
        return str;
    }
    public static String[] convertStringToArray(String str){
        String[] arr = str.split("#!%");
        return arr;
    }
}