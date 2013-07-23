package com.ismobile.blaagent.notificationTypes;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
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
 * Created by ats on 2013-07-15.
 */
public class DeadlineMissedNotification extends NotificationType {
    private int nrOfCriticAssignments;
    private final int MISSBOOKED = 0;
    private final int MISSNEXTASS = 1;
    private final int NOTMISS = 2;

    /**
     *
     * @param assignments
     * @param context
     * @return
     */
    @Override
    public void evaluate(Vector<Assignment> assignments, Assignment previous, Context context) {
        // Assignments is sorted by stop time. Earliest stop time  = first element in assignments.
        NotificationItem notificationItem;
        String contentText;
        Test test = new Test();
        Assignment first = test.createTestAssignment("2013-07-23 09:11", "2013-07-23 15:50", "hgfd732jgfd7y32");//assignments.firstElement();
        String stopTime = first.getStop();

        // My location.
        float latitude = first.getLatitude();
        float longitude = first.getLongitude();
        double distance = 0.4; //getDistance(latitude, longitude);

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

                if (d1.after(d2)) {
                    Log.d("deadlinemiss: ", "d1.after(d2)");
                    // Deadline miss.
                    if (checkIfMakeNextAss(assignments) == MISSBOOKED) {
                        // Miss the booked meeting
                        String[] details = new String [2];
                        contentText = "You will miss your next booked meeting with the current" +
                                "traffic time.";
                        details[0] = "Booked meeting starting: " + getNextBooked(assignments).getStart();
                        details[1] = "Assignment: " + getNextBooked(assignments).getTitle();
                        notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"scheme"+first.getUid());
                        if(notificationItem != null) {
                            Log.d("Sending: ", "MISSBOOKED");
                            sendNotification(assignments, details, contentText, context);
                            addNewItem(notificationItem);
                        }

                    } else if (checkIfMakeNextAss(assignments) == NOTMISS){
                        // Make the booked meeting
                        String[] details = new String [3];
                        int assignmentNr = 1;
                        contentText = "You have to leave this assignment now. Next booked meeting starts: " + getNextBooked(assignments).getStart();
                        details[0] = "Next assignment starts at: " + getNextAssigment(assignments).getStart();
                        details[1] = "Assignment: " + getNextAssigment(assignments).getTitle();
                        details[2] = "Next assignment in current traffic: " + getCurrentTrafficTime(assignments, assignmentNr) + " min";
                        notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"scheme"+first.getUid());
                        if(notificationItem != null) {
                            Log.d("Sending: ", "NOTMISS");
                            sendNotification(assignments, details, contentText, context);
                            addNewItem(notificationItem);
                        }

                    } else if (checkIfMakeNextAss(assignments) == MISSNEXTASS) {
                        // Miss the next assignment
                        String[] details = new String [2];
                        contentText = "You will miss your next assignment with the current" +
                                "traffic time.";
                        details[0] = "Next assignment starting: " + getNextAssigment(assignments).getStart();
                        details[1] = "Assignment: " + getNextAssigment(assignments).getTitle();
                        notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"scheme"+first.getUid());
                        if(notificationItem != null) {
                            Log.d("Sending: ", "MISSNEXTASS");
                            sendNotification(assignments, details, contentText, context);
                            addNewItem(notificationItem);
                        }
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
    /**
     *
     * @param assignments
     * @param details
     * @param contentText
     * @param context
     */
    @Override
    public void sendNotification(Vector<Assignment> assignments, String[] details, CharSequence contentText, Context context) {
        CharSequence contentTitle = "Booked meeting";
        String uid = assignments.firstElement().getUid();
        Intent resultIntent;
        Intent mapsIntent = null;
        NotificationAction[] notiActions;

        if (checkIfMakeNextAss(assignments) == NOTMISS) {
            // Make the booked meeting
            // Opens Blå Android and show assignment.
            resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
            resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);

            float longi = assignments.firstElement().getLongitude();
            float lati = assignments.firstElement().getLatitude();

            // Opens google maps, from: My Location to: an assignments lat, long.
            mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?f=d&daddr=" + lati + "," + longi));
            mapsIntent.setComponent(new ComponentName("com.google.android.apps.maps",
                    "com.google.android.maps.MapsActivity"));
        } else {
            // Opens Blå Agent and show the list of critical assignments.
            // Will make the user choose an assignment to skip.
            resultIntent = new Intent("com.ismobile.blaagent.XXX");
        }

