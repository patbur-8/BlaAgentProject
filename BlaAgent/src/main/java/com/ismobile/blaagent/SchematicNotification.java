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
 * Created by pbm on 2013-07-04.
 */
public class SchematicNotification extends NotificationType {
    private int nrOfCriticAssignments;

    /**
     * Evaluates what type of notification we want to send.
     * @param assignments
     * @param context
     * @return
     */
    @Override
    public boolean evaluate(Vector<Assignment> assignments, Context context) {
        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        CharSequence contentText;
        String title = assignments.firstElement().getTitle();
        String stopTime = assignments.firstElement().getStop();
        int currentDriveTime = 30;
        String[] details = new String [3];
        boolean booked = assignments.firstElement().getBooked();

        // My location.
        float latitude = assignments.firstElement().getLatitude();
        float longitude = assignments.firstElement().getLongitude();
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
                if (difference > 15) {
                    Log.d("NOTIF", ">15min");
                    //Do nothing

                } else if (13 <= difference && difference <= 15) {
                    Log.d("NOTIF", "<15min");
                    //Info
                    contentText = difference + " min left to deadline";
                    details[0] = "Deadline: " + stopTime;
                    details[1] = "Assignment: " + title;
                    details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
                    sendNotification(assignments, details, contentText, context);

                } else if (4 <= difference && difference <= 5) {
                    Log.d("NOTIF", "<5min");
                    //Warning
                    contentText = difference + " min left to deadline";
                    details[0] = "Deadline: " + stopTime;
                    details[1] = "Assignment: " + title;
                    details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
                    sendNotification(assignments, details, contentText, context);

                } else if (difference <= 0) {
                    Log.d("NOTIF", "<0min");
                    //Error
                    contentText = difference + " min, time is up!!!";
                    details[0] = "Deadline: " + stopTime;
                    details[1] = "Assignment: " + title;
                    details[2] = "Next assignment in current traffic: " + currentDriveTime + " min";
                    sendNotification(assignments, details,  contentText, context);
                    // Kolla här om vi kommer hinna till nästa bokade mötet.
                }
            }
        }
        return false;
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

    /**
     * Returns the first booked assignment.
     * @param assignments
     * @return
     */
    public Assignment deadlineMiss(Vector<Assignment> assignments) {
        nrOfCriticAssignments = 0;
        for (int i=0; i<assignments.size(); i++) {
            if (assignments.elementAt(i).getBooked()) {
                nrOfCriticAssignments = i;
                return assignments.elementAt(i);
            }
        }
        return null;
    }

    public void checkTime(Vector<Assignment> assignments) {
        int currentDriveTime = 30;
        Assignment nextBooked = deadlineMiss(assignments);
        if (nextBooked != null) {
            Long difference;
            double totalTime = 0;

            Date d1 = null, d2 = null;
            String myFormatString = "yyyy-MM-dd HH:mm";
            SimpleDateFormat df = new SimpleDateFormat(myFormatString);

            // Notification:
            // Calculate the drive time between the next assignments to the booked assignment.

            for (int i=0; i<=nrOfCriticAssignments; i++) {
                //float tola = assignments.elementAt(i).getLatitude();
                //float tolo = assignments.elementAt(i).getLongitude();
                String start = assignments.elementAt(i).getStart();
                String stop = assignments.elementAt(i).getStop();

                try {
                    d1 = df.parse(start);
                    d2 = df.parse(stop);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                difference = (d2.getTime() - d1.getTime())/(1000*60);
                totalTime += difference + currentDriveTime;
            }

            // kommer den missas:
            String nextBookedStop = nextBooked.getStop();
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
            try {
                d1 = df.parse(currentTime);
                d2 = df.parse(nextBookedStop);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            double result = (d2.getTime() - d1.getTime())/(1000*60); //ÄNDRA TILL RÄTT TID.
        }
//                    --Deadline miss--
//                    vi har en bokad
//                      vi vet vilket element de är
//                      vi vet hur många element innan som inte är bokade
//                      Notifiering:
//                          räkna ut körtiden mha currentTraffic --
//                              om vi hinner: mäta vanliga tiden och gå efter den. hitta tjänst att använda!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//                                  notify: Skynda!!!! visa stoptiden för nästa uppdrag
//                              om vi inte hinner:
//                                  visa lista, förslag på uppdrag att skippa
//                                  teknikern får välja en i listan
//                                  de uppdraget tas bort ur assignments
//                                  visas i feeden highlightad! --> länkas till BlåAndroid.
    }
}