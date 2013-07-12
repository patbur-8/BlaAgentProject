package com.ismobile.blaagent;

import android.app.ListActivity;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ismobile.blaagent.sqlite.NotificationItem;
import com.ismobile.blaagent.sqlite.NotificationItemsDataSource;

import java.util.List;
import java.util.Random;

public class MainActivity extends ListActivity {
    private List<NotificationItem> values;
    private NotificationAdapter adapter;
    private NotificationItemsDataSource datasource;
    StatusNotificationIntent sn = new StatusNotificationIntent(this);
    BackgroundService bs = new BackgroundService(this);
    private ListView listView1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle","onCreate");
        setContentView(R.layout.activity_main);
        bs = new BackgroundService(this);
        bs.connect();

        datasource = new NotificationItemsDataSource(this);
        datasource.open();

        values = datasource.getAllNotificationItems();

        // Use the SimpleCursorAdapter to show the
        // elements in a ListView
        adapter = new NotificationAdapter(this, R.layout.custom_list_item, values);
        //setListAdapter(adapter);


        listView1 = (ListView)findViewById(android.R.id.list);

        View header = getLayoutInflater().inflate(R.layout.listview_header_row, null);
        listView1.addHeaderView(header);

        listView1.setAdapter(adapter);
    }

    // Will be called via the onClick attribute
    // of the buttons in main.xml
   public void onClick(View view) {
        @SuppressWarnings("unchecked")
        //NotificationAdapter adapter = (NotificationAdapter) listView1.getAdapter();
        NotificationItem notificationItem = null;
        switch (view.getId()) {
            case R.id.add:
                Log.d("WORKING", "ISIT2");
                String[] NotificationItems = new String[] { "Cool", "Very nice", "Hate it" };
                int nextInt = new Random().nextInt(3);
                // Save the new NotificationItem to the database

                notificationItem = datasource.createNotificationItem("HEJSAN2222","qwe","RWER","123","124",0.1f,0.2f, NotificationItems ,"5 man123");
                Log.d("WORKING", "ISIT3");
                adapter.add(notificationItem);
                Log.d("WORKING", "ISIT4");
                //adapter.notifyDataSetChanged();
                Log.d("WORKING", "ISIT5");
                break;
            case R.id.delete:
                if (getListAdapter().getCount() > 0) {
                    notificationItem = (NotificationItem) getListAdapter().getItem(0);
                    datasource.deleteNotificationItem(notificationItem);
                    adapter.remove(notificationItem);
                }
                break;
        }
       //((NotificationAdapter)((ListView)findViewById(android.R.id.list)).getAdapter()).notifyDataSetChanged();
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle","onDestroy");
        bs.removeBroadCastListener();
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    /**
     * Click of the buttons.
     * @param view
     */


    /*public void onClick(View view) {
        /*switch (view.getId()) {
            case R.id.button: // Info
                sn.sendNotification(1);
                break;
            case R.id.button2: // Warning
                sn.sendNotification(2);
                break;
            case R.id.button3: // Error
                sn.sendNotification(3);
                break;
        }
    }*/
}
