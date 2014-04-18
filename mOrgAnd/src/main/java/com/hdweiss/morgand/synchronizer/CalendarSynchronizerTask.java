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
import com.hdweiss.morgand.utils.PreferenceUtils;
import com.hdweiss.morgand.utils.SafeAsyncTask;

import java.util.HashSet;
import java.util.List;

public class CalendarSynchronizerTask extends SafeAsyncTask<String, Void, Void> {

    private CalendarWrapper calendarWrapper;

    private HashSet<String> inactiveTodoKeywords = new HashSet<String>();
    private boolean showDone = true;
    private boolean showPast = true;
    private boolean showHabits = true;
    private boolean pullEnabled = false;
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

        if (pullEnabled) // TODO complete assimilate Calendar (implement function that delivers default capture node)
            //assimilateCalendar();

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
                if (shouldInsertEntry(date, node))
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
            calendarWrapper.insertEntry(date, node.getBody(), filename,
                    node.getProperty("LOCATION"), node.getProperty("BUSY"));
            inserted++;
        }
    }

    private boolean shouldInsertEntry(OrgNodeDate date, OrgNode node) {
        String todo = node.getTodo();
        if (this.showDone == false && TextUtils.isEmpty(todo) == false && inactiveTodoKeywords.contains(todo))
            return false;

        if (this.showPast == false && date.isInPast())
            return false;

        if (this.showHabits == false && "habit".equals(node.getProperty("STYLE")))
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

    private void assimilateCalendar() {
        Cursor query = calendarWrapper.getUnassimilatedCalendarCursor();

        CalendarEntriesParser entriesParser = new CalendarEntriesParser(query);

        while(query.isAfterLast() == false) {
            CalendarEntry entry = entriesParser.getEntryFromCursor(query);

            OrgNode captureNode = OrgNodeRepository.getDefaultCaptureNode();
            entry.writeToOrgNodes(captureNode);

            if (this.pullDelete)
                calendarWrapper.deleteEntry(entry);

            query.moveToNext();
        }

        query.close();
        // OrgUtils.announceSyncDone(this);
    }

    private void refreshPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.pullEnabled = sharedPreferences.getBoolean("calendar_pull", false);
        this.pullDelete = sharedPreferences.getBoolean("calendar_pull_delete", false);
        this.showDone = sharedPreferences.getBoolean("calendar_show_done", true);
        this.showPast = sharedPreferences.getBoolean("calendar_show_past", true);
        this.showHabits = sharedPreferences.getBoolean("calendar_habits", true);
        this.inactiveTodoKeywords = PreferenceUtils.getInactiveTodoKeywords();
        this.calendarWrapper.refreshPreferences();
    }
}
