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
import java.util.Date;
import java.util.Vector;

/**
 * If start time has passed for an assignment and the user is not in place,
 * this notification will appear.
 * Created by ats on 2013-07-12.
 */
public class LocationBasedNotification extends NotificationType {
    int TIME_THRESHOLD = 5;
    double DISTANCE_THRESHOLD = 0.5;
    SharedPreferences prefs;
    Test test;
    private String notificationType;

    /**
     * Evaluates if a notification should be sent or not.
     *
     * @param assignments a vector containing all the assignments.
     * @param previous The previous assignment, if any.
     * @param context
     */
    @Override
    public void evaluate(Vector<Assignment> assignments,Assignment previous, Context context) {
        if(assignments == null || assignments.size() == 0) return;
        //Checks in the settings if it's enabled or not
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayNotification = prefs.getBoolean("locOnNewAss", true);
        DISTANCE_THRESHOLD = Double.parseDouble(prefs.getString("prefDistanceThreshold","0.5"));
        TIME_THRESHOLD = Integer.parseInt(prefs.getString("prefTimeInterval","5"));
        Log.d("LOCONNEWASS",""+displayNotification);
        if(!displayNotification) return;

        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        NotificationItem notificationItem;

        String contentText;
        String contentTitle = assignments.firstElement().getTitle();
        Assignment first = assignments.firstElement();

        //Parse timestamp string to Date object, in order to be able to compare them.
        Date startTime = getDateFromString(first.getStart());
        Date stopTime = getDateFromString(first.getStop());
        Date currentTime = getCurrentDate();

        String[] details = null;

        // My location.
        float latitude = first.getLatitude();
        float longitude = first.getLongitude();
        double distance = getDistance(latitude, longitude);

        contentText = "Just started and you are not in place.";

        //The notification has to be unique for each notification type and assignment.
        //The notification type is used to be sure that each notification can only
        //be displayed once for each assignment.
        //A good solution is using the notification type + assignment uid like bellow.
        notificationType = "loc" + first.getUid();


        //If enough time has passed and the technician is not on the location of
        //the assignment we can just assume that the assignment is over.
        //But if it's within a certain threshold we can assume that the technician is late
        //for the assignment.
        long timePassed = (currentTime.getTime() - startTime.getTime())/(1000*60);
        if (timePassed <= TIME_THRESHOLD) {
            if ((currentTime.after(startTime) || currentTime.equals(startTime)) && currentTime.before(stopTime)) {
                if (!(distance <= DISTANCE_THRESHOLD)) {
                    Log.d("STEP ONE", "Create notification item");
                    Log.d("STEP - TYPE", notificationType);
                    notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details , notificationType);
                    if(notificationItem != null) {
                        Log.d("STEP FIVE", "Send notification");
                        Log.d("STEP - TYPE", notificationItem.getType());
                        Log.i("CHECKTYPE", "Location notification sent");
                        sendNotification(assignments, details, contentTitle, contentText, context);
                        Log.d("STEP SIX", "Add notification item");
                        addNewItem(notificationItem);
                    }
                }
            }
        }
    }

    /**
     * This is used to add a new notification item to the notification feed.
     * Copy this if your new notification type should be displayed in the feed.
     * @param noti
     */
    public void addNewItem(final NotificationItem noti) {
        MainActivity.getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d("STEP - TYPE", noti.getType());
                MainActivity.addNotificationItem(noti);

            }
        });
    }

    /**
     * This is where you create the actions and decide on the notification style.
     * @param assignments
     * @param details
     * @param contentText
     * @param context
     */
    @Override
    public void sendNotification(Vector<Assignment> assignments, String[] details, String contentTitle, CharSequence contentText, Context context) {
        String uid = assignments.firstElement().getUid();

        //Adding the result intent, this goes directly to a specific assignment in BlaAndroid
        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
        resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);

        float longi = assignments.firstElement().getLongitude();
        float lati = assignments.firstElement().getLatitude();

        // Opens google maps, from: My Location to: an assignments lat, long.
        Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?f=d&daddr=" + lati + "," + longi));
        mapsIntent.setComponent(new ComponentName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"));

        boolean bigStyle = false;
        NotificationAction[] notiActions = new NotificationAction[2];
        notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
        notiActions[1] = new NotificationAction(R.drawable.google_maps_logo, "", mapsIntent);

        //Build the notification using StatusNotificationIntent
        StatusNotificationIntent sni = new StatusNotificationIntent(context);
        sni.buildNotification(contentTitle,contentText,resultIntent,details,bigStyle,notiActions,notificationType);

    }

    /**
     * Gets the distance between the user and assignments location.
     * @param latitude
     * @param longitude
     * @return
     */
    public double getDistance(float latitude, float longitude) {
        // My location.
        Location location;
        boolean testEnabled = MainActivity.testEnabled();
        if(testEnabled) {
            //TESTSET;
            location = stringToLocation(test.getMyLocation());
        } else {
            location = MainActivity.getMyLocation();
        }

        // The assignments location.
        Location aLocation = new Location("");
        aLocation.setLatitude(latitude);
        aLocation.setLongitude(longitude);

        int distance = (int)aLocation.distanceTo(location) / 1000; // Distance in km.
        String str = " (" + String.valueOf(distance) + " km)";
        Log.d("distance", str);
        return distance;
    }

    /**
     *
     * @param loc
     * @return
     */
    public Location stringToLocation(String loc) {
        String[] latlong = loc.split(",");
        Location location = new Location("");
        location.setLatitude(Double.parseDouble(latlong[0]));
        location.setLongitude(Double.parseDouble(latlong[1]));
        return location;
    }

    /**
     * Converts a String to a Date.
     * @param time
     * @return
     */
    public Date getDateFromString(String time) {
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        Date date = new Date();
        try {
            date = df.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public Date getCurrentDate() {
        boolean testEnabled = MainActivity.testEnabled();
        if(testEnabled) {
            return Test.getCurrentDate();
        } else {
            return new Date();
        }
    }

}
