package com.hdweiss.morgand.synchronizer.git;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.events.SyncEvent;
import com.hdweiss.morgand.gui.SynchronizerNotification;
import com.hdweiss.morgand.synchronizer.parser.SyncParserTask;
import com.hdweiss.morgand.utils.SafeAsyncTask;
import com.hdweiss.morgand.utils.Utils;

import org.eclipse.jgit.lib.ProgressMonitor;

public class SyncGitTask extends SafeAsyncTask<Void, SyncEvent, Void> {

    private JGitWrapper jGitWrapper;

    public SyncGitTask(Context context) {
        super(context, ReportMode.Toast);
    }

    @Override
    protected Void safeDoInBackground(Void... params) throws Exception {
        Log.d("Git", "Started synchronization");

        if (Utils.isNetworkOnline(context) == false) {
            Log.d("Git", "Network is offline, aborting git synchronization");
            return null;
        }

        publishProgress(new SyncEvent(SyncEvent.State.Intermediate));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        jGitWrapper = new JGitWrapper(preferences);
        jGitWrapper.commitAllChanges(Build.MODEL + ": Automatic commit");
        jGitWrapper.updateChanges(monitor);

        Log.d("Git", "Ended synchronization");
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
        notification.errorNotification(exception.getLocalizedMessage() + "\n" + Utils.ExceptionTraceToString(exception));
    }

    @Override
    protected void onCleanup() {
        jGitWrapper.cleanup();
    }


    private ProgressMonitor monitor = new ProgressMonitor() {

        private int progress = 0;
        private int totalWork = 0;
        private int workCompleted = 0;

        public void start(int totalTasks) {
        }

        public void beginTask(String title, int totalWork) {
            this.totalWork = totalWork;
            this.workCompleted = 0;
            publishProgress(new SyncEvent(SyncEvent.State.SecondaryProgress, 0));
        }

        public void update(int completed) {
            this.workCompleted += completed;
            int newProgress = getProgress();

            if(this.progress != newProgress) {
                this.progress = newProgress;
                publishProgress(new SyncEvent(SyncEvent.State.SecondaryProgress, newProgress));
            }
        }

        private int getProgress() {
            if(totalWork == 0)
                return 0;

            final int taskWorkProgress = (int) ((100.0 / totalWork)
                    * workCompleted);
            return taskWorkProgress;
        }

        public void endTask() {
        }

        public boolean isCancelled() {
            return false;
        }
    };
}
