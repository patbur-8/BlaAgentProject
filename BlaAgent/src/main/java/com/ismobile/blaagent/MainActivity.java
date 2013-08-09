package com.ismobile.blaagent;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import com.ismobile.blaagent.Test.Test;
import com.ismobile.blaagent.menuSettings.SettingsActivity;
import com.ismobile.blaagent.sqlite.NotificationItem;
import com.ismobile.blaagent.sqlite.NotificationItemsDataSource;

import java.util.List;

public class MainActivity extends ListActivity  {
    private List<NotificationItem> values;
    private static NotificationAdapter adapter;
    private static NotificationItemsDataSource datasource;
    StatusNotificationIntent sn = new StatusNotificationIntent(this);
    SharedPreferences prefs;
    BackgroundService bs;
    private ListView listView1;
    private static Handler handler;
    static MyLocation loc;
    private static boolean isTestEnabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle","onCreate");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        //Creates and opens an new data source.
        datasource = new NotificationItemsDataSource(this);
        datasource.open();

        //Retrieves the items from the database
        values = datasource.getAllNotificationItems();

        // Use the SimpleCursorAdapter to show the
        // elements in a ListView
        adapter = new NotificationAdapter(this, R.layout.custom_list_item, values);

        listView1 = (ListView)findViewById(android.R.id.list);

        //Adds the header row
        View header = getLayoutInflater().inflate(R.layout.listview_header_row, null);
        listView1.addHeaderView(header);

        listView1.setAdapter(adapter);
        loc = new MyLocation(this);
        getMyLocation();
        //Creates an instace of the background service and connects.
        isTestEnabled = prefs.getBoolean("testEnabled", true);
        if(isTestEnabled) {
            new DownloadFilesTask().execute(this);
        } else {
            bs = new BackgroundService(this);
            bs.connect();
        }

    }

    public static NotificationAdapter getNotificationAdapter() {
        return adapter;
    }

    public static void addNotificationItem(NotificationItem noti) {
        adapter.data.add(0, noti);
        Log.d("STEP SEVEN", "Notify data set changed");
        adapter.notifyDataSetChanged();
    }

    public static NotificationItemsDataSource getDatasource() {
        return datasource;
    }

    public static Handler getUIHandler() {
        return handler;
    }
    public static Location getMyLocation() {
        Location location;
        if(isTestEnabled) {
            //TESTSET;
            location = stringToLocation(Test.getMyLocation());
        } else {
            location = loc.getLocation();
        }
        return location;
    }

    public static Location stringToLocation(String loc) {
        String[] latlong = loc.split(",");
        Location location = new Location("");
        location.setLatitude(Double.parseDouble(latlong[0]));
        location.setLongitude(Double.parseDouble(latlong[1]));
        return location;
    }


    public static boolean testEnabled() {
        return isTestEnabled;
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle","onDestroy");
        datasource.close();
        if(bs != null) bs.removeBroadCastListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    //Opens BlaAndroid to view a certain assignment
    protected void onListItemClick (ListView l, View v, int position, long id) {
        NotificationItem noti = adapter.getNoti(position);
        Intent resultIntent = new Intent("com.ismobile.blaandroid.showAssDetails");
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        resultIntent.putExtra("com.ismobile.blaandroid.showAssDetails", noti.getUid());
        TaskStackBuilder resultStackBuilder = TaskStackBuilder.create(this);
        resultStackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = resultStackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            resultPendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private class DownloadFilesTask extends AsyncTask<Context , Void, Void> {

        @Override
        protected Void doInBackground(Context... context) {
            Test test = new Test();
            test.setMyLocation(18.05123f,59.33010f);
            Assignment prev = test.createPrevious();
            test.runTest(test.createAssignmentList(), prev,context[0]);
            return null;
        }
    }
}
