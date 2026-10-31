import java.util.Scanner;

/**
 * Main class for the Ride-Sharing System.
 * This class is responsible for reading input and displaying output.
 */
public class Main {

    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {

        RideSharingSystem system = new RideSharingSystem();

        // Load the data before displaying the menu.
        boolean ridersLoaded = system.loadRidersFromCSV("riders_100.csv");
        boolean driversLoaded = system.loadDriversFromCSV("drivers_30.csv");
        boolean ridesLoaded = system.loadRidesFromCSV("rides_40.csv");

        System.out.println("Riders loaded: " + ridersLoaded);
        System.out.println("Drivers loaded: " + driversLoaded);
        System.out.println("Rides loaded: " + ridesLoaded);

        int choice;

        do {
            printMenu();
            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    printList(system.getAllRiders());
                    break;

                case 2:
                    System.out.print("Enter home city: ");
                    printList(system.searchRidersByHomeCity(input.nextLine()));
                    break;

                case 3:
                    System.out.print("Enter rider's full name: ");
                    printList(system.searchRidersByName(input.nextLine()));
                    break;

                case 4:
                    addRider(system);
                    break;

                case 5:
                    printList(system.getAllDrivers());
                    break;

                case 6:
                    VehicleType vehicleType = readVehicleType();
                    printList(system.searchDriversByVehicleType(vehicleType));
                    break;

                case 7:
                    addDriver(system);
                    break;

                case 8:
                    printList(system.getAllRidesAlphabetically());
                    break;

                case 9:
                    System.out.print("Enter pickup location: ");
                    printList(system.searchRidesByPickupLocation(input.nextLine()));
                    break;

                case 10:
                    System.out.print("Enter rider's full name: ");
                    printList(system.searchRidesByRiderName(input.nextLine()));
                    break;

                case 11:
                    System.out.print("Enter shared ride pickup location: ");
                    printList(system.getSharedRideParticipants(input.nextLine()));
                    break;

                case 12:
                    addRide(system);
                    break;

                case 0:
                    System.out.println("Exiting the program.");
                    break;

                default:
                    System.out.println("Invalid choice. Enter a number from 0 to 12.");
            }

            System.out.println();

        } while (choice != 0);
    }

    /**
     * Displays the required menu.
     */
    private static void printMenu() {
        System.out.println("1. List all riders.");
        System.out.println("2. Search for riders by home city.");
        System.out.println("3. Search for riders by name.");
        System.out.println("4. Add a rider.");
        System.out.println("5. List all drivers.");
        System.out.println("6. Search for drivers by vehicle type.");
        System.out.println("7. Add a driver.");
        System.out.println("8. List all rides alphabetically by pickup location.");
        System.out.println("9. Search for rides by pickup location.");
        System.out.println("10. Search for rides by rider name.");
        System.out.println("11. List the participants of a shared ride by pickup location.");
        System.out.println("12. Add a ride (private or shared).");
        System.out.println("0. Exit the program.");
    }

    /**
     * Prints the elements of the custom LinkedList using the List ADT methods
     */
    private static <T> void printList(LinkedList<T> list) {

        if (list.empty()) {
            System.out.println("No results found.");
            return;
        }

        list.findFirst();

        while (!list.last()) {
            System.out.println(list.retrieve());
            list.findNext();
        }

        System.out.println(list.retrieve());
    }

    /**
     * Reads an integer from the user.
     */
    private static int readInt(String message) {

        while (true) {
            System.out.print(message);
            String value = input.nextLine();

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    /**
     * Reads the vehicle type selected by the user.
     */
    private static VehicleType readVehicleType() {

        while (true) {
            System.out.print("Enter vehicle type (SEDAN, LUXURY_SEDAN, SUV, VAN): ");
            String value = input.nextLine().trim().toUpperCase().replace(' ', '_');

            try {
                return VehicleType.valueOf(value);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid vehicle type. Try again.");
            }
        }
    }

    /**
     * Reads the rider information and adds the rider to the system.
     */
    private static void addRider(RideSharingSystem system) {

        int id = readInt("Enter rider ID: ");

        System.out.print("Enter full name: ");
        String name = input.nextLine();

        System.out.print("Enter email: ");
        String email = input.nextLine();

        System.out.print("Enter phone number: ");
        String phoneNumber = input.nextLine();

        System.out.print("Enter home city: ");
        String homeCity = input.nextLine();

        try {
            Rider rider = new Rider(id, name, email, phoneNumber, homeCity);
            boolean added = system.addRider(rider);

            if (added)
                System.out.println("Rider added successfully.");
            else
                System.out.println("Rider could not be added.");

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid rider information: " + e.getMessage());
        }
    }

    /**
     * Reads the driver information and adds the driver to the system.
     */
    private static void addDriver(RideSharingSystem system) {

        int id = readInt("Enter driver ID: ");

        System.out.print("Enter full name: ");
        String name = input.nextLine();

        System.out.print("Enter phone number: ");
        String phoneNumber = input.nextLine();

        System.out.print("Enter vehicle plate: ");
        String vehiclePlate = input.nextLine();

        VehicleType vehicleType = readVehicleType();

        try {
            Driver driver = new Driver(id, name, phoneNumber, vehiclePlate, vehicleType);
            boolean added = system.addDriver(driver);

            if (added)
                System.out.println("Driver added successfully.");
            else
                System.out.println("Driver could not be added.");

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid driver information: " + e.getMessage());
        }
    }

    /**
     * Reads the ride information and schedules a private or shared ride.
     */
    private static void addRide(RideSharingSystem system) {

        String rideType;

        while (true) {
            System.out.print("Enter ride type (PRIVATE or SHARED): ");
            rideType = input.nextLine().trim().toUpperCase();

            if (rideType.equals("PRIVATE") || rideType.equals("SHARED"))
                break;

            System.out.println("Invalid ride type. Enter PRIVATE or SHARED.");
        }

        System.out.print("Enter pickup location: ");
        String pickupLocation = input.nextLine();

        IDateTime pickupTime = readDateTime("pickup");
        IDateTime dropoffTime = readDateTime("drop-off");

        System.out.print("Enter drop-off location: ");
        String dropoffLocation = input.nextLine();

        int driverId = readInt("Enter driver ID: ");

        if (rideType.equals("PRIVATE")) {

            int riderId = readInt("Enter rider ID: ");

            boolean scheduled = system.schedulePrivateRide(
                    pickupLocation, pickupTime, dropoffTime,
                    dropoffLocation, riderId, driverId);

            if (scheduled)
                System.out.println("Private ride scheduled successfully.");
            else
                System.out.println("Private ride could not be scheduled.");

        } else {

            int numberOfRiders;

            do {
                numberOfRiders = readInt("Enter number of riders (at least 2): ");

                if (numberOfRiders < 2)
                    System.out.println("A shared ride must have at least two riders.");

            } while (numberOfRiders < 2);

            int[] riderIds = new int[numberOfRiders];

            for (int i = 0; i < numberOfRiders; i++)
                riderIds[i] = readInt("Enter rider ID " + (i + 1) + ": ");

            boolean scheduled = system.scheduleSharedRide(
                    pickupLocation, pickupTime, dropoffTime,
                    dropoffLocation, riderIds, driverId);

            if (scheduled)
                System.out.println("Shared ride scheduled successfully.");
            else
                System.out.println("Shared ride could not be scheduled.");
        }
    }

    /**
     * Reads a date and time and creates a DateTime object.
     */
    private static IDateTime readDateTime(String label) {

        System.out.println("Enter " + label + " date and time:");

        int month = readInt("Month: ");
        int day = readInt("Day: ");
        int year = readInt("Year: ");
        int hour = readInt("Hour (0-23): ");
        int minute = readInt("Minute (0-59): ");

        return new DateTime(year, month, day, hour, minute);
    }
}
