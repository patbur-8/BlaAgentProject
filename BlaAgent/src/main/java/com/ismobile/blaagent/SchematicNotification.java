package com.ismobile.blaagent;

import android.content.Context;
import android.content.Intent;

import java.util.Vector;

/**
 * Created by pbm on 2013-07-04.
 */
public class SchematicNotification extends NotificationType {
    @Override
    public boolean evaluate(Vector<Assignment> assignments, Context context) {
        Assignment firstAss = assignments.firstElement();

        sendNotification(assignments, context);
        return false;
    }

    @Override
    public void sendNotification(Vector<Assignment> assignments, Context context) {
        CharSequence contentTitle = assignments.firstElement().getTitle();
        CharSequence contentText = "hejja";
        String uid = assignments.firstElement().getUid();
        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
            resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", uid);
        String[] details = new String [3];
        details[0] = "första";
        details[1] = "andra";
        details[2] = "tredje";
        boolean bigStyle = true;
        NotificationAction[] notiActions = new NotificationAction[2];
        NotificationAction na = new NotificationAction(R.drawable.ic_launcher, "", resultIntent);
        notiActions[0] = na;
        na = new NotificationAction(R.drawable.google_maps_logo, "", resultIntent);
        notiActions[1] = na;
        StatusNotificationIntent sni = new StatusNotificationIntent(context);
        sni.buildNotification(contentTitle,contentText,resultIntent,details,bigStyle,notiActions);
    }
}
