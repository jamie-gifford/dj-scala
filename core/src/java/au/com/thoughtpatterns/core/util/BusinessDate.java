package au.com.thoughtpatterns.core.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Represents a date in the usual "business" sense of the word. That is, only
 * the "date" part is significant - there is no "time" part.
 * 
 * It is here as a placeholder and so that we can change the implementation in
 * the future if we realise that we need a better implementation.
 */
public class BusinessDate implements Serializable, Comparable<BusinessDate> { 

    private static final long serialVersionUID = 1L;

    private static final DateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");

    private static final DateFormat YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final DateFormat DD_MM_YYYY = new SimpleDateFormat("dd/MM/yyyy");

    private static final Parameters params = Parameters.instance();
    
    private static final String TODAY_OVERRIDE = "business_date.today";

    private static final String[] MMM = new String[] {
      "",
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec"
    };
    
    // ----------------------
    // State
    
    /**
     * The day of the month (1 - 31)
     */
    private int day;
    
    /**
     * The month of the year (1 - 12)
     */
    private int month; 
    
    /**
     * The year (2008)
     */
    private int year;
    
    /**
     * Used internally to access the Java calendar implementation
     */
    private transient Date backing;

    private Date backing() {
    	if (backing == null) {
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(year, month - 1, day);
            backing = cal.getTime();
    	}
    	return backing;
    }
    
    // Factory methods

    public static BusinessDate newYYYYMMDD(String yyyymmdd) throws ParseException {
        synchronized (YYYY_MM_DD) {
            Date javaDate = YYYY_MM_DD.parse(yyyymmdd);
            BusinessDate businessDate = new BusinessDate(javaDate.getTime());
            return businessDate;
        }
    }

    public static BusinessDate newYYYYMMDD_quiet(String yyyymmdd) {
        return new BusinessDate(yyyymmdd);
    }

    public static BusinessDate newDDMMYYYY(String ddmmyyyy) throws ParseException {
        synchronized (DD_MM_YYYY) {
            Date javaDate = DD_MM_YYYY.parse(ddmmyyyy);
            BusinessDate businessDate = new BusinessDate(javaDate.getTime());
            return businessDate;
        }
    }

