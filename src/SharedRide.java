/*
  A carpool ride that can carry several riders. The participants are stored
  in a BST keyed by rider ID, so each rider appears at most once and
  add / remove / lookup cost O(h).
 */
public class SharedRide extends Ride implements ISharedRide {

    private final IBST<Integer, IRider> participants;

    /*
      Creates a shared ride with no participants yet; add them with addParticipant.

      throws IllegalArgumentException if the common ride fields are invalid (see Ride)
     */
    public SharedRide(int rideId, String pickupLocation, String dropoffLocation,
                      ITimeSlot timeSlot, IDriver driver) throws IllegalArgumentException {
        super(rideId, pickupLocation, dropoffLocation, timeSlot, driver);
        participants = new BST<Integer, IRider>();
    }

    /* Returns the participants sorted by rider ID (a new list; changing it does not affect the ride). */
    @Override
    public LinkedList<IRider> getParticipants() {
        return participants.inOrder();
    }

    /* Adds a rider; returns false if the rider is null or already on the ride. */
    @Override
    public boolean addParticipant(IRider rider) {
        if (rider == null) {
            return false;
        }
        return participants.insert(rider.getId(), rider);
    }

    /* Removes the participant with the given ID; returns false if there is none. */
    @Override
    public boolean removeParticipantById(int riderId) {
        return participants.removeKey(riderId);
    }

    @Override
    public boolean isEmpty() {
        return participants.empty();
    }

    /* True if a rider with this ID is on the ride. */
    @Override
    public boolean hasRider(int riderId) {
        return participants.findKey(riderId);
    }

    @Override
    protected String getRideType() {
        return "SHARED";
    }

    /* Lists the participants as "Riders: name (ID x), name (ID y), ...". */
    @Override
    protected String describeRiders() {
        StringBuilder sb = new StringBuilder("Riders: ");
        LinkedList<IRider> list = participants.inOrder();
        if (!list.empty()) {
            list.findFirst();
            while (true) {
                IRider r = list.retrieve();
                sb.append(r.getName()).append(" (ID ").append(r.getId()).append(")");
                if (list.last()) {
                    break;
                }
                sb.append(", ");
                list.findNext();
            }
        }
        return sb.toString();
    }
}
