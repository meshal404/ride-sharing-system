/*
  Rides stored in a plain singly linked list (head only, cursor-style
  LinkedList), kept in alphabetical order by pickup location
  (case-insensitive). Rides with the same pickup location stay in the order
  they were added.
 
  The LinkedList can only insert AFTER the current node, so inserting
  "before" a node X is done by putting the new ride into X's slot and
  re-inserting X's old ride right after it.
 
  Every list returned by this class has its cursor reset to the first
  element, so callers can start reading with retrieve() straight away
  (check empty() first).
 */
public class RideList implements IRideList {

    private final LinkedList<IRide> rides = new LinkedList<>();
    private int count = 0; // the LinkedList has no size(), so we keep our own count

    /*
      Inserts a ride in alphabetical position. Returns false if the ride is
      null or a ride with the same ride ID is already stored. O(n).
     */
    @Override
    public boolean addRide(IRide ride) {
        if (ride == null) {
            return false;
        }
        if (rides.empty()) {
            rides.insert(ride);
            count++;
            return true;
        }

        // Pass 1: reject a duplicate ride ID.
        rides.findFirst();
        while (true) {
            if (rides.retrieve().getRideId() == ride.getRideId()) {
                return false;
            }
            if (rides.last()) {
                break;
            }
            rides.findNext();
        }

        // Pass 2: find the first ride that sorts after the new one.
        rides.findFirst();
        while (true) {
            IRide existing = rides.retrieve();
            if (existing.compareTo(ride) > 0) {
                rides.update(ride);     // new ride takes this slot 
                rides.insert(existing); // and the old one goes right after it
                break;
            }
            if (rides.last()) {         // nothing sorts after it: append at the end
                rides.insert(ride);
                break;
            }
            rides.findNext();
        }
        count++;
        return true;
    }

    /* Removes the ride with the given ride ID; returns true if it was found. O(n). */
    @Override
    public boolean removeRideById(int rideId) {
        if (rides.empty()) {
            return false;
        }
        rides.findFirst();
        while (true) {
            if (rides.retrieve().getRideId() == rideId) {
                rides.remove();
                count--;
                return true;
            }
            if (rides.last()) {
                return false;
            }
            rides.findNext();
        }
    }

    /* Returns a copy of all rides in alphabetical order. O(n). */
    @Override
    public LinkedList<IRide> getAllAlphabetically() {
        LinkedList<IRide> result = new LinkedList<>();
        if (!rides.empty()) {
            rides.findFirst();
            while (true) {
                result.insert(rides.retrieve()); // insert appends after the cursor, keeping order
                if (rides.last()) {
                    break;
                }
                rides.findNext();
            }
        }
        result.findFirst();
        return result;
    }

    /* Rides whose pickup location equals the given one (ignoring case). O(n). */
    @Override
    public LinkedList<IRide> findByPickupLocation(String pickupLocation) {
        LinkedList<IRide> result = new LinkedList<>();
        if (!rides.empty()) {
            rides.findFirst();
            while (true) {
                IRide ride = rides.retrieve();
                if (ride.getPickupLocation().equalsIgnoreCase(pickupLocation)) {
                    result.insert(ride);
                }
                if (rides.last()) {
                    break;
                }
                rides.findNext();
            }
        }
        result.findFirst();
        return result;
    }

    /* Rides (private or shared) that include a rider with exactly this full name. */
    @Override
    public LinkedList<IRide> findByRiderName(String riderFullName) {
        LinkedList<IRide> result = new LinkedList<>();
        if (!rides.empty()) {
            rides.findFirst();
            while (true) {
                IRide ride = rides.retrieve();
                if (involvesRiderNamed(ride, riderFullName)) {
                    result.insert(ride);
                }
                if (rides.last()) {
                    break;
                }
                rides.findNext();
            }
        }
        result.findFirst();
        return result;
    }

    /* Checks whether any rider on the ride has the given full name. */
    private static boolean involvesRiderNamed(IRide ride, String name) {
        if (ride instanceof IPrivateRide) {
            IRider rider = ((IPrivateRide) ride).getRider();
            return rider != null && rider.getName().equals(name);
        }
        if (ride instanceof ISharedRide) {
            LinkedList<IRider> participants = ((ISharedRide) ride).getParticipants();
            if (participants.empty()) {
                return false;
            }
            participants.findFirst();
            while (true) {
                if (participants.retrieve().getName().equals(name)) {
                    return true;
                }
                if (participants.last()) {
                    return false;
                }
                participants.findNext();
            }
        }
        return false;
    }

    /* Number of rides stored. O(1). */
    @Override
    public int size() {
        return count;
    }
}
