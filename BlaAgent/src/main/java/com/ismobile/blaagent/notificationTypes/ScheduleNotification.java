package com.ismobile.blaagent.notificationTypes;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ismobile.blaagent.Assignment;
import com.ismobile.blaagent.MainActivity;
import com.ismobile.blaagent.NotificationAction;
import com.ismobile.blaagent.R;
import com.ismobile.blaagent.StatusNotificationIntent;
import com.ismobile.blaagent.Test.Test;
import com.ismobile.blaagent.sqlite.NotificationItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public class ScheduleNotification extends NotificationType {

    /**
     * Evaluates what type of notification we want to send.
     *
     * @param assignments
     * @param context
     * @return
     */
    @Override
    public void evaluate(Vector<Assignment> assignments, Assignment previous, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayNotification = prefs.getBoolean("schEnabled", true);
        if(!displayNotification) return;

        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        NotificationItem notificationItem;
        String contentText;
        Test test = new Test();
        Assignment first = test.createTestAssignment("2013-07-22 10:00", "2013-07-22 16:23", "ghfd3dfbg45n3j42");//assignments.firstElement();
        String title = first.getTitle();
        String stopTime = first.getStop();
        String[] details = new String [3];

        // My location.
        float latitude = first.getLatitude();
        float longitude = first.getLongitude();
        double distance = getDistance(latitude, longitude);

        Date d1 = null, d2 = null;
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);

        if (0 <= distance && distance <= 0.5) { // Check if we are in place.
            if (assignments.size() > 0) {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
                try {
                    d1 = df.parse(currentTime);
                    d2 = df.parse(stopTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Long difference = (d2.getTime() - d1.getTime())/(1000*60);

                if(0 <= difference && difference <= 5) {
                    boolean display5MinWarning =  prefs.getBoolean("sch5Min", true);
                    if(display5MinWarning) {
                        Log.d("NOTIF", "<5min");
                        //Warning
                        contentText = difference + " min left to deadline";
                        details[0] = "Deadline: " + stopTime;
                        details[1] = "Assignment: " + title;
                        details[2] = "Next assignment in current traffic: " + getCurrentTrafficTime(assignments, 1) + " min";
                        notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"scheme"+first.getUid());
                        if(notificationItem != null) {
                            sendNotification(assignments, details, contentText, context);
                            addNewItem(notificationItem);
                        }
                        return;
                    }
                } else if(10 <= difference && difference <= 15) {
                    boolean display15MinWarning =  prefs.getBoolean("sch15Min", true);
                    if(display15MinWarning) {
                        Log.d("NOTIF", "<15min");
                        //Info
                        contentText = difference + " min left to deadline";
                        details[0] = "Deadline: " + stopTime;
                        details[1] = "Assignment: " + title;
                        details[2] = "Next assignment in current traffic: " + getCurrentTrafficTime(assignments, 1) + " min";
                        notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"scheme"+first.getUid());
                        if(notificationItem != null) {
                            sendNotification(assignments, details, contentText, context);
                            addNewItem(notificationItem);
                        }
                        return;
                    }
                }
            }
        }
    }

    /**
     * Sends a notification.
     * @param assignments
     * @param details
     * @param contentText
     * @param context
     */
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

        String notificationId = uid+"schema";

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
        return 0.4;//distance;
    }

    public int getCurrentTrafficTime(Vector<Assignment> assignments, int i) {
        //Hämta lat, long för nästa uppdrag, i = 1.
        // Beräkna current traffic time och returnera.
        return 30;
    }

    public void addNewItem(final NotificationItem noti) {
        MainActivity.getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                MainActivity.getNotificationAdapter().add(noti);
                MainActivity.getNotificationAdapter().notifyDataSetChanged();
            }
        });
    }
}