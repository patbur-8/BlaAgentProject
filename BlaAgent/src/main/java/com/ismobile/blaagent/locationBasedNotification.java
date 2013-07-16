package com.ismobile.blaagent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.widget.ListView;

import com.ismobile.blaagent.sqlite.NotificationItem;
import com.ismobile.blaagent.sqlite.NotificationItemsDataSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * If start time has passed for an assignment and the user is not in place,
 * this notification will appear.
 * Created by ats on 2013-07-12.
 */
public class locationBasedNotification extends NotificationType {

    @Override
    public boolean evaluate(Vector<Assignment> assignments, Context context) {
        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        NotificationItem notificationItem;

        String contentText;
        Assignment first = assignments.firstElement();
        String startTime = first.getStart();
        String stopTime = first.getStop();
        String[] details = null;
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());

        // My location.
        float latitude = first.getLatitude();
        float longitude = first.getLongitude();
        double distance = getDistance(latitude, longitude);

        // Date object.
        Date d1 = null, d2 = null, d3 = null;
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        try {
            d1 = df.parse(currentTime);
            d2 = df.parse(startTime);
            d3 = df.parse(stopTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timePassed = (d1.getTime() - d2.getTime())/(1000*60);
        if (timePassed <= 6) {
            if (d1.after(d2) && d1.before(d3)) {
                if (!(0 <= distance && distance <= 0.5)) {
                    Log.d("NOTIF", "Fungerar!!!");
                    contentText = "A new assignment has started and you are not in place.";
                    sendNotification(assignments, details, contentText, context);
                    notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"loc"+first.getUid());
                    if(notificationItem != null) {
                         MainActivity.getNotificationAdapter().add(notificationItem);
                        MainActivity.getNotificationAdapter().notifyDataSetChanged();
                    }
                }
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

        String notificationId = uid+"location";

        StatusNotificationIntent sni = new StatusNotificationIntent(context);
        sni.buildNotification(contentTitle,contentText,resultIntent,details,bigStyle,notiActions,notificationId);

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
        return distance;
    }
}
