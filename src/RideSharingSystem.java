import java.io.IOException;

/**
 * The Ride-Sharing System (Phase 2). All data is stored in the nine BST
 * indexes below. This class never prints; all input and output is in Main.
 *
 * SKELETON: every method currently throws UnsupportedOperationException.
 * Each person replaces ONLY the stubs of their own section:
 *   Person A: riders          Person B: rides          Person C: drivers and statistics
 */
public class RideSharingSystem implements IRideSharingSystem {

    // ---------------------------------------------------------------- indexes
    // Riders
    private IBST<Integer, IRider> ridersById = new BST<Integer, IRider>();
    private IBST<String, IBST<Integer, IRider>> ridersByName = new BST<String, IBST<Integer, IRider>>();
    private IBST<String, IBST<Integer, IRider>> ridersByCity = new BST<String, IBST<Integer, IRider>>();

    // Drivers
    private IBST<Integer, IDriver> driversById = new BST<Integer, IDriver>();
    private IBST<String, IDriver> driversByPlate = new BST<String, IDriver>();
    private IBST<VehicleType, IBST<Integer, IDriver>> driversByType = new BST<VehicleType, IBST<Integer, IDriver>>();

    // Rides
    private IBST<Integer, IRide> ridesById = new BST<Integer, IRide>();
    private IBST<String, IBST<Integer, IRide>> ridesByPickupLocation = new BST<String, IBST<Integer, IRide>>();
    private IBST<IDateTime, IBST<Integer, IRide>> ridesByPickupTime = new BST<IDateTime, IBST<Integer, IRide>>();

    /** The ID given to the next successfully scheduled ride (1, 2, 3, ...; never reused). */
    private int nextRideId = 1;

    /** Creates an empty system. */
    public RideSharingSystem() {}

    // ================================================================ PERSON A: riders
    public int loadRidersFromCSV(String ridersFilePath) 
    {
        String[][] rows;

        try 
        {
            rows = CsvHelper.readRows(ridersFilePath);
        } 
        catch (IOException e) 
        {
            return -1; // file cannot be opened
        }

        int loaded = 0;

        for (int i = 0; i < rows.length; i++) 
        {
            String[] f = rows[i];

            // layout: id, firstName, lastName, email, phoneNumber, homeCity
            if (f.length < 6)
                continue; // not correct, skip it

            try 
            {
                String fullName = f[1] + " " + f[2];
                IRider rider = new Rider(Integer.parseInt(f[0]), fullName, f[3], f[4], f[5]);

                if (addRider(rider))
                    loaded++; // duplicate IDs are skipped
            } 
            catch (IllegalArgumentException e) 
            {
                // covers bad IDs and an invalid phone 
            }
        }

        return loaded;
    }

    public boolean addRider(IRider rider) 
    {
        // Check uniqueness first so a rejected rider is not left in any index
        if (rider == null || ridersById.findKey(rider.getId()))
            return false;

        ridersById.insert(rider.getId(), rider);
        IndexHelper.addToIndex(ridersByName, rider.getName(), rider.getId(), rider);
        IndexHelper.addToIndex(ridersByCity, rider.getHomeCity(), rider.getId(), rider);

        return true;
    }

    public IRider searchRiderById(int riderId) 
    {
        if(ridersById.findKey(riderId))
            return ridersById.retrieve();

        return null; //not found
    }

    public LinkedList<IRider> searchRidersByName(String fullName) 
    {
        return IndexHelper.getAll(ridersByName, fullName);
    }

    public LinkedList<IRider> searchRidersByHomeCity(String homeCity) 
    {
        return IndexHelper.getAll(ridersByCity, homeCity);
    }

    public LinkedList<IRider> getAllRiders() 
    {
        return ridersById.inOrder();
    }

