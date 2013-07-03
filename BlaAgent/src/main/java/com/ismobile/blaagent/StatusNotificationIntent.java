package com.ismobile.blaagent;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.support.v4.app.TaskStackBuilder;
import android.view.Display;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StatusNotificationIntent {
    private Context context;

    public StatusNotificationIntent(Context context) {
        this.context = context;
    }

    public void sendNotification(int typeOfMessage) {
        switch (typeOfMessage) {
            case 1:
                buildNotification("Info", "15 min to deadline");
                break;
            case 2:
                buildNotification("Warning", "5 min to deadline");
                break;
            case 3:
                buildNotification("Error", "You missed deadline");
                break;
        }
    }

    public void buildNotification(CharSequence contentTitle, CharSequence contentText) {
        String[] events = new String[6];

        // Sets a title for the Inbox style big view
        events[0] = "Helloo..!";
        events[1] = "How are you?";
        events[2] = "HIII !!";
        events[3] = "i am fine...";
        events[4] = "what about you? all is well?";
        events[5] = "Yes, every thing is all right..";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND);

        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
        resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", "9Bk5THugReWsbQ6xq2nTkA");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(ResultActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Assignment Details");

        // Moves events into the big view
        for (int i=0; i < events.length; i++) {
            inboxStyle.addLine(events[i]);
        }

        // Moves the big view style object into the notification object.
        builder.setStyle(inboxStyle);

        nm.notify(100, builder.build());
    }
}