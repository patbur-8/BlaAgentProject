package com.ismobile.blaagent;

import android.content.Context;

import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public abstract class NotificationType {

    public abstract void evaluate(Vector<Assignment> assignments, Assignment previous, Context context);
    public abstract void sendNotification(Vector<Assignment> assignments, String[] details, CharSequence contentText, Context context);


}
