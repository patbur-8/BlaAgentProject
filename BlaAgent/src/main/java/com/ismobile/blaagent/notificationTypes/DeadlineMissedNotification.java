package com.ismobile.blaagent.notificationTypes;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.ismobile.blaagent.Assignment;
import com.ismobile.blaagent.GetDirections;
import com.ismobile.blaagent.MainActivity;
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
import java.util.Date;
import java.util.Vector;

/**
 * If the deadline has passed for an assignment and the user is still in place,
 * this notification will appear.
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
     * Evaluates if a notification should be sent or not.
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

        // My location.
        float latitude = previous.getLatitude();
        float longitude = previous.getLongitude();
        double distance = getDistance(latitude, longitude);

        String from = getMyLocation();
        String to = first.getLatitude() + "," + first.getLongitude();

        missStatus = checkIfMakeNextAss(assignments);

        if (distance <= 0.5) {
            if (assignments.size() > 0) {
                // Check if stop time has passed --> We missed a deadline.
                Date currentTime = new Date();
                Date stopTime = getDateFromString(previous.getStop());
                if (currentTime.after(stopTime)) {

                    // Miss the booked meeting.
                    if (missStatus == MISSBOOKED) {
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

                    // NOT miss:
                    // *booked meeting
                    // *next assignment or
                    // *if no upcoming assignments.
                    } else if (missStatus == NOTMISS) {

                        //If no upcoming assignments.
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

                        // If we have a booked meeting.
                        } else if(getNextBooked(assignments) != null) {
                            if(timeToLeaveForNextAss(assignments)) {
                                details = new String [3];
                                contentText = "You have to leave this assignment now";
                                details[0] = "Next assignment starts at: " + first.getStart();
                                details[1] = "Assignment: " + first.getTitle();
                                details[2] = "Next assignment in current traffic: " + getCurrentTrafficTime(from,to,true) + " min";
                                notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"deadNM"+first.getUid());
                                if(notificationItem != null) {
                                    Log.d("Sending: ", "NOTMISS+booked");
                                    sendNotification(assignments, details, contentText, context);
                                    addNewItem(notificationItem);
                                }
                            }

                        // If we have a next assignment.
                        } else {
                            if(timeToLeaveForNextAss(assignments)) {
                                details = new String [3];
                                contentText = "You have to leave this assignment now";
                                details[0] = "Next assignment starts at: " + first.getStart();
                                details[1] = "Assignment: " + first.getTitle();
                                details[2] = "Next assignment in current traffic: " + getCurrentTrafficTime(from,to,true) + " min";
                                notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,"deadNM"+first.getUid());
                                if(notificationItem != null) {
                                    Log.d("Sending: ", "NOTMISS+notbooked");
                                    sendNotification(assignments, details, contentText, context);
                                    addNewItem(notificationItem);
                                }
                            }
                        }

                    // Miss the next assignment.
                    } else if (missStatus == MISSNEXTASS) {
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

    /**
     * Adds new notification to the database.
     * @param noti
     */
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
     * Sends a notification.
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

        boolean bigStyle = true;

        // Adds intent/intents depending on what type of notification that will be sent.
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

        // Builds the notification.
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

    /**
     * Gets estimated drive time.
     * @param from
     * @param to
     * @param traffic
     * @return
     */
    public double getCurrentTrafficTime(String from, String to, boolean traffic) {
        int realTimeInSec;
        double realTime = -1;
        double time = 9999;

        try {
            JSONObject obj = dir.getDirectionsJSON(from, to);
            if(traffic) {
                realTimeInSec = obj.getJSONObject("route").getInt("realTime");
                if(realTimeInSec > 0) {
                    realTime = secondsToMinute(realTimeInSec);
                }
            }
            time = secondsToMinute(obj.getJSONObject("route").getInt("time"));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(realTime > 0) return realTime;
        return time;
    }

    /**
     * Returns true if we need to leave the current assignment, means the user is in a hurry.
     * @param assignments
     * @return
     */
    public boolean timeToLeaveForNextAss(Vector<Assignment> assignments) {
        Date currentTime = new Date();
        Date startTime = getDateFromString(assignments.firstElement().getStart());
        String from = getMyLocation();
        String to = assignments.firstElement().getLatitude() + "," + assignments.firstElement().getLongitude();

        double totalTime = ((startTime.getTime() - currentTime.getTime())/(1000*60)) - getCurrentTrafficTime(from,to,true);
        return !(totalTime > 0);
    }

    /**
     * Calculates the total time from now to see if we will make the booked assignment or next.
     * @param assignments
     * @param nrOfAssignments
     * @return
     */
    public double calculateTotalTime(Vector<Assignment> assignments, int nrOfAssignments) {
        Long estimatedWorkTime = 0L;
        double totalTime = 0;
        String locationOfLastAss = "";
        Date startTime = null, stopTime = null;
        String from = getMyLocation();
        String to;

        for (int i=0; i<nrOfAssignments; i++) {
            startTime = getDateFromString(assignments.elementAt(i).getStart());
            stopTime = getDateFromString(assignments.elementAt(i).getStop());
            estimatedWorkTime = (stopTime.getTime() - startTime.getTime())/(1000*60);

            if (i == 0) {
                to = assignments.elementAt(i).getLatitude() + "," + assignments.elementAt(i).getLongitude();
                locationOfLastAss = to;
                totalTime += estimatedWorkTime + getCurrentTrafficTime(to, from, true);
            } else {
                to = assignments.elementAt(i).getLatitude() + "," + assignments.elementAt(i).getLongitude();
                totalTime += estimatedWorkTime + getCurrentTrafficTime(to, locationOfLastAss, false);
                locationOfLastAss = to;
            }
        }
        return totalTime;
    }

    /**
     * Check if the user will make it to the next booked meeting or assignment.
     * @param assignments
     * @return
     */
    public int checkIfMakeNextAss(Vector<Assignment> assignments) {
        Assignment nextBooked = getNextBooked(assignments);
        Assignment nextAss = assignments.firstElement();
        Date currentTime = new Date();

        // Check if there is any booked meetings today.
        if (nextBooked != null) {
            double totalTime = calculateTotalTime(assignments, nrOfCriticAssignments);
            Date nextBookedStartTime = getDateFromString(nextBooked.getStart());

            // A new date with current time + drive time + estimated work time.
            Date newDate = new Date((long) (currentTime.getTime() + totalTime));
            if (newDate.after(nextBookedStartTime)) {
                Log.d("notify", "MISSBOOKED");
                return MISSBOOKED;
            }

        } else {
            // Check if we will make it to next assignment.
            if (assignments.firstElement() != null) {
                double totalTime = calculateTotalTime(assignments, 1);
                Date nextAssStarTime = getDateFromString(nextAss.getStart());

                // A new date with current time + drive time + estimated work time.
                Date newDate = new Date((long) (currentTime.getTime() + totalTime));
                if (newDate.after(nextAssStarTime)) {
                    Log.d("notify", "MISSNEXTASS");
                    return MISSNEXTASS;
                }
            }
        }

        Log.d("notify", "NOTMISS");
        return NOTMISS;
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

    /**
     * Gets my location with help from the GPS on the phone.
     * @return
     */
    public String getMyLocation() {
        Location loc = MainActivity.getMyLocation();
        return loc.getLatitude() + "," + loc.getLongitude();
    }

    /**
     * Returns seconds into minutes.
     * @param seconds
     * @return
     */
    public double secondsToMinute(int seconds) {
        return seconds/60;
    }
}