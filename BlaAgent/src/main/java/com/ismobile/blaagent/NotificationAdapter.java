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
        Log.d("WHEREISIT","getView");
        View row = convertView;
        NotificationHolder holder = null;
        Log.d("WHEREISIT","getView2");
        if(row == null) {
            Log.d("WHEREISIT","getView3");
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
            Log.d("WHEREISIT","getView4");
            holder = (NotificationHolder)row.getTag();
        }
        Log.d("WHEREISIT","getView5");
        NotificationItem noti = data.get(position);
        Log.d("WHEREISIT","getView6");
        String details = "eje";
        /*for(int i = 0; i<noti.getDetails().length-1;i++) {
            details = details + noti.getDetails()[i];
            if(i < noti.getDetails().length-1) {
                details = details + "\n";
            }
        }*/
        Log.d("WHEREISIT","getView7");
        holder.txtDetail.setText(details);
        Log.d("WHEREISIT","getView7");
        holder.txtTitle.setText(noti.getTitle());
        holder.txtDate.setText(noti.getDateCreated().toString());

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
