package com.hdweiss.morgand.orgdata;

public class CalendarEntry {
	public String title = "";
	public String description = "";
	public String location = "";
	public long id = -1;
	public long dtStart = 0;
	public long dtEnd = 0;
	public int allDay = 0;

	@Override
	public boolean equals(Object o) {
		if (o instanceof OrgNodeDate) {
			OrgNodeDate entry = (OrgNodeDate) o;
			return this.dtStart == entry.beginTime
					&& this.dtEnd == entry.endTime
					&& entry.getTitle().startsWith(this.title);
		}

		return super.equals(o);
	}
}
