/*
  Abstract base class for every ride. It holds the attributes common to
  private and shared rides and cannot be instantiated directly. The pickup
  and drop-off times are set only in the constructor and never change.
  Each ride gets a unique, automatically assigned ride ID.
 */
public abstract class Ride implements IRide {

    private static int nextRideId = 1;

    private final int rideId;
    private String pickupLocation;
    private final IDateTime pickupTime;
    private final IDateTime dropoffTime;
    private String dropoffLocation;
    private IDriver driver;

    /*
      Creates a ride.
     
      throws IllegalArgumentException if a field is null/empty or the
                pickup time is not before the drop-off time
     */
    protected Ride(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime,
                   String dropoffLocation, IDriver driver) throws IllegalArgumentException {
        if (pickupTime == null || dropoffTime == null) {
            throw new IllegalArgumentException("Pickup and drop-off times are required.");
        }
        if (pickupTime.compareTo(dropoffTime) >= 0) {
            throw new IllegalArgumentException("Pickup time must be before drop-off time.");
        }
        this.rideId = nextRideId++;
        this.pickupTime = pickupTime;
        this.dropoffTime = dropoffTime;
        setPickupLocation(pickupLocation);
        setDropoffLocation(dropoffLocation);
        setDriver(driver);
    }

    @Override
    public int getRideId() {
        return rideId;
    }

    @Override
    public String getPickupLocation() {
        return pickupLocation;
    }

    /*
      Changes the pickup location. Note: do not call this on a ride that is
      already stored in a RideList, because the list is ordered by pickup location.
     */
    @Override
    public final void setPickupLocation(String pickupLocation) throws IllegalArgumentException {
        if (pickupLocation == null || pickupLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location must not be empty.");
        }
        this.pickupLocation = pickupLocation.trim();
    }

    @Override
    public IDateTime getPickupTime() {
        return pickupTime;
    }

    @Override
    public IDateTime getDropoffTime() {
        return dropoffTime;
    }

    @Override
    public String getDropoffLocation() {
        return dropoffLocation;
    }

    @Override
    public final void setDropoffLocation(String dropoffLocation) throws IllegalArgumentException {
        if (dropoffLocation == null || dropoffLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Drop-off location must not be empty.");
        }
        this.dropoffLocation = dropoffLocation.trim();
    }

    @Override
    public IDriver getDriver() {
        return driver;
    }

    @Override
    public final void setDriver(IDriver driver) throws IllegalArgumentException {
        if (driver == null) {
            throw new IllegalArgumentException("A ride needs a driver.");
        }
        this.driver = driver;
    }

    /* Returns "PRIVATE" or "SHARED". */
    protected abstract String getRideType();

    /* Returns a description of the rider(s) on this ride, for toString. */
    protected abstract String describeRiders();

    /* Orders rides alphabetically (ignoring case) by pickup location. */
    @Override
    public int compareTo(IRide other) {
        return pickupLocation.compareToIgnoreCase(other.getPickupLocation());
    }

    /* Two rides are equal when they have the same ride ID. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Ride && ((Ride) obj).rideId == rideId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(rideId);
    }

    @Override
    public String toString() {
        return "Ride #" + rideId + " [" + getRideType() + "] " + pickupLocation + " (" + pickupTime.format()
                + ") -> " + dropoffLocation + " (" + dropoffTime.format() + ") | Driver: "
                + driver.getName() + " (ID " + driver.getId() + ") | " + describeRiders();
    }
}
