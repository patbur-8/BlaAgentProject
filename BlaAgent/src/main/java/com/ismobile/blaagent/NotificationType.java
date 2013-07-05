package com.ismobile.blaagent;

import android.content.Context;

import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public abstract class NotificationType {

    public abstract boolean evaluate(Vector<Assignment> assignments, Context context);
    public abstract void sendNotification(Vector<Assignment> assignments, Context context);


}
