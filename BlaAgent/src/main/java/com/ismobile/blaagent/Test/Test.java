package com.ismobile.blaagent.Test;

import com.ismobile.blaagent.Assignment;

import java.util.Random;

/**
 * Created by pbm on 2013-07-17.
 */
public class Test {

    public Assignment createTestAssignment(String start, String stop, String uid) {
        String title = "TestAssignment wohooohowohoooho wohooohowohoooho wohoooho wohooohowohooohowohoooho";
        //"yyyy-MM-dd HH:mm";
        float lati = 59.33019f;
        float longi = 18.05723f;
        boolean booked = true;
        Assignment ass = new Assignment(title, uid, booked, start, stop, lati, longi );

        return ass;
    }
}

