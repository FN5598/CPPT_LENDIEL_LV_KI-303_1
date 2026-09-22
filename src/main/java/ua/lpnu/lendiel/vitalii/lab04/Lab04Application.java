package ua.lpnu.lendiel.vitalii.lab04;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ua.lpnu.lendiel.vitalii.VersionInfo;
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
     * @param args optional command-line arguments; the first argument is used
     *             as a medicine-name lookup
     */
    public static void main(String[] args) {
        if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println(VersionInfo.labVersion("lab04"));
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

        List<ParseResult> results = parseLines(lines);

        List<Medicine> medicines = results.stream()
                .filter(ParseResult::isValid)
                .map(ParseResult::medicine)
                .toList();

        List<String> errors = results.stream()
                .filter(result -> !result.isValid())
                .map(result -> "Row %d: %s".formatted(result.rowNumber(), result.error()))
                .toList();

        DoubleSummaryStatistics priceStatistics = summarizePrices(medicines);
        double averagePrice = priceStatistics.getAverage();
        int shortestExpirationPeriod = shortestExpirationPeriod(medicines);
        long prescriptionCount = countPrescriptionMedicines(medicines);

        long expirationDateUntilThirtyDaysCount = countExpiringWithinThirtyDays(medicines);
        List<String> names = mapMedicineNames(medicines);
        Map<MedicineForm, Long> countByForm = countByForm(medicines);
        List<String> topFiveSmallestDaysToExpireNames = topFiveByExpiration(medicines);

        Optional<Medicine> nameLookup = args.length > 0 && !args[0].isBlank()
                ? findByName(medicines, args[0])
                : Optional.empty();

        printSummary(averagePrice, shortestExpirationPeriod, prescriptionCount,
                medicines.size(), errors);

        printStreamResults(expirationDateUntilThirtyDaysCount, names, countByForm,
                topFiveSmallestDaysToExpireNames,
                priceStatistics, nameLookup);
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

    /**
     * Parses all CSV rows while preserving their original row numbers and
     * converting failures into invalid results.
     *
     * @param lines CSV rows to parse
     * @return one parsing result for each input row
     */
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

    /**
     * Counts medicines whose expiration period is at most 30 days.
     *
     * @param medicines medicines to inspect
     * @return number of medicines expiring within 30 days
     */
    static long countExpiringWithinThirtyDays(List<Medicine> medicines) {
        return medicines.stream()
                .filter(medicine -> medicine.getExpirationDays() <= 30)
                .count();
    }

    /**
     * Maps medicines to their names while preserving encounter order.
     *
     * @param medicines medicines to transform
     * @return medicine names in the input order
     */
    static List<String> mapMedicineNames(List<Medicine> medicines) {
        return medicines.stream()
                .map(Medicine::getName)
                .toList();
    }

    /**
     * Groups medicines by physical form and counts each group.
     *
     * @param medicines medicines to group
     * @return counts keyed by medicine form
     */
    static Map<MedicineForm, Long> countByForm(List<Medicine> medicines) {
        return medicines.stream()
                .collect(Collectors.groupingBy(Medicine::getForm,
                        () -> new EnumMap<>(MedicineForm.class), Collectors.counting()));
    }

    /**
     * Calculates summary statistics for medicine prices.
     *
     * @param medicines medicines whose prices should be summarized
     * @return count, sum, minimum, average, and maximum price statistics
     */
    static DoubleSummaryStatistics summarizePrices(List<Medicine> medicines) {
        return medicines.stream()
                .collect(Collectors.summarizingDouble(Medicine::getPrice));
    }

    /**
     * Selects up to five medicines with the shortest expiration periods.
     * Expiration is the primary criterion and name is the tie-breaker.
     *
     * @param medicines medicines to sort and limit
     * @return names of the selected medicines
     */
    static List<String> topFiveByExpiration(List<Medicine> medicines) {
        return medicines.stream()
                .sorted(Comparator.comparingInt(Medicine::getExpirationDays)
                        .thenComparing(Medicine::getName))
                .limit(5)
                .map(Medicine::getName)
                .toList();
    }

    /**
     * Finds the shortest expiration period.
     *
     * @param medicines medicines to inspect
     * @return shortest period, or {@code -1} when the input is empty
     */
    static int shortestExpirationPeriod(List<Medicine> medicines) {
        return medicines.stream()
                .mapToInt(Medicine::getExpirationDays)
                .min()
                .orElse(-1);
    }

    /**
     * Counts medicines that require a prescription.
     *
     * @param medicines medicines to inspect
     * @return number of prescription medicines
     */
    static long countPrescriptionMedicines(List<Medicine> medicines) {
        return medicines.stream()
                .filter(Medicine::requiresPrescription)
                .count();
    }

    /**
     * Prints the common summary values and parsing errors.
     *
     * @param averagePrice average price of valid medicines
     * @param shortestExpirationPeriod shortest expiration period
     * @param prescriptionCount number of prescription medicines
     * @param totalRows number of valid rows
     * @param errors parsing errors
     */
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

    /**
     * Prints the results produced by the Lab 04 stream queries.
     *
     * @param expirationDateUntillThirtyDaysCount count of medicines expiring
     *                                             within 30 days
     * @param namesMap names produced by the mapping query
     * @param countByForm counts produced by the grouping query
     * @param topFiveSmallestDaysToExpireNames names produced by the top-five query
     * @param priceStatistics statistics produced by the price query
     * @param nameLookup optional result of the name lookup
     */
    private static void printStreamResults(long expirationDateUntillThirtyDaysCount, List<String> namesMap,
            Map<MedicineForm, Long> countByForm, List<String> topFiveSmallestDaysToExpireNames,
            DoubleSummaryStatistics priceStatistics, Optional<Medicine> nameLookup) {
        System.out.printf(Locale.ROOT, "%nMedicines that expire within 30 days: %d%n",
                expirationDateUntillThirtyDaysCount);
        System.out.printf(Locale.ROOT,
                "Price statistics: count=%d, sum=%.2f, average=%.2f, min=%.2f, max=%.2f%n",
                priceStatistics.getCount(), priceStatistics.getSum(),
                priceStatistics.getAverage(), priceStatistics.getMin(),
                priceStatistics.getMax());
        System.out.printf(Locale.ROOT, "Names of all medicines: %s%n", formatList(namesMap));
        System.out.printf(Locale.ROOT,
                "%nTop Five Medicine names with least expiration time: %s%n",
                formatList(topFiveSmallestDaysToExpireNames));

        countByForm.forEach((name, price) -> System.out.println(name + " -> " + price));
        nameLookup.map(Medicine::getName)
                .ifPresent(name -> System.out.printf(Locale.ROOT,
                        "%nName lookup result: %s", name));
    }

    /**
     * Formats string values with braces and comma separators.
     *
     * @param values values to format
     * @return formatted list representation
     */
    static String formatList(List<String> values) {
        return "{" + String.join(", ", values) + "}";
    }

    /**
     * Represents either a successfully parsed medicine or a row-level error.
     *
     * @param rowNumber one-based input row number
     * @param medicine parsed medicine, or {@code null} for an invalid row
     * @param error error message, or {@code null} for a valid row
     */
    record ParseResult(int rowNumber, Medicine medicine, String error) {
        /**
         * Checks whether parsing produced a medicine.
         *
         * @return {@code true} when the result is valid
         */
        boolean isValid() {
            return medicine != null;
        }
    }

    /**
     * Finds the first medicine with the requested name.
     *
     * @param medicines medicines to search
     * @param name name to find
     * @return the first matching medicine, or an empty optional when absent
     */
    static Optional<Medicine> findByName(List<Medicine> medicines, String name) {
        return medicines.stream()
                .filter(medicine -> medicine.getName().equals(name))
                .findFirst();
    }
}
