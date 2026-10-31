import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*
 * Coordinates RiderList, DriverList and RideList and enforces the system
  rules: unique IDs/plates/emails, existence checks before scheduling,
  conflict-free schedules, and cascade deletion of rides
  This class never prints; every search returns its result
 */
public class RideSharingSystem implements IRideSharingSystem {

    private final RiderList riders = new RiderList();
    private final DriverList drivers = new DriverList();
    private final RideList rides = new RideList();

    /* Number of CSV rows rejected (malformed, duplicate, missing rider/driver, or conflicting) in the last load */
    private int lastLoadSkipped = 0;

    /** Returns how many rows the most recent CSV load rejected */
    public int getLastLoadSkippedCount() {
        return lastLoadSkipped;
    }

    // ------------------------------------------------------------------
    // CSV loading
    // ------------------------------------------------------------------

    /*
      Loads riders (riderId,name,email,phoneNumber,homeCity). Returns true if
      the file could be read; invalid or duplicate rows are skipped and counted
     */
    @Override
    public boolean loadRidersFromCSV(String ridersFilePath) {
        lastLoadSkipped = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(ridersFilePath))) {
            String line;
            boolean first = true;
            while ((line = in.readLine()) != null) {
                line = cleanLine(line, first);
                first = false;
                if (line.isEmpty() || isHeader(line, "riderId")) {
                    continue;
                }
                try {
                    String[] f = line.split(",", -1);
                    if (f.length != 5) {
                        throw new IllegalArgumentException("Wrong number of fields");
                    }
                    Rider rider = new Rider(Integer.parseInt(f[0].trim()), f[1].trim(), f[2].trim(),
                            f[3].trim(), f[4].trim());
                    if (!addRider(rider)) {
                        lastLoadSkipped++;
                    }
                } catch (IllegalArgumentException e) {
                    lastLoadSkipped++;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /*
      Loads drivers (driverId,name,phoneNumber,vehiclePlate,vehicleType)
      Returns true if the file could be read; bad rows are skipped and counted
     */
    @Override
    public boolean loadDriversFromCSV(String driversFilePath) {
        lastLoadSkipped = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(driversFilePath))) {
            String line;
            boolean first = true;
            while ((line = in.readLine()) != null) {
                line = cleanLine(line, first);
                first = false;
                if (line.isEmpty() || isHeader(line, "driverId")) {
                    continue;
                }
                try {
                    String[] f = line.split(",", -1);
                    if (f.length != 5) {
                        throw new IllegalArgumentException("Wrong number of fields");
                    }
                    Driver driver = new Driver(Integer.parseInt(f[0].trim()), f[1].trim(), f[2].trim(),
                            f[3].trim(), parseVehicleType(f[4]));
                    if (!addDriver(driver)) {
                        lastLoadSkipped++;
                    }
                } catch (IllegalArgumentException e) {
                    lastLoadSkipped++;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /*
      Loads rides (rideType,pickupLocation,pickupDateTime,dropoffDateTime,
      dropoffLocation,driverId,riderIds). Riders and drivers must already be
      loaded; rows that reference missing people or conflict with an existing
      schedule are skipped and counted. Returns true if the file could be read.
     */
    @Override
    public boolean loadRidesFromCSV(String ridesFilePath) {
        lastLoadSkipped = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(ridesFilePath))) {
            String line;
            boolean first = true;
            while ((line = in.readLine()) != null) {
                line = cleanLine(line, first);
                first = false;
                if (line.isEmpty() || isHeader(line, "rideType")) {
                    continue;
                }
                try {
                    String[] f = line.split(",", -1);
                    if (f.length != 7) {
                        throw new IllegalArgumentException("Wrong number of fields");
                    }
                    String type = f[0].trim().toUpperCase();
                    IDateTime pickup = DateTime.parse(f[2]);
                    IDateTime dropoff = DateTime.parse(f[3]);
                    int driverId = Integer.parseInt(f[5].trim());
                    String[] idTexts = f[6].trim().split(";");
                    int[] riderIds = new int[idTexts.length];
                    for (int i = 0; i < idTexts.length; i++) {
                        riderIds[i] = Integer.parseInt(idTexts[i].trim());
                    }
                    boolean ok;
                    if (type.equals("PRIVATE")) {
                        if (riderIds.length != 1) {
                            throw new IllegalArgumentException("Private ride needs exactly one rider");
                        }
                        ok = schedulePrivateRide(f[1].trim(), pickup, dropoff, f[4].trim(), riderIds[0], driverId);
                    } else if (type.equals("SHARED")) {
                        ok = scheduleSharedRide(f[1].trim(), pickup, dropoff, f[4].trim(), riderIds, driverId);
                    } else {
                        throw new IllegalArgumentException("Unknown ride type");
                    }
                    if (!ok) {
                        lastLoadSkipped++;
                    }
                } catch (IllegalArgumentException e) {
                    lastLoadSkipped++;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* Trims a line and removes a UTF-8 byte-order mark from the first line */
    private static String cleanLine(String line, boolean first) {
        if (first && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
            line = line.substring(1);
        }
        return line.trim();
    }

    /* True if the line is a header row starting with the given column name */
    private static boolean isHeader(String line, String firstColumn) {
        return line.toLowerCase().startsWith(firstColumn.toLowerCase() + ",");
    }

    /* Converts text such as "LUXURY_SEDAN" or "luxury sedan" to a VehicleType */
    public static VehicleType parseVehicleType(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Vehicle type is null.");
        }
        return VehicleType.valueOf(text.trim().toUpperCase().replace(' ', '_'));
    }

    // ------------------------------------------------------------------
    // Adding riders and drivers
    // ------------------------------------------------------------------

    /* Adds a rider; false if null or if the ID or email is already used */
    @Override
    public boolean addRider(IRider rider) {
        return riders.add(rider);
    }

    /* Adds a driver; false if null or if the ID or vehicle plate is already used */
    @Override
    public boolean addDriver(IDriver driver) {
        return drivers.add(driver);
    }

    // ------------------------------------------------------------------
    // Searching riders and drivers
    // ------------------------------------------------------------------

    @Override
    public IRider searchRiderById(int riderId) {
        return riders.findById(riderId);
    }

    @Override
    public IRider searchRiderByEmail(String email) {
        return riders.findByEmail(email);
    }

    @Override
    public LinkedList<IRider> searchRidersByName(String fullName) {
        return riders.findByName(fullName);
    }

    @Override
    public LinkedList<IRider> searchRidersByHomeCity(String homeCity) {
        return riders.findByHomeCity(homeCity);
    }

    @Override
    public LinkedList<IRider> getAllRiders() {
        return riders.getAll();
    }

    @Override
    public IDriver searchDriverById(int driverId) {
        return drivers.findById(driverId);
    }

    @Override
    public IDriver searchDriverByVehiclePlate(String vehiclePlate) {
        return drivers.findByVehiclePlate(vehiclePlate);
    }

    @Override
    public LinkedList<IDriver> searchDriversByVehicleType(VehicleType vehicleType) {
        return drivers.findByVehicleType(vehicleType);
    }

    @Override
    public LinkedList<IDriver> getAllDrivers() {
        return drivers.getAll();
    }

    // ------------------------------------------------------------------
    // Removal with cascade
    // ------------------------------------------------------------------

    /*
      Removes a rider. Private rides of the rider are deleted; the rider is
      taken off shared rides, and shared rides left with no riders are deleted
     */
    @Override
    public boolean removeRider(int riderId) {
        IRider rider = riders.findById(riderId);
        if (rider == null) {
            return false;
        }
        // Work on a copy because deleting rides changes the rider's history
        LinkedList<IRide> history = copyOf(rider.getRideHistory());
        if (!history.empty()) {
            history.findFirst();
            while (true) {
                IRide ride = history.retrieve();
                if (ride instanceof ISharedRide) {
                    ISharedRide shared = (ISharedRide) ride;
                    shared.removeParticipantById(riderId);
                    removeItem(rider.getRideHistory(), ride);
                    if (shared.isEmpty()) {
                        detachRide(ride);
                    }
                } else {
                    detachRide(ride);
                }
                if (history.last()) {
                    break;
                }
                history.findNext();
            }
        }
        riders.removeById(riderId);
        return true;
    }

    /* Removes a driver and deletes every ride (private or shared) assigned to that driver */
    @Override
    public boolean removeDriver(int driverId) {
        IDriver driver = drivers.findById(driverId);
        if (driver == null) {
            return false;
        }
        LinkedList<IRide> history = copyOf(driver.getRideHistory());
        if (!history.empty()) {
            history.findFirst();
            while (true) {
                detachRide(history.retrieve());
                if (history.last()) {
                    break;
                }
                history.findNext();
            }
        }
        drivers.removeById(driverId);
        return true;
    }

    /* Removes a ride from the ride list and from the history of its driver and all its riders */
    private void detachRide(IRide ride) {
        rides.removeRideById(ride.getRideId());
        removeItem(ride.getDriver().getRideHistory(), ride);
        if (ride instanceof IPrivateRide) {
            removeItem(((IPrivateRide) ride).getRider().getRideHistory(), ride);
        } else if (ride instanceof ISharedRide) {
            LinkedList<IRider> participants = ((ISharedRide) ride).getParticipants();
            if (!participants.empty()) {
                participants.findFirst();
                while (true) {
                    removeItem(participants.retrieve().getRideHistory(), ride);
                    if (participants.last()) {
                        break;
                    }
                    participants.findNext();
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Small LinkedList helpers (the cursor-style LinkedList only offers
    // insert-after-current, remove-current and cursor movement)
    // ------------------------------------------------------------------

    /* Returns a copy of the list with its cursor on the first element */
    private static <T> LinkedList<T> copyOf(LinkedList<T> source) {
        LinkedList<T> copy = new LinkedList<>();
        if (!source.empty()) {
            source.findFirst();
            while (true) {
                copy.insert(source.retrieve()); // insert appends after the cursor, keeping order
                if (source.last()) {
                    break;
                }
                source.findNext();
            }
        }
        copy.findFirst();
        return copy;
    }

    /* Adds an item at the end of the list (moves the cursor to the last node first) */
    private static <T> void append(LinkedList<T> list, T item) {
        if (!list.empty()) {
            list.findFirst();
            while (!list.last()) {
                list.findNext();
            }
        }
        list.insert(item);
    }

    /* Removes the first element equal to the item, if present*/
    private static <T> void removeItem(LinkedList<T> list, T item) {
        if (list.empty()) {
            return;
        }
        list.findFirst();
        while (true) {
            if (list.retrieve().equals(item)) {
                list.remove();
                return;
            }
            if (list.last()) {
                return;
            }
            list.findNext();
        }
    }

    /* True if the list holds an element equal to the item*/
    private static <T> boolean containsItem(LinkedList<T> list, T item) {
        if (list.empty()) {
            return false;
        }
        list.findFirst();
        while (true) {
            if (list.retrieve().equals(item)) {
                return true;
            }
            if (list.last()) {
                return false;
            }
            list.findNext();
        }
    }

    // ------------------------------------------------------------------
    // Scheduling
    // ------------------------------------------------------------------

    /*
      Schedules a private ride. Returns false if the input is invalid, the
      rider or driver does not exist, or the ride overlaps the rider's or
      the driver's existing rides
     */
    @Override
    public boolean schedulePrivateRide(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime,
                                       String dropoffLocation, int riderId, int driverId) {
        if (!validRideInput(pickupLocation, pickupTime, dropoffTime, dropoffLocation)) {
            return false;
        }
        IRider rider = riders.findById(riderId);
        IDriver driver = drivers.findById(driverId);
        if (rider == null || driver == null) {
            return false;
        }
        if (hasConflict(rider, pickupTime, dropoffTime) || hasConflict(driver, pickupTime, dropoffTime)) {
            return false;
        }
        IRide ride = new PrivateRide(pickupLocation, pickupTime, dropoffTime, dropoffLocation, rider, driver);
        rides.addRide(ride);
        append(rider.getRideHistory(), ride);
        append(driver.getRideHistory(), ride);
        return true;
    }

    /*
      Schedules a shared ride. Returns false if the input is invalid, the
      rider list is empty or has a repeated ID, any rider or the driver does
      not exist, or the ride overlaps any rider's or the driver's rides
     */
    @Override
    public boolean scheduleSharedRide(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime,
                                      String dropoffLocation, int[] riderIds, int driverId) {
        if (!validRideInput(pickupLocation, pickupTime, dropoffTime, dropoffLocation)
                || riderIds == null || riderIds.length == 0) {
            return false;
        }
        IDriver driver = drivers.findById(driverId);
        if (driver == null || hasConflict(driver, pickupTime, dropoffTime)) {
            return false;
        }
        // Check every rider before changing anything, so a failure leaves no trace
        IRider[] found = new IRider[riderIds.length];
        for (int i = 0; i < riderIds.length; i++) {
            for (int j = 0; j < i; j++) {
                if (riderIds[j] == riderIds[i]) {
                    return false; // same rider listed twice
                }
            }
            found[i] = riders.findById(riderIds[i]);
            if (found[i] == null || hasConflict(found[i], pickupTime, dropoffTime)) {
                return false;
            }
        }
        SharedRide ride = new SharedRide(pickupLocation, pickupTime, dropoffTime, dropoffLocation, driver);
        for (int i = 0; i < found.length; i++) {
            ride.addParticipant(found[i]);
            append(found[i].getRideHistory(), ride);
        }
        rides.addRide(ride);
        append(driver.getRideHistory(), ride);
        return true;
    }

    /* Basic validation shared by both scheduling methods */
    private static boolean validRideInput(String pickup, IDateTime start, IDateTime end, String dropoff) {
        return pickup != null && !pickup.trim().isEmpty() && dropoff != null && !dropoff.trim().isEmpty()
                && start != null && end != null && start.compareTo(end) < 0;
    }

    /* True if the interval [start, end) overlaps any ride in the person's history */
    private static boolean hasConflict(IPerson person, IDateTime start, IDateTime end) {
        LinkedList<IRide> history = person.getRideHistory();
        if (history.empty()) {
            return false;
        }
        history.findFirst();
        while (true) {
            IRide r = history.retrieve();
            // [s1,e1) and [s2,e2) overlap exactly when s1 < e2 and s2 < e1
            if (start.compareTo(r.getDropoffTime()) < 0 && r.getPickupTime().compareTo(end) < 0) {
                return true;
            }
            if (history.last()) {
                return false;
            }
            history.findNext();
        }
    }

    // ------------------------------------------------------------------
    // Ride queries
    // ------------------------------------------------------------------

    @Override
    public LinkedList<IRide> searchRidesByPickupLocation(String pickupLocation) {
        return rides.findByPickupLocation(pickupLocation);
    }

    @Override
    public LinkedList<IRide> searchRidesByRiderName(String riderName) {
        return rides.findByRiderName(riderName);
    }

    /*
      Returns the riders on the shared ride(s) with this pickup location
      If several shared rides share the location, each rider is listed once
      The returned list's cursor is on the first element
     */
    @Override
    public LinkedList<IRider> getSharedRideParticipants(String pickupLocation) {
        LinkedList<IRider> result = new LinkedList<>();
        LinkedList<IRide> matches = rides.findByPickupLocation(pickupLocation);
        if (!matches.empty()) {
            matches.findFirst();
            while (true) {
                if (matches.retrieve() instanceof ISharedRide) {
                    LinkedList<IRider> participants = ((ISharedRide) matches.retrieve()).getParticipants();
                    if (!participants.empty()) {
                        participants.findFirst();
                        while (true) {
                            if (!containsItem(result, participants.retrieve())) {
                                append(result, participants.retrieve());
                            }
                            if (participants.last()) {
                                break;
                            }
                            participants.findNext();
                        }
                    }
                }
                if (matches.last()) {
                    break;
                }
                matches.findNext();
            }
        }
        result.findFirst();
        return result;
    }

    @Override
    public LinkedList<IRide> getAllRidesAlphabetically() {
        return rides.getAllAlphabetically();
    }
}
