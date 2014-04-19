package com.hdweiss.morgand;

import com.squareup.otto.Bus;

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

    @Override
    public void onCreate() {
        super.onCreate();
        instace = this;
        //SyncService.startAlarm(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instace = null;
    }
}
