/* A ride with exactly one rider. */
public class PrivateRide extends Ride implements IPrivateRide {

    private IRider rider;

    /* Creates a private ride for one rider. */
    public PrivateRide(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime,
                       String dropoffLocation, IRider rider, IDriver driver) {
        super(pickupLocation, pickupTime, dropoffTime, dropoffLocation, driver);
        setRider(rider);
    }

    @Override
    public IRider getRider() {
        return rider;
    }

    @Override
    public final void setRider(IRider rider) throws IllegalArgumentException {
        if (rider == null) {
            throw new IllegalArgumentException("A private ride needs a rider.");
        }
        this.rider = rider;
    }

    @Override
    public boolean hasRider(int riderId) {
        return rider != null && rider.getId() == riderId;
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
