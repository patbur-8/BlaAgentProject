package com.ismobile.blaagent.Test;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ismobile.blaagent.Assignment;
import com.ismobile.blaagent.MainActivity;
import com.ismobile.blaagent.notificationTypes.DeadlineMissedNotification;
import com.ismobile.blaagent.notificationTypes.LocationBasedNotification;
import com.ismobile.blaagent.notificationTypes.ScheduleNotification;
import com.ismobile.blaagent.sqlite.NotificationItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Created by pbm on 2013-07-17.
 */
public class Test {

    static Date currentTime;
    static String myLocation = "";
    private ScheduleNotification sn;
    private LocationBasedNotification lbn;
    private DeadlineMissedNotification dmn;
    private int assignmentNumber = 1;
    private int TIME_THRESHOLD;
    SharedPreferences prefs;
    public Test() {
        currentTime = new Date();
        currentTime.setMinutes(0);
        currentTime.setHours(6);
        currentTime.setSeconds(0);
        sn = new ScheduleNotification();
        lbn = new LocationBasedNotification();
        dmn = new DeadlineMissedNotification();
    }


    public Assignment createTestAssignment(String start, String stop, String uid, float lati, float longi, boolean booked) {
        String title = "TestAssignment " + assignmentNumber;
        //"yyyy-MM-dd HH:mm";

        Assignment ass = new Assignment(title, uid, booked, start, stop, lati, longi );
        assignmentNumber ++;
        return ass;
    }

    static public Date getCurrentDate() {
        return currentTime;
    }

    public void addMinutesToDate(int minutes) {
        long t = currentTime.getTime();
        currentTime = new Date(t+(minutes*60000));
    }

    public void setDataChanged() {
        MainActivity.getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                MainActivity.getNotificationAdapter().notifyDataSetChanged();

            }
        });
    }

    /**
     * Runs evaluate on a list of assignment while adding time between the runs
     * @param assignments
     * @param previous
     * @param context
     */
    public void runTest(Vector<Assignment> assignments, Assignment previous, Context context){
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        TIME_THRESHOLD = Integer.parseInt(prefs.getString("prefTimeInterval","5"));
        //If assignnment list is empty, evaluate deadlineNoti on previous.
        if (assignments.size() == 0) {
            setMyLocation(59.4433f, 17.942f);

            while(!isStopTimeBeforeCurrentTime(previous.getStop())) {
                dmn.evaluate(assignments, previous,context);
                addMinutesToDate(5);
            }
            dmn.evaluate(assignments, previous,context);
            Log.d("WhileEnded",""+Test.getCurrentDate());
        } else {
            Assignment first = assignments.firstElement();
            setMyLocation(59.4433f, 17.942f);

            //Remove old assignments
            while(assignments.size() >= 1) {
                Log.w("asSize2",assignments.size()+"");
                Log.w("asCurrentTime",currentTime.getTime()+"");
                if(isStopTimeBeforeCurrentTime(first.getStop())) {
                    Log.d("SLUT","SLUT");

                        previous = first;
                        assignments.removeElementAt(0);
                    if(assignments.size() >= 1) {
                        first = assignments.firstElement();
                    } else {
                        break;
                    }
                }
                //evaluate
                sn.evaluate(assignments,previous,context);
                lbn.evaluate(assignments,previous,context);
                dmn.evaluate(assignments, previous,context);
                //Add some minutes to current time and run again
                addMinutesToDate(5);
            }
        }
        dmn.evaluate(assignments, previous,context);
        setDataChanged();
    }

    /**
     * Checks if the stop time is in the past
     * @param stopTime
     * @return
     */
    public boolean isStopTimeBeforeCurrentTime(String stopTime) {
        String myFormatString = "yyyy-MM-dd HH:mm"; // for example
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        try {
            Date d1 = df.parse(stopTime);
            Log.d("TIME1", d1.getTime()+"");
            Log.d("TIME2", currentTime.getTime()+"");
            return d1.before(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setMyLocation(float lati, float longi) {
        myLocation = lati +","+ longi;
    }

    /**
     * Returns "my location"
     * @return
     */
    static public String getMyLocation() {
        Log.d("TEST",myLocation);
        return myLocation;
    }

    /**
     * Creates a vector filled with new assignments
     * @return
     */
    public Vector<Assignment> createAssignmentList() {
        Vector<Assignment> assignments = new Vector<Assignment>();
        assignments.add(createTestAssignment("2013-08-09 09:15", "2013-08-09 10:15", "bbbbbbbbbbbbb",59.4433f, 17.942f,false)); //sollentuna
        assignments.add(1,createTestAssignment("2013-08-09 11:00", "2013-08-09 11:25", "ccccccccccccc",59.3337f, 18.056f,true)); //sthlm c
        assignments.add(2,createTestAssignment("2013-08-09 11:30", "2013-08-09 12:30", "ddddddddddddd",59.30932f, 18.16613f,false)); //nacka

        return assignments;
    }

}

