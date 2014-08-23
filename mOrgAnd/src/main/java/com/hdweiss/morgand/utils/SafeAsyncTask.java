package com.hdweiss.morgand.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Wrapper for {@link android.os.AsyncTask} that allows exceptions to be caught and presented to the
 * user in a meaningful way.
 * <p/>
 * It is required to override {@link #safeDoInBackground(Object[])}, which will be executed within
 * {@link android.os.AsyncTask#execute(Object[])}. If an exception is caught it will be sent to
 * {@link #onCancelled(Object)}, which will extract a meaningful message from an exception. The
 * message will be reported to the user with {@link #reportError(String)}, which will either display a toast, log the error
 * message to Logcat or do nothing. The reporting behaviour is controlled by {@link #mode}.
 * <p/>
 * When an exception is caught {@link #onError()} will be called, otherwise {@link
 * #onSuccess(Object)} is called. Finally {@link #onCleanup()} will always be called.
 * <p/>
 */
public abstract class SafeAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    public enum ReportMode {
        Toast, Log, Silent
    }

    final private ReportMode mode;
    final protected Context context;

    protected Exception exception = null;

    /**
     * Main worker method. It is run within a {@link android.os.AsyncTask#doInBackground(Object[])}.
     * Any exception this method throws is caught and reported according to the given {@link
     * #mode}.
     */
    abstract protected Result safeDoInBackground(Params... params) throws Exception;

    /** Run after {@link #safeDoInBackground(Object[])} doesn't throw exception. */
    protected void onSuccess(Result result) {}

    /** Run after {@link #safeDoInBackground(Object[])} throws an exception. */
    protected void onError() {}

    /** Guaranteed to run after either {@link #onSuccess(Object)} or {@link #onError()} is run. */
    protected void onCleanup() {}

    public SafeAsyncTask(Context context, ReportMode mode) {
        this.context = context;
        this.mode = mode;
    }

    @Override
    final protected Result doInBackground(Params... params) {
        try {
            return safeDoInBackground(params);
        }
        catch (Exception e) {
            exception = e;
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        onSuccess(result);
        onCleanup();
    }


    @Override
    final protected void onCancelled() {
        if (exception != null) {
            if (exception instanceof ReportableException)
                reportError(exception.getLocalizedMessage());
            else
                reportError(exception.getLocalizedMessage());
            Log.e("SafeAsyncTask", "safeDoInBackground() threw exception", exception);
        }

        onError();
        onCleanup();
    }

    @Override
    final protected void onCancelled(Result result) {
        onCancelled();
    }

    /** Reports the error according to {@link #mode}. */
    protected void reportError(String error) {
        if (error == null || error.isEmpty())
            return;

        if (mode == ReportMode.Toast && context != null)
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        if (mode != ReportMode.Silent)
            Log.d("SafeAsyncTask", error);
    }


    public static class ReportableException extends Exception {
        public ReportableException(String message) {
            super(message);
        }
    }
}
