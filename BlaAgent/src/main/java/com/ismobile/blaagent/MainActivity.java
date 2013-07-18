package com.ismobile.blaagent;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ismobile.blaagent.sqlite.NotificationItem;
import com.ismobile.blaagent.sqlite.NotificationItemsDataSource;

import java.util.List;

public class MainActivity extends ListActivity  {
    private List<NotificationItem> values;
    private static NotificationAdapter adapter;
    private static NotificationItemsDataSource datasource;
    StatusNotificationIntent sn = new StatusNotificationIntent(this);
    BackgroundService bs = new BackgroundService(this);
    private ListView listView1;
    private static Handler handler;
    private static final int RESULT_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle","onCreate");
        setContentView(R.layout.activity_main);
        bs = new BackgroundService(this);
        bs.connect();
        handler = new Handler();
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

        showUserSettings();
    }

    // Will be called via the onClick attribute
    // of the buttons in main.xml


    public static NotificationAdapter getNotificationAdapter() {
        return adapter;
    }

    public static NotificationItemsDataSource getDatasource() {
        return datasource;
    }

    public static Handler getUIHandler() {
        return handler;
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
        datasource.close();
        bs.removeBroadCastListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent i = new Intent(this, UserSettingActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                showUserSettings();
                break;

        }

    }

    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        StringBuilder builder = new StringBuilder();

        builder.append("\n Username: "
                + sharedPrefs.getString("prefUsername", "NULL"));

        builder.append("\n Send report:"
                + sharedPrefs.getBoolean("prefSendReport", false));

        builder.append("\n Sync Frequency: "
                + sharedPrefs.getString("prefSyncFrequency", "NULL"));

        TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);

        settingsTextView.setText(builder.toString());
    }

}
