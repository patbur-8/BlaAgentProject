package com.ismobile.blaagent.sqlite;

/**
 * Created by pbm on 2013-07-10.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.ismobile.blaagent.Assignment;

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

    public NotificationItem createNotificationItem(Assignment ass, String contentText,
                                                    String[] details, String type) {


        String title = ass.getTitle();
        String uid = ass.getUid();
        String start = ass.getStart();
        String stop = ass.getStop();
        float lati = ass.getLatitude();
        float longi = ass.getLongitude();

        if(!checkIfNotificationExist(uid,type)) {
            String detailString;
            if(details != null) {
                detailString = convertArrayToString(details);
            } else {
                detailString = "";
            }


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
            values.put(SQLHelper.COLUMN_DATE, (""+System.currentTimeMillis() / 1000L));
            Log.d("TIIIIIID", values.get(SQLHelper.COLUMN_DATE) + "");
            long insertId = database.insert(SQLHelper.TABLE_NOTIFICATIONS, null,
                    values);

            Cursor cursor = database.query(SQLHelper.TABLE_NOTIFICATIONS,
                    allColumns, SQLHelper.COLUMN_UID + " = '" + uid + "'"  + " AND "
                    + SQLHelper.COLUMN_TYPE + " = '" + type + "'", null, null, null, SQLHelper.COLUMN_DATE);

            cursor.moveToLast();
            NotificationItem newNoti = cursorToNotification(cursor);
            cursor.close();
            return newNoti;
        }
        return null;
    }

    public boolean checkIfNotificationExist(String uid, String type) {
        SQLiteStatement s = database.compileStatement("SELECT " + SQLHelper.COLUMN_TITLE + " FROM " +
                SQLHelper.TABLE_NOTIFICATIONS + " WHERE " + SQLHelper.COLUMN_UID + " = '" + uid +
                "' AND " + SQLHelper.COLUMN_TYPE + " = '" + type + "'");
        long count =  s.simpleQueryForLong();
        if (count > 0)  {
            return true;
        } else {
            return false;
        }

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
        noti.setDetails(cursor.getString(5));
        noti.setContentText(cursor.getString(2));
        Date date = new Date ();
        date.setTime((long)cursor.getInt(9) * 1000);
        DateFormat df = new SimpleDateFormat("HH:mm, d MMM yyyy:");
        noti.setDateCreated(df.format(date));

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