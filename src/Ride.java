/*
  Abstract base class for every ride. It holds the attributes shared by
  private and shared rides and cannot be instantiated directly.
  A ride is immutable: every field is final and is set only in the
  constructor, so there are no setters. The ride ID is assigned by the
  RideSharingSystem and passed to the constructor.
  Rides are compared by ride ID.
 */
public abstract class Ride implements IRide {

    private final int rideId;
    private final String pickupLocation;
    private final String dropoffLocation;
    private final ITimeSlot timeSlot;
    private final IDriver driver;

    /*
      Creates a ride.

      throws IllegalArgumentException if a location is null or empty,
                or if the time slot or the driver is null
     */
    protected Ride(int rideId, String pickupLocation, String dropoffLocation,
                   ITimeSlot timeSlot, IDriver driver) throws IllegalArgumentException {
        if (pickupLocation == null || pickupLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location must not be empty.");
        }
        if (dropoffLocation == null || dropoffLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Drop-off location must not be empty.");
        }
        if (timeSlot == null) {
            throw new IllegalArgumentException("A ride needs a time slot.");
        }
        if (driver == null) {
            throw new IllegalArgumentException("A ride needs a driver.");
        }
        this.rideId = rideId;
        this.pickupLocation = pickupLocation.trim();
        this.dropoffLocation = dropoffLocation.trim();
        this.timeSlot = timeSlot;
        this.driver = driver;
    }

    @Override
    public int getRideId() {
        return rideId;
    }

    @Override
    public String getPickupLocation() {
        return pickupLocation;
    }

    @Override
    public String getDropoffLocation() {
        return dropoffLocation;
    }

    @Override
    public ITimeSlot getTimeSlot() {
        return timeSlot;
    }

    @Override
    public IDriver getDriver() {
        return driver;
    }

    /* Returns true if the rider takes part in this ride (implemented by the subclasses). */
    @Override
    public abstract boolean hasRider(int riderId);

    /* Returns "PRIVATE" or "SHARED". */
    protected abstract String getRideType();

    /* Returns a description of the rider(s) on this ride, used by toString. */
    protected abstract String describeRiders();

    /* Orders rides by ride ID. */
    @Override
    public int compareTo(IRide other) {
        return Integer.compare(rideId, other.getRideId());
    }

    /* Two rides are equal when they have the same ride ID. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof IRide && ((IRide) obj).getRideId() == rideId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(rideId);
    }

    @Override
    public String toString() {
        return "Ride #" + rideId + " [" + getRideType() + "] " + pickupLocation + " ("
                + timeSlot.getStart().format() + ") -> " + dropoffLocation + " ("
                + timeSlot.getEnd().format() + ") | Driver: " + driver.getName()
                + " (ID " + driver.getId() + ") | " + describeRiders();
    }
}
