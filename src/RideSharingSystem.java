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

            // layout: id, fullname, email, phoneNumber, homeCity
            if (f.length < 5)
                continue; // not correct, skip it

            try 
            {
                IRider rider = new Rider(Integer.parseInt(f[0]), f[1], f[2], f[3], f[4]);

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

    // ---- index helpers: every add / delete of a ride goes through these two methods ----

    /* Adds the ride to the three ride indexes (by ID, by pickup location, by pickup time). */
    private void indexRide(IRide ride)
    {
        ridesById.insert(ride.getRideId(), ride);
        IndexHelper.addToIndex(ridesByPickupLocation, ride.getPickupLocation(), ride.getRideId(), ride);
        IndexHelper.addToIndex(ridesByPickupTime, ride.getTimeSlot().getStart(), ride.getRideId(), ride);
    }

    /* Removes the ride from the three ride indexes. */
    private void unindexRide(IRide ride)
    {
        ridesById.removeKey(ride.getRideId());
        IndexHelper.removeFromIndex(ridesByPickupLocation, ride.getPickupLocation(), ride.getRideId());
        IndexHelper.removeFromIndex(ridesByPickupTime, ride.getTimeSlot().getStart(), ride.getRideId());
    }

    /*
      Deletes a ride everywhere: from the three ride indexes, from the schedule
      of its driver and from the schedule of every rider on it.
      Used by cancelRide, removeRider and removeDriver.
     */
    private void deleteRide(IRide ride)
    {
        ITimeSlot slot = ride.getTimeSlot();

        unindexRide(ride);
        ride.getDriver().getSchedule().removeKey(slot);

        if (ride instanceof IPrivateRide)
        {
            ((IPrivateRide) ride).getRider().getSchedule().removeKey(slot);
        }
        else if (ride instanceof ISharedRide)
        {
            // getParticipants() returns a new list, so the ride is not modified while we walk it
            LinkedList<IRider> riders = ((ISharedRide) ride).getParticipants();

            if (!riders.empty())
            {
                riders.findFirst();

                while (true)
                {
                    riders.retrieve().getSchedule().removeKey(slot);

                    if (riders.last())
                        break;

                    riders.findNext();
                }
            }
        }
    }

    /* Returns true if the text is not null and has at least one non-blank character. */
    private boolean isValidLocation(String location)
    {
        return location != null && !location.trim().isEmpty();
    }

    /* Appends every element of src to the end of dest (dest keeps its order). */
    private <T> void appendAll(LinkedList<T> dest, LinkedList<T> src)
    {
        if (src.empty())
            return;

        src.findFirst();

        while (true)
        {
            dest.insert(src.retrieve()); // insert goes after the current node, i.e. at the end
            if (src.last())
                break;

            src.findNext();
        }
    }

    /*
      Loads rides from a CSV file without header:
      rideType,pickupLocation,pickupDateTime,dropoffDateTime,dropoffLocation,driverId,riderIds
      rideType is PRIVATE or SHARED; shared rider IDs are separated by ';'.
      Each line is scheduled in file order, so the first valid line gets ride ID 1, etc.
      Lines that cannot be parsed or scheduled are skipped.
      Returns the number of rides scheduled, or -1 if the file cannot be opened.
     */
    public int loadRidesFromCSV(String ridesFilePath)
    {
        String[][] rows;

        try
        {
            rows = CsvHelper.readRows(ridesFilePath);
        }
        catch (IOException e)
        {
            return -1; // file cannot be opened
        }

        int loaded = 0;

        for (int i = 0; i < rows.length; i++)
        {
            String[] f = rows[i];

            if (f.length < 7)
                continue; // missing columns, skip it

            try
            {
                String type = f[0].trim();
                IDateTime start = DateTime.parse(f[2]);
                IDateTime end = DateTime.parse(f[3]);
                ITimeSlot slot = new TimeSlot(start, end); // throws if start is not before end
                int driverId = Integer.parseInt(f[5].trim());
                int[] riderIds = CsvHelper.parseRiderIds(f[6]);
                int rideId = -1;

                // NOTE: the file lists the driver BEFORE the riders, the schedule methods take riders first
                if (type.equalsIgnoreCase("PRIVATE") && riderIds.length == 1)
                    rideId = schedulePrivateRide(f[1], f[4], slot, riderIds[0], driverId);

                else if (type.equalsIgnoreCase("SHARED"))
                    rideId = scheduleSharedRide(f[1], f[4], slot, riderIds, driverId);

                if (rideId != -1)
                    loaded++;
            }
            catch (IllegalArgumentException e)
            {
                // covers bad numbers, bad date/times and an invalid time slot
            }
        }

        return loaded;
    }

    /*
      Schedules a private ride. Everything is validated first (locations, time slot,
      rider and driver exist, no overlap with the rider's or the driver's schedule);
      only then is anything changed. A ride that starts exactly when another ends
      is not a conflict.
      Returns the new ride ID, or -1 (nothing changed) if a check fails.
     */
    public int schedulePrivateRide(String pickupLocation, String dropoffLocation,
                                   ITimeSlot timeSlot, int riderId, int driverId)
    {
        // 1. validate everything
        if (!isValidLocation(pickupLocation) || !isValidLocation(dropoffLocation) || timeSlot == null)
            return -1;

        if (!ridersById.findKey(riderId) || !driversById.findKey(driverId))
            return -1;

        IRider rider = ridersById.retrieve();
        IDriver driver = driversById.retrieve();

        // an overlapping ride makes findKey true (ITimeSlot.compareTo is 0 for overlapping slots)
        if (rider.getSchedule().findKey(timeSlot) || driver.getSchedule().findKey(timeSlot))
            return -1;

        // 2. nothing can fail now: create the ride and update every index and schedule
        IRide ride = new PrivateRide(nextRideId, pickupLocation, dropoffLocation, timeSlot, rider, driver);

        indexRide(ride);
        driver.getSchedule().insert(timeSlot, ride);
        rider.getSchedule().insert(timeSlot, ride);

        return nextRideId++;
    }

    /*
      Schedules a shared ride. riderIds must hold at least two IDs, without duplicates,
      and every rider and the driver must exist and be free during the time slot.
      If any check fails nothing is changed.
      Returns the new ride ID, or -1.
     */
    public int scheduleSharedRide(String pickupLocation, String dropoffLocation,
                                  ITimeSlot timeSlot, int[] riderIds, int driverId)
    {
        // 1. validate everything
        if (!isValidLocation(pickupLocation) || !isValidLocation(dropoffLocation) || timeSlot == null)
            return -1;

        if (riderIds == null || riderIds.length < 2 || !driversById.findKey(driverId))
            return -1;

        IDriver driver = driversById.retrieve();

        if (driver.getSchedule().findKey(timeSlot))
            return -1;

        // the temporary BST detects duplicate IDs (insert returns false) and keeps the riders found
        IBST<Integer, IRider> riders = new BST<Integer, IRider>();

        for (int i = 0; i < riderIds.length; i++)
        {
            if (!ridersById.findKey(riderIds[i]))
                return -1; // unknown rider

            IRider rider = ridersById.retrieve();

            if (rider.getSchedule().findKey(timeSlot))
                return -1; // rider is busy

            if (!riders.insert(rider.getId(), rider))
                return -1; // duplicate rider ID
        }

        // 2. nothing can fail now: create the ride and update every index and schedule
        SharedRide ride = new SharedRide(nextRideId, pickupLocation, dropoffLocation, timeSlot, driver);

        LinkedList<IRider> list = riders.inOrder(); // at least two riders, so never empty
        list.findFirst();

        while (true)
        {
            IRider rider = list.retrieve();

            ride.addParticipant(rider);
            rider.getSchedule().insert(timeSlot, ride);

            if (list.last())
                break;

            list.findNext();
        }

        indexRide(ride);
        driver.getSchedule().insert(timeSlot, ride);

        return nextRideId++;
    }

    /* Cancels a ride: deletes it from every index and schedule. False if the ID does not exist. */
    public boolean cancelRide(int rideId)
    {
        if (!ridesById.findKey(rideId))
            return false;

        deleteRide(ridesById.retrieve());

        return true;
    }

    /* Rides from the pickup location, sorted by ride ID (inner BST is keyed by ride ID). */
    public LinkedList<IRide> searchRidesByPickupLocation(String pickupLocation)
    {
        return IndexHelper.getAll(ridesByPickupLocation, pickupLocation);
    }

    /*
      Rides of every rider with this full name, found through the riders' schedules.
      A shared ride can appear in the schedule of several riders with the same name, so a
      temporary BST keyed by ride ID removes the duplicates and sorts the result.
     */
    public LinkedList<IRide> searchRidesByRiderName(String riderName)
    {
        IBST<Integer, IRide> found = new BST<Integer, IRide>();
        LinkedList<IRider> riders = searchRidersByName(riderName);

        if (!riders.empty())
        {
            riders.findFirst();

            while (true)
            {
                LinkedList<IRide> rides = riders.retrieve().getSchedule().inOrder();

                if (!rides.empty())
                {
                    rides.findFirst();

                    while (true)
                    {
                        IRide ride = rides.retrieve();
                        found.insert(ride.getRideId(), ride); // false (ignored) if already there

                        if (rides.last())
                            break;

                        rides.findNext();
                    }
                }

                if (riders.last())
                    break;

                riders.findNext();
            }
        }

        return found.inOrder();
    }

    /* Participants of a shared ride sorted by ID; empty if the ride does not exist or is private. */
    public LinkedList<IRider> getSharedRideParticipants(int rideId)
    {
        if (ridesById.findKey(rideId))
        {
            IRide ride = ridesById.retrieve();

            if (ride instanceof ISharedRide)
                return ((ISharedRide) ride).getParticipants();
        }

        return new LinkedList<IRider>();
    }

    /*
      All rides alphabetically by pickup location. The outer BST is ordered by location and
      each inner BST by ride ID, so an in-order walk gives ties sorted by ride ID. O(n), no sorting.
     */
    public LinkedList<IRide> getAllRidesAlphabetically()
    {
        LinkedList<IRide> result = new LinkedList<IRide>();
        LinkedList<IBST<Integer, IRide>> groups = ridesByPickupLocation.inOrder();

        if (!groups.empty())
        {
            groups.findFirst();

            while (true)
            {
                appendAll(result, groups.retrieve().inOrder());

                if (groups.last())
                    break;

                groups.findNext();
            }
        }

        return result;
    }

    /* Rides with from <= pickup date/time <= to, sorted by pickup date/time, then ride ID. */
    public LinkedList<IRide> searchRidesByPickupTimeRange(IDateTime from, IDateTime to)
    {
        LinkedList<IRide> result = new LinkedList<IRide>();

        if (from == null || to == null || from.compareTo(to) > 0)
            return result; // nothing can be in an empty or invalid range

        LinkedList<IBST<Integer, IRide>> groups = ridesByPickupTime.rangeSearch(from, to);

        if (!groups.empty())
        {
            groups.findFirst();

            while (true)
            {
                appendAll(result, groups.retrieve().inOrder());

                if (groups.last())
                    break;

                groups.findNext();
            }
        }

        return result;
    }



    // ============================================== PERSON C: drivers and statistics

    // Adds a driver to all three driver indexes.
    private void indexDriver(IDriver driver) {
        driversById.insert(driver.getId(), driver);

        // Vehicle plate is a regular BST index.
        driversByPlate.insert(driver.getVehiclePlate(), driver);

        // Vehicle type is a nested BST index.
        IndexHelper.addToIndex(
            driversByType,
            driver.getVehicleType(),
            driver.getId(),
            driver
        );
    }

    // Removes a driver from all three driver indexes.
    private void unindexDriver(IDriver driver) {
        driversById.removeKey(driver.getId());

        // Remove directly from the regular BST.
        driversByPlate.removeKey(driver.getVehiclePlate());

        // Remove from the nested BST.
        IndexHelper.removeFromIndex(
            driversByType,
            driver.getVehicleType(),
            driver.getId()
        );
    }

    // Loads drivers from a CSV file.
    // CSV format: driverId,name,phoneNumber,vehiclePlate,vehicleType
    public int loadDriversFromCSV(String driversFilePath) {
        String[][] rows;

        try {
            rows = CsvHelper.readRows(driversFilePath);
        } catch (IOException e) {
            return -1;
        }

        int loaded = 0;

        for (int i = 0; i < rows.length; i++) {
            String[] fields = rows[i];

            if (fields.length != 5)
                continue;

            try {
                int id = Integer.parseInt(fields[0]);
                String name = fields[1];
                String phone = fields[2];
                String plate = fields[3];

                VehicleType type = CsvHelper.parseVehicleType(fields[4]);

                IDriver driver = new Driver(
                    id, name, phone, plate, type
                );

                if (addDriver(driver))
                    loaded++;

            } catch (IllegalArgumentException e) {
                // Skip invalid records.
            }
        }

        return loaded;
    }

    // Adds a driver if their ID and vehicle plate are unique.
    public boolean addDriver(IDriver driver) {
        if (driver == null)
            return false;

        if (driversById.findKey(driver.getId()))
            return false;

        if (driversByPlate.findKey(driver.getVehiclePlate()))
            return false;

        indexDriver(driver);

        return true;
    }

    // Searches for a driver using their ID.
    public IDriver searchDriverById(int driverId) {
        if (driversById.findKey(driverId))
            return driversById.retrieve();

        return null;
    }

    // Returns all drivers with the requested vehicle type, sorted by ID.
    public LinkedList<IDriver> searchDriversByVehicleType(
            VehicleType vehicleType) {

        if (vehicleType == null)
            return new LinkedList<IDriver>();

        return IndexHelper.getAll(driversByType, vehicleType);
    }

    // Returns all drivers sorted by ID.
    public LinkedList<IDriver> getAllDrivers() {
        return driversById.inOrder();
    }

    // Removes a driver and all rides assigned to that driver.
    public boolean removeDriver(int driverId) {
        IDriver driver = searchDriverById(driverId);

        if (driver == null)
            return false;

        // Get a separate list before cancelling the rides.
        LinkedList<IRide> rides = driver.getSchedule().inOrder();

        if (!rides.empty()) {
            rides.findFirst();

            while (true) {
                IRide ride = rides.retrieve();

                cancelRide(ride.getRideId());

                if (rides.last())
                    break;

                rides.findNext();
            }
        }

        unindexDriver(driver);

        return true;
    }

    // Finds drivers of the requested type who have no overlapping ride.
    // Results are sorted by driver ID.
    public LinkedList<IDriver> findAvailableDrivers(
            ITimeSlot timeSlot, VehicleType vehicleType) {

        LinkedList<IDriver> available =
            new LinkedList<IDriver>();

        if (timeSlot == null || vehicleType == null)
            return available;

        LinkedList<IDriver> drivers =
            searchDriversByVehicleType(vehicleType);

        if (drivers.empty())
            return available;

        drivers.findFirst();

        while (true) {
            IDriver driver = drivers.retrieve();

            if (!driver.getSchedule().findKey(timeSlot))
                available.insert(driver);

            if (drivers.last())
                break;

            drivers.findNext();
        }

        return available;
    }

    // Returns the number of nodes visited when searching the rider ID index.
    public int nbKeyCompRiderById(int riderId) {
        return ridersById.nbKeyComp(riderId);
    }

    // Returns the number of keys in the requested index.
    public int indexSize(IndexName index) {
        if (index == null)
            return 0;

        switch (index) {
            case RIDERS_BY_ID:
                return ridersById.size();

            case RIDERS_BY_NAME:
                return ridersByName.size();

            case RIDERS_BY_HOME_CITY:
                return ridersByCity.size();

            case DRIVERS_BY_ID:
                return driversById.size();

            case DRIVERS_BY_PLATE:
                return driversByPlate.size();

            case DRIVERS_BY_VEHICLE_TYPE:
                return driversByType.size();

            case RIDES_BY_ID:
                return ridesById.size();

            case RIDES_BY_PICKUP_LOCATION:
                return ridesByPickupLocation.size();

            case RIDES_BY_PICKUP_TIME:
                return ridesByPickupTime.size();

            default:
                return 0;
        }
    }

    // Returns the height of the requested index.
    // For nested BST indexes, the outer BST height is returned.
    public int indexHeight(IndexName index) {
        if (index == null)
            return 0;

        switch (index) {
            case RIDERS_BY_ID:
                return ridersById.height();

            case RIDERS_BY_NAME:
                return ridersByName.height();

            case RIDERS_BY_HOME_CITY:
                return ridersByCity.height();

            case DRIVERS_BY_ID:
                return driversById.height();

            case DRIVERS_BY_PLATE:
                return driversByPlate.height();

            case DRIVERS_BY_VEHICLE_TYPE:
                return driversByType.height();

            case RIDES_BY_ID:
                return ridesById.height();

            case RIDES_BY_PICKUP_LOCATION:
                return ridesByPickupLocation.height();

            case RIDES_BY_PICKUP_TIME:
                return ridesByPickupTime.height();

            default:
                return 0;
        }
    }
}