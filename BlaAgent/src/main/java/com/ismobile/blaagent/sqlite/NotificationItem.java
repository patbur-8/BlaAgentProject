package com.ismobile.blaagent.sqlite;

import java.util.Date;

/**
 * Created by pbm on 2013-07-10.
 */
public class NotificationItem
{
    private String uid;
    private String title;
    private CharSequence contentText;
    private float latitude;
    private float longitude;
    private String[] details;
    private String start;
    private String stop;
    private Date dateCreated;
    private String type;

    public NotificationItem() {
        this.dateCreated = new Date();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CharSequence getContentText() {
        return contentText;
    }

    public void setContentText(CharSequence contentText) {
        this.contentText = contentText;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String[] getDetails() {
        return details;
    }

    public void setDetails(String[] details) {
        this.details = details;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return title;
    }
}

/*
package com.ismobile.blaagent.sqlite;

import java.util.Date;

public class NotificationItem
{
    private String uid;
    private String title;
    private CharSequence contentText;
    private float latitude;
    private float longitude;
    private String[] details;
    private String start;
    private String stop;
    private Date dateCreated;
    private String type;

    public NotificationItem(String title, String uid, String contentText, String start, String stop,
                            float lati, float longi, String[] details, String type) {
        this.title = title;
        this.uid = uid;
        this.contentText = contentText;
        this.start = start;
        this.stop = stop;
        this.latitude = lati;
        this.longitude = longi;
        this.details = details;
        this.title = title;
        this.type = type;
        this.dateCreated = new Date();
    }

    public String getUid() {
        return uid;
    }

    public String getTitle() {
        return title;
    }


    public CharSequence getContentText() {
        return contentText;
    }


    public float getLongitude() {
        return longitude;
    }


    public float getLatitude() {
        return latitude;
    }

    public String[] getDetails() {
        return details;
    }


    public String getStart() {
        return start;
    }


    public String getStop() {
        return stop;
    }


    public Date getDateCreated() {
        return dateCreated;
    }

    public String getType() {
        return type;
    }


    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return title;
    }
}

*/