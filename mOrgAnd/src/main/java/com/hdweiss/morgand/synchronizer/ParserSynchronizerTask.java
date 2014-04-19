package com.hdweiss.morgand.synchronizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.gui.SynchronizerNotification;
import com.hdweiss.morgand.orgdata.OrgRepository;
import com.hdweiss.morgand.utils.SafeAsyncTask;
import com.hdweiss.morgand.utils.Utils;

import java.util.ArrayList;

public class ParserSynchronizerTask extends SafeAsyncTask<Void, SynchronizerEvent, Void> {

    public ParserSynchronizerTask(Context context) {
        super(context, ReportMode.Log);
    }

    @Override
    protected Void safeDoInBackground(Void... voids) throws Exception {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String localRepoPath = preferences.getString("git_local_path", "");
        OrgRepository repository = new OrgRepository(localRepoPath);

        ArrayList<String> files = new ArrayList<String>();
        repository.parse();

        int fileIndex = 0;
        for(String file: files) {
            fileIndex++;

            int progress = (100 / files.size()) * fileIndex;
            publishProgress(new SynchronizerEvent(SynchronizerEvent.State.Progress, progress));
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(SynchronizerEvent... events) {
        super.onProgressUpdate(events);

        for(SynchronizerEvent event: events)
            Application.getBus().post(event);

        Application.getBus().post(new DataUpdatedEvent());
    }

    @Override
    protected void onCleanup() {
        Application.getBus().post(new SynchronizerEvent(SynchronizerEvent.State.Done));
    }

    @Override
    protected void onError() {
        SynchronizerNotification notification = new SynchronizerNotification(context);
        notification.errorNotification(exception.getMessage() + "\n" + Utils.ExceptionTraceToString(exception));
    }
}
