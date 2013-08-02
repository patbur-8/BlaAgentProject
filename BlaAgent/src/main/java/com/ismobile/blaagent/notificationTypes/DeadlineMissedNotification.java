package com.ismobile.blaagent.notificationTypes;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.ismobile.blaagent.Assignment;
import com.ismobile.blaagent.GetDirections;
import com.ismobile.blaagent.MainActivity;
import com.ismobile.blaagent.MyLocation;
import com.ismobile.blaagent.NotificationAction;
import com.ismobile.blaagent.R;
import com.ismobile.blaagent.StatusNotificationIntent;
import com.ismobile.blaagent.Test.Test;
import com.ismobile.blaagent.sqlite.NotificationItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by ats on 2013-07-15.
 */
public class DeadlineMissedNotification extends NotificationType {
    private int nrOfCriticAssignments;
    private final int MISSBOOKED = 0;
    private final int MISSNEXTASS = 1;
    private final int NOTMISS = 2;
    GetDirections dir = new GetDirections();
    private int missStatus;
    /**
     *
     * @param assignments
     * @param context
     * @return
     */
    @Override
    public void evaluate(Vector<Assignment> assignments, Assignment previous, Context context) {

        NotificationItem notificationItem;
        String contentText;
        String[] details;
        Test test = new Test();
        Assignment first = test.createTestAssignment("2013-07-30 17:15", "2013-07-31 15:08", "hgfd732jgfd7y3hgfd2");
        assignments.add(0,first);
        previous = test.createTestAssignment("2013-07-30 09:11", "2013-07-30 11:08", "hgfd732jgfd7y32");//assignments.firstElement();
        String stopTime = previous.getStop();

        // My location.
        float latitude = previous.getLatitude();
        float longitude = previous.getLongitude();
        double distance = getDistance(latitude, longitude);

        Date d1 = null, d2 = null;
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);