    public static BusinessDate newDDMMYYYY_quiet(String ddmmyyyy) {
        try {
            return newDDMMYYYY(ddmmyyyy);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static BusinessDate newDMY(int day, int month, int year) {
        String str = year + "-" + month + "-" + day;
        return newYYYYMMDD_quiet(str);
    }

    /**
     * Quiet version of the above. In fact the above method never throws a ParseException since
     * it internally calls the "quiet" factory method. But changing the signature now would break project 
     * code since Java refuses to compile unreachable "catch" blocks.  
     */
    public static BusinessDate newDMYq(int day, int month, int year) {
        String str = year + "-" + month + "-" + day;
        return newYYYYMMDD_quiet(str);
    }

    public static BusinessDate newSystemToday() {
        
        // Allow override of today
        String today = params.get(TODAY_OVERRIDE);
        if (today != null) {
            return newYYYYMMDD_quiet(today);
        }
        
        BusinessDate approxBusinessDate = new BusinessDate(System.currentTimeMillis());
        String intermediate = approxBusinessDate.toYYYY_MM_DD();
        BusinessDate businessDate = new BusinessDate(intermediate);
        return businessDate;
    }

    public static BusinessDate newDate(Date date) {
        if (date == null) {
            return null;
        }
        BusinessDate businessDate = new BusinessDate(date.getTime());
        return businessDate;
    }

    public static BusinessDate newDate(BusinessDate date) {
        return date;
    }

    /**
     * Construct a BusinessDate from the given time (milliseconds from the
     * epoch). The time will be interpreted as a "raw" time (ie, forgetting
     * about timezones) and any time component will be discarded before
     * constructing the date.
     * 
     * @param time
     */
    private BusinessDate(long time) {
        setTime(time);
    }
    
    private void setTime(long time) {
        backing = new Date(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(backing);
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH) + 1;
        year = cal.get(Calendar.YEAR);
    }

    public static BusinessDate newSqlDate(java.sql.Date sqlDate) {
        int day = sqlDate.getDate();
        int month = sqlDate.getMonth() + 1;
        int year = sqlDate.getYear() + 1900;
        String format = year + "-" + month + "-" + day;
        return newYYYYMMDD_quiet(format);
    }

    /**
     * Opposite to toYYYYMMDD. Don't use this - use newDD_MM_YYYY instead
     * 
     * @param string date in yyyymmdd format
     */
    public BusinessDate(String yyyymmdd) {
        try {
            synchronized (YYYY_MM_DD) {
                Date backing = YYYY_MM_DD.parse(yyyymmdd);
                setTime(backing.getTime());
            }
        } catch (ParseException ex) {
            throw new SystemException(ex);
        }
    }

    public String toString() {
        String str = toDD_MM_YYYY();
        return str;
    }

    public String toDD_MM_YYYY() {
        int[] bits = getDDMMYYYY();
        String formatted = pad("" + bits[0], 2) + "/" + pad("" + bits[1], 2) + "/" + bits[2];
        return formatted;
    }

    /**
     * Zero-padded YYYY_MM_DD format. Some client code depends on the zero-padding so don't change this!
     */
    public String toYYYY_MM_DD() {
        int[] bits = getDDMMYYYY();
        String formatted = bits[2] + "-" + pad("" + bits[1], 2) + "-" + pad("" + bits[0], 2);
        return formatted;
    }
    
    public String toDD_MMM_YYYY() {
        int[] bits = getDDMMYYYY();
        return bits[0] + " " + MMM[bits[1]] + " " + bits[2];
    }

    private String pad(String in, int minLength) {
        while (in.length() < minLength) {
            in = "0" + in;
        }
        return in;
    }

    /**
     * Return the day, month and year components as integers (1..31, 1..12,
     * 2000...)
     */
    public int[] getDDMMYYYY() {
        int[] bits = new int[3];
        bits[0] = getBusinessDayOfMonth();
        bits[1] = getBusinessMonth();
        bits[2] = getBusinessYear();
        return bits;
    }

    public java.sql.Date toSqlDate() {
        int[] bits = getDDMMYYYY();
        java.sql.Date sqlDate = new java.sql.Date(bits[2] - 1900, bits[1] - 1, bits[0]);
        return sqlDate;
    }

    private long getTime() {
        Date backing = backing();
        return backing.getTime();
    }

    /**
     * Get the java.lang.Date that corresponds to this BusinessDate, in the
     * timezone of the executing JVM.
     */
    public Date getLocalMidnight() {
        String yyyymmdd = toYYYY_MM_DD();
        try {
            Date d6 = YYYY_MM_DD_HH_MM_SS.parse(yyyymmdd + " 06:00:00");
            long time = d6.getTime() - 6 * 3600 * 1000;
            Date d = new Date(time);
            return d;
        } catch (ParseException impossible) { 
            throw new SystemException(impossible);
        }
    }

    /**
     * Calculate the number of days elapsed since the given business date
     */
    public int daysSince(BusinessDate pastDate) {
        long now = getTime();
        long then = pastDate.getTime();

        long millisElapsed = now - then;
        
        // The difference in milliseconds between different "midnights" will not 
        // always be a multiple of 24 hours, since there can be changes due to daylight savings, 
        // leap seconds, etc. But it's safe to think that such adjustments will always amount to less
        // than half a day, so simple rounding will be adequate to compensate.
        
        double z = Math.round(((double)millisElapsed) / 1000 / 60 / 60 / 24);
        
        int days = (int) z;
        
        return days;
    }

    /**
     * Calculate the number of days elapsed since the given business date
     */
    public int daysTo(BusinessDate futureDate) {
        return 0 - daysSince(futureDate);
    }

    /**
     * Return true if this date is after the given other date
     * 
     * @param other
     * @return
     */
    public boolean after(BusinessDate other) {
        return backing().after(other.backing());
    }

    /**
     * Return true if this date is after or the same as the given other date
     * @param other
     * @return
     */
    public boolean afterOrEquals(BusinessDate other) {
        return backing().after(other.backing()) || compareTo(other) == 0;
    }

    /**
     * Return true if this date is before the given other date
     * 
     * @param other
     * @return
     */
    public boolean before(BusinessDate other) {
        return backing().before(other.backing());
    }

    /**
     * Return true if this date is before or the same as the given other date
     * @param other
     * @return
     */
    public boolean beforeOrEquals(BusinessDate other) {
        return backing().before(other.backing()) || compareTo(other) == 0;
    }

    /**
     * Like {@link Date#compareTo}.
     * 
     * @param other
     * @return
     */
    public int compareTo(BusinessDate other) {
        return backing().compareTo(other.backing());
    }

    private Calendar calendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        return cal;
    }
    
    /**
     * Calculate the number of whole years elapsed since the pastDate. Good for
     * calculating people's ages. Can return null if pastDate is null
     */
    public Integer yearsSince(BusinessDate pastDate) {

        if (pastDate == null) {
            return null;
        }
        
        Calendar birthCal = pastDate.calendar();
        Calendar nowCal = this.calendar();

        int birthYear = pastDate.year;
        int nowYear = this.year;

        // The age is the nowYear - birthYear, less one if we haven't
        // reached the birthday yet

        int years = nowYear - birthYear;

        nowCal.set(Calendar.YEAR, birthYear);
        if (nowCal.before(birthCal)) {
            years--;
        }

        return years;
    }

    /**
     * Calculate a new date by adding a given number of days.
     * 
     * @param days may be negative
     */
    public BusinessDate addDays(int days) {
        Calendar cal = calendar();
        cal.add(Calendar.DAY_OF_YEAR, days);
        Date then = cal.getTime();
        BusinessDate bthen = newDate(then);
        return bthen;
    }

    /**
     * Calculate a new date by adding a given number of months.
     * 
     * @param days may be negative
     */
    public BusinessDate addMonths(int months) {
        Calendar cal = calendar();
        cal.add(Calendar.MONTH, months);
        Date then = cal.getTime();
        BusinessDate bthen = newDate(then);
        return bthen;
    }

    /**
     * Calculate a new date by adding a given number of years.
     * 
     * @param years may be negative
     */
    public BusinessDate addYears(int years) {
        Calendar cal = calendar();
        cal.add(Calendar.YEAR, years);
        Date then = cal.getTime();
        BusinessDate bthen = newDate(then);
        return bthen;
    }

    /**
     * Return the day of month (1..31)
     */
    public int getBusinessDayOfMonth() {
        return day;
    }

    /**
     * Return the month of year (1..12)
     */
    public int getBusinessMonth() {
        return month;
    }

    /**
     * Return the day of week - values are {@link Calendar#MONDAY}, ..., {@link Calendar#SUNDAY}
     */
    public int getDayOfWeek() {
        Calendar cal = calendar();
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Return the year (4-digit format)
     */
    public int getBusinessYear() {
        return year;
    }
    
    /**
     * Get the earliest of a series of BusinessDates
     */
    public static BusinessDate getEarliest(BusinessDate... dates) {
        BusinessDate earliest = null;
        for (BusinessDate candidate: dates) {
            if (earliest == null) {
                earliest = candidate;
            } else {
                if (candidate != null) {
                    if (candidate.before(earliest)) {
                        earliest = candidate;
                    }
                }
            }
        }
        return earliest;
    }

    /**
     * Get the latest of a series of BusinessDates
     */
    public static BusinessDate getLatest(BusinessDate... dates) {
        BusinessDate latest = null;
        for (BusinessDate candidate: dates) {
            if (latest == null) {
                latest = candidate;
            } else {
                if (candidate != null) {
                    if (candidate.after(latest)) {
                        latest = candidate;
                    }
                }
            }
        }
        return latest;
    }

    public boolean equals(Object other) {
        if (!(other instanceof BusinessDate)) {
            return false;
        }
        BusinessDate o = (BusinessDate) other;
        String me = toDD_MM_YYYY();
        String you = o.toDD_MM_YYYY();
        return me.equals(you);
    }

    public int hashCode() {
        return toDD_MM_YYYY().hashCode();
    }
    
    
    
}
