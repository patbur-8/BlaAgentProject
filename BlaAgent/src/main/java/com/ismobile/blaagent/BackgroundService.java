package com.ismobile.blaagent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by pbm on 2013-06-24.
 */
public class BackgroundService {
    private final String alarmManagerAction = "com.ismobile.blaagent.alarmManagerAction";
    BroadcastReceiver receiver;
    PendingIntent pi;
    BAConnection bacon;
    private boolean registered = false;
    AlarmManager am;
    private PowerManager.WakeLock wl;
    private Context context;
    public BackgroundService(Context context) {
        Log.d("Superduper","constructor");
        this.context = context;
        bacon = new BAConnection(context);
        this.receiver = null;
    }

    public void initialize() {
        // We need to listen to connectivity events to update navigator.connection
        Log.d("Superduper","initialize");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(alarmManagerAction);
        if (this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "BlaAgent-BackgroundService");
                    wl.acquire();
                    // (The null check is for the ARM Emulator, please use Intel Emulator for better results)
                    Log.d("Superduper", "It works!");
                    bacon.startRetrieval();
                    wl.release();
                }
            };
            pi = PendingIntent.getBroadcast( context, 0, new Intent(alarmManagerAction),0 );
            context.registerReceiver(this.receiver, intentFilter);
            this.registered = true;
        }
    }

    protected void connect() {
        initialize();
        am = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
        am.setRepeating( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),20000, pi );
    }

    protected void removeBroadCastListener() {
        if (this.receiver != null) {
            try {
                am.cancel(pi);
                context.unregisterReceiver(this.receiver);
                this.registered = false;
                this.receiver = null;
            } catch (Exception e) {
            }
        }
    }
}
