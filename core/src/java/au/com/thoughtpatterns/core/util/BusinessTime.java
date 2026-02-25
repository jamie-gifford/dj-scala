package au.com.thoughtpatterns.core.util;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BusinessTime implements Serializable, Comparable<BusinessTime> {

    private static final long serialVersionUID = 1L;

    private static final Pattern PATTERN24 = Pattern.compile("([0-9]+)((h|:)([0-9]{2})?)");
    
    /**
     * 0 - 23
     */
    private int hour;

    /**
     * 0 - 59
     */
    private int minute;

    public static BusinessTime newTime(int h, int m) {
        if (h > 23 || h < 0 || m > 59 || m < 0) {
            return null;
        }
        return new BusinessTime(h, m);
    }
    
    /**
     * @return null if time not parseable
     */
    public static BusinessTime newTime(String time) {
        Matcher m = PATTERN24.matcher(time);
        if (m.matches()) {
            int hour = Integer.parseInt(m.group(1));
            String min = m.group(4);
            int minute = min != null ? Integer.parseInt(min) : 0;
            return newTime(hour, minute);
        } else {
            return null;
        }
    }
    
    public static BusinessTime newLocalTime(Date date) {
        BusinessDate d = BusinessDate.newDate(date);
        long millis = date.getTime() - d.getLocalMidnight().getTime();
        long mins = millis / 1000 / 60;
        
        int h = (int)(mins / 60);
        int m = (int)(mins % 60);
        
        return newTime(h, m);
    }
    
    private BusinessTime(int aHour, int aMinute) {
        hour = aHour;
        minute = aMinute;
    }

    private int getDayMinute() {
        return hour * 60 + minute;
    }

    @Override public int compareTo(BusinessTime o) {
        int a = getDayMinute();
        int b = o.getDayMinute();
        if (a == b) {
            return 0;
        } else {
            return a < b ? -1 : 1;
        }
    }

    @Override public int hashCode() {
        return getDayMinute();
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof BusinessTime)) {
            return false;
        }
        return getDayMinute() == ((BusinessTime) obj).getDayMinute();
    }

    public String toString() {
        return String.format("%02d:%02d", hour, minute);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public long getMillisFromMidnight() {
        return (60 * hour + minute) * 60 * 1000;
    }
    
}
