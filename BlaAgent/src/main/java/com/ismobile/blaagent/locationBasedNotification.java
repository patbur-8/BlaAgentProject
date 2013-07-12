package com.ismobile.blaagent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by ats on 2013-07-12.
 */
public class locationBasedNotification extends NotificationType {

    @Override
    public boolean evaluate(Vector<Assignment> assignments, Context context) {
        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        CharSequence contentText;
        String title = assignments.firstElement().getTitle();
        String startTime = assignments.firstElement().getStart();
        int currentDriveTime = 30;
        String[] details = new String [3];
        boolean booked = assignments.firstElement().getBooked();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());

        // My location.
        float latitude = assignments.firstElement().getLatitude();
        float longitude = assignments.firstElement().getLongitude();
        double distance = getDistance(latitude, longitude);

        // Date object.
        Date d1 = null, d2 = null;
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        try {
            d1 = df.parse(currentTime);
            d2 = df.parse(startTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Long difference = (d2.getTime() - d1.getTime())/(1000*60);

        if (d1.after(d2)) { // Check if we are in place.
            if (0 <= distance && distance <= 0.5) {
                Log.d("NOTIF", "Fungerar!!!");
                contentText = "A new assignment has started and you are not in place.";
                sendNotification(assignments, details, contentText, context);
            }
        }
        return false;
    }

    @Override
    public void sendNotification(Vector<Assignment> assignments, String[] details, CharSequence contentText, Context context) {
        CharSequence contentTitle = assignments.firstElement().getTitle();

        String uid = assignments.firstElement().getUid();

        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
        resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);

        boolean bigStyle = false;
        NotificationAction[] notiActions = new NotificationAction[1];
        notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);

        StatusNotificationIntent sni = new StatusNotificationIntent(context);
        sni.buildNotification(contentTitle,contentText,resultIntent,details,bigStyle,notiActions);

    }

    /**
     * Gets the distance between the user and assignments location.
     * @param latitude
     * @param longitude
     * @return
     */
    public double getDistance(float latitude, float longitude) {
        // My location.
        Location location = new Location("");
        location.setLatitude(location.getLatitude());
        location.setLongitude(location.getLongitude());
        location.distanceTo(location);

        // The assignments location.
        Location aLocation = new Location("");
        aLocation.setLatitude(latitude);
        aLocation.setLongitude(longitude);

        int distance = (int)aLocation.distanceTo(location) / 1000; // Distance in km.
        String str = " (" + String.valueOf(distance) + " km)";
        Log.d("distance", str);
        return 0.4; //distance;
    }
}
