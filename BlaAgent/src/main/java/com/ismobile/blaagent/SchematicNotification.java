package com.ismobile.blaagent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public class SchematicNotification extends NotificationType {

    @Override
    public boolean evaluate(Vector<Assignment> assignments, Context context) {
        // Assignments is sorted by stop time. First deadline = first element.
        CharSequence contentText;
        String title = assignments.firstElement().getTitle();
        String stopTime = assignments.firstElement().getStop();
        int currentDriveTime = 30;
        String[] details = new String [3];

        if (assignments.size() > 0) {
            Date d1 = null, d2 = null;
            String myFormatString = "yyyy-MM-dd HH:mm";
            SimpleDateFormat df = new SimpleDateFormat(myFormatString);

            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
            try {
                d1 = df.parse(currentTime);
                d2 = df.parse(stopTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Long difference = (d2.getTime() - d1.getTime())/(1000*60);
            if (difference > 15) {
                //Do nothing
                contentText = "Time is up looong ago";
                details[0] = "Deadline: " + stopTime;
                details[1] = "Assignment: " + title;
                details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
                sendNotification(assignments, details, contentText, context);

            } else if (13 <= difference && difference <= 15) {
                //Info
                contentText = difference + " min left to deadline";
                details[0] = "Deadline: " + stopTime;
                details[1] = "Assignment: " + title;
                details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
                sendNotification(assignments, details, contentText, context);

            } else if (4 <= difference && difference <= 5) {
                //Warning
                contentText = difference + " min left to deadline";
                details[0] = "Deadline: " + stopTime;
                details[1] = "Assignment: " + title;
                details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
                sendNotification(assignments, details, contentText, context);

            } else if (difference <= 0) {
                //Error
                contentText = difference + " min, time is up!!!";
                details[0] = "Deadline: " + stopTime;
                details[1] = "Assignment: " + title;
                details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
                sendNotification(assignments, details,  contentText, context);
            }
            //Kolla om det finns någon bokad under dagen, kommer vi bli sen till den?
        }
        return false;
    }

    @Override
    public void sendNotification(Vector<Assignment> assignments, String[] details, CharSequence contentText, Context context) {
        CharSequence contentTitle = assignments.firstElement().getTitle();

        String uid = assignments.firstElement().getUid();
        float longi = assignments.firstElement().getLongitude();
        float lati = assignments.firstElement().getLatitude();

        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
            resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);

        // Opens google maps, from: My Location to: an assignments lat, long.
        Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?f=d&daddr=" + lati + "," + longi));
        mapsIntent.setComponent(new ComponentName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"));

        boolean bigStyle = true;
        NotificationAction[] notiActions = new NotificationAction[2];
        notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
        notiActions[1] = new NotificationAction(R.drawable.google_maps_logo, "", mapsIntent);

        StatusNotificationIntent sni = new StatusNotificationIntent(context);
        sni.buildNotification(contentTitle,contentText,resultIntent,details,bigStyle,notiActions);
    }
}
