package com.hdweiss.morgand;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hdweiss.morgand.events.SyncEvent;
import com.hdweiss.morgand.synchronizer.SyncService;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

public class Application extends android.app.Application {

    private static Application instace;
    public static Application getInstace() {
        return instace;
    }

    private static Bus bus;
    public static Bus getBus() {
        if (bus == null)
            bus = new Bus();
        return bus;
    }

    private SyncEvent syncEvent = new SyncEvent(SyncEvent.State.Done);

    @Produce public SyncEvent produceSyncEvent() {
        return this.syncEvent;
    }

    @Subscribe public void syncEvent(SyncEvent event) {
        this.syncEvent = event;
    }

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(instace);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instace = this;
        SyncService.startAlarm(this);
        getBus().register(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instace = null;
    }
}
