package com.hdweiss.morgand.data;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalendarUtils {

    public static final SimpleDateFormat dateTimeformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd");


    public static String formatDate(long dtStart, long dtEnd, boolean allDay) {
        String date;

        if (allDay)
            date = dateformatter.format(new Date(dtStart));
        else
            date = dateTimeformatter.format(new Date(dtStart));

        if (dtEnd > 0 && dtStart != dtEnd) {
            long timeDiff = dtEnd - dtStart;

            if(timeDiff <= DateUtils.DAY_IN_MILLIS) {
                SimpleDateFormat timeformatter = new SimpleDateFormat("HH:mm");
                String endTime = timeformatter.format(new Date(dtEnd));

                date += "-" + endTime;
            }
        }

        return "<" + date + ">";
    }

    public static long getDayInUTC(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cal.getTimeInMillis();
    }
}
