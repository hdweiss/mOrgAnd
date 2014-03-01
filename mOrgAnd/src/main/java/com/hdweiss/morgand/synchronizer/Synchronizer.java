package com.hdweiss.morgand.synchronizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Synchronizer {

    private JGitWrapper jGitWrapper;

    public Synchronizer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            this.jGitWrapper = new JGitWrapper(preferences);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(context, "Error on sync", Toast.LENGTH_LONG).show();
        }
    }
}
