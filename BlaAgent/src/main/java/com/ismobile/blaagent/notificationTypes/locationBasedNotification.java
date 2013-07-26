package com.ismobile.blaagent.notificationTypes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
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
public class locationBasedNotification extends NotificationType {
    static final int TIME_THRESHOLD = 6;
    static final int DISTANCE_THRESHOLD = 6;

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

        //Checks in the settings if it's enabled or not
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayNotification = prefs.getBoolean("locOnNewAss", true);
        Log.d("LOCONNEWASS",""+displayNotification);
        if(!displayNotification) return;

        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        NotificationItem notificationItem;

        //TEST, TA BORT SEN
        Test test = new Test();
        String contentText;
        Assignment first = test.createTestAssignment("2013-07-23 11:53", "2013-07-23 23:05","xFDGDF2234xfhhy24");//assignments.firstElement();
        assignments.add(0,first);

        //Start, stop and current timestamp
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String start = first.getStart();
        String stop = first.getStop();

        String[] details = null;
        // My location.
        float latitude = first.getLatitude();
        float longitude = first.getLongitude();
        double distance = getDistance(latitude, longitude);

        //Parse timestamp string to Date object, in order to be able to compare them.
        Date currentTime = null, startTime = null, stopTime = null;
        try {
            currentTime = new Date();
            startTime = df.parse(start);
            stopTime = df.parse(stop);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        contentText = "A new assignment has started and you are not in place.";

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
                    Log.d("NOTIF", "Fungerar!!!");

                    notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details , notificationType);
                    if(notificationItem != null) {
                        Log.d("LocationB", "kommer jag hit?");
                        sendNotification(assignments, details, contentText, context);
                        addNewItem(notificationItem);
                    }
                }
            }
        }
    }

    //This is used to add a new notification item to the notification feed
    //Copy this if your new notification type should be displayed in the feed.
    public void addNewItem(final NotificationItem noti) {
        MainActivity.getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                MainActivity.getNotificationAdapter().add(noti);
                MainActivity.getNotificationAdapter().notifyDataSetChanged();
            }
        });
    }

    /**
     * This is where you create the actions and decide on the notification style.
     *
     * @param assignments
     * @param details
     * @param contentText
     * @param context
     */
    @Override
    public void sendNotification(Vector<Assignment> assignments, String[] details, CharSequence contentText, Context context) {
        CharSequence contentTitle = assignments.firstElement().getTitle();

        String uid = assignments.firstElement().getUid();

        //Adding the result intent, this goes directly to a specific assignment in BlaAndroid
        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
        resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);

        boolean bigStyle = false;
        NotificationAction[] notiActions = new NotificationAction[1];
        notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);

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
        return  0.6;//distance;
    }
}
