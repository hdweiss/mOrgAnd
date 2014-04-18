package com.hdweiss.morgand.synchronizer;

import android.text.TextUtils;

import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.orgdata.OrgNodeDate;
import com.hdweiss.morgand.orgdata.OrgNodeTimeDate;

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

    public OrgNode writeToOrgNodes(OrgNode parentNode) {
        OrgNode headingNode = parentNode.addChild(OrgNode.Type.Headline, this.title);

        boolean isAllDay = allDay > 0;
        String date = OrgNodeDate.getDate(this.dtStart, this.dtEnd, isAllDay);
        String formatedDate = OrgNodeTimeDate.formatDate(
                OrgNodeTimeDate.TYPE.Timestamp, date);

        headingNode.addChild(OrgNode.Type.Date, formatedDate);
        headingNode.addChild(OrgNode.Type.Body, this.description);

        if (TextUtils.isEmpty(this.location) == false)
            headingNode.addChild(OrgNode.Type.Drawer, ":LOCATION: " + this.location);

        return headingNode;
    }
}
