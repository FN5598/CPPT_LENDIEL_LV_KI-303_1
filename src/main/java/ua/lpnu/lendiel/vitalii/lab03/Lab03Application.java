package ua.lpnu.lendiel.vitalii.lab03;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.lpnu.lendiel.vitalii.lab01.Lab01Application;
import ua.lpnu.lendiel.vitalii.LabCli;
import ua.lpnu.lendiel.vitalii.VersionInfo;

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
        try {
            LabCli options = LabCli.parse(args);
            if (!options.positional().isEmpty()) {
                throw new IllegalArgumentException("unexpected positional argument");
            }
            if (options.help()) {
                System.out.println("Usage: lab03 [--input PATH] [--output PATH] [--help] [--version]");
                return;
            }
            if (options.version()) {
                System.out.println(VersionInfo.labVersion("lab03"));
                return;
            }
            options.writeReport(buildReport(options.readLines(
                    Lab03Application.class, DATA_CSV_PATH)));
        } catch (IllegalArgumentException exception) {
            System.err.println("Argument error: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Could not process input or output file: "
                    + exception.getMessage());
        }
    }

    private static String buildReport(String[] lines) {
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
                        medicine.getDaysToExpire());
                if (medicine.requiresPrescription()) {
                    prescriptionCount++;
                }
            } catch (IllegalArgumentException exception) {
                errors.add("Row %d: %s".formatted(index + 1, exception.getMessage()));
            }
        }

        double averagePrice = medicines.isEmpty() ? 0 : totalPrice / medicines.size();
        return formatSummary(averagePrice, shortestExpirationPeriod,
                prescriptionCount, medicines.size(), errors);
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

    private static String formatSummary(double averagePrice, int shortestExpirationPeriod,
            int prescriptionCount, int totalRows, List<String> errors) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "Average medicine price: %.2f%n",
                averagePrice));
        report.append("Shortest medicine expiration period: ")
                .append(shortestExpirationPeriod).append(System.lineSeparator());
        report.append("Total medicines that had prescription: ")
                .append(prescriptionCount).append(System.lineSeparator());
        report.append("Total correct rows: ").append(totalRows)
                .append(System.lineSeparator());
        report.append(System.lineSeparator().repeat(3))
                .append("Errors: ").append(errors.size()).append(System.lineSeparator());
        errors.forEach(error -> report.append(error).append(System.lineSeparator()));
        return report.toString();
    }
}
