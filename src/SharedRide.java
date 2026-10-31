/*
  A carpool ride that can carry several riders; each rider appears at most
  once
 */
public class SharedRide extends Ride implements ISharedRide {

    private final LinkedList<IRider> participants;

    /* Creates a shared ride with no participants yet; add them with addParticipant. */
    public SharedRide(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime,
                      String dropoffLocation, IDriver driver) {
        super(pickupLocation, pickupTime, dropoffTime, dropoffLocation, driver);
        participants = new LinkedList<>();
    }

    /*
      Returns a copy of the participant list, in the order they were added.
      The copy's cursor is on the first element (check empty() before retrieve())
     */
    @Override
    public LinkedList<IRider> getParticipants() {
        LinkedList<IRider> copy = new LinkedList<>();
        if (!participants.empty()) {
            participants.findFirst();
            while (true) {
                copy.insert(participants.retrieve()); // insert appends after the cursor, keeping order
                if (participants.last()) {
                    break;
                }
                participants.findNext();
            }
        }
        copy.findFirst();
        return copy;
    }

    /* Adds a rider at the end; returns false if the rider is null or already on the ride*/
    @Override
    public boolean addParticipant(IRider rider) {
        if (rider == null || hasRider(rider.getId())) {
            return false;
        }
        if (!participants.empty()) {
            // insert() adds after the cursor, so move the cursor to the last node first
            participants.findFirst();
            while (!participants.last()) {
                participants.findNext();
            }
        }
        participants.insert(rider);
        return true;
    }

    /* Removes the participant with the given ID; returns true if found*/
    @Override
    public boolean removeParticipantById(int riderId) {
        if (participants.empty()) {
            return false;
        }
        participants.findFirst();
        while (true) {
            if (participants.retrieve().getId() == riderId) {
                participants.remove();
                return true;
            }
            if (participants.last()) {
                return false;
            }
            participants.findNext();
        }
    }

    @Override
    public boolean isEmpty() {
        return participants.empty();
    }

    /* True if a rider with this ID is on the ride */
    @Override
    public boolean hasRider(int riderId) {
        if (participants.empty()) {
            return false;
        }
        participants.findFirst();
        while (true) {
            if (participants.retrieve().getId() == riderId) {
                return true;
            }
            if (participants.last()) {
                return false;
            }
            participants.findNext();
        }
    }

    @Override
    protected String getRideType() {
        return "SHARED";
    }

    @Override
    protected String describeRiders() {
        StringBuilder sb = new StringBuilder("Riders: ");
        if (!participants.empty()) {
            participants.findFirst();
            while (true) {
                IRider r = participants.retrieve();
                sb.append(r.getName()).append(" (ID ").append(r.getId()).append(")");
                if (participants.last()) {
                    break;
                }
                sb.append(", ");
                participants.findNext();
            }
        }
        return sb.toString();
    }
}