        missStatus = checkIfMakeNextAss(assignments);

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
                    if (missStatus == MISSBOOKED) {
                        // Miss the booked meeting
                        details = new String [2];
                        contentText = "You will miss your next booked meeting with the current" +
                                " traffic time.";
                        details[0] = "Booked meeting starting: " + getNextBooked(assignments).getStart();
                        details[1] = "Assignment: " + getNextBooked(assignments).getTitle();
                        notificationItem = MainActivity.getDatasource().createNotificationItem(previous, contentText, details ,"deadMB"+previous.getUid());
                        if(notificationItem != null) {
                            Log.d("Sending: ", "MISSBOOKED");
                            sendNotification(assignments, details, contentText, context);
                            addNewItem(notificationItem);
                        }
                    } else if (missStatus == NOTMISS) {
                        // Make the booked meeting, next assignment or assignment.size() = null.

                        //IF no upcoming assignments
                        if(assignments.size() == 0) {
                            contentText = "The deadline for this assignment has passed.";
                            details = new String [1];
                            details[0] =  "The deadline for this assignment has passed but you have no upcoming assignments.";
                            notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"deadNM"+first.getUid());
                            if(notificationItem != null) {
                                Log.d("Sending: ", "NOTMISS");
                                sendNotification(assignments, details, contentText, context);
                                addNewItem(notificationItem);
                            }
                        } else if(getNextBooked(assignments) != null) {
                            if(timeToLeaveForNextAss(assignments)) {
                                details = new String [3];
                                contentText = "You have to leave this assignment now";
                                details[0] = "Next assignment starts at: " + first.getStart();
                                details[1] = "Assignment: " + first.getTitle();
                                //details[2] = "Next assignment in current traffic: " + getCurrentTrafficTime(assignments, 0) + " min";
                                notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"deadNM"+first.getUid());
                                if(notificationItem != null) {
                                    Log.d("Sending: ", "NOTMISS+booked");
                                    sendNotification(assignments, details, contentText, context);
                                    addNewItem(notificationItem);
                                }
                            }
                        } else {
                            if(timeToLeaveForNextAss(assignments)) {
                                details = new String [3];
                                contentText = "You have to leave this assignment now";
                                details[0] = "Next assignment starts at: " + first.getStart();
                                details[1] = "Assignment: " + first.getTitle();
                                //details[2] = "Next assignment in current traffic: " + getCurrentTrafficTime(assignments, 0) + " min";
                                notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"deadNM"+first.getUid());
                                if(notificationItem != null) {
                                    Log.d("Sending: ", "NOTMISS+notbooked");
                                    sendNotification(assignments, details, contentText, context);
                                    addNewItem(notificationItem);
                                }
                            }
                        }


                    } else if (missStatus == MISSNEXTASS) {
                        // Miss the next assignment
                        details = new String [2];
                        contentText = "You will miss your next assignment with the current" +
                                " traffic time.";
                        details[0] = "Next assignment starting: " + assignments.firstElement().getStart();
                        details[1] = "Assignment: " + assignments.firstElement().getTitle();
                        notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"deadMN"+first.getUid());
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
        // Opens Blå Agent and show the list of critical assignments.
        // Will make the user choose an assignment to skip.
        resultIntent = new Intent("com.ismobile.blaagent.XXX");


        boolean bigStyle = true;

        if (missStatus == NOTMISS) {
            notiActions = new NotificationAction[2];
            notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
            notiActions[1] = new NotificationAction(R.drawable.google_maps_logo, "", mapsIntent);
        } else if (missStatus == MISSNEXTASS) {
            contentTitle = "Next assignment";
            notiActions = new NotificationAction[1];
            notiActions[0] = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
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

    public double getCurrentTrafficTime(String from, String to, boolean traffic) {
        // Beräkna current traffic time och returnera.
        // Get estimated drive time.
        int realTimeInSec;
        double realTime = -1;
        double time = 9999;
        try {
            JSONObject obj = dir.getDirectionsJSON(from, to);
            //Log.d("JSON", obj.getInt("realTime")+"");
            Log.d("JSON", obj.getJSONObject("route").getInt("time")+"");
            if(traffic) {
                realTimeInSec = obj.getJSONObject("route").getInt("realTime");
                if(realTimeInSec > 0) {
                    realTime = secondsToMinute(realTimeInSec);
                }
            }

            time = secondsToMinute(obj.getJSONObject("route").getInt("time"));
            Log.d("JSON",time +", " + realTime);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(realTime > 0) return realTime;
        return time;
    }

    public boolean timeToLeaveForNextAss(Vector<Assignment> assignments) {
        //Date variables
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        Date currentTime = new Date();
        Date startTime = null;

        String start = assignments.firstElement().getStart();
        try {
            startTime = df.parse(start);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //double totalTime = ((startTime.getTime() - currentTime.getTime())/(1000*60)) - getCurrentTrafficTime(assignments, 0);
        //return !(totalTime > 0);
        return true;
    }

    public double calculateTotalTime(Vector<Assignment> assignments, int nrOfAssignments) {
        Long difference = 0L;
        double totalTime = 0;
        String locationOfLastAss = "";
        Date startTime = null, stopTime = null, nextAssStartTime = null;
        String from, to;
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);

        from = getMyLocation();

        for (int i=0; i<nrOfAssignments; i++) {
            String start = assignments.elementAt(i).getStart();
            String stop = assignments.elementAt(i).getStop();
            try {
                startTime = df.parse(start);
                stopTime = df.parse(stop);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            difference = (stopTime.getTime() - startTime.getTime())/(1000*60);
            if (i == 0) {
                to = assignments.elementAt(i).getLatitude() + "," + assignments.elementAt(i).getLongitude();
                locationOfLastAss = to;
                totalTime += difference + getCurrentTrafficTime(to, from, true);
            } else {
                to = assignments.elementAt(i).getLatitude() + "," + assignments.elementAt(i).getLongitude();
                totalTime += difference + getCurrentTrafficTime(to, locationOfLastAss, false);
                locationOfLastAss = to;
            }
        }
        return totalTime;
    }

    /**
     * Check if we have to skip an assignment if we missed a deadline.
     * @param assignments
     * @return
     */
    public int checkIfMakeNextAss(Vector<Assignment> assignments) {
        Log.d("size", assignments.size() + "");
        Assignment nextBooked = getNextBooked(assignments);
        Assignment nextAss = assignments.firstElement();
        double totalTime = 0;

        //Date variables
        String myFormatString = "yyyy-MM-dd HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        Date currentTime = new Date();

        //http://www.mapquestapi.com/directions/v1/route?key=Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14&fr
        // om=59.33,18.07&to=59.4333,17.95&callback=renderNarrative sollentuna-sthlm.

        // Check if we have any booked meetings today.
        if (nextBooked != null) {
            // We have a booked meeting.
            Long difference;
            Date startTime = null, stopTime = null, nextAssStartTime = null;
            String from, to;

            // Calculate the drive time between the next assignments to the booked meeting.
            // assignments.indexOf(nextAss); is either 0 or 1.


            // Will the total drive time + estimated work time exceed the stop time for the
            // booked assignment.
            String nextBookedStart = nextBooked.getStart();
            try {
                nextAssStartTime = df.parse(nextBookedStart);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // A new date with current time + drive time + estimated work time.
            Date newDate = new Date((long) (currentTime.getTime() + totalTime));
            Log.d("newDate", df.format(newDate));
            Log.d("nextDate", df.format(nextAssStartTime));
            if (newDate.after(nextAssStartTime)) {
                Log.d("notify", MISSBOOKED + "");
                return MISSBOOKED;
            }

        } else {
            // We do not have booked meeting
            // Check if we will make it to next assignment
          /*  if (assignments.firstElement() != null) {
                Date starTime = null;
                String start = nextAss.getStart();
                try {
                    starTime = df.parse(start);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                double checkTime =  getCurrentTrafficTime(assignments, 0)/(1000*60) + currentTime.getTime();
                Date newDate = new Date();
                newDate.setMinutes(currentTime.getMinutes()+getCurrentTrafficTime(assignments,0));
                Log.d("newDate", df.format(newDate)+"");

                if (newDate.after(starTime)) return MISSNEXTASS;
            }*/
        }

        Log.d("notify", NOTMISS + "");
        return NOTMISS;
    }

    public String getMyLocation() {
        Location loc = MainActivity.getMyLocation();
        return loc.getLatitude() + "," + loc.getLongitude();
    }


    public double secondsToMinute(int seconds) {
        return seconds/60;
    }
}