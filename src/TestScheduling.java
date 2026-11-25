/*
  Person B's tests: ride classes, scheduling, conflicts, cancelRide, ride searches,
  the cascade of removeRider, and the ride loader.
  Run from the project root (so the data/ folder is found):
      javac -d out src/*.java
      java -cp out TestScheduling
  It needs the rider and driver methods of the system (addRider, addDriver, ...).
 */
public class TestScheduling {

    private static int passed = 0;
    private static int failed = 0;

    /* Records and prints the result of one check. */
    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("  FAILED: " + name);
        }
    }

    /* Builds a time slot on 2026-12-01 from hour:minute to hour:minute. */
    private static ITimeSlot slot(int h1, int m1, int h2, int m2) {
        return new TimeSlot(new DateTime(2026, 12, 1, h1, m1), new DateTime(2026, 12, 1, h2, m2));
    }

    /* Number of elements of a list. */
    private static <T> int count(LinkedList<T> list) {
        int n = 0;
        if (!list.empty()) {
            list.findFirst();
            while (true) {
                n++;
                if (list.last()) {
                    break;
                }
                list.findNext();
            }
        }
        return n;
    }

    /* Returns the i-th element (0-based) of a list; the list must be long enough. */
    private static <T> T at(LinkedList<T> list, int i) {
        list.findFirst();
        for (int k = 0; k < i; k++) {
            list.findNext();
        }
        return list.retrieve();
    }

    /* True if the list holds exactly the given ride IDs, in this order. */
    private static boolean rideIds(LinkedList<IRide> list, int... ids) {
        if (count(list) != ids.length) {
            return false;
        }
        for (int i = 0; i < ids.length; i++) {
            if (at(list, i).getRideId() != ids[i]) {
                return false;
            }
        }
        return true;
    }

    /* A system with riders 1..5 (riders 4 and 5 share the name "Sam Twin") and drivers 1..3. */
    private static RideSharingSystem newSystem() {
        RideSharingSystem s = new RideSharingSystem();
        s.addRider(new Rider(1, "Ali Ahmed", "ali@x.com", "0500000001", "Dammam"));
        s.addRider(new Rider(2, "Bader Saad", "bader@x.com", "0500000002", "Riyadh"));
        s.addRider(new Rider(3, "Cara Noor", "cara@x.com", "0500000003", "Dammam"));
        s.addRider(new Rider(4, "Sam Twin", "sam4@x.com", "0500000004", "Khobar"));
        s.addRider(new Rider(5, "Sam Twin", "sam5@x.com", "0500000005", "Khobar"));
        s.addDriver(new Driver(1, "Driver One", "0511111111", "AAA1111", VehicleType.SEDAN));
        s.addDriver(new Driver(2, "Driver Two", "0522222222", "BBB2222", VehicleType.SUV));
        s.addDriver(new Driver(3, "Driver Three", "0533333333", "CCC3333", VehicleType.VAN));
        return s;
    }

    // ------------------------------------------------------------ ride classes
    private static void testRideClasses() {
        System.out.println("Ride classes");
        IRider r1 = new Rider(1, "Ali Ahmed", "ali@x.com", "0500000001", "Dammam");
        IRider r2 = new Rider(2, "Bader Saad", "bader@x.com", "0500000002", "Riyadh");
        IDriver d = new Driver(1, "Driver One", "0511111111", "AAA1111", VehicleType.SEDAN);
        ITimeSlot t = slot(9, 0, 10, 0);

        PrivateRide p = new PrivateRide(7, "Dammam", "Khobar", t, r1, d);
        check("private getters", p.getRideId() == 7 && p.getRider() == r1 && p.getDriver() == d
                && p.getTimeSlot() == t && p.getPickupLocation().equals("Dammam")
                && p.getDropoffLocation().equals("Khobar"));
        check("private hasRider", p.hasRider(1) && !p.hasRider(2));
        check("toString has ride ID", p.toString().indexOf("7") >= 0);

        SharedRide sh = new SharedRide(8, "Dammam", "Khobar", t, d);
        check("shared starts empty", sh.isEmpty() && count(sh.getParticipants()) == 0);
        check("addParticipant", sh.addParticipant(r2) && sh.addParticipant(r1));
        check("addParticipant duplicate", !sh.addParticipant(r1));
        check("addParticipant null", !sh.addParticipant(null));
        check("participants sorted by ID", at(sh.getParticipants(), 0).getId() == 1
                && at(sh.getParticipants(), 1).getId() == 2);
        check("shared hasRider", sh.hasRider(1) && sh.hasRider(2) && !sh.hasRider(3));
        check("removeParticipantById", sh.removeParticipantById(1) && !sh.hasRider(1));
        check("removeParticipantById absent", !sh.removeParticipantById(1));
        sh.removeParticipantById(2);
        check("shared empty again", sh.isEmpty());

        check("compareTo by ride ID", p.compareTo(sh) < 0 && sh.compareTo(p) > 0 && p.compareTo(p) == 0);

        boolean threw = false;
        try {
            new PrivateRide(1, "", "Khobar", t, r1, d);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("empty pickup rejected", threw);
        threw = false;
        try {
            new PrivateRide(1, "A", "B", t, null, d);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("null rider rejected", threw);
    }

    // -------------------------------------------------- scheduling and conflicts
    private static void testScheduling() {
        System.out.println("Scheduling");
        RideSharingSystem s = newSystem();

        check("first ride ID is 1", s.schedulePrivateRide("Dammam", "Khobar", slot(9, 0, 10, 0), 1, 1) == 1);
        check("second ride ID is 2", s.schedulePrivateRide("Dammam", "Khobar", slot(9, 0, 10, 0), 2, 2) == 2);

        // conflicts
        check("same slot, same rider", s.schedulePrivateRide("A", "B", slot(9, 0, 10, 0), 1, 2) == -1);
        check("same slot, same driver", s.schedulePrivateRide("A", "B", slot(9, 0, 10, 0), 3, 1) == -1);
        check("partial overlap at the start", s.schedulePrivateRide("A", "B", slot(8, 30, 9, 30), 3, 1) == -1);
        check("partial overlap at the end", s.schedulePrivateRide("A", "B", slot(9, 30, 10, 30), 3, 1) == -1);
        check("slot inside a ride", s.schedulePrivateRide("A", "B", slot(9, 15, 9, 45), 3, 1) == -1);
        check("slot around a ride", s.schedulePrivateRide("A", "B", slot(8, 0, 11, 0), 3, 1) == -1);

        // back to back is NOT a conflict
        int after = s.schedulePrivateRide("A", "B", slot(10, 0, 11, 0), 1, 1);
        int before = s.schedulePrivateRide("A", "B", slot(8, 0, 9, 0), 1, 1);
        check("starts exactly when another ends", after == 3);
        check("ends exactly when another starts", before == 4);

        // invalid input, and the ID is not consumed by a failure
        check("unknown rider", s.schedulePrivateRide("A", "B", slot(12, 0, 13, 0), 99, 1) == -1);
        check("unknown driver", s.schedulePrivateRide("A", "B", slot(12, 0, 13, 0), 1, 99) == -1);
        check("null slot", s.schedulePrivateRide("A", "B", null, 1, 1) == -1);
        check("empty pickup", s.schedulePrivateRide("  ", "B", slot(12, 0, 13, 0), 1, 1) == -1);
        check("null dropoff", s.schedulePrivateRide("A", null, slot(12, 0, 13, 0), 1, 1) == -1);
        check("ID sequence has no gap after failures", s.schedulePrivateRide("A", "B", slot(12, 0, 13, 0), 1, 1) == 5);

        // schedules were updated
        check("rider schedule holds its rides", s.searchRiderById(1).getSchedule().size() == 4);
        check("driver schedule holds its rides", s.searchDriverById(1).getSchedule().size() == 4);
        check("schedule is in time order", at(s.searchRiderById(1).getSchedule().inOrder(), 0).getRideId() == 4);
    }

    private static void testSharedRides() {
        System.out.println("Shared rides");
        RideSharingSystem s = newSystem();
        s.schedulePrivateRide("A", "B", slot(9, 0, 10, 0), 2, 2); // ride 1: rider 2 busy, driver 2 busy

        int[] ok = {3, 1};
        int id = s.scheduleSharedRide("Dammam", "Khobar", slot(9, 0, 10, 0), ok, 1);
        check("shared ride scheduled", id == 2);
        LinkedList<IRider> parts = s.getSharedRideParticipants(2);
        check("participants sorted by ID", count(parts) == 2 && at(parts, 0).getId() == 1 && at(parts, 1).getId() == 3);
        check("shared ride in every schedule", s.searchRiderById(1).getSchedule().size() == 1
                && s.searchRiderById(3).getSchedule().size() == 1 && s.searchDriverById(1).getSchedule().size() == 1);
        check("participants of a private ride", count(s.getSharedRideParticipants(1)) == 0);
        check("participants of an unknown ride", count(s.getSharedRideParticipants(99)) == 0);

        // each failure must change nothing, even when the bad rider is the last in the array
        int before = count(s.getAllRidesAlphabetically());
        int[] one = {4};
        int[] dup = {4, 5, 4};
        int[] unknown = {4, 99};
        int[] busyRider = {4, 2};   // rider 2 is busy at 9:00
        int[] busyLast = {4, 5, 1}; // rider 1 is busy (shared ride 2)
        check("one rider only", s.scheduleSharedRide("A", "B", slot(9, 0, 10, 0), one, 2) == -1);
        check("null riders", s.scheduleSharedRide("A", "B", slot(9, 0, 10, 0), null, 2) == -1);
        check("empty rider array", s.scheduleSharedRide("A", "B", slot(9, 0, 10, 0), new int[0], 2) == -1);
        check("duplicate rider ID", s.scheduleSharedRide("A", "B", slot(13, 0, 14, 0), dup, 2) == -1);
        check("unknown rider", s.scheduleSharedRide("A", "B", slot(13, 0, 14, 0), unknown, 2) == -1);
        check("unknown driver", s.scheduleSharedRide("A", "B", slot(13, 0, 14, 0), new int[] {4, 5}, 99) == -1);
        check("busy rider", s.scheduleSharedRide("A", "B", slot(9, 30, 9, 45), busyRider, 3) == -1);
        check("busy rider at the end of the array", s.scheduleSharedRide("A", "B", slot(9, 30, 9, 45), busyLast, 3) == -1);
        check("busy driver", s.scheduleSharedRide("A", "B", slot(9, 30, 9, 45), new int[] {4, 5}, 1) == -1);
        check("failed scheduling changed no ride count", count(s.getAllRidesAlphabetically()) == before);
        check("failed scheduling left rider 4 empty", s.searchRiderById(4).getSchedule().empty());
        check("failed scheduling left rider 5 empty", s.searchRiderById(5).getSchedule().empty());
        check("next ID still 3", s.scheduleSharedRide("A", "B", slot(13, 0, 14, 0), new int[] {4, 5}, 2) == 3);

        // back to back with a shared ride
        check("back to back shared", s.scheduleSharedRide("A", "B", slot(10, 0, 11, 0), ok, 1) == 4);
    }

    // ------------------------------------------------------------- cancelRide
    private static void testCancel() {
        System.out.println("cancelRide");
        RideSharingSystem s = newSystem();
        s.schedulePrivateRide("Dammam", "Khobar", slot(9, 0, 10, 0), 1, 1);                   // 1
        s.scheduleSharedRide("Dammam", "Khobar", slot(11, 0, 12, 0), new int[] {2, 3}, 2);    // 2

        check("cancel unknown ID", !s.cancelRide(99));
        check("cancel private", s.cancelRide(1));
        check("cancel twice", !s.cancelRide(1));
        check("private ride gone from rider schedule", s.searchRiderById(1).getSchedule().empty());
        check("private ride gone from driver schedule", s.searchDriverById(1).getSchedule().empty());
        check("private ride gone from location index", count(s.searchRidesByPickupLocation("Dammam")) == 1);
        check("slot is free again", s.schedulePrivateRide("A", "B", slot(9, 0, 10, 0), 1, 1) == 3);

        check("cancel shared", s.cancelRide(2));
        check("shared ride gone from every rider", s.searchRiderById(2).getSchedule().empty()
                && s.searchRiderById(3).getSchedule().empty() && s.searchDriverById(2).getSchedule().empty());
        check("shared ride gone from ID index", count(s.getSharedRideParticipants(2)) == 0);
    }

    // ---------------------------------------------------------------- searches
    private static void testSearches() {
        System.out.println("Searches");
        RideSharingSystem s = newSystem();
        s.schedulePrivateRide("Khobar", "X", slot(15, 0, 16, 0), 1, 1);                        // 1
        s.scheduleSharedRide("Dammam", "X", slot(9, 0, 10, 0), new int[] {4, 5}, 2);          // 2 (two "Sam Twin")
        s.schedulePrivateRide("Dammam", "X", slot(11, 0, 12, 0), 4, 1);                       // 3
        s.schedulePrivateRide("Abha", "X", slot(8, 0, 8, 30), 2, 2);                          // 4
        s.schedulePrivateRide("Dammam", "X", slot(9, 0, 10, 0), 3, 1);                        // 5

        check("by pickup location, sorted by ID", rideIds(s.searchRidesByPickupLocation("Dammam"), 2, 3, 5));
        check("by unknown location", s.searchRidesByPickupLocation("Nowhere").empty());

        check("by rider name, each ride once", rideIds(s.searchRidesByRiderName("Sam Twin"), 2, 3));
        check("by rider name, one rider", rideIds(s.searchRidesByRiderName("Ali Ahmed"), 1));
        check("by unknown name", s.searchRidesByRiderName("Nobody").empty());

        check("alphabetical, ties by ride ID", rideIds(s.getAllRidesAlphabetically(), 4, 2, 3, 5, 1));

        IDateTime t9 = new DateTime(2026, 12, 1, 9, 0);
        IDateTime t11 = new DateTime(2026, 12, 1, 11, 0);
        check("range is inclusive at both ends", rideIds(s.searchRidesByPickupTimeRange(t9, t11), 2, 5, 3));
        check("range with one point", rideIds(s.searchRidesByPickupTimeRange(t9, t9), 2, 5));
        check("range with no rides", s.searchRidesByPickupTimeRange(new DateTime(2026, 12, 2, 0, 0),
                new DateTime(2026, 12, 3, 0, 0)).empty());
        check("range with from after to", s.searchRidesByPickupTimeRange(t11, t9).empty());
        check("range finds the earliest ride", rideIds(s.searchRidesByPickupTimeRange(
                new DateTime(2026, 12, 1, 0, 0), new DateTime(2026, 12, 1, 8, 0)), 4));

        RideSharingSystem empty = new RideSharingSystem();
        check("empty system: all rides", empty.getAllRidesAlphabetically().empty());
        check("empty system: range", empty.searchRidesByPickupTimeRange(t9, t11).empty());
        check("empty system: by location", empty.searchRidesByPickupLocation("Dammam").empty());
        check("empty system: by name", empty.searchRidesByRiderName("Sam Twin").empty());
    }

    // -------------------------------------------------------- removeRider cascade
    private static void testCascade() {
        System.out.println("removeRider cascade");
        RideSharingSystem s = newSystem();
        s.schedulePrivateRide("P", "X", slot(9, 0, 10, 0), 1, 1);                              // 1 private of rider 1
        s.scheduleSharedRide("S", "X", slot(11, 0, 12, 0), new int[] {1, 2, 3}, 2);           // 2 shared 1,2,3
        s.scheduleSharedRide("T", "X", slot(13, 0, 14, 0), new int[] {4, 5}, 1);              // 3 shared 4,5

        check("remove unknown rider", !s.removeRider(99));
        check("remove rider 1", s.removeRider(1));
        check("rider is gone", s.searchRiderById(1) == null);
        check("private ride deleted", s.searchRidesByPickupLocation("P").empty()
                && s.searchDriverById(1).getSchedule().size() == 1);
        check("shared ride keeps the others", count(s.getSharedRideParticipants(2)) == 2
                && s.searchRidesByPickupLocation("S").empty() == false);
        check("driver schedule of shared ride kept", s.searchDriverById(2).getSchedule().size() == 1);

        s.removeRider(2);
        s.removeRider(3);
        check("empty shared ride deleted", s.searchRidesByPickupLocation("S").empty()
                && s.searchDriverById(2).getSchedule().empty());
        check("unrelated ride untouched", rideIds(s.getAllRidesAlphabetically(), 3));
    }

    // ----------------------------------------------------------------- CSV loader
    private static void testLoader() {
        System.out.println("loadRidesFromCSV");
        RideSharingSystem s = new RideSharingSystem();
        check("missing file returns -1", s.loadRidesFromCSV("data/does_not_exist.csv") == -1);

        int riders = s.loadRidersFromCSV("data/riders_100.csv");
        int drivers = s.loadDriversFromCSV("data/drivers_30.csv");
        int rides = s.loadRidesFromCSV("data/rides_40.csv");
        System.out.println("  loaded riders=" + riders + " drivers=" + drivers + " rides=" + rides);
        check("100 riders loaded", riders == 100);
        check("30 drivers loaded", drivers == 30);
        check("all 40 rides loaded", rides == 40);
        check("40 rides in the ID order", count(s.getAllRidesAlphabetically()) == 40);
        check("first line got ride ID 1", s.searchRidesByPickupLocation("RiyadhSouth").empty() == false
                && at(s.searchRidesByPickupLocation("RiyadhSouth"), 0).getRideId() == 1);
    }

    public static void main(String[] args) {
        testRideClasses();
        testScheduling();
        testSharedRides();
        testCancel();
        testSearches();
        testCascade();
        testLoader();
        System.out.println();
        System.out.println("PASSED: " + passed);
        System.out.println("FAILED: " + failed);
    }
}
