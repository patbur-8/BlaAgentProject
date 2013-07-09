package com.ismobile.blaagent;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
    StatusNotificationIntent sn = new StatusNotificationIntent(this);
    BackgroundService bs = new BackgroundService(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle","onCreate");
        setContentView(R.layout.activity_main);
        bs = new BackgroundService(this);
        bs.connect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onStop() {
        super.onStop();
        Log.d("Lifecycle","onStop");
        bs.removeBroadCastListener();
    }

    public void onRestart() {
        super.onRestart();
        Log.d("Lifecycle","onRestart");
        bs.connect();
    }
    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle","onDestroy");
        bs.removeBroadCastListener();
    }

    /**
     * Click of the buttons.
     * @param view
     */


    public void onClick(View view) {
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
        }*/
    }
}
