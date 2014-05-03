package com.hdweiss.morgand.synchronizer.parser;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.data.dao.OrgFile;
import com.hdweiss.morgand.events.DataUpdatedEvent;
import com.hdweiss.morgand.events.SyncEvent;
import com.hdweiss.morgand.gui.SynchronizerNotification;
import com.hdweiss.morgand.settings.PreferenceUtils;
import com.hdweiss.morgand.synchronizer.calendar.SyncCalendarTask;
import com.hdweiss.morgand.utils.SafeAsyncTask;
import com.hdweiss.morgand.utils.Utils;

import java.util.ArrayList;

public class SyncParserTask extends SafeAsyncTask<Void, SyncEvent, Void> {

    private ArrayList<OrgFile> modifiedFiles;

    public SyncParserTask(Context context) {
        super(context, ReportMode.Log);
    }

    @Override
    protected Void safeDoInBackground(Void... voids) throws Exception {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String localRepoPath = preferences.getString("git_local_path", "");

        publishProgress(new SyncEvent(SyncEvent.State.Progress, 0));

        OrgRepository repository = new OrgRepository(localRepoPath);
        modifiedFiles = repository.getModifiedFiles();

        int fileIndex = 0;
        for(OrgFile orgFile: modifiedFiles) {
            fileIndex++;

            new OrgFileParser().parse(orgFile);

            int progress = (100 / modifiedFiles.size()) * fileIndex;
            publishProgress(new SyncEvent(SyncEvent.State.Progress, progress, orgFile.path));
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(SyncEvent... events) {
        super.onProgressUpdate(events);

        for(SyncEvent event: events)
            Application.getBus().post(event);

        Application.getBus().post(new DataUpdatedEvent());
    }

    @Override
    protected void onSuccess(Void aVoid) {
        String[] filenames = new String[modifiedFiles.size()];
        for(int i = 0; i < modifiedFiles.size(); i++) {
            filenames[i] = modifiedFiles.get(i).path;
        }

        if (PreferenceUtils.syncCalendar())
            new SyncCalendarTask(context).execute(filenames);
    }

    @Override
    protected void onCleanup() {
        Application.getBus().post(new SyncEvent(SyncEvent.State.Done));
    }

    @Override
    protected void onError() {
        SynchronizerNotification notification = new SynchronizerNotification(context);
        notification.errorNotification(exception.getMessage() + "\n" + Utils.ExceptionTraceToString(exception));
    }
}
