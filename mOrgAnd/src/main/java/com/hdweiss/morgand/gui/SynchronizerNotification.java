package com.hdweiss.morgand.gui;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.hdweiss.morgand.R;

public class SynchronizerNotification {
    private NotificationManager notificationManager;
    private Notification notification;
    private int notifyRef = 1;
    private Context context;

    public SynchronizerNotification(Context context) {
        this.context = context;
    }

    public void errorNotification(String errorMsg) {
        this.notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = getBigstyleNotification(errorMsg, contentIntent);
        } else {
            notification = getSimpleNotification(errorMsg, contentIntent);
        }
        notificationManager.notify(notifyRef, notification);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification getBigstyleNotification(String message, PendingIntent contentIntent) {
        Notification notification = new Notification.BigTextStyle(
                new Notification.Builder(context)
                        .setContentIntent(contentIntent)
                        .setContentTitle(context.getString(R.string.error_sync))
                        .setContentText("Error")
                        .setSmallIcon(R.drawable.ic_launcher))
                .bigText(message)
                .build();
        return notification;
    }


    private Notification getSimpleNotification(String message, PendingIntent contentIntent) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentText(message);
        return builder.getNotification();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setupNotification() {
        this.notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, 0);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setOngoing(true);
        builder.setContentTitle(context.getString(R.string.title_synchronizing_changes));
        builder.setProgress(100, 0, true);
        notification = builder.getNotification();

        notificationManager.notify(notifyRef, notification);
    }

    public void updateNotification(String message) {
        if(notification == null)
            return;

        if(message != null) {
            notificationManager.notify(notifyRef, notification);
        }
    }

    public void updateNotification(int progress) {
        updateNotification(progress, null);
    }

    public void updateNotification(int progress, String message) {
        if(notification == null)
            return;

        notification.contentView.setProgressBar(android.R.id.progress, 100,
                progress, false);

        notificationManager.notify(notifyRef, notification);
    }

    public void finalizeNotification() {
        notificationManager.cancel(notifyRef);
    }
}

