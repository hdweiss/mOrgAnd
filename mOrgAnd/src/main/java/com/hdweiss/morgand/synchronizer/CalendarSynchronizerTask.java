package com.hdweiss.morgand.synchronizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.orgdata.OrgNodeDate;
import com.hdweiss.morgand.orgdata.OrgNodeRepository;
import com.hdweiss.morgand.utils.MultiMap;
import com.hdweiss.morgand.utils.SafeAsyncTask;

import java.util.HashSet;
import java.util.List;

public class CalendarSynchronizerTask extends SafeAsyncTask<String, Void, Void> {

    private CalendarWrapper calendarWrapper;

    private HashSet<String> activeTodos = new HashSet<String>();
    private HashSet<String> allTodos = new HashSet<String>();
    private boolean showDone = true;
    private boolean showPast = true;
    private boolean showHabits = true;
    private boolean pullEnabled = false; // TODO
    private boolean pullDelete = false;

    private int inserted = 0;
    private int deleted = 0;
    private int unchanged = 0;

    public CalendarSynchronizerTask(Context context) {
        super(context, ReportMode.Log);

        this.calendarWrapper = new CalendarWrapper(context);
        refreshPreferences();
    }

    @Override
    protected Void safeDoInBackground(String... files) throws Exception {
        Log.d("Calendar", "Started synchronization");
        if (files.length == 0)
            return null;

        for(String file: files) {
            syncFileSchedule(file);
        }

        Log.d("Calendar", "Ended synchronization");
        return null;
    }


    private void syncFileSchedule(String filename) {
        inserted = 0;
        deleted = 0;
        unchanged = 0;

        MultiMap<CalendarEntry> entries = getCalendarEntries(filename);
        List<OrgNode> nodes = OrgNodeRepository.getScheduledNodes(filename, showHabits);

        Log.d("Calendar", filename + " has " + nodes.size() + " entries");

        for(OrgNode node: nodes) {
            for(OrgNodeDate date: node.getOrgNodeDates()) {
                if (shouldInsertEntry("", date)) // TODO
                    createOrUpdateEntry(entries, date, filename, node);
            }
        }

        removeCalendarEntries(entries);

        Log.d("Calendar", "Calendar (" + filename + ") Inserted: " + inserted
                + " and deleted: " + deleted + ", unchanged: " + unchanged);
    }

    private void createOrUpdateEntry(MultiMap<CalendarEntry> entries,
                                     OrgNodeDate date, String filename, OrgNode node) {
        CalendarEntry insertedEntry = entries.findValue(date.beginTime, date);

        if (insertedEntry != null && insertedEntry.title.equals(date.getTitle())) {
            entries.remove(date.beginTime, insertedEntry);
            unchanged++;
        } else {
            calendarWrapper.insertEntry(date, "Payload", filename,
                    "", ""); // TODO
            inserted++;
        }
    }

    private boolean shouldInsertEntry(String todo, OrgNodeDate date) {
        boolean isTodoActive = true;
        if (TextUtils.isEmpty(todo) == false && allTodos.contains(todo))
            isTodoActive = this.activeTodos.contains(todo);

        if (this.showDone == false && isTodoActive == false)
            return false;

        if (this.showPast == false && date.isInPast())
            return false;

        return true;
    }

    private MultiMap<CalendarEntry> getCalendarEntries(String filename) {
        Cursor query = calendarWrapper.getCalendarCursor(filename);
        CalendarEntriesParser entriesParser = new CalendarEntriesParser(query);

        MultiMap<CalendarEntry> map = new MultiMap<CalendarEntry>();
        while (query.isAfterLast() == false) {
            CalendarEntry entry = entriesParser.getEntryFromCursor(query);
            map.put(entry.dtStart, entry);

            query.moveToNext();
        }

        return map;
    }

    private void removeCalendarEntries(MultiMap<CalendarEntry> entries) {
        for (Long entryKey : entries.keySet()) {
            for (CalendarEntry entry : entries.get(entryKey)) {
                calendarWrapper.deleteEntry(entry);
                deleted++;
            }
        }
    }

    private void refreshPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.pullEnabled = sharedPreferences.getBoolean("calendar_pull", false);
        this.pullDelete = sharedPreferences.getBoolean("calendar_pull_delete", false);
        this.showDone = sharedPreferences.getBoolean("calendar_show_done", true);
        this.showPast = sharedPreferences.getBoolean("calendar_show_past", true);
        this.showHabits = sharedPreferences.getBoolean("calendar_habits", true);
//        this.activeTodos = new HashSet<String>(
//                OrgProviderUtils.getActiveTodos(resolver));
//        this.allTodos = new HashSet<String>(OrgProviderUtils.getTodos(resolver));
        this.calendarWrapper.refreshPreferences();
    }
}
