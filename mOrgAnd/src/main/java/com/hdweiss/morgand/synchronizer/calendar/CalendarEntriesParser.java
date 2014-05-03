package com.hdweiss.morgand.synchronizer.calendar;

import android.database.Cursor;
import android.provider.CalendarContract;

public class CalendarEntriesParser {

	private int idColumn;
	private int dtStartColumn;
	private int dtEndColumn;
	private int titleColumn;
	private int descriptionColumn;
	private int locationColumn;
	private int allDayColumn;

	public CalendarEntriesParser(Cursor cursor) {
		dtStartColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART);
		dtEndColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND);
		titleColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE);
		idColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID);
		descriptionColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION);
		locationColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION);
		allDayColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY);
	}

	public CalendarEntry getEntryFromCursor(Cursor cursor) {
		CalendarEntry entry = new CalendarEntry();
		
		entry.dtStart = cursor.getLong(dtStartColumn);
		entry.dtEnd = cursor.getLong(dtEndColumn);
		entry.title = cursor.getString(titleColumn);
		entry.id = cursor.getLong(idColumn);
		entry.description = cursor.getString(descriptionColumn);
		entry.location = cursor.getString(locationColumn);
		entry.allDay = cursor.getInt(allDayColumn);
		
		return entry;
	}
}
