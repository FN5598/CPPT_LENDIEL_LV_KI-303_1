package ua.lpnu.lendiel.vitalii.lab03;

import java.util.Locale;

/**
 * Classifies a medicine by its dispensing rules.
 */
public enum MedicineKind {
    /** A medicine that may be dispensed only with a prescription. */
    PRESCRIPTION(true),

    /** A medicine that may be dispensed without a prescription. */
    FREE_SALE(false);

    private final boolean prescription;

    MedicineKind(boolean prescription) {
        this.prescription = prescription;
    }

    /**
     * Indicates whether this kind requires a prescription.
     *
     * @return {@code true} for prescription medicines
     */
    public boolean requiresPrescription() {
        return prescription;
    }

    /**
     * Converts the boolean value used by the existing CSV format to a medicine
     * kind.
     *
     * @param value CSV prescription value
     * @return the corresponding medicine kind
     * @throws IllegalArgumentException if the value is not {@code true} or
     *                                  {@code false}
     */
    public static MedicineKind fromCsvValue(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true" -> PRESCRIPTION;
            case "false" -> FREE_SALE;
            default -> throw new IllegalArgumentException("Invalid prescription value.");
        };
    }

    /**
     * Returns the boolean representation required by the existing CSV format.
     *
     * @return {@code true} or {@code false} as text
     */
    public String toCsvValue() {
        return Boolean.toString(prescription);
    }
}
