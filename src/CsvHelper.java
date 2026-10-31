import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Helper methods used when reading the project CSV files.
 * This class uses arrays and does not use Java collection classes.
 */
public class CsvHelper {

    /**
     * Reads all non-empty lines from a file and returns the parsed rows.
     * The file is read twice so the result array can be created at its final size.
     */
    public static String[][] readRows(String fileName) throws IOException {

        int numberOfRows = 0;

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty())
                numberOfRows++;
        }

        reader.close();

        String[][] rows = new String[numberOfRows][];
        reader = new BufferedReader(new FileReader(fileName));

        int index = 0;

        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                rows[index] = parseLine(line);
                index++;
            }
        }

        reader.close();

        return rows;
    }

    /**
     * Splits one CSV line into fields.
     * Commas inside quotation marks are treated as part of the field.
     */
    public static String[] parseLine(String line) {

        String[] fields = new String[0];
        String field = "";
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char current = line.charAt(i);

            if (current == '"') {
                insideQuotes = !insideQuotes;
            } else if (current == ',' && !insideQuotes) {
                fields = addField(fields, field.trim());
                field = "";
            } else {
                field = field + current;
            }
        }

        fields = addField(fields, field.trim());

        return fields;
    }

    /**
     * Adds one field to the end of a String array.
     */
    private static String[] addField(String[] fields, String value) {

        String[] newFields = new String[fields.length + 1];

        for (int i = 0; i < fields.length; i++)
            newFields[i] = fields[i];

        newFields[fields.length] = value;

        return newFields;
    }

    /**
     * Converts a date/time string in MM/DD/YYYY HH:MM format to DateTime.
     */
    public static IDateTime parseDateTime(String value) {

        String[] parts = value.trim().split(" ");
        String[] date = parts[0].split("/");
        String[] time = parts[1].split(":");

        int month = Integer.parseInt(date[0]);
        int day = Integer.parseInt(date[1]);
        int year = Integer.parseInt(date[2]);
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        return new DateTime(year, month, day, hour, minute);
    }

    /**
     * Converts a semicolon-separated list of rider IDs to an int array.
     */
    public static int[] parseRiderIds(String value) {

        String[] values = value.split(";");
        int[] riderIds = new int[values.length];

        for (int i = 0; i < values.length; i++)
            riderIds[i] = Integer.parseInt(values[i].trim());

        return riderIds;
    }

    /**
     * Converts a vehicle type string to the VehicleType enum.
     */
    public static VehicleType parseVehicleType(String value) {

        String type = value.trim().toUpperCase().replace(' ', '_');

        return VehicleType.valueOf(type);
    }
}
