package com.ismobile.blaagent;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.HashMap;

public class StatusNotificationIntent {
    private Context context;
    static int notiId = 0;
    static HashMap<String, Integer> hm = new HashMap<String, Integer>();

    public StatusNotificationIntent(Context context) {
        this.context = context;
    }

    public void buildNotification(CharSequence contentTitle, CharSequence contentText, Intent resultIntent, String[] details, boolean bigStyle, NotificationAction[] notiActions, String notificationId) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND); // Notification.DEFAULT_ALL

        if(notiActions != null) {
            for(int i = 0; i<notiActions.length;i++) {
                int img = notiActions[i].getImage();
                String title = notiActions[i].getTitle();
                Intent intent = notiActions[i].getIntent();
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(intent);
                PendingIntent pi = stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(img, title, pi);
            }
        }

        // Opens assignment with uid in Blå Android.
        if(resultIntent != null) {
            TaskStackBuilder resultStackBuilder = TaskStackBuilder.create(context);
            resultStackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = resultStackBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        // Moves the big view style object into the notification object.
        if(bigStyle) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle("Assignment Details");

            // Moves events into the big view
            if(details != null) {
                for (int i=0; i < details.length; i++) {
                    inboxStyle.addLine(details[i]);
                }
            }
            builder.setStyle(inboxStyle);
        }

        int id;
        if(hm.containsKey(notificationId)) {
            Log.d("NotifID", "Contains ID");
            id = hm.get(notificationId);
        } else {
            hm.put(notificationId, notiId);
            id = notiId;
            notiId++;
        }

        Log.d("NotifID", id+"");
        nm.notify(id, builder.build());
    }
}