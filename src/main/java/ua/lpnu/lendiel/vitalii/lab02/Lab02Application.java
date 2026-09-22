package ua.lpnu.lendiel.vitalii.lab02;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


import ua.lpnu.lendiel.vitalii.lab01.Lab01Application;
import ua.lpnu.lendiel.vitalii.VersionInfo;

/**
 * Reads, validates, and summarizes medicine data from a classpath CSV resource.
 *
 * <p>The application prints summary statistics and validation errors to standard
 * output. It does not accept command-line arguments.</p>
 */
public final class Lab02Application {
    private static final String DATA_CSV_PATH = "/lab01/Data.csv";

    /** Prevents instantiation of this utility-style application class. */
    private Lab02Application() {
    }


    /** Immutable validated medicine entity used by the Lab 02 application. */
    public static final class MedicineInformation {
        private final String name;
        private final String form;
        private final double price;
        private final int daysToExpire;
        private final boolean isPrescription;   

        /**
         * Creates validated medicine metadata.
         * 
         * @param name medicine name; it must not be {@code null}
         * @param form medicine form; only {@code Liquid} and {@code Pills} are
         *             accepted, ignoring case
         * @param price medicine price; it must not be negative
         * @param daysToExpire number of days until expiration; it must not be
         *                     negative
         * @param isPrescription whether a prescription is required
         * @throws IllegalArgumentException if the form is unsupported or the
         *                                  price or expiration period is negative
         * @throws NullPointerException if {@code name} is {@code null}
         */
        /**
         * Creates validated medicine metadata.
         *
         * @param name medicine name
         * @param form medicine form
         * @param price non-negative finite price
         * @param daysToExpire non-negative expiration period
         * @param isPrescription whether a prescription is required
         */
        public MedicineInformation(String name, String form, double price, int daysToExpire,
                boolean isPrescription) {
            String validatedName = Objects.requireNonNull(name,
                    "Name cannot be null. Please enter medicine name").trim();
            String validatedForm = Objects.requireNonNull(form,
                    "Form cannot be null. Please enter medicine form").trim();
            if (validatedName.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            if (!validatedForm.equalsIgnoreCase("Liquid")
                    && !validatedForm.equalsIgnoreCase("Pills")) {
                throw new IllegalArgumentException("Medicine can only be in 2 forms \"Liquid\" or \"Pills\". Please use correct form.");
            }

            if (!Double.isFinite(price) || price < 0 || daysToExpire < 0)  {
                throw new IllegalArgumentException("Medicine price or days untill expiration cannot be negative. Please use correct value.");
            }

            this.name = validatedName;
            this.form = validatedForm;
            this.price = price;
            this.daysToExpire = daysToExpire;
            this.isPrescription = isPrescription;
        }

        /**
         * Creates medicine metadata from a semicolon-separated CSV row.
         * 
         * <p>All fields are trimmed. The expected format is
         * {@code name;form;price;daysToExpire;isPrescription}.</p>
         *
         * @param line CSV row to parse; it must not be {@code null}
         * @return validated medicine metadata
         * @throws IllegalArgumentException if the row has the wrong number of
         *                                  fields, contains invalid numeric data,
         *                                  uses an unsupported form, has a
         *                                  negative value, or contains an
         *                                  invalid prescription value
         * @throws NullPointerException if {@code line} is {@code null}
         */
        public static MedicineInformation fromCsv(String line) {
            Objects.requireNonNull(line, "Line cannot be null");

            String[] fields = line.split(";", -1);
            if(fields.length != 5) {
                throw new IllegalArgumentException("Expected 5 colomns");
            }

            try {
                String name = fields[0].trim();
                String form = fields[1].trim();
                double price = Double.parseDouble(fields[2].trim());
                int daysToExpire = Integer.parseInt(fields[3].trim());

                String prescriptionValue = fields[4].trim();
                if(!prescriptionValue.equalsIgnoreCase("true") && !prescriptionValue.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("Invalid prescription value");
                }

                boolean isPrescription = Boolean.parseBoolean(prescriptionValue);

                return new MedicineInformation(name, form, price, daysToExpire, isPrescription);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Numerical field has incorrect format.", e);
            }
        }


        /**
         * Returns the medicine price.
         *
         * @return medicine price
         */
        public double getPrice() {
            return price;
        }

        /**
         * Returns the medicine name.
         *
         * @return medicine name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the validated medicine form.
         *
         * @return medicine form
         */
        public String getForm() {
            return form;
        }

        /**
         * Returns the number of days until the medicine expires.
         *
         * @return expiration period in days
         */
        public int getDaysToExpire() {
            return daysToExpire;
        }

        /**
         * Indicates whether the medicine requires a prescription.
         *
         * @return {@code true} when a prescription is required
         */
        public boolean getIsPrescription() {
            return isPrescription;
        }

        /**
         * Returns the medicine as a semicolon-separated row.
         *
         * @return medicine name, form, price, expiration period, and
         *         prescription flag in CSV-compatible order
         */
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s;%s;%.2f;%d;%s",
                    name, form, price, daysToExpire, isPrescription);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MedicineInformation medicine)) {
                return false;
            }
            return Double.compare(price, medicine.price) == 0
                    && daysToExpire == medicine.daysToExpire
                    && isPrescription == medicine.isPrescription
                    && name.equals(medicine.name)
                    && form.equalsIgnoreCase(medicine.form);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, form.toLowerCase(Locale.ROOT), price,
                    daysToExpire, isPrescription);
        }
    }

    /**
     * Immutable summary of the valid medicine records processed by the
     * application.
     *
     * @param averagePrice average price of valid medicines
     * @param shortestExpirationPeriod shortest expiration period in days, or
     *                                {@link Integer#MAX_VALUE} when no valid
     *                                medicine exists
     * @param prescriptionCount number of valid medicines requiring a prescription
     */
    /**
     * Immutable summary of the valid records processed by the application.
     *
     * @param averagePrice average valid medicine price
     * @param shortestExpirationPeriod shortest valid expiration period
     * @param prescriptionCount number of valid prescription medicines
     */
    public record MedicineInformationSummary(double averagePrice,
            int shortestExpirationPeriod, int prescriptionCount) {
        /**
         * Validates the summary values.
         *
         * @throws IllegalArgumentException if any summary value is negative,
         *                                  except the empty-summary sentinel
         *                                  {@link Integer#MAX_VALUE}
         */
        public MedicineInformationSummary {
            if(!Double.isFinite(averagePrice) || averagePrice < 0
                    || (shortestExpirationPeriod < 0
                    && shortestExpirationPeriod != Integer.MAX_VALUE)
                    || prescriptionCount < 0) {
                throw new IllegalArgumentException("Results cannot be negative values");
            }
        }
    }

    /**
     * Reads a UTF-8 CSV resource from the application classpath.
     *
     * @param path absolute classpath resource path, such as
     *             {@code /lab01/Data.csv}
     * @return an array containing one CSV row per element
     * @throws NoSuchFileException if the resource does not exist
     * @throws IOException if the resource cannot be read
     */
    private static String[] getData(String path) throws IOException {
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

    /**
     * Loads the medicine resource, calculates summary statistics, and prints
     * them together with any row-validation errors.
     *
     * @param args command-line arguments; the application does not require any
     *             arguments
     */
    public static void main(String[] args) {
        if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println(VersionInfo.labVersion("lab02"));
            return;
        }
        String[] lines;

        try {
            lines = getData(DATA_CSV_PATH);
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

        List<MedicineInformation> summaryOutput = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        double totalPrice = 0;
        int shortestExpirationPeriod = Integer.MAX_VALUE;
        int prescriptionCount = 0;

        for (int index = 0; index < lines.length; index++) {
            try {
                MedicineInformation medicineMetadata = MedicineInformation.fromCsv(lines[index]);
                summaryOutput.add(medicineMetadata);
  
                if (medicineMetadata.getDaysToExpire() < shortestExpirationPeriod) {
                    shortestExpirationPeriod = medicineMetadata.getDaysToExpire();
                }

                if (medicineMetadata.getIsPrescription()) {
                    prescriptionCount++;
                }

                totalPrice += medicineMetadata.getPrice();
            } catch (IllegalArgumentException e) {
                errors.add("Row %d: %s".formatted(index + 1, e.getMessage()));
            }
        }

        int totalRows = summaryOutput.size();
        double averagePrice = totalRows == 0 ? 0 : totalPrice / totalRows;

        MedicineInformationSummary summary = new MedicineInformationSummary(averagePrice, shortestExpirationPeriod, prescriptionCount);

        System.out.printf(Locale.ROOT, "Average medicine price: %.2f%n", summary.averagePrice());
        System.out.printf(Locale.ROOT, "Shortest medicine expiration period: %d%n", summary.shortestExpirationPeriod());
        System.out.printf(Locale.ROOT, "Total medicines that had prescription: %d%n", summary.prescriptionCount());
        System.out.printf(Locale.ROOT, "Total correct rows: %d%n", summaryOutput.size());

        System.out.printf(Locale.ROOT, "%n%n%nErrors: %d%n", errors.size());
        errors.forEach(System.out::println);
    }
}
