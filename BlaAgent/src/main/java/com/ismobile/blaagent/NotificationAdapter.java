package com.ismobile.blaagent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ismobile.blaagent.sqlite.NotificationItem;

import java.util.HashMap;
import java.util.List;

/**
 * Created by pbm on 2013-07-11.
 */

//The custom array adapter for displaying notification items in a list.
public class NotificationAdapter extends ArrayAdapter {

    Context context;
    int layoutResourceId;
    List<NotificationItem> data = null;

    /**
     * Constructor.
     * @param context
     * @param layoutResourceId
     * @param data
     */
    public NotificationAdapter(Context context, int layoutResourceId, List<NotificationItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    /**
     * Sets the view of the list.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        NotificationHolder holder = null;
        final NotificationItem noti = data.get(position);
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new NotificationHolder();
            if (checkIfUseMapsIntent(noti)) {

                holder.imgMaps = (ImageView)row.findViewById(R.id.imgMaps);
                holder.imgMaps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?f=d&daddr=" + noti.getLatitude() + "," + noti.getLongitude()));
                        mapsIntent.setComponent(new ComponentName("com.google.android.apps.maps",
                                "com.google.android.maps.MapsActivity"));
                        mapsIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        context.startActivity(mapsIntent);
                    }
                });
            } else {
                holder.imgMaps = (ImageView)row.findViewById(R.id.imgMaps);
                holder.imgMaps.setVisibility(View.GONE);
            }

            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtDetail = (TextView)row.findViewById(R.id.txtDetail);
            holder.txtDate = (TextView)row.findViewById(R.id.txtDate);
            row.setTag(holder);
        }
        else {
            holder = (NotificationHolder)row.getTag();
        }

        holder.txtDetail.setText(noti.getContentText() + "\n" + noti.getDetails().replace("#!%", "\n"));
        holder.txtTitle.setText(noti.getTitle());
        holder.txtDate.setText(noti.getDateCreated());
        return row;
    }

    /**
     * Returns a notification item.
     * @param position
     * @return
     */
    public NotificationItem getNoti(int position) {
        return data.get(position-1);
    }

    /**
     *
     */
    static class NotificationHolder {
        ImageView imgMaps;
        TextView txtTitle;
        TextView txtDetail;
        TextView txtDate;
    }

    /**
     * Checks if a link to google maps should be in the list with an item.
     * @param noti
     * @return
     */
    public boolean checkIfUseMapsIntent(NotificationItem noti) {
        String type = noti.getType();
        String deadlineMissNotMiss = "deadNM0" + noti.getUid();
        String deadlineMissed = "deadNM1" + noti.getUid();

        // Not in use at the moment. They all returns true, they should all show the imgMaps.
        /*String deadlineMissBooked = "deadMB" + noti.getUid();
        String deadlineMissNotMissBooked = "deadNMB" + noti.getUid();
        String deadlineMissNotMissNextAss = "deadNMN" + noti.getUid();
        String deadlineMissNextAss = "deadMN" + noti.getUid();
        String locationBased = "loc" + noti.getUid();
        String schedule5 = "scheme5" + noti.getUid();
        String schedule15 = "scheme15" + noti.getUid();*/

       if (type.equals(deadlineMissNotMiss)) {
            return false;
        } else if (type.equals(deadlineMissed)) {
           return false;
       }
        return true;
    }

}
