/*
  Linked list of rides kept in alphabetical order by pickup location
  (case-insensitive). Rides with the same pickup location stay in the order
  they were added.
 */
public class RideList implements IRideList {

    private final LinkedList<IRide> rides = new LinkedList<>();

    /*
      Inserts a ride in alphabetical position. Returns false if the ride is
      null or a ride with the same ride ID is already stored. O(n).
     */
    @Override
    public boolean addRide(IRide ride) {
        if (ride == null) {
            return false;
        }
        LinkedList.Node<IRide> insertAfter = null;
        for (LinkedList.Node<IRide> cur = rides.getHead(); cur != null; cur = cur.getNext()) {
            if (cur.getData().getRideId() == ride.getRideId()) {
                return false;
            }
            if (cur.getData().compareTo(ride) <= 0) {
                insertAfter = cur; // keep going after equal locations (stable)
            }
        }
        rides.insertAfter(insertAfter, ride);
        return true;
    }

    @Override
    public boolean removeRideById(int rideId) {
        LinkedList.Node<IRide> prev = null;
        for (LinkedList.Node<IRide> cur = rides.getHead(); cur != null; cur = cur.getNext()) {
            if (cur.getData().getRideId() == rideId) {
                rides.removeAfter(prev);
                return true;
            }
            prev = cur;
        }
        return false;
    }

    /* Returns a copy of the list in alphabetical order. O(n). */
    @Override
    public LinkedList<IRide> getAllAlphabetically() {
        LinkedList<IRide> result = new LinkedList<>();
        for (LinkedList.Node<IRide> cur = rides.getHead(); cur != null; cur = cur.getNext()) {
            result.addLast(cur.getData());
        }
        return result;
    }

    @Override
    public LinkedList<IRide> findByPickupLocation(String pickupLocation) {
        LinkedList<IRide> result = new LinkedList<>();
        for (LinkedList.Node<IRide> cur = rides.getHead(); cur != null; cur = cur.getNext()) {
            if (cur.getData().getPickupLocation().equalsIgnoreCase(pickupLocation)) {
                result.addLast(cur.getData());
            }
        }
        return result;
    }

    /* Rides (private or shared) that include a rider with exactly this full name. */
    @Override
    public LinkedList<IRide> findByRiderName(String riderFullName) {
        LinkedList<IRide> result = new LinkedList<>();
        for (LinkedList.Node<IRide> cur = rides.getHead(); cur != null; cur = cur.getNext()) {
            if (involvesRiderNamed(cur.getData(), riderFullName)) {
                result.addLast(cur.getData());
            }
        }
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
            for (LinkedList.Node<IRider> n = participants.getHead(); n != null; n = n.getNext()) {
                if (n.getData().getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int size() {
        return rides.size();
    }
}
