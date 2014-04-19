package com.hdweiss.morgand.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    public static String ExceptionTraceToString(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static boolean isWifiOnline(Context context) {
        try {
            ConnectivityManager conMan = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .getState();

            if (wifi == NetworkInfo.State.CONNECTED)
                return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean isMobileOnline(Context context) {
        try {
            ConnectivityManager conMan = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo.State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .getState();

            if (mobile == NetworkInfo.State.CONNECTED)
                return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public static boolean isNetworkOnline(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean wifiOnly = prefs.getBoolean("sync_wifi_only", false);

        if (wifiOnly)
            return isWifiOnline(context);
        else
            return isWifiOnline(context)
                    || isMobileOnline(context);
    }
}
