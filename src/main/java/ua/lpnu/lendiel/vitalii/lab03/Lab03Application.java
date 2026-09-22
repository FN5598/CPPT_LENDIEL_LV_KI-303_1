package ua.lpnu.lendiel.vitalii.lab03;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.lpnu.lendiel.vitalii.VersionInfo;
import ua.lpnu.lendiel.vitalii.lab01.Lab01Application;

/**
 * Reads medicine rows into a polymorphic model and prints a summary report.
 */
public final class Lab03Application {
    private static final String DATA_CSV_PATH = "/lab01/Data.csv";

    private Lab03Application() {
    }

    /**
     * Loads the medicine resource, validates every row, and prints the summary.
     *
     * @param args command-line arguments; no arguments are required
     */
    public static void main(String[] args) {
        if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println(VersionInfo.labVersion("lab03"));
            return;
        }
        String[] lines;
        try {
            lines = getData(DATA_CSV_PATH);
        } catch (IOException exception) {
            System.err.println("Could not read data file: " + DATA_CSV_PATH);
            System.err.println(exception.getMessage());
            return;
        }

        List<Medicine> medicines = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        double totalPrice = 0;
        int shortestExpirationPeriod = Integer.MAX_VALUE;
        int prescriptionCount = 0;

        for (int index = 0; index < lines.length; index++) {
            try {
                Medicine medicine = MedicineFactory.fromCsv(lines[index]);
                medicines.add(medicine);
                totalPrice += medicine.getPrice();
                shortestExpirationPeriod = Math.min(shortestExpirationPeriod,
                        medicine.getExpirationDays());
                if (medicine.requiresPrescription()) {
                    prescriptionCount++;
                }
            } catch (IllegalArgumentException exception) {
                errors.add("Row %d: %s".formatted(index + 1, exception.getMessage()));
            }
        }

        double averagePrice = medicines.isEmpty() ? 0 : totalPrice / medicines.size();
        printSummary(averagePrice, shortestExpirationPeriod, prescriptionCount,
                medicines.size(), errors);
    }

    /**
     * Reads a UTF-8 CSV resource from the classpath.
     *
     * @param path absolute classpath resource path
     * @return one CSV row per array element
     * @throws IOException if the resource is missing or cannot be read
     */
    public static String[] getData(String path) throws IOException {
        try (var input = Lab01Application.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new NoSuchFileException(path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .toArray(String[]::new);
        }
    }

    private static void printSummary(double averagePrice, int shortestExpirationPeriod,
            int prescriptionCount, int totalRows, List<String> errors) {
        System.out.printf(Locale.ROOT, "Average medicine price: %.2f%n", averagePrice);
        System.out.printf(Locale.ROOT,
                "Shortest medicine expiration period: %d%n", shortestExpirationPeriod);
        System.out.printf(Locale.ROOT,
                "Total medicines that had prescription: %d%n", prescriptionCount);
        System.out.printf(Locale.ROOT, "Total correct rows: %d%n", totalRows);
        System.out.printf(Locale.ROOT, "%n%n%nErrors: %d%n", errors.size());
        errors.forEach(System.out::println);
    }
}