    public boolean removeRider(int riderId) 
    {
        if(!ridersById.findKey(riderId))
            return false;

        IRider rider = ridersById.retrieve();

        // inOrder() returns a new linkedlist, it's safe to change the rider's schedule BST
        LinkedList<IRide> rides = rider.getSchedule().inOrder();

        if(!rides.empty())
        {
            rides.findFirst();

            while(true)
            {
                IRide ride = rides.retrieve();
                
                if(ride instanceof ISharedRide)
                {
                    ISharedRide shared = (ISharedRide)ride;

                    // Take the rider out of the shared ride and out of their own schedule
                    shared.removeParticipantById(riderId);
                    rider.getSchedule().removeKey(shared.getTimeSlot());

                    // A shared ride left with no riders is deleted everywhere
                    if(shared.isEmpty())
                        cancelRide(shared.getRideId());

                }

                else
                    cancelRide(ride.getRideId());

                if(rides.last())
                    break;

                rides.findNext();
                
            }
        }

        // Remove the rider from all three rider indexes
        IndexHelper.removeFromIndex(ridersByName, rider.getName(), riderId);
        IndexHelper.removeFromIndex(ridersByCity, rider.getHomeCity(), riderId);
        ridersById.removeKey(riderId);

        return true;
    }

    // ================================================================ PERSON B: rides
    public int loadRidesFromCSV(String ridesFilePath) {
        throw new UnsupportedOperationException("TODO (Person B): loadRidesFromCSV");
    }

    public int schedulePrivateRide(String pickupLocation, String dropoffLocation,
                                   ITimeSlot timeSlot, int riderId, int driverId) {
        throw new UnsupportedOperationException("TODO (Person B): schedulePrivateRide");
    }

    public int scheduleSharedRide(String pickupLocation, String dropoffLocation,
                                  ITimeSlot timeSlot, int[] riderIds, int driverId) {
        throw new UnsupportedOperationException("TODO (Person B): scheduleSharedRide");
    }

    public boolean cancelRide(int rideId) {
        throw new UnsupportedOperationException("TODO (Person B): cancelRide");
    }

    public LinkedList<IRide> searchRidesByPickupLocation(String pickupLocation) {
        throw new UnsupportedOperationException("TODO (Person B): searchRidesByPickupLocation");
    }

    public LinkedList<IRide> searchRidesByRiderName(String riderName) {
        throw new UnsupportedOperationException("TODO (Person B): searchRidesByRiderName");
    }

    public LinkedList<IRider> getSharedRideParticipants(int rideId) {
        throw new UnsupportedOperationException("TODO (Person B): getSharedRideParticipants");
    }

    public LinkedList<IRide> getAllRidesAlphabetically() {
        throw new UnsupportedOperationException("TODO (Person B): getAllRidesAlphabetically");
    }

    public LinkedList<IRide> searchRidesByPickupTimeRange(IDateTime from, IDateTime to) {
        throw new UnsupportedOperationException("TODO (Person B): searchRidesByPickupTimeRange");
    }

    // ============================================== PERSON C: drivers and statistics
    public int loadDriversFromCSV(String driversFilePath) {
        throw new UnsupportedOperationException("TODO (Person C): loadDriversFromCSV");
    }

    public boolean addDriver(IDriver driver) {
        throw new UnsupportedOperationException("TODO (Person C): addDriver");
    }

    public IDriver searchDriverById(int driverId) {
        throw new UnsupportedOperationException("TODO (Person C): searchDriverById");
    }

    public LinkedList<IDriver> searchDriversByVehicleType(VehicleType vehicleType) {
        throw new UnsupportedOperationException("TODO (Person C): searchDriversByVehicleType");
    }

    public LinkedList<IDriver> getAllDrivers() {
        throw new UnsupportedOperationException("TODO (Person C): getAllDrivers");
    }

    public boolean removeDriver(int driverId) {
        throw new UnsupportedOperationException("TODO (Person C): removeDriver");
    }

    public LinkedList<IDriver> findAvailableDrivers(ITimeSlot timeSlot, VehicleType vehicleType) {
        throw new UnsupportedOperationException("TODO (Person C): findAvailableDrivers");
    }

    public int nbKeyCompRiderById(int riderId) {
        throw new UnsupportedOperationException("TODO (Person C): nbKeyCompRiderById");
    }

    public int indexSize(IndexName index) {
        throw new UnsupportedOperationException("TODO (Person C): indexSize");
    }

    public int indexHeight(IndexName index) {
        throw new UnsupportedOperationException("TODO (Person C): indexHeight");
    }
}