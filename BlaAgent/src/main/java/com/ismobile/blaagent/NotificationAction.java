package com.ismobile.blaagent;

import android.content.Intent;

/**
 * Created by pbm on 2013-07-05.
 */
public class NotificationAction {
    private int image;
    private String title;
    private Intent pi;
    public NotificationAction(int image, String title, Intent pi) {
        this.image = image;
        this.title = title;
        this.pi = pi;
    }

    public int getImage() {
        return this.image;
    }

    public String getTitle() {
        return this.title;
    }
    public Intent getIntent() {
        return this.pi;
    }
}
