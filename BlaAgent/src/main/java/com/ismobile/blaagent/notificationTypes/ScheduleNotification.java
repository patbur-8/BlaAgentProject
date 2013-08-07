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
 * If the deadline is close, this notification will appear.
 * Created by pbm on 2013-07-04.
 */
public class ScheduleNotification extends NotificationType {
    double DISTANCE_THRESHOLD = 0.5;
    int EARLY_WARNING = 15;
    int LATE_WARNING = 5;
    SharedPreferences prefs;
    Boolean testEnabled;
    GetDirections dir = new GetDirections();

    /**
     * Evaluates if a notification should be sent or not.
     * @param assignments
     * @param context
     * @return
     */
    @Override
    public void evaluate(Vector<Assignment> assignments, Assignment previous, Context context) {
        if(assignments.size() == 0) return;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayNotification = prefs.getBoolean("schEnabled", true);
        DISTANCE_THRESHOLD = Double.parseDouble(prefs.getString("prefDistanceThreshold", "0.5"));
        LATE_WARNING = Integer.parseInt(prefs.getString("prefLateWarning","5"));
        EARLY_WARNING = Integer.parseInt(prefs.getString("prefEarlyWarning","15"));
        testEnabled = prefs.getBoolean("testEnabled", true);
        if(!displayNotification) return;

        // Assignments is sorted by stop time. Earliest stop time  = first element in vector.
        NotificationItem notificationItem;
        String contentText;
        Assignment first = assignments.firstElement();
        String title = first.getTitle();
        String[] details;
        String from, to;

        float latitude = first.getLatitude();
        float longitude = first.getLongitude();
        from = getMyLocation();

        double distance = getDistance(stringToLocation(from),latitude, longitude);
        boolean hasNext = assignments.size() > 1;
        if(hasNext) {
            details = new String[3];
        } else {
            details = new String[2];
        }

        if (distance <= DISTANCE_THRESHOLD) { // Check if we are in place.

            Date currentTime = getCurrentDate();
            Date stopTime = getDateFromString(first.getStop());
            Long difference = (stopTime.getTime() - currentTime.getTime())/(1000*60);
            String contentTitle = assignments.firstElement().getTitle();
            String notiType;
            if(0 <= difference && difference <= 6) {
                boolean display5MinWarning =  prefs.getBoolean("sch5Min", true);
                if(display5MinWarning) {
                    Log.d("NOTIF", "<5min");
                    notiType = "scheme5" + first.getUid();
                    //Warning
                    contentText = difference + " min left to deadline";
                    details[0] = "Deadline: " + stopTime;
                    details[1] = "Assignment: " + title;
                    if(hasNext) {
                        to = assignments.elementAt(1).getLatitude() + "," + assignments.elementAt(1).getLongitude();
                        details[2] = "Next assignment in current traffic: " +
                                getCurrentTrafficTime(from,to,true) + " min";
                    }
                    notificationItem = MainActivity.getDatasource().createNotificationItem(
                            first, contentText, details ,notiType);
                    if(notificationItem != null) {
                        sendNotification(assignments, details, contentTitle, contentText, context);
                        addNewItem(notificationItem);
                    }
                    return;
                }
            } else if(10 <= difference && difference <= 16) {
                boolean display15MinWarning =  prefs.getBoolean("sch15Min", true);
                if(display15MinWarning) {
                    Log.d("NOTIF", "<15min");
                    notiType = "scheme15" + first.getUid();
                    //Info
                    contentText = difference + " min left to deadline";
                    details[0] = "Deadline: " + stopTime;
                    details[1] = "Assignment: " + title;
                    if(hasNext) {
                        to = assignments.elementAt(1).getLatitude() + "," + assignments.elementAt(1).getLongitude();
                        details[2] = "Next assignment in current traffic: " +
                                getCurrentTrafficTime(from,to,true) + " min";
                    }
                    notificationItem = MainActivity.getDatasource().createNotificationItem(first, contentText, details ,notiType);
                    if(notificationItem != null) {
                        sendNotification(assignments, details, contentTitle, contentText, context);
                        addNewItem(notificationItem);
                    }
                    return;
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
    public void sendNotification(Vector<Assignment> assignments, String[] details, String contentTitle, CharSequence contentText, Context context) {
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
    public double getDistance(Location myLocation, float latitude, float longitude) {

        // The assignments location.
        Location aLocation = new Location("");
        aLocation.setLatitude(latitude);
        aLocation.setLongitude(longitude);

        int distance = (int)aLocation.distanceTo(myLocation) / 1000; // Distance in km.
        String str = " (" + String.valueOf(distance) + " km)";
        Log.d("distance", str);
        return 0.4; //distance;
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject obj = dir.getDirectionsJSON(from, to);
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
     * Returns seconds into minutes.
     * @param seconds
     * @return
     */
    public double secondsToMinute(int seconds) {
        return seconds/60;
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
     * Gets my location with help from the GPS on the phone.
     * @return
     */
    public String getMyLocation() {
        Location loc = MainActivity.getMyLocation();
        return loc.getLatitude() + "," + loc.getLongitude();
    }

    /**
     * Adds new notification to the database.
     * @param noti
     */
    public void addNewItem(final NotificationItem noti) {
        MainActivity.getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                MainActivity.addNotificationItem(noti);

            }
        });
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