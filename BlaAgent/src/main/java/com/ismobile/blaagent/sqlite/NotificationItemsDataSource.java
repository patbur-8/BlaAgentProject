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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ismobile.blaagent.Assignment;
import com.ismobile.blaagent.MainActivity;
import com.ismobile.blaagent.Test.Test;

public class NotificationItemsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private SQLHelper dbHelper;
    private String[] allColumns = { SQLHelper.COLUMN_UID, SQLHelper.COLUMN_TITLE,
            SQLHelper.COLUMN_CONTENTTEXT, SQLHelper.COLUMN_LATITUDE, SQLHelper.COLUMN_LONGITUDE,
            SQLHelper.COLUMN_DETAILS, SQLHelper.COLUMN_START, SQLHelper.COLUMN_STOP,
            SQLHelper.COLUMN_TYPE, SQLHelper.COLUMN_DATE};

    /**
     * Constructor.
     * @param context
     */
    public NotificationItemsDataSource(Context context) {
        dbHelper = new SQLHelper(context);
    }

    /**
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     *
     */
    public void close() {
        dbHelper.close();
    }

    Test test = new Test();

    /**
     * Creates a notification item and inserts it into the database
     * @param ass
     * @param contentText
     * @param details
     * @param type
     * @return
     */
    public NotificationItem createNotificationItem(Assignment ass, String contentText,
                                                    String[] details, String type) {
        String title = ass.getTitle();
        String uid = ass.getUid();
        String start = ass.getStart();
        String stop = ass.getStop();
        float lati = ass.getLatitude();
        float longi = ass.getLongitude();
        Log.d("uid+type", uid + " : " +type);

        //Each notification may only come once for each assignment.
        if(!checkIfNotificationExist(uid,type)) {
            //Convert details array to string as SQLite can't store string arrays.
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
            values.put(SQLHelper.COLUMN_DATE, (""+getCurrentDate().getTime()/1000L));

            //Inserts the notification into database.
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

    /**
     * Checks if the notification exists in the database.
     * @param uid
     * @param type
     * @return
     */
    public boolean checkIfNotificationExist(String uid, String type) {
        Cursor dataCount = database.rawQuery("SELECT " + SQLHelper.COLUMN_TITLE + " FROM " +
                SQLHelper.TABLE_NOTIFICATIONS + " WHERE " + SQLHelper.COLUMN_UID + " = ? AND " +
                SQLHelper.COLUMN_TYPE + " = ?", new String[] {uid, type});
        return (dataCount != null && dataCount.moveToFirst());
    }

    /**
     * Retrieves all notification items from the database.
     * @return
     */
    public List<NotificationItem> getAllNotificationItems(Date from, Date to) {
        List<NotificationItem> notiList = new ArrayList<NotificationItem>();

        String [] whereArgs = new String[2];
        whereArgs[0] = from.getTime()/1000L+"";
        whereArgs[1] = to.getTime()/1000L+"";

        //SQLHelper.COLUMN_DATE + " => ? AND " + SQLHelper.COLUMN_DATE + " <= ?"

        Cursor cursor = database.query(SQLHelper.TABLE_NOTIFICATIONS,
                allColumns, SQLHelper.COLUMN_DATE + " BETWEEN ? AND ?", whereArgs, null, null, null);

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

    /**
     *
     * @param cursor
     * @return
     */
    private NotificationItem cursorToNotification(Cursor cursor) {
        NotificationItem noti = new NotificationItem();

        noti.setUid(cursor.getString(0));
        noti.setTitle(cursor.getString(1));
        noti.setContentText(cursor.getString(2));
        noti.setLatitude(cursor.getFloat(3));
        noti.setLongitude(cursor.getFloat(4));
        noti.setDetails(cursor.getString(5));
        noti.setType(cursor.getString(8));

        Date date = new Date ();
        date.setTime((long)cursor.getInt(9)*1000);
        DateFormat df = new SimpleDateFormat("HH:mm");
        noti.setDateCreated(df.format(date));

        return noti;
    }

    /**
     * Converts String Array to String.
     * @param array
     * @return
     */
    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            if(i<array.length-1){
                str = str+"#!%";
            }
        }
        return str;
    }

    /**
     * Converts String to String Array.
     * @param str
     * @return
     */
    public static String[] convertStringToArray(String str){
        String[] arr = str.split("#!%");
        return arr;
    }

    /**
     * If test is enabled, returns the current date modified by test, otherwise, current date.
     * @return
     */
    public Date getCurrentDate() {
        boolean testEnabled = MainActivity.testEnabled();
        if(testEnabled) {
            return Test.getCurrentDate();
        } else {
            return new Date();
        }
    }

}