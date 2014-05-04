package com.hdweiss.morgand.data;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgCalendarEntry {

    public long beginTime = 0;
    public long endTime = 0;
    public int allDay = 0;
    public String type = "";
    public String title = "";


    private static final String datePattern = "(\\d{1,2}\\:\\d{2})";  // d:dd or dd:dd
    private static final Pattern schedulePattern = Pattern
            .compile("(\\d{4}-\\d{2}-\\d{2})" // YYYY-MM-DD
                    + "(?:[^\\d]*)" // Strip out month
                    + datePattern + "?" // Begin time
                    + "(?:\\-" + datePattern + ")?"); // "-" followed by end time

    private static final int DATE = 1;
    private static final int BEGIN_TIME = 2;
    private static final int END_TIME = 3;


	public OrgCalendarEntry(String date) throws IllegalArgumentException {
		Matcher schedule = schedulePattern.matcher(date);

		if (schedule.find()) {
			try {
				if(schedule.group(BEGIN_TIME) == null) { // event is an entire day event
					this.beginTime = CalendarUtils.dateformatter.parse(schedule.group(DATE)).getTime();
					
					// All day events need to be in UTC and end time is exactly one day after
					this.beginTime = CalendarUtils.getDayInUTC(beginTime);
					this.endTime = this.beginTime + DateUtils.DAY_IN_MILLIS;
					this.allDay = 1;
				}
				else if (schedule.group(BEGIN_TIME) != null && schedule.group(END_TIME) != null) {
					this.beginTime = CalendarUtils.dateTimeformatter.parse(schedule.group(DATE) + " " + schedule.group(BEGIN_TIME)).getTime();
					this.endTime = CalendarUtils.dateTimeformatter.parse(schedule.group(DATE) + " " + schedule.group(END_TIME)).getTime();
					this.allDay = 0;
				} else if(schedule.group(BEGIN_TIME) != null) {
					this.beginTime = CalendarUtils.dateTimeformatter.parse(schedule.group(DATE) + " " + schedule.group(BEGIN_TIME)).getTime();
					this.endTime = beginTime + DateUtils.HOUR_IN_MILLIS;
					this.allDay = 0;
				}

				return;
			} catch (ParseException e) {
				Log.w("MobileOrg", "Unable to parse schedule: " + date);
			}
		} else
			throw new IllegalArgumentException("Could not create date out of entry");
	}


	
	/**
	 * Whether an event is in the past. True if event ended 24 hours ago or
	 * sometime in the future.
	 */
	public boolean isInPast() {
		return System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS >= endTime;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}


	public String getCalendarTitle() {
        String formatedType = this.type;
        if (type.startsWith("SCHEDULED"))
            formatedType = "SC";
        else if (type.startsWith("DEADLINE"))
            formatedType = "DL";

        if (TextUtils.isEmpty(formatedType))
            return this.title;
        else
		    return formatedType + ": " + this.title;
	}
}
