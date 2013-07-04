package com.ismobile.blaagent;

import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public class SchematicNotification extends NotificationType {
    @Override
    public boolean evaluate(Vector<Assignment> assignments) {
        return false;
    }

    @Override
    public void sendNotification() {

    }
}
