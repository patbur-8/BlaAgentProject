package com.ismobile.blaagent;

/**
 * Created by pbm on 2013-07-03.
 */

public class Assignment {
    private float longi;
    private float lati;
    private String title;
    private String uid;
    private boolean booked;
    private String start;
    private String stop;

    public Assignment(String title, String uid, boolean booked, String startTime, String stopTime, float latitude, float longitude) {
        this.title = title;
        this.uid = uid;
        this.booked = booked;
        this.start = startTime;
        this.stop = stopTime;
        this.lati = latitude;
        this.longi = longitude;
    }
    public String getTitle() {
        return this.title;
    }

    public String getUid() {
        return this.uid;
    }

    public String getStart() {
        return this.start;
    }

    public String getStop() {
        return this.stop;
    }

    public boolean getBooked() {
        return this.booked;
    }

    public float getLongitude() {
        return this.longi;
    }

    public float getLatitude() {
        return this.lati;
    }
}
