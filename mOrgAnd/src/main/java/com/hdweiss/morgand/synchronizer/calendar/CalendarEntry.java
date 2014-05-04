package com.hdweiss.morgand.synchronizer.calendar;

import android.text.TextUtils;

import com.hdweiss.morgand.data.CalendarUtils;
import com.hdweiss.morgand.data.OrgCalendarEntry;
import com.hdweiss.morgand.data.dao.OrgNode;

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
		if (o instanceof OrgCalendarEntry) {
			OrgCalendarEntry entry = (OrgCalendarEntry) o;
			return this.dtStart == entry.beginTime
					&& this.dtEnd == entry.endTime
					&& entry.getCalendarTitle().startsWith(this.title);
		}

		return super.equals(o);
	}

    public OrgNode writeToOrgNodes(OrgNode parentNode) {
        OrgNode headingNode = parentNode.addChild(OrgNode.Type.Headline, this.title);

        boolean isAllDay = allDay > 0;
        String date = CalendarUtils.formatDate(this.dtStart, this.dtEnd, isAllDay);

        headingNode.addChild(OrgNode.Type.Date, date);
        headingNode.addChild(OrgNode.Type.Body, this.description);

        if (TextUtils.isEmpty(this.location) == false)
            headingNode.addChild(OrgNode.Type.Drawer, ":LOCATION: " + this.location);

        return headingNode;
    }
}