        boolean bigStyle = true;

        if (checkIfMakeNextAss(assignments) == NOTMISS) {
            notiActions = new NotificationAction[2];
            notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
            notiActions[1] = new NotificationAction(R.drawable.google_maps_logo, "", mapsIntent);
        } else {
            notiActions = new NotificationAction[1];
            notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
        }

        String notificationId = uid+"deadlineMiss";

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
        return 0.4; //distance;
    }

    /**
     * Returns the first booked assignment.
     * @param assignments
     * @return
     */
    public Assignment getNextBooked(Vector<Assignment> assignments) {
        nrOfCriticAssignments = 0;
        for (int i=0; i<assignments.size(); i++) {
            if (assignments.elementAt(i).getBooked()) {
                nrOfCriticAssignments = i;
                return assignments.elementAt(i);
            }
        }
        return null;
    }

    public int getCurrentTrafficTime(Vector<Assignment> assignments, int i) {
        //Hämta lat, long för nästa uppdrag, i = 1.
        // Beräkna current traffic time och returnera.
        return 30;
    }

    public Assignment getNextAssigment(Vector<Assignment> assignments) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
        String startTime = assignments.firstElement().getStart();
        String stopTime = assignments.firstElement().getStop();
        Date d1 = null, d2 = null, d3 = null;
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        try {
            d1 = df.parse(currentTime);
            d2 = df.parse(stopTime);
            d3 = df.parse(startTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (d1.after(d3) && d1.before(d2)) {
            return assignments.firstElement();
        } else if (assignments.elementAt(1) != null){
            return assignments.elementAt(1);
        } else {
            return null;
        }
    }

    /**
     * Check if we have to skip an assignment if we missed a deadline.
     * @param assignments
     * @return
     */
    public int checkIfMakeNextAss(Vector<Assignment> assignments) {
        Assignment nextBooked = getNextBooked(assignments);
        Assignment nextAss = getNextAssigment(assignments);
        double totalTime = 0;

        // Check if we have any booked meetings today.
        if (nextBooked != null) {
            // We have a booked meeting.
            Long difference;
            Date d1 = null, d2 = null;
            String myFormatString = "yyyy-MM-dd HH:mm";
            SimpleDateFormat df = new SimpleDateFormat(myFormatString);

            // Calculate the drive time between the next assignments to the booked assignment.
            // assignments.indexOf(nextAss); is either 0 or 1.
            for (int i=assignments.indexOf(nextAss); i<nrOfCriticAssignments; i++) {
                String start = assignments.elementAt(i).getStart();
                String stop = assignments.elementAt(i).getStop();
                try {
                    d1 = df.parse(start);
                    d2 = df.parse(stop);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                difference = (d2.getTime() - d1.getTime())/(1000*60);
                totalTime += difference + getCurrentTrafficTime(assignments, i);
            }

            // Will the total drive time + estimated work time exceed the stop time for the
            // booked assignment.
            String nextBookedStart = nextBooked.getStart();
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
            try {
                d1 = df.parse(currentTime);
                d2 = df.parse(nextBookedStart);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // A new date with current time + drive time + estimated work time.
            Date newDate = new Date((long) (d1.getTime() + (2L * totalTime)));

            if (newDate.after(d2)) {
                return MISSBOOKED;
            }

        } else {
            // We do not have booked meeting
            // Check if we will make it to next assignment
            if (nextAss != null) {
                Long difference;
                Date d1 = null, d2 = null, d3 = null;
                String myFormatString = "yyyy-MM-dd HH:mm";
                SimpleDateFormat df = new SimpleDateFormat(myFormatString);

                String start = nextAss.getStart();
                String stop = nextAss.getStop();
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
                try {
                    d1 = df.parse(start);
                    d2 = df.parse(stop);
                    d3 = df.parse(currentTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                difference = (d2.getTime() - d1.getTime())/(1000*60);
                totalTime += difference + getCurrentTrafficTime(assignments, assignments.indexOf(nextAss));

                // A new date with current time + drive time + estimated work time.
                Date newDate = new Date((long) (d3.getTime() + (2L * totalTime)));

                if (newDate.after(d2)) {
                    return MISSNEXTASS;
                }

            }
        }

        return NOTMISS;
    }
}