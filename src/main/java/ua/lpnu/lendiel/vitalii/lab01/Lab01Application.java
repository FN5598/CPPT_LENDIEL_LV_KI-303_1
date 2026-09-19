package ua.lpnu.lendiel.vitalii.lab01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads and validates laboratory data about medicines and prints summary statistics.
 */
public class Lab01Application {

    private static final String DATA_CSV_PATH = "/lab01/Data.csv";
    private static final String ERROR_TXT_PATH = "./Errors.txt";

    /**
     * Creates an application instance.
     */
    public Lab01Application() {
    }

    /**
     * Loads the CSV data, validates each row, calculates summary statistics, and
     * prints the results to standard output. Validation errors are also written
     * to {@value #ERROR_TXT_PATH} when present.
     *
     * @param args command-line arguments; the application does not require any
     *             arguments
     */
    public static void main(String[] args) {
        List<String> errors = new ArrayList<>();
        double totalPrice = 0;
        int totalRows = 0;
        double averagePrice = 0;
        int shortestExpirationPeriod = Integer.MAX_VALUE;
        int prescriptionCount = 0;
        String[] data;
        try {
            data = getData();
        } catch (NoSuchFileException e) {
            fail("Data file not found: " + DATA_CSV_PATH, e);
            return;
        } catch (AccessDeniedException e) {
            fail("Cannot access data file: " + DATA_CSV_PATH, e);
            return;
        } catch (IOException e) {
            fail("Could not read data file: " + DATA_CSV_PATH, e);
            return;
        }

        for (int index = 0; index < data.length; index++) {
            String[] fields = data[index].split(";", -1);

            if (fields.length != 5) {
                errors.add("Row %d: expected at least 5 columns".formatted(index + 1));
            }

            if (Arrays.stream(fields).anyMatch(field -> field.isEmpty())) {
                errors.add("Row %d: has empty column".formatted(index + 1));
            }

            try {
                double price = Double.parseDouble(fields[2]);
                int expiration = Integer.parseInt(fields[3]);

                if (!fields[4].equalsIgnoreCase("false") && !fields[4].equalsIgnoreCase("true")) {
                    errors.add("Row %d: Invalid Boolean value".formatted(index + 1));
                } else if (fields[4].equalsIgnoreCase("true")) {
                    prescriptionCount++;
                }

                if (expiration < shortestExpirationPeriod) {
                    shortestExpirationPeriod = expiration;
                }

                totalRows++;
                totalPrice += price;
                averagePrice = totalRows == 0 ? 0 : totalPrice / totalRows;

            } catch (NumberFormatException e) {
                errors.add("Row %d: Invalid number format".formatted(index + 1));
            }
        }

        System.out.println("Average price: " + averagePrice);
        System.out.println("Prescription count: " + prescriptionCount);
        System.out.println("Shortest expiration period: " + shortestExpirationPeriod);
        System.out.println("Total Rows: " + totalRows);

        if (!errors.isEmpty()) {
            System.out.println("\n\n\nErrors:");
            try {
                Files.write(Path.of(ERROR_TXT_PATH), errors);
            } catch (NoSuchFileException e) {
                fail("Data file not found: " + DATA_CSV_PATH, e);
            } catch (AccessDeniedException e) {
                fail("Cannot access data file: " + DATA_CSV_PATH, e);
            } catch (IOException e) {
                fail("Could not read data file: " + DATA_CSV_PATH, e);
            }

            for (String error : errors) {
                System.out.println(error);
            }
        }
    }

    /**
     * Reads the laboratory CSV file from the application classpath as UTF-8
     * text.
     *
     * @return an array containing one CSV row per element
     * @throws IOException if the resource is missing or cannot be read
     */
    public static String[] getData() throws IOException {
        try (var input = Lab01Application.class.getResourceAsStream(DATA_CSV_PATH)) {
            if (input == null) {
                throw new NoSuchFileException(DATA_CSV_PATH);
            }

            return new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .toArray(String[]::new);
        }
    }

    /**
     * Reports an unrecoverable I/O error and terminates the application.
     *
     * @param message human-readable description of the failure
     * @param cause exception that caused the failure
     * @implNote This method writes the error to standard error and exits the
     *           process with status code {@code 1}; it does not return.
     */
    private static void fail(String message, Exception cause) {
        System.err.println("Fatal error: " + message);
        System.err.println(cause.getMessage());
        System.exit(1);
    }
}
