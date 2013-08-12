package com.ismobile.blaagent.sqlite;

import java.util.Date;

/**
 * Notification items used for storing notifications in database and displaying in feed
 * Created by pbm on 2013-07-10.
 */
public class NotificationItem {
    private String uid;
    private String title;
    private CharSequence contentText;
    private float latitude;
    private float longitude;
    private String details;
    private String start;
    private String stop;
    private String dateCreated;
    private String type;

    public NotificationItem() {
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
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

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
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

