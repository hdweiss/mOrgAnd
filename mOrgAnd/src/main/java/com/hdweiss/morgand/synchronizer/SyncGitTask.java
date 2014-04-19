package com.hdweiss.morgand.synchronizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.gui.SynchronizerNotification;
import com.hdweiss.morgand.utils.SafeAsyncTask;
import com.hdweiss.morgand.utils.Utils;

public class SyncGitTask extends SafeAsyncTask<Void, SyncEvent, Void> {

    public SyncGitTask(Context context) {
        super(context, ReportMode.Toast);
    }

    @Override
    protected Void safeDoInBackground(Void... params) throws Exception {
        if (Utils.isNetworkOnline(context) == false)
            return null;

        publishProgress(new SyncEvent(SyncEvent.State.Intermediate));

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
    protected void onProgressUpdate(SyncEvent... events) {
        super.onProgressUpdate(events);
        for(SyncEvent event: events)
            Application.getBus().post(event);
    }

    @Override
    protected void onSuccess(Void aVoid) {
        Application.getBus().post(new SyncEvent(SyncEvent.State.SecondaryProgress, 100));
        new SyncParserTask(context).execute();
    }

    @Override
    protected void onError() {
        Application.getBus().post(new SyncEvent(SyncEvent.State.Done));
        SynchronizerNotification notification = new SynchronizerNotification(context);
        notification.errorNotification(exception.getMessage() + "\n" + Utils.ExceptionTraceToString(exception));
    }
}
