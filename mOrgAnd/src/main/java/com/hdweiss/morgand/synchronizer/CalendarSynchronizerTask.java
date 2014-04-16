package com.hdweiss.morgand.synchronizer;

import android.content.Context;

import com.hdweiss.morgand.utils.SafeAsyncTask;

public class CalendarSynchronizerTask extends SafeAsyncTask<String, Void, Void> {
    public CalendarSynchronizerTask(Context context) {
        super(context, ReportMode.Log);
    }

    @Override
    protected Void safeDoInBackground(String... files) throws Exception {
        return null;
    }
}
