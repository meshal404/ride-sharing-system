/** A half-open interval [start, end) used in a person's ride schedule. */
public class TimeSlot implements ITimeSlot {
    private final IDateTime start;
    private final IDateTime end;

    public TimeSlot(IDateTime start, IDateTime end) {
        if (start == null || end == null || start.compareTo(end) >= 0)
            throw new IllegalArgumentException("A time slot requires a start before its end.");
        this.start = start;
        this.end = end;
    }

    public IDateTime getStart() { return start; }
    public IDateTime getEnd() { return end; }

    public int compareTo(ITimeSlot other) {
        if (other == null) throw new IllegalArgumentException("Other time slot cannot be null.");
        if (end.compareTo(other.getStart()) <= 0) return -1;
        if (start.compareTo(other.getEnd()) >= 0) return 1;
        return 0;
    }

    public String toString() { return "[" + start.format() + " - " + end.format() + ")"; }
}
