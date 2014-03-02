package com.hdweiss.morgand;

public class Application extends android.app.Application {

    private static Application instace;

    @Override
    public void onCreate() {
        super.onCreate();
        instace = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instace = null;
    }

    public static Application getInstace() {
        return instace;
    }
}
