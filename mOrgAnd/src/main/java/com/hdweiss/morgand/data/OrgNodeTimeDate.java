package com.hdweiss.morgand.data;

import android.text.TextUtils;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgNodeTimeDate {
    private TYPE type = TYPE.Scheduled;

    private int year = -1;
    private int monthOfYear = -1;
    private int dayOfMonth = -1;
    private int startTimeOfDay = -1;
    private int startMinute = -1;
    private int endTimeOfDay = -1;
    private int endMinute = -1;
	
	private enum TYPE {
		Scheduled,
		Deadline,
		Timestamp,
		InactiveTimestamp
	};

    private OrgNodeTimeDate(TYPE type) {
		this.type = type;
	}


    private void setToCurrentDate() {
		final Calendar c = Calendar.getInstance();
		this.year = c.get(Calendar.YEAR);
		this.monthOfYear = c.get(Calendar.MONTH) + 1;
		this.dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
	}

    private static final Pattern schedulePattern = Pattern
			.compile("((\\d{4})-(\\d{1,2})-(\\d{1,2}))(?:[^\\d]*)"
					+ "((\\d{1,2})\\:(\\d{2}))?(-((\\d{1,2})\\:(\\d{2})))?");
    private void parseDate(String date) {
		if(date == null)
			return;

		Matcher propm = schedulePattern.matcher(date);

		if (propm.find()) {
			try {
				year = Integer.parseInt(propm.group(2));
				monthOfYear = Integer.parseInt(propm.group(3));
				dayOfMonth = Integer.parseInt(propm.group(4));
				
				startTimeOfDay = Integer.parseInt(propm.group(6));
				startMinute = Integer.parseInt(propm.group(7));

				endTimeOfDay = Integer.parseInt(propm.group(10));
				endMinute = Integer.parseInt(propm.group(11));
			} catch (NumberFormatException e) {
                e.printStackTrace();
            }
		}
	}


    private String getDate() {
		return String.format("%d-%02d-%02d", year, monthOfYear, dayOfMonth);
	}

    private String getStartTime() {
		return String.format("%02d:%02d", startTimeOfDay, startMinute);
	}

    private String getEndTime() {
		return String.format("%02d:%02d", endTimeOfDay, endMinute);
	}
	
	
	private String toString2() {
		return getDate().toString() + getStartTimeFormated() + getEndTimeFormated();
	}
	
	public String toFormatedString() {
		return formatDate(type, getDate());
	}

	
	private String getStartTimeFormated() {
		String time = getStartTime().toString();

		if (startTimeOfDay == -1
				|| startMinute == -1 || TextUtils.isEmpty(time))
			return "";
		else
			return " " + time;
	}
	
	private String getEndTimeFormated() {
		String time = getEndTime().toString();

		if (endTimeOfDay == -1
				|| endMinute == -1 || TextUtils.isEmpty(time))
			return "";
		else
			return "-" + time;
	}
	
	
	private static String typeToFormated(TYPE type) {
		switch (type) {
		case Scheduled:
			return "SCHEDULED: ";
		case Deadline:
			return "DEADLINE: ";
		case Timestamp:
			return "";
		default:
			return "";
		}
	}

	private String formatDate(TYPE type, String timestamp) {
		if (TextUtils.isEmpty(timestamp))
			return "";
		else {
			return OrgNodeTimeDate.typeToFormated(type) + "<" + timestamp + ">";
		}
	}
}
