package com.ismobile.blaagent.Test;

import com.ismobile.blaagent.Assignment;

/**
 * Created by pbm on 2013-07-17.
 */
public class Test {

    public Assignment createTestAssignment(String start, String stop) {
        String title = "TestAssignment";
        String uid = "X654HFGHWfghf534GFD";
        //"yyyy-MM-dd HH:mm";
        float lati = 0.5f;
        float longi = 0.4f;
        boolean booked = true;
        Assignment ass = new Assignment(title, uid, booked, start, stop, lati, longi );

        return ass;
    }
}

