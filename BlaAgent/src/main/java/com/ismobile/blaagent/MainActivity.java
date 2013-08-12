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
import com.ismobile.blaagent.sqlite.SQLHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        //Retrieves the items from the database between to dates
        values = getValuesFromDataSource();

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
            new RunTest().execute(this);
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

    /**
     *
     * @return
     */
    public static Handler getUIHandler() {
        return handler;
    }

    /**
     * Returns "My location"
     * If test is enabled, a fake location will be
     * @return
     */
    public static Location getMyLocation() {
        Location location;
        if(isTestEnabled) {
            location = stringToLocation(Test.getMyLocation());
        } else {
            location = loc.getLocation();
        }
        return location;
    }

    /**
     * Converts a string to a location object
     * @param loc
     * @return
     */
    public static Location stringToLocation(String loc) {
        String[] latlong = loc.split(",");
        Location location = new Location("");
        location.setLatitude(Double.parseDouble(latlong[0]));
        location.setLongitude(Double.parseDouble(latlong[1]));
        return location;
    }

    /**
     * Retrieves the notification items from database
     * @return
     */
    public List<NotificationItem> getValuesFromDataSource() {
        boolean filterToday;
        filterToday = prefs.getBoolean("filterToday",true);
        Date fromDate = new Date();
        fromDate.setHours(0);
        fromDate.setMinutes(0);
        fromDate.setSeconds(0);

        Date toDate = new Date();
        toDate.setHours(24);
        toDate.setMinutes(0);
        toDate.setSeconds(0);
        List<NotificationItem> vals = null;
        if(filterToday) {

            vals = datasource.getAllNotificationItems(fromDate, toDate);
        } else {
            String myFormatString = "yyyy-MM-dd";
            SimpleDateFormat df = new SimpleDateFormat(myFormatString);

            String fromPref = prefs.getString("filterFrom",df.format(fromDate));
            String toPref = prefs.getString("filterTo",df.format(toDate));


            try {
                vals = datasource.getAllNotificationItems(df.parse(fromPref), df.parse(toPref));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return vals;
    }

    /**
     * Returns if test is enabled or not
     * @return
     */
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
            case R.id.action_filter:
                filterList();
                return true;
            case R.id.action_clear:
                SQLHelper.clearDatabase(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens the settings activity
     */
    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Opens the filter activity
     */
    public void filterList() {
        Intent filter = new Intent(this,FilterListview.class);
        startActivityForResult(filter, 1);
    }

    /**
     * Close the datasource and remove the broadcast listener
     */
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if(resultCode == RESULT_OK){
                //Filter
                updateFilter();
            }
        }
    }

    //Updates the feed after setting a new filter
    public void updateFilter() {
        values = getValuesFromDataSource();
        if(values != null) {
            adapter.data.clear();
            for (int i = 0; i < values.size();i++) {
                adapter.add(values.get(i));
            }
            adapter.notifyDataSetChanged();
        }
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

    /**
     * Private class that executes the test in a background thread
     */
    private class RunTest extends AsyncTask<Context , Void, Void> {

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
