package ua.lpnu.lendiel.vitalii.lab03;

import java.util.Objects;

/**
 * Creates polymorphic medicine objects from the five-column CSV format.
 */
public final class MedicineFactory {

    private MedicineFactory() {
    }

    /**
     * Parses one semicolon-separated medicine row.
     *
     * <p>The expected format is
     * {@code name;form;price;expirationDays;prescription}. The form selects
     * {@link PillsMedicine} or {@link LiquidMedicine}; the boolean value is
     * retained as common medicine data.</p>
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
            MedicineForm form = MedicineForm.fromCsvValue(fields[1]);
            double price = Double.parseDouble(fields[2].trim());
            int expirationDays = Integer.parseInt(fields[3].trim());
            String prescriptionValue = fields[4].trim();
            if (!prescriptionValue.equalsIgnoreCase("true")
                    && !prescriptionValue.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException("Invalid prescription value.");
            }
            boolean prescription = Boolean.parseBoolean(prescriptionValue);

            return switch (form) {
                case PILLS -> new PillsMedicine(name, price, expirationDays, prescription);
                case LIQUID -> new LiquidMedicine(name, price, expirationDays, prescription);
            };
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Numerical field has incorrect format.", exception);
        }
    }
}
