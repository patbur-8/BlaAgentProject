package com.ismobile.blaagent.Test;

import android.content.Context;
import android.util.Log;

import com.ismobile.blaagent.Assignment;
import com.ismobile.blaagent.notificationTypes.DeadlineMissedNotification;
import com.ismobile.blaagent.notificationTypes.LocationBasedNotification;
import com.ismobile.blaagent.notificationTypes.ScheduleNotification;

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
    public Test() {
        currentTime = new Date();
        currentTime.setMinutes(0);
        currentTime.setHours(6);
        currentTime.setSeconds(0);
        sn = new ScheduleNotification();
        lbn = new LocationBasedNotification();
        dmn = new DeadlineMissedNotification();
    }


    public Assignment createTestAssignment(String start, String stop, String uid) {
        String title = "TestAssignment wohooohowohoooho wohooohowohoooho wohoooho wohooohowohooohowohoooho";
        //"yyyy-MM-dd HH:mm";
        float lati = 59.33019f;
        float longi = 18.05723f;
        boolean booked = true;
        Assignment ass = new Assignment(title, uid, booked, start, stop, lati, longi );

        return ass;
    }

    static public Date getCurrentDate() {
        return currentTime;
    }

    public void addMinutesToDate(int minutes) {
        long t = currentTime.getTime();
        currentTime = new Date(t+(minutes*60000));
    }

    public void runTest(Vector<Assignment> assignments, Assignment previous, Context context){
        Assignment first = assignments.firstElement();
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
            //sn.evaluate(assignments,previous,context);
            lbn.evaluate(assignments,previous,context);
            //dmn.evaluate(assignments, previous,context);
            addMinutesToDate(5);
        }
    }

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

    static public String getMyLocation() {
        Log.d("TEST",myLocation);
        return myLocation;
    }

    public Vector<Assignment> createAssignmentList() {
        Vector<Assignment> assignments = new Vector<Assignment>();
        assignments.add(createTestAssignment("2013-08-05 09:13", "2013-08-05 11:23", "gdfbg45n331j42"));
        return assignments;
    }

    public Assignment createPrevious() {
        Assignment previous = createTestAssignment("2013-07-22 10:00", "2013-07-22 16:23", "ghfd3dfbg45n3j42");
        return previous;
    }

}

