package com.ismobile.blaagent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public class SchematicNotification extends NotificationType {

    @Override
    public boolean evaluate(Vector<Assignment> assignments, Context context) {
        Assignment firstAss = assignments.firstElement();

        sendNotification(assignments, context);
        return false;
    }

    @Override
    public void sendNotification(Vector<Assignment> assignments, Context context) {
        CharSequence contentTitle = assignments.firstElement().getTitle();
        CharSequence contentText = "15 min left to deadline";
        String uid = assignments.firstElement().getUid();
        float longi = assignments.firstElement().getLongitude();
        float lati = assignments.firstElement().getLatitude();
        String title = assignments.firstElement().getTitle();
        boolean booked = assignments.firstElement().getBooked();
        String start = assignments.firstElement().getStart();
        String stop = assignments.firstElement().getStop();
        int currentDriveTime = 30;

        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
            resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);

        // Opens google maps, from: My Location to: an assignments lat, long.
        Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?f=d&daddr=" + lati + "," + longi));
        mapsIntent.setComponent(new ComponentName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"));

        String[] details = new String [3];
        details[0] = "Deadline: " + stop;
        details[1] = "Assignment: " + title;
        details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
        boolean bigStyle = true;
        NotificationAction[] notiActions = new NotificationAction[2];
        notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
        notiActions[1] = new NotificationAction(R.drawable.google_maps_logo, "", mapsIntent);
        
        StatusNotificationIntent sni = new StatusNotificationIntent(context);
        sni.buildNotification(contentTitle,contentText,resultIntent,details,bigStyle,notiActions);
    }
}
