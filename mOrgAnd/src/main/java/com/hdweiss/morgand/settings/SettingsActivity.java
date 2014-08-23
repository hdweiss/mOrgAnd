package com.hdweiss.morgand.settings;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.synchronizer.calendar.CalendarWrapper;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }


    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onResume() {
            super.onResume();
            try {
                String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
                findPreference("version").setSummary(versionName);
            } catch (PackageManager.NameNotFoundException ex) {
            }
        }
    }

    public static class InterfacePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_interface);
        }
    }

    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
            bindPreferenceSummaryToValue(findPreference("todo_active"));
            bindPreferenceSummaryToValue(findPreference("todo_inactive"));
            bindPreferenceSummaryToValue(findPreference("priorities"));
        }
    }


    public static class GitPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_git);

            bindPreferenceSummaryToValue(findPreference("git_commit_author"));
            bindPreferenceSummaryToValue(findPreference("git_commit_email"));
            bindPreferenceSummaryToValue(findPreference("git_key_info"));
            bindPreferenceSummaryToValue(findPreference("git_username"));
            bindPreferenceSummaryToValue(findPreference("git_local_path"));
            bindPreferenceSummaryToValue(findPreference("git_merge_strategy"));
            bindPreferenceSummaryToValue(findPreference("git_branch"));

            bindPreferenceSummaryToValue(findPreference("git_url"));
            findPreference("git_url").setOnPreferenceChangeListener(sResetGitLocalPathListener);
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Update setting keys that are modified by starting another activity
            findPreference("git_local_path").setSummary(preferences.getString("git_local_path", ""));
            findPreference("git_key_info").setSummary(preferences.getString("git_key_info", ""));
        }
    }


    public static class CalendarPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_calendar);

            populateCalendarNames();

            bindPreferenceSummaryToValue(findPreference("calendar_name"));
            bindPreferenceSummaryToValue(findPreference("calendar_reminder"));
            bindPreferenceSummaryToValue(findPreference("calendar_reminder_interval"));
        }

        private void populateCalendarNames() {
            try {
                ListPreference calendarName = (ListPreference) findPreference("calendar_name");

                CharSequence[] calendars = CalendarWrapper
                        .getCalendars(getActivity());

                calendarName.setEntries(calendars);
                calendarName.setEntryValues(calendars);
            } catch (Exception e) {
                e.printStackTrace();
                // Don't crash because of anything in calendar
            }
        }
    }

    private static Preference.OnPreferenceChangeListener sResetGitLocalPathListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            preference.setSummary(value.toString());

            PreferenceUtils.set("git_local_path", "");
            preference.getPreferenceManager().findPreference("git_local_path").setSummary("");

            return true;
        }
    };

    /**
     * A preference value change listener that updates the preference's summary to reflect its new
     * value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null
                );
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is
     * changed, its summary (line of text below the preference title) is updated to reflect the
     * value. The summary is also immediately updated upon calling this method. The exact display
     * format is dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference == null)
            return;

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "")
        );
    }
}
