package ua.lpnu.lendiel.vitalii.lab03;

import java.util.Objects;

/**
 * Creates polymorphic medicine objects from the unchanged Lab 02 CSV format.
 */
public final class MedicineFactory {

    private MedicineFactory() {
    }

    /**
     * Parses one semicolon-separated medicine row.
     *
     * <p>The expected format is
     * {@code name;form;price;daysToExpire;prescription}. The boolean field is
     * mapped to {@link PrescriptionMedicine} or {@link FreeMedicine} without
     * changing the external input format.</p>
     *
     * @param line CSV row to parse
     * @return a validated concrete medicine
     * @throws NullPointerException if {@code line} is {@code null}
     * @throws IllegalArgumentException if the row is malformed or invalid
     */
    public static Medicine fromCsv(String line) {
        Objects.requireNonNull(line, "Line cannot be null");

        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("Expected 5 columns");
        }

        try {
            String name = fields[0].trim();
            String form = fields[1].trim();
            double price = Double.parseDouble(fields[2].trim());
            int daysToExpire = Integer.parseInt(fields[3].trim());
            String prescriptionValue = fields[4].trim();
            if (!prescriptionValue.equalsIgnoreCase("true")
                    && !prescriptionValue.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException("Invalid prescription value.");
            }
            boolean prescription = Boolean.parseBoolean(prescriptionValue);

            if (prescription) {
                return new PrescriptionMedicine(name, form, price, daysToExpire);
            }
            return new FreeMedicine(name, form, price, daysToExpire);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Numerical field has incorrect format.", exception);
        }
    }
}
