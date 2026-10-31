/**
  An immutable point in time (year, month, day, hour, minute). Once created
  its fields never change. Instances are ordered chronologically.
 */
public class DateTime implements IDateTime {

    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;

    /**
      Creates a date/time.
     
      throws IllegalArgumentException if any field is out of range
            (including a day that does not exist in that month)
     */
    public DateTime(int year, int month, int day, int hour, int minute) throws IllegalArgumentException {
        if (year < 1) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        if (day < 1 || day > daysInMonth(year, month)) {
            throw new IllegalArgumentException("Invalid day: " + day);
        }
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Invalid hour: " + hour);
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Invalid minute: " + minute);
        }
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    /**
      Parses text in the format "MM/DD/YYYY HH:MM".
     
      throws IllegalArgumentException if the text is malformed or out of range
     */
    public static DateTime parse(String text) throws IllegalArgumentException {
        if (text == null) {
            throw new IllegalArgumentException("Date/time text is null.");
        }
        String[] parts = text.trim().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected format MM/DD/YYYY HH:MM");
        }
        String[] date = parts[0].split("/");
        String[] time = parts[1].split(":");
        if (date.length != 3 || time.length != 2) {
            throw new IllegalArgumentException("Expected format MM/DD/YYYY HH:MM");
        }
        // NumberFormatException is a subclass of IllegalArgumentException
        return new DateTime(Integer.parseInt(date[2]), Integer.parseInt(date[0]),
                Integer.parseInt(date[1]), Integer.parseInt(time[0]), Integer.parseInt(time[1]));
    }

    /** Returns the number of days in the given month of the given year. */
    private static int daysInMonth(int year, int month) {
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                boolean leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
                return leap ? 29 : 28;
            default:
                return 31;
        }
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getDay() {
        return day;
    }

    @Override
    public int getHour() {
        return hour;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    /** Formats as "MM/DD/YYYY HH:MM". */
    @Override
    public String format() {
        return String.format("%02d/%02d/%04d %02d:%02d", month, day, year, hour, minute);
    }

    /** Chronological order: year, then month, then day, then hour, then minute. */
    @Override
    public int compareTo(IDateTime other) {
        int c = Integer.compare(year, other.getYear());
        if (c != 0) {
            return c;
        }
        c = Integer.compare(month, other.getMonth());
        if (c != 0) {
            return c;
        }
        c = Integer.compare(day, other.getDay());
        if (c != 0) {
            return c;
        }
        c = Integer.compare(hour, other.getHour());
        if (c != 0) {
            return c;
        }
        return Integer.compare(minute, other.getMinute());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IDateTime && compareTo((IDateTime) obj) == 0;
    }

    @Override
    public int hashCode() {
        return ((((year * 12 + month) * 31 + day) * 24 + hour) * 60) + minute;
    }

    @Override
    public String toString() {
        return format();
    }
}
