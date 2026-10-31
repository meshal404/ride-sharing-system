/* A carpool ride that can carry several riders. Each rider appears at most once. */
public class SharedRide extends Ride implements ISharedRide {

    private final LinkedList<IRider> participants;

    /* Creates a shared ride with no participants yet; add them with addParticipant. */
    public SharedRide(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime,
                      String dropoffLocation, IDriver driver) {
        super(pickupLocation, pickupTime, dropoffTime, dropoffLocation, driver);
        participants = new LinkedList<>();
    }

    /* Returns a copy of the participant list, in the order they were added. */
    @Override
    public LinkedList<IRider> getParticipants() {
        LinkedList<IRider> copy = new LinkedList<>();
        for (LinkedList.Node<IRider> n = participants.getHead(); n != null; n = n.getNext()) {
            copy.addLast(n.getData());
        }
        return copy;
    }

    /* Adds a rider; returns false if the rider is null or already on the ride. O(k). */
    @Override
    public boolean addParticipant(IRider rider) {
        if (rider == null || hasRider(rider.getId())) {
            return false;
        }
        participants.addLast(rider);
        return true;
    }

    /* Removes the participant with the given ID; returns true if found. O(k). */
    @Override
    public boolean removeParticipantById(int riderId) {
        LinkedList.Node<IRider> prev = null;
        for (LinkedList.Node<IRider> cur = participants.getHead(); cur != null; cur = cur.getNext()) {
            if (cur.getData().getId() == riderId) {
                participants.removeAfter(prev);
                return true;
            }
            prev = cur;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return participants.isEmpty();
    }

    @Override
    public boolean hasRider(int riderId) {
        for (LinkedList.Node<IRider> n = participants.getHead(); n != null; n = n.getNext()) {
            if (n.getData().getId() == riderId) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getRideType() {
        return "SHARED";
    }

    @Override
    protected String describeRiders() {
        StringBuilder sb = new StringBuilder("Riders: ");
        for (LinkedList.Node<IRider> n = participants.getHead(); n != null; n = n.getNext()) {
            sb.append(n.getData().getName()).append(" (ID ").append(n.getData().getId()).append(")");
            if (n.getNext() != null) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
