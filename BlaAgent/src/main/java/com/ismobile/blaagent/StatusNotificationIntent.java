package com.ismobile.blaagent;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.support.v4.app.TaskStackBuilder;

public class StatusNotificationIntent {
    private Context context;

    Assignment assignment = new Assignment();
    private float longi = assignment.getLongitude();
    private float lati = assignment.getLatitude();
    private String title = assignment.getTitle();
    private String uid = "9Bk5THugReWsbQ6xq2nTkA"; //assignment.getUid();
    private boolean booked = assignment.getBooked();
    private String start = assignment.getStart();
    private String stop = assignment.getStop();
    private int currentDriveTime = 30;

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
        events[0] = "Title: " + title;
        events[1] = "Deadline: " + stop;
        events[2] = "Drive time to next assignment: " + currentDriveTime + " min.";

        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
        resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);

        // lat, long
        // from: 20.344,34.34
        // to: 20.5666,45.345
        Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?f=d&daddr=59.6534, 17.9336"));
        mapsIntent.setComponent(new ComponentName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);

        TaskStackBuilder mapsStackBuilder = TaskStackBuilder.create(context);
        mapsStackBuilder.addNextIntent(mapsIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent mapsPendingIntent = mapsStackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND)
                .addAction(R.drawable.ic_launcher, "", resultPendingIntent)
                .addAction(R.drawable.google_maps_logo, "", mapsPendingIntent); // Ta bort notifieringen efter man klickat på bilderna.

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