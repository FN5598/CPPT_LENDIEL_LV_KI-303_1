package ua.lpnu.lendiel.vitalii.lab03;

import java.util.Locale;
import java.util.Objects;

/**
 * Physical form of a medicine.
 */
public enum MedicineForm {
    /** Tablets or pills. */
    PILLS("Pills"),

    /** Liquid medicine. */
    LIQUID("Liquid");

    private final String csvValue;

    MedicineForm(String csvValue) {
        this.csvValue = csvValue;
    }

    /**
     * Parses the form value used by the CSV file.
     *
     * @param value CSV form value
     * @return matching medicine form
     * @throws NullPointerException if {@code value} is null
     * @throws IllegalArgumentException if the form is unsupported
     */
    public static MedicineForm fromCsvValue(String value) {
        String normalized = Objects.requireNonNull(value, "Medicine form cannot be null")
                .trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "pills" -> PILLS;
            case "liquid" -> LIQUID;
            default -> throw new IllegalArgumentException(
                    "Medicine can only be in 2 forms \"Liquid\" or \"Pills\".");
        };
    }

    /**
     * Returns the form spelling used in CSV rows.
     *
     * @return CSV form value
     */
    public String toCsvValue() {
        return csvValue;
    }
}
