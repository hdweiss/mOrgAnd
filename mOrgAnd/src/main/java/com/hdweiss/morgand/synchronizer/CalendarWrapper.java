package com.hdweiss.morgand.synchronizer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.format.Time;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.orgdata.OrgNodeDate;

public class CalendarWrapper {

	private final static String CALENDAR_ORGANIZER = "MobileOrg";
	
	private Context context;
	private SharedPreferences sharedPreferences;

	private String calendarName = "";
	private int calendarId = -1;
	private Integer reminderTime = 0;
	private boolean reminderEnabled = false;

    private String[] eventsProjection = new String[] { CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.TITLE,
            CalendarContract.Events.EVENT_LOCATION, CalendarContract.Events._ID, CalendarContract.Events.ALL_DAY };

	public CalendarWrapper(Context context) {
		this.context = context;
		this.sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
	}
	
	public int deleteEntries() {
		refreshPreferences();
		return context.getContentResolver().delete(CalendarContract.Events.CONTENT_URI,
                CalendarContract.Events.DESCRIPTION + " LIKE ?",
				new String[] { CALENDAR_ORGANIZER + "%" });
	}

	public void deleteFileEntries(String[] files) {
		for (String file : files) {
			deleteFileEntries(file);
		}
	}
	
	public int deleteFileEntries(String filename) {
		refreshPreferences();
		return context.getContentResolver().delete(CalendarContract.Events.CONTENT_URI,
                CalendarContract.Events.DESCRIPTION + " LIKE ?",
				new String[] { CALENDAR_ORGANIZER + ":" + filename + "%" });
	}
	
	public String insertEntry(OrgNodeDate date, String payload,
			String filename, String location, String busy) throws IllegalArgumentException {

		if (this.calendarId == -1)
			throw new IllegalArgumentException(
					"Couldn't find selected calendar: " + calendarName);

		ContentValues values = new ContentValues();
		values.put(CalendarContract.Events.CALENDAR_ID, this.calendarId);
		values.put(CalendarContract.Events.TITLE, date.getTitle());
		values.put(CalendarContract.Events.DESCRIPTION, CALENDAR_ORGANIZER + ":"
				+ filename + "\n" + payload);
		values.put(CalendarContract.Events.EVENT_LOCATION, location);

		// If a busy state was given, send that info to calendar
		if (busy != null) {
			// Trying to be reasonably tolerant with respect to the accepted values.
			if (busy.equals("nil") || busy.equals("0") ||
			    busy.equals("no")  || busy.equals("available"))
				values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
		
			else if (busy.equals("t")   || busy.equals("1") ||
				 busy.equals("yes") || busy.equals("busy"))
				values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

			else if (busy.equals("2") || busy.equals("tentative") || busy.equals("maybe"))
				values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_TENTATIVE);
		}
		
		// Sync with google will overwrite organizer :(
		// values.put(intEvents.ORGANIZER, embeddedNodeMetadata);

		values.put(CalendarContract.Events.DTSTART, date.beginTime);
		values.put(CalendarContract.Events.DTEND, date.endTime);
		values.put(CalendarContract.Events.ALL_DAY, date.allDay);
		values.put(CalendarContract.Events.HAS_ALARM, 0);
		values.put(CalendarContract.Events.EVENT_TIMEZONE, Time.getCurrentTimezone());

		Uri uri = context.getContentResolver().insert(
                CalendarContract.Events.CONTENT_URI, values);
		String nodeID = uri.getLastPathSegment();

		if (date.allDay == 0 && this.reminderEnabled)
			addReminder(nodeID, date.beginTime, date.endTime);

		return nodeID;
	}

	
	private void addReminder(String eventID, long beginTime, long endTime) {
		if (beginTime < System.currentTimeMillis())
			return;

		ContentValues reminderValues = new ContentValues();
		reminderValues.put(CalendarContract.Reminders.MINUTES, this.reminderTime);
		reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
		reminderValues.put(CalendarContract.Reminders.METHOD,
                CalendarContract.Reminders.METHOD_ALERT);
		context.getContentResolver().insert(CalendarContract.CalendarAlerts.CONTENT_URI,
				reminderValues);

		ContentValues alertvalues = new ContentValues();
		alertvalues.put(CalendarContract.CalendarAlerts.EVENT_ID, eventID);
		alertvalues.put(CalendarContract.CalendarAlerts.BEGIN, beginTime);
		alertvalues.put(CalendarContract.CalendarAlerts.END, endTime);
		alertvalues.put(CalendarContract.CalendarAlerts.ALARM_TIME, this.reminderTime);
		alertvalues.put(CalendarContract.CalendarAlerts.STATE,
				CalendarContract.CalendarAlerts.STATE_SCHEDULED);
		alertvalues.put(CalendarContract.CalendarAlerts.MINUTES, this.reminderTime);
		context.getContentResolver().insert(
				CalendarContract.CalendarAlerts.CONTENT_URI, alertvalues);

		ContentValues eventValues = new ContentValues();
		eventValues.put(CalendarContract.Events.HAS_ALARM, 1);
		context.getContentResolver().update(
				ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
						Long.valueOf(eventID)), eventValues, null, null);
	}


	public int deleteEntry(CalendarEntry entry) {
		return context.getContentResolver().delete(
				ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
						entry.id), null, null);
	}
	
	public int getCalendarID(String calendarName) {
		Cursor cursor = context.getContentResolver().query(
				CalendarContract.Calendars.CONTENT_URI,
				new String[] { CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME }, null, null,
				null);
		if (cursor != null && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				int calId = cursor.getInt(0);
				String calName = cursor.getString(1);

				if (calName.equals(calendarName)) {
					cursor.close();
					return calId;
				}
				cursor.moveToNext();
			}
			cursor.close();
		}
		return -1;
	}

	public Cursor getUnassimilatedCalendarCursor() {
		Cursor query = context.getContentResolver().query(
				CalendarContract.Events.CONTENT_URI, eventsProjection,
                CalendarContract.Events.CALENDAR_ID + "=? AND "
						+ CalendarContract.Events.DESCRIPTION + " NOT LIKE ?",
				new String[] { Integer.toString(this.calendarId),
						CALENDAR_ORGANIZER + "%" }, null);
		query.moveToFirst();
		
		return query;
	}
	
	public Cursor getCalendarCursor(String filename) {
		Cursor query = context.getContentResolver().query(
                CalendarContract.Events.CONTENT_URI, eventsProjection,
                CalendarContract.Events.DESCRIPTION + " LIKE ?",
				new String[] { CALENDAR_ORGANIZER + ":" + filename + "%" },
				null);
		query.moveToFirst();
		
		return query;
	}
	
	public static CharSequence[] getCalendars(Context context) {
		CharSequence[] result = new CharSequence[1];
		result[0] = context.getString(R.string.error_setting_no_calendar);

		try {
			Cursor cursor = context.getContentResolver().query(
                    CalendarContract.Calendars.CONTENT_URI,
					new String[] { CalendarContract.Calendars._ID,
                            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME }, null,
					null, null);
			if (cursor == null)
				return result;

			if (cursor.getCount() == 0) {
				cursor.close();
				return result;
			}

			if (cursor.moveToFirst()) {
				result = new CharSequence[cursor.getCount()];

				for (int i = 0; i < cursor.getCount(); i++) {
					result[i] = cursor.getString(1);
					cursor.moveToNext();
				}
			}
			cursor.close();
		} catch (SQLException e) {
            e.printStackTrace();
        }

		return result;
	}
	
	public void refreshPreferences() {
		this.reminderEnabled = sharedPreferences.getBoolean("calendar_reminder",
				false);

		if (reminderEnabled) {
			String intervalString = sharedPreferences.getString(
					"calendar_reminder_interval", "0");
			if (intervalString == null)
				throw new IllegalArgumentException(
						"Invalid calendar reminder interval");
			this.reminderTime = Integer.valueOf(intervalString);
		}
		
		this.calendarName = PreferenceManager.getDefaultSharedPreferences(
				context).getString("calendar_name", "");
		this.calendarId = getCalendarID(calendarName);
	}
}
