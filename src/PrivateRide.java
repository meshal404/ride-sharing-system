/* A ride with exactly one rider. Immutable: the rider is set only in the constructor. */
public class PrivateRide extends Ride implements IPrivateRide {

    private final IRider rider;

    /*
      Creates a private ride for one rider.

      throws IllegalArgumentException if the rider is null or the
                common ride fields are invalid (see Ride)
     */
    public PrivateRide(int rideId, String pickupLocation, String dropoffLocation,
                       ITimeSlot timeSlot, IRider rider, IDriver driver) throws IllegalArgumentException {
        super(rideId, pickupLocation, dropoffLocation, timeSlot, driver);
        if (rider == null) {
            throw new IllegalArgumentException("A private ride needs a rider.");
        }
        this.rider = rider;
    }

    @Override
    public IRider getRider() {
        return rider;
    }

    /* True if the given ID is the ID of this ride's rider. */
    @Override
    public boolean hasRider(int riderId) {
        return rider.getId() == riderId;
    }

    @Override
    protected String getRideType() {
        return "PRIVATE";
    }

    @Override
    protected String describeRiders() {
        return "Rider: " + rider.getName() + " (ID " + rider.getId() + ")";
    }
}
