package com.ismobile.blaagent.notificationTypes;

import android.content.Context;

import com.ismobile.blaagent.Assignment;

import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public abstract class NotificationType {

    /**
     * Evaluate.
     * This is where you write the code for evaluating if a notification should be sent or not.
     * @param assignments
     * @param previous
     * @param context
     */
    public abstract void evaluate(Vector<Assignment> assignments, Assignment previous, Context context);

    /**
     * SendNotification.
     * This is where you assemble the information for the notification and call the notification builder.
     * @param assignments
     * @param details
     * @param contentTitle
     * @param contentText
     * @param context
     */
    public abstract void sendNotification(Vector<Assignment> assignments, String[] details,String contentTitle, CharSequence contentText, Context context);
}
