package com.hdweiss.morgand.synchronizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hdweiss.morgand.gui.SynchronizerNotification;
import com.hdweiss.morgand.utils.SafeAsyncTask;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SynchronizerTask extends SafeAsyncTask<Void, String, Void> {

    private SynchronizerNotification notification;

    public SynchronizerTask(Context context) {
        super(context, ReportMode.Toast);
        this.notification = new SynchronizerNotification(context);
    }

    @Override
    protected Void safeDoInBackground(Void... params) throws Exception {
        notification.setupNotification();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            JGitWrapper jGitWrapper = new JGitWrapper(preferences);
            jGitWrapper.updateChanges();
        } catch (IllegalArgumentException ex) {
            throw new ReportableException(ex.getMessage());
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String message = values[0];
        notification.updateNotification(message);
    }

    @Override
    protected void onSuccess(Void aVoid) {
        notification.finalizeNotification();
    }

    @Override
    protected void onError() {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        notification.errorNotification(exception.getMessage() + "\n" + exceptionAsString);
    }
}
