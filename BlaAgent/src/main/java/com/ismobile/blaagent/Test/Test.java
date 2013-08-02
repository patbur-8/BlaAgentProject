package com.ismobile.blaagent.Test;

import android.content.Context;

import com.ismobile.blaagent.Assignment;
import com.ismobile.blaagent.notificationTypes.DeadlineMissedNotification;
import com.ismobile.blaagent.notificationTypes.ScheduleNotification;
import com.ismobile.blaagent.notificationTypes.locationBasedNotification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

/**
 * Created by pbm on 2013-07-17.
 */
public class Test {
    static Date currentTime;
    private ScheduleNotification sn;
    private locationBasedNotification lbn;
    private DeadlineMissedNotification dmn;
    public Test() {
        sn = new ScheduleNotification();
        lbn = new locationBasedNotification();
        dmn = new DeadlineMissedNotification();
    }
    public Assignment createTestAssignment(String start, String stop, String uid) {
        String title = "TestAssignment";
        //"yyyy-MM-dd HH:mm";
        float lati = 59.33019f;
        float longi = 18.05723f;
        boolean booked = true;
        Assignment ass = new Assignment(title, uid, booked, start, stop, lati, longi );

        return ass;
    }

    public Date getCurrentDate() {
        return currentTime;
    }

    public void addToDate(int minutes) {
        long t = currentTime.getTime();
        currentTime = new Date(t+(minutes*60000));
    }

    public void runTest(Vector<Assignment> assignments, Assignment previous, Context context){
        Assignment first = assignments.firstElement();
        //Remove old assignments
        while(assignments.size() >= 1) {
            if(isStopTimeBeforeCurrentTime(first.getStop())) {
                if(assignments.size() >= 1) {
                    previous = first;
                    assignments.removeElementAt(0);
                    first = assignments.firstElement();
                } else {
                    break;
                }
            }
            //evaluate
            sn.evaluate(assignments,previous,context);
            lbn.evaluate(assignments,previous,context);
            dmn.evaluate(assignments, previous,context);
            addToDate(5);
        }
    }

    public boolean isStopTimeBeforeCurrentTime(String stopTime) {
        String myFormatString = "yyyy-MM-dd HH:mm"; // for example
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        try {
            Date d1 = df.parse(stopTime);
            return d1.before(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}

