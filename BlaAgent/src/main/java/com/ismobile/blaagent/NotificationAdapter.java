package com.ismobile.blaagent;

import android.app.Activity;
import android.content.Context;
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
public class NotificationAdapter extends ArrayAdapter {

    Context context;
    int layoutResourceId;
    List<NotificationItem> data = null;

    public NotificationAdapter(Context context, int layoutResourceId, List<NotificationItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        NotificationHolder holder = null;
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new NotificationHolder();
            holder.imgBlaAndroid = (ImageView)row.findViewById(R.id.imgBlaAndroid);
            holder.imgMaps = (ImageView)row.findViewById(R.id.imgMaps);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtDetail = (TextView)row.findViewById(R.id.txtDetail);
            holder.txtDate = (TextView)row.findViewById(R.id.txtDate);

            row.setTag(holder);
        }
        else
        {
            holder = (NotificationHolder)row.getTag();
        }
        NotificationItem noti = data.get(position);

        holder.txtDetail.setText(noti.getDetails());
        holder.txtTitle.setText(noti.getTitle());
        holder.txtDate.setText(noti.getDateCreated() +"");
        return row;
    }

    static class NotificationHolder
    {
        ImageView imgBlaAndroid;
        ImageView imgMaps;
        TextView txtTitle;
        TextView txtDetail;
        TextView txtDate;
    }
}
