package ua.lpnu.lendiel.vitalii.lab01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ua.lpnu.lendiel.vitalii.VersionInfo;

/**
 * Reads, validates, and summarizes the five-column medicine input format.
 *
 * <p>The command line accepts {@code --input}, {@code --output},
 * {@code --help}, and {@code --version}. A filesystem input is preferred; when
 * the documented default does not exist, the committed classpath fixture is
 * used so the application can still be run directly from the packaged JAR.</p>
 */
public final class Lab01Application {
    private static final String DATA_RESOURCE = "/lab01/Data.csv";
    private static final Path DEFAULT_INPUT = Path.of("data", "input.csv");
    private static final Path DEFAULT_OUTPUT = Path.of("target", "lab01", "report.txt");

    private Lab01Application() {
    }

    /**
     * Runs the report command.
     *
     * @param args command-line options
     */
    public static void main(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.help()) {
                System.out.print(helpText());
                return;
            }
            if (options.version()) {
                System.out.println(VersionInfo.labVersion("lab01"));
                return;
            }

            String report = buildReport(List.of(readLines(readInput(options.input()))));
            Path parent = options.output().toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(options.output(), report, StandardCharsets.UTF_8);
            System.out.print(report);
        } catch (IllegalArgumentException exception) {
            System.err.println("Argument error: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Could not process input or output file: "
                    + exception.getMessage());
        }
    }

    /**
     * Loads the committed classpath fixture used by the earlier labs.
     *
     * @return one physical input line per array element
     * @throws IOException if the fixture cannot be read
     */
    public static String[] getData() throws IOException {
        return readLines(readResource(DATA_RESOURCE));
    }

    /**
     * Validates one semicolon-delimited medicine row.
     *
     * @param line input row
     * @return validated immutable row
     * @throws NullPointerException if {@code line} is null
     * @throws IllegalArgumentException if the row violates the five-column
     *                                  format or a domain invariant
     */
    public static MedicineRow parseRow(String line) {
        Objects.requireNonNull(line, "Input row cannot be null");
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("expected exactly 5 semicolon-separated fields");
        }

        String name = fields[0].trim();
        String form = fields[1].trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (!form.equalsIgnoreCase("Liquid") && !form.equalsIgnoreCase("Pills")) {
            throw new IllegalArgumentException("form must be Liquid or Pills");
        }

        final double price;
        final int expirationDays;
        try {
            price = Double.parseDouble(fields[2].trim());
            expirationDays = Integer.parseInt(fields[3].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("price and expiration must be numeric", exception);
        }
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("price must be finite and non-negative");
        }
        if (expirationDays < 0) {
            throw new IllegalArgumentException("expiration must be non-negative");
        }

        String prescriptionValue = fields[4].trim();
        if (!prescriptionValue.equalsIgnoreCase("true")
                && !prescriptionValue.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("prescription must be true or false");
        }
        return new MedicineRow(name, form, price, expirationDays,
                Boolean.parseBoolean(prescriptionValue));
    }

    /**
     * Builds the deterministic report for supplied physical input lines.
     * Invalid lines are excluded from all statistics and retain their physical
     * one-based line number in the error section.
     *
     * @param lines input lines
     * @return report text using portable LF separators and UTF-8-compatible text
     */
    public static String buildReport(List<String> lines) {
        Objects.requireNonNull(lines, "Input lines cannot be null");
        List<MedicineRow> validRows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            try {
                validRows.add(parseRow(lines.get(index)));
            } catch (RuntimeException exception) {
                String reason = exception.getMessage() == null
                        ? exception.getClass().getSimpleName() : exception.getMessage();
                errors.add("Line %d: %s".formatted(index + 1, reason));
            }
        }

        double totalPrice = validRows.stream().mapToDouble(MedicineRow::price).sum();
        double averagePrice = validRows.isEmpty() ? 0 : totalPrice / validRows.size();
        int shortestExpiration = validRows.stream()
                .mapToInt(MedicineRow::expirationDays)
                .min()
                .orElse(0);
        long prescriptionCount = validRows.stream().filter(MedicineRow::prescription).count();

        StringBuilder report = new StringBuilder();
        report.append("Average price: ")
                .append(String.format(Locale.ROOT, "%.2f", averagePrice)).append('\n');
        report.append("Prescription count: ").append(prescriptionCount).append('\n');
        report.append("Shortest expiration period: ").append(shortestExpiration).append('\n');
        report.append("Total Rows: ").append(validRows.size()).append('\n');
        if (!errors.isEmpty()) {
            report.append("Errors:\n");
            errors.forEach(error -> report.append(error).append('\n'));
        }
        return report.toString();
    }

    private static String readInput(Path path) throws IOException {
        if (Files.exists(path)) {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        if (DEFAULT_INPUT.equals(path)) {
            return readResource(DATA_RESOURCE);
        }
        throw new NoSuchFileException(path.toString());
    }

    private static String readResource(String resource) throws IOException {
        try (var input = Lab01Application.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new NoSuchFileException(resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String[] readLines(String text) {
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.isEmpty()) {
            return new String[0];
        }
        String[] lines = normalized.split("\n", -1);
        if (lines.length > 0 && lines[lines.length - 1].isEmpty()) {
            return java.util.Arrays.copyOf(lines, lines.length - 1);
        }
        return lines;
    }

    private static String helpText() {
        return "Usage: lab01 [--input PATH] [--output PATH] [--help] [--version]\n"
                + "Defaults: data/input.csv (classpath fixture fallback), "
                + "target/lab01/report.txt\n";
    }

    /**
     * Immutable validated row in the Lab 01 input format.
     *
     * @param name medicine name
     * @param form medicine form
     * @param price finite non-negative price
     * @param expirationDays non-negative expiration period
     * @param prescription whether a prescription is required
     */
    public record MedicineRow(String name, String form, double price,
            int expirationDays, boolean prescription) {
    }

    private record Options(Path input, Path output, boolean help, boolean version) {
        private static Options parse(String[] args) {
            Objects.requireNonNull(args, "Arguments cannot be null");
            Path input = DEFAULT_INPUT;
            Path output = DEFAULT_OUTPUT;
            boolean help = false;
            boolean version = false;
            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                switch (argument) {
                    case "--help", "-h" -> help = true;
                    case "--version" -> version = true;
                    case "--input", "-i" -> input = Path.of(nextValue(args, ++index, argument));
                    case "--output", "-o" -> output = Path.of(nextValue(args, ++index, argument));
                    default -> {
                        if (argument.startsWith("--input=")) {
                            input = Path.of(argument.substring("--input=".length()));
                        } else if (argument.startsWith("--output=")) {
                            output = Path.of(argument.substring("--output=".length()));
                        } else {
                            throw new IllegalArgumentException("unknown option: " + argument);
                        }
                    }
                }
            }
            return new Options(input, output, help, version);
        }

        private static String nextValue(String[] args, int index, String option) {
            if (index >= args.length || args[index].isBlank()) {
                throw new IllegalArgumentException(option + " requires a path");
            }
            return args[index];
        }
    }
}
