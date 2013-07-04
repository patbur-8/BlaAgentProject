package com.ismobile.blaagent;

import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public abstract class NotificationType {

    public abstract boolean evaluate(Vector<Assignment> assignments);
    public abstract void sendNotification();


}
