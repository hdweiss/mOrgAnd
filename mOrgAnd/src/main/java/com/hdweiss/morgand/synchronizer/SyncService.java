package com.hdweiss.morgand.synchronizer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.synchronizer.writer.SyncWriterTask;

public class SyncService extends Service implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String ACTION = "action";
    private static final String START_ALARM = "START_ALARM";
    private static final String STOP_ALARM = "STOP_ALARM";

    private SharedPreferences appSettings;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private boolean alarmScheduled = false;
    private AsyncTask syncTask;

    @Override
    public void onCreate() {
        super.onCreate();
        this.appSettings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        this.appSettings.registerOnSharedPreferenceChangeListener(this);
        this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void onDestroy() {
        unsetAlarm();
        this.appSettings.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public static void stopAlarm(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(ACTION, SyncService.STOP_ALARM);
        context.startService(intent);
    }

    public static void startAlarm(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(ACTION, SyncService.START_ALARM);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return 0;
        String action = intent.getStringExtra(ACTION);
        if (action != null && action.equals(START_ALARM))
            setAlarm();
        else if (action != null && action.equals(STOP_ALARM))
            unsetAlarm();
        else if (syncTask == null || syncTask.getStatus() == AsyncTask.Status.FINISHED)
            runSynchronizerAsync();
        return 0;
    }

    private void runSynchronizerAsync() {
        unsetAlarm();
        syncTask = new SyncWriterTask(getBaseContext()).execute();
        setAlarm();
    }


    private void setAlarm() {
        boolean doAutoSync = this.appSettings.getBoolean("sync_auto", false);
        if (!this.alarmScheduled && doAutoSync) {

            int interval = Integer.parseInt(
                    this.appSettings.getString("sync_frequency", "1800000"),
                    10);

            this.alarmIntent = PendingIntent.getService(Application.getInstace(), 0, new Intent(
                    this, SyncService.class), 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC,
                    System.currentTimeMillis() + interval, interval,
                    alarmIntent);

            this.alarmScheduled = true;
        }
    }

    private void unsetAlarm() {
        if (this.alarmScheduled) {
            this.alarmManager.cancel(this.alarmIntent);
            this.alarmScheduled = false;
        }
    }

    private void resetAlarm() {
        unsetAlarm();
        setAlarm();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals("sync_auto")) {
            boolean syncAuto = preferences.getBoolean("sync_auto", false);
            if (syncAuto && !this.alarmScheduled)
                setAlarm();
            else if (syncAuto == false && this.alarmScheduled)
                unsetAlarm();
        } else if (key.equals("sync_frequency"))
            resetAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
