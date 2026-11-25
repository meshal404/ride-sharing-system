import java.util.Scanner;

/** Command-line interface for the Ride-Sharing System. */
public class Main {
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        RideSharingSystem system = new RideSharingSystem();

        // Load the CSV files in the order required by the assignment.
        System.out.println("Riders loaded: " + system.loadRidersFromCSV("riders_100.csv"));
        System.out.println("Drivers loaded: " + system.loadDriversFromCSV("drivers_30.csv"));
        System.out.println("Rides loaded: " + system.loadRidesFromCSV("rides_40.csv"));

        int choice;
        do {
            printMenu();
            choice = readInt("Choice: ");

            switch (choice) {
                case 1:
                    printList(system.getAllRiders());
                    break;
                case 2: {
                    int id = readInt("Rider ID: ");
                    IRider rider = system.searchRiderById(id);
                    if (rider == null) {
                        System.out.println("Rider not found.");
                    } else {
                        System.out.println(rider);
                        System.out.println("Key comparisons: " + system.nbKeyCompRiderById(id));
                    }
                    break;
                }
                case 3:
                    printList(system.searchRidersByHomeCity(readText("Home city: ")));
                    break;
                case 4:
                    printList(system.getAllRidesAlphabetically());
                    break;
                case 5:
                    showSchedule(system);
                    break;
                case 6:
                    showIndexStatistics(system);
                    break;
                case 7:
                    addRider(system);
                    break;
                case 8: {
                    DateTime from = readDateTime("From");
                    DateTime to = readDateTime("To");
                    printList(system.searchRidesByPickupTimeRange(from, to));
                    break;
                }
                case 9:
                    findAvailableDrivers(system);
                    break;
                case 10:
                    addRide(system);
                    break;
                case 11:
                    System.out.println(system.cancelRide(readInt("Ride ID to cancel: "))
                            ? "Ride cancelled." : "Ride not found.");
                    break;
                case 12:
                    System.out.println(system.removeRider(readInt("Rider ID to remove: "))
                            ? "Rider removed." : "Rider not found.");
                    break;
                case 0:
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Choose a number from 0 to 12.");
            }
            System.out.println();
        } while (choice != 0);
    }

    private static void printMenu() {
        System.out.println("1. List all riders");
        System.out.println("2. Search for a rider by ID and show key comparisons");
        System.out.println("3. Search for riders by home city");
        System.out.println("4. List all rides alphabetically by pickup location");
        System.out.println("5. Show a rider's or driver's schedule");
        System.out.println("6. Show index size, height, and minimum possible height");
        System.out.println("7. Add a rider");
        System.out.println("8. List rides in a pickup-time range");
        System.out.println("9. Find available drivers");
        System.out.println("10. Add a private or shared ride");
        System.out.println("11. Cancel a ride");
        System.out.println("12. Remove a rider");
        System.out.println("0. Exit");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    private static DateTime readDateTime(String prompt) {
        while (true) {
            try {
                return DateTime.parse(readText(prompt + " (MM/DD/YYYY HH:MM): "));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid date/time. Try again.");
            }
        }
    }

    private static VehicleType readVehicleType() {
        while (true) {
            String value = readText("Vehicle type (SEDAN, LUXURY_SEDAN, SUV, VAN): ")
                    .toUpperCase().replace(' ', '_');
            try {
                return VehicleType.valueOf(value);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid vehicle type.");
            }
        }
    }

    private static <T> void printList(LinkedList<T> list) {
        if (list == null || list.empty()) {
            System.out.println("No results.");
            return;
        }
        list.findFirst();
        while (true) {
            System.out.println(list.retrieve());
            if (list.last()) {
                break;
            }
            list.findNext();
        }
    }

    private static void addRider(RideSharingSystem system) {
        try {
            Rider rider = new Rider(
                    readInt("Rider ID: "),
                    readText("Full name: "),
                    readText("Email: "),
                    readText("Phone number: "),
                    readText("Home city: "));
            System.out.println(system.addRider(rider)
                    ? "Rider added." : "Rider could not be added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid rider: " + e.getMessage());
        }
    }

    private static void showSchedule(RideSharingSystem system) {
        String kind = readText("Rider or driver? (R/D): ");
        int id = readInt("ID: ");
        IPerson person;

        if (kind.equalsIgnoreCase("R")) {
            person = system.searchRiderById(id);
        } else if (kind.equalsIgnoreCase("D")) {
            person = system.searchDriverById(id);
        } else {
            System.out.println("Choose R or D.");
            return;
        }

        if (person == null) {
            System.out.println("Person not found.");
        } else {
            printList(person.getSchedule().inOrder());
        }
    }

    private static void showIndexStatistics(RideSharingSystem system) {
        for (IndexName index : IndexName.values()) {
            int size = system.indexSize(index);
            int minimumHeight = size == 0
                    ? 0 : (int) Math.ceil(Math.log(size + 1) / Math.log(2));
            System.out.println(index + ": size=" + size
                    + ", height=" + system.indexHeight(index)
                    + ", minimum height=" + minimumHeight);
        }
    }

    private static void findAvailableDrivers(RideSharingSystem system) {
        VehicleType type = readVehicleType();
        DateTime start = readDateTime("Start");
        DateTime end = readDateTime("End");
        try {
            TimeSlot slot = new TimeSlot(start, end);
            printList(system.findAvailableDrivers(slot, type));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid time slot: " + e.getMessage());
        }
    }

    private static void addRide(RideSharingSystem system) {
        String kind = readText("Ride type (PRIVATE/SHARED): ").toUpperCase();
        String pickup = readText("Pickup location: ");
        String dropoff = readText("Drop-off location: ");
        DateTime start = readDateTime("Pickup time");
        DateTime end = readDateTime("Drop-off time");
        int driverId = readInt("Driver ID: ");

        try {
            TimeSlot slot = new TimeSlot(start, end);
            int rideId;
            if (kind.equals("PRIVATE")) {
                int riderId = readInt("Rider ID: ");
                rideId = system.schedulePrivateRide(pickup, dropoff, slot, riderId, driverId);
            } else if (kind.equals("SHARED")) {
                int count = readInt("Number of riders (at least 2): ");
                if (count < 2) {
                    System.out.println("A shared ride needs at least two riders.");
                    return;
                }
                int[] riderIds = new int[count];
                for (int i = 0; i < count; i++) {
                    riderIds[i] = readInt("Rider ID " + (i + 1) + ": ");
                }
                rideId = system.scheduleSharedRide(pickup, dropoff, slot, riderIds, driverId);
            } else {
                System.out.println("Invalid ride type.");
                return;
            }
            System.out.println(rideId == -1
                    ? "Ride could not be scheduled."
                    : "Ride scheduled. Ride ID: " + rideId);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ride: " + e.getMessage());
        }
    }
}

