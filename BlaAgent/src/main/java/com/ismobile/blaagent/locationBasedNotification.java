package com.ismobile.blaagent;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.ismobile.blaagent.Test.Test;
import com.ismobile.blaagent.sqlite.NotificationItem;

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
    public void evaluate(Vector<Assignment> assignments, Context context) {
        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        NotificationItem notificationItem;
        Test test = new Test();
        String contentText;
        Assignment first = test.createTestAssignment("2013-07-18 11:09", "2013-07-18 23:05");//assignments.firstElement();
        String start = first.getStart();
        String stop = first.getStop();
        String current= new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());

        String[] details = null;
        // My location.
        float latitude = first.getLatitude();
        float longitude = first.getLongitude();
        double distance = getDistance(latitude, longitude);

        // Date object.
        Date currentTime = null, startTime = null, stopTime = null;
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        try {
            currentTime = df.parse(current);
            startTime = df.parse(start);
            stopTime = df.parse(stop);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timePassed = (currentTime.getTime() - startTime.getTime())/(1000*60);
        Log.d("locationB", "TimePassed:" + (timePassed <= 6));
        if (timePassed <= 6) {
            Log.d("locationB", (!currentTime.before(startTime) && currentTime.before(stopTime)) + "");
            Log.d("locationB", "StartTime: " + (startTime.getTime()));
            Log.d("locationB", "StopTime: " + (stopTime.getTime()));
            Log.d("locationB", "CurrentTime: " + (currentTime.getTime()));
            if ((currentTime.after(startTime) || currentTime.equals(startTime)) && currentTime.before(stopTime)) {
                if (!(0 <= distance && distance <= 0.5)) {
                    Log.d("NOTIF", "Fungerar!!!");
                    contentText = "A new assignment has started and you are not in place.";
                    notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"loc"+first.getUid());
                    if(notificationItem != null) {
                        Log.d("LocationB", "kommer jag hit?");
                        sendNotification(assignments, details, contentText, context);
                        addNewItem(notificationItem);
                    }
                }
            }
        }
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
        return  0.6;//distance;
    }
}
