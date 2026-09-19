package ua.lpnu.lendiel.vitalii.lab02;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


import ua.lpnu.lendiel.vitalii.lab01.Lab01Application;

public final class Lab02Application {
    private static final String DATA_CSV_PATH = "/lab01/Data.csv";

    private Lab02Application() {
    }


    private static final class MedicineInformation {
        private final String name;
        private final String form;
        private final double price;
        private final int daysToExpire;
        private final boolean isPrescription;   

        /**
         * Creates correct medicine metadata
         * 
         * @param name - medicine name
         * @param form - medicine form pills/liquid
         * @param price - price of medicine
         * @param daysToExpire - dayss until expiration
         * @param isPrescription - defines if prescriptio n existed when purchasing the medicine (booean) 
         */
        private MedicineInformation(String name, String form, double price, int daysToExpire, boolean isPrescription) {
            if (!form.equalsIgnoreCase("Liquid") && !form.equalsIgnoreCase("Pills")) {
                throw new IllegalArgumentException("Medicine can only be in 2 forms \"Liquid\" or \"Pills\". Please use correct form.");
            }

            if (price < 0 || daysToExpire < 0)  {
                throw new IllegalArgumentException("Medicine price or days untill expiration cannot be negative. Please use correct value.");
            }

            this.name = Objects.requireNonNull(name, "Name cannot be null. Please enter medicine name");
            this.form = form;
            this.price = price;
            this.daysToExpire = daysToExpire;
            this.isPrescription = isPrescription;
        }

        /**
         * Creates record from CSV-row
         * 
         * @param line - line in format name;form;price;daysToExpire;isPrescription
         * @return created record
         * @throws IllegalArgumentException - in case the input data is invalid
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

                if(!fields[4].equalsIgnoreCase("true") && !fields[4].equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException();
                }

                boolean isPrescription = Boolean.parseBoolean(fields[4]);

                return new MedicineInformation(name, form, price, daysToExpire, isPrescription);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Numerical field has incorrect format.", e);
            }
        }


        /** Setters per Medicine metadata arguments */
        public double getPrice() {
            return price;
        }
        public int getDaysToExpire() {
            return daysToExpire;
        }
        public boolean getIsPrescription() {
            return isPrescription;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%.2f: average price%n %d: shortest expiration period%n %d: medicines with prescription");
        }
    }

    private record MedicineInformationSummary(double averagePrice, int shortestExpirationPeriod, int prescriptionCount) {
        /** Verify validity of final metadata summary */
        public MedicineInformationSummary {
            if(averagePrice < 0 || (shortestExpirationPeriod < 0 && shortestExpirationPeriod != Integer.MAX_VALUE)|| prescriptionCount < 0) {
                throw new IllegalArgumentException("Results cannot be negative values");
            }
        }
    }

        /**
     * Reads the laboratory CSV file from the application classpath.
     *
     * @return one CSV row per array element
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
     */
    private static void fail(String message, Exception cause) {
        System.err.println("Fatal error: " + message);
        System.err.println(cause.getMessage());
        System.exit(1);
    }

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
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