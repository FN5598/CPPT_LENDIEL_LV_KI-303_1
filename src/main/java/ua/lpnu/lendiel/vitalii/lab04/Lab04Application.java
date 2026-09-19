package ua.lpnu.lendiel.vitalii.lab04;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ua.lpnu.lendiel.vitalii.lab01.Lab01Application;
import ua.lpnu.lendiel.vitalii.lab03.Medicine;
import ua.lpnu.lendiel.vitalii.lab03.MedicineFactory;
import ua.lpnu.lendiel.vitalii.lab03.MedicineForm;

/**
 * Reads medicine rows into a polymorphic model and prints a summary report.
 */
public final class Lab04Application {
    private static final String DATA_CSV_PATH = "/lab01/Data.csv";

    private Lab04Application() {
    }

    /**
     * Loads the medicine resource, processes every valid row through the common
     * {@link Medicine} type, and prints the existing summary format.
     *
     * @param args command-line arguments; no arguments are required
     */
    public static void main(String[] args) {
        String[] lines;
        try {
            lines = getData(DATA_CSV_PATH);
        } catch (IOException exception) {
            System.err.println("Could not read data file: " + DATA_CSV_PATH);
            System.err.println(exception.getMessage());
            return;
        }

        List<ParseResult> results = parseLines(lines);

        List<Medicine> medicines = results.stream()
                .filter(ParseResult::isValid)
                .map(ParseResult::medicine)
                .toList();

        List<String> errors = results.stream()
                .filter(result -> !result.isValid())
                .map(result -> "Row %d: %s".formatted(result.rowNumber(), result.error()))
                .toList();

        double totalPrice = medicines.stream().mapToDouble(Medicine::getPrice).sum();
        int shortestExpirationPeriod = medicines.stream()
                .mapToInt(Medicine::getExpirationDays)
                .min()
                .orElse(-1);
        long prescriptionCount = medicines.stream()
                .filter(Medicine::requiresPrescription)
                .count();
        double averagePrice = medicines.isEmpty() ? 0 : totalPrice / medicines.size();

        long expirationDateUntilThirtyDaysCount = medicines.stream()
                .filter(medicine -> medicine.getExpirationDays() <= 30).count();
        List<String> names = medicines.stream().map(Medicine::getName).toList();
        Map<MedicineForm, Long> countByForm = medicines.stream()
                .collect(Collectors.groupingBy(Medicine::getForm,
                        () -> new EnumMap<>(MedicineForm.class), Collectors.counting()));
        List<String> topFiveSmallestDaysToExpireNames = medicines.stream()
                .sorted(Comparator.comparingInt(Medicine::getExpirationDays)
                        .thenComparing(Medicine::getName))
                .limit(5)
                .map(Medicine::getName)
                .toList();

        String nameLookup = args.length > 0 && !args[0].isBlank()
                ? findByName(medicines, args[0])
                : "";

        printSummary(averagePrice, shortestExpirationPeriod, prescriptionCount,
                medicines.size(), errors);

        printStreamResults(expirationDateUntilThirtyDaysCount, names, countByForm,
                topFiveSmallestDaysToExpireNames,
                nameLookup);
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

    static List<ParseResult> parseLines(String[] lines) {
        return IntStream.range(0, lines.length).mapToObj(index -> {
            try {
                Medicine medicine = MedicineFactory.fromCsv(lines[index]);
                return new ParseResult(index + 1, medicine, null);
            } catch (RuntimeException exception) {
                return new ParseResult(index + 1, null, exception.getMessage());
            }
        }).toList();
    }

    private static void printSummary(double averagePrice, int shortestExpirationPeriod,
            long prescriptionCount, int totalRows, List<String> errors) {
        System.out.printf(Locale.ROOT, "Average medicine price: %.2f%n", averagePrice);
        System.out.printf(Locale.ROOT,
                "Shortest medicine expiration period: %d%n", shortestExpirationPeriod);
        System.out.printf(Locale.ROOT,
                "Total medicines that had prescription: %d%n", prescriptionCount);
        System.out.printf(Locale.ROOT, "Total correct rows: %d%n", totalRows);
        System.out.printf(Locale.ROOT, "%n%n%nErrors: %d%n", errors.size());
        errors.forEach(System.out::println);
    }

    private static void printStreamResults(long expirationDateUntillThirtyDaysCount, List<String> namesMap,
            Map<MedicineForm, Long> countByForm, List<String> topFiveSmallestDaysToExpireNames, String nameLookup) {
        System.out.printf(Locale.ROOT, "%nMedicines that expire within 30 days: %d%n",
                expirationDateUntillThirtyDaysCount);
        System.out.printf(Locale.ROOT, "Names of all medicines: %s%n", formatList(namesMap));
        System.out.printf(Locale.ROOT,
                "%nTop Five Medicine names with least expiration time: %s%n",
                formatList(topFiveSmallestDaysToExpireNames));

        countByForm.forEach((name, price) -> System.out.println(name + " -> " + price));
        if (!nameLookup.isBlank()) {
            System.out.printf(Locale.ROOT, "%nName lookup result: %s", nameLookup);
        }
    }

    static String formatList(List<String> values) {
        return "{" + String.join(", ", values) + "}";
    }

    record ParseResult(int rowNumber, Medicine medicine, String error) {
        boolean isValid() {
            return medicine != null;
        }
    }

    private static String findByName(List<Medicine> medicines, String name) {
        return medicines.stream()
                .filter(medicine -> medicine.getName().equals(name))
                .map(Medicine::getName)
                .findFirst()
                .orElse("");
    }
}
