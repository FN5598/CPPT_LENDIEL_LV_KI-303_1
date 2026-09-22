package ua.lpnu.lendiel.vitalii.lab05;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import ua.lpnu.lendiel.vitalii.lab03.LiquidMedicine;
import ua.lpnu.lendiel.vitalii.lab03.Medicine;
import ua.lpnu.lendiel.vitalii.lab03.PillsMedicine;

/**
 * Converts between the existing polymorphic model and the flat Lab 5 record.
 */
public final class MedicineRecordMapper {
    /** Stable anchor used to represent the previous model's expiration days. */
    public static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 1, 1);

    private MedicineRecordMapper() {
    }

    /**
     * Converts a medicine using the stable reference date.
     *
     * @param medicine medicine to convert
     * @return flat record
     */
    public static MedicineRecord fromMedicine(Medicine medicine) {
        return fromMedicine(medicine, REFERENCE_DATE);
    }

    /**
     * Converts expiration days to an absolute date relative to a supplied date.
     *
     * @param medicine medicine to convert
     * @param referenceDate date corresponding to zero expiration days
     * @return flat record
     */
    public static MedicineRecord fromMedicine(Medicine medicine, LocalDate referenceDate) {
        Objects.requireNonNull(medicine, "Medicine cannot be null");
        Objects.requireNonNull(referenceDate, "Reference date cannot be null");
        return new MedicineRecord(medicine.getName(), medicine.getForm(), medicine.getPrice(),
                referenceDate.plusDays(medicine.getExpirationDays()),
                medicine.requiresPrescription());
    }

    /**
     * Restores a medicine using the stable reference date.
     *
     * @param record record to convert
     * @return polymorphic medicine
     */
    public static Medicine toMedicine(MedicineRecord record) {
        return toMedicine(record, REFERENCE_DATE);
    }

    /**
     * Converts an absolute expiration date back to expiration days.
     *
     * @param record record to convert
     * @param referenceDate date corresponding to zero expiration days
     * @return polymorphic medicine
     * @throws IllegalArgumentException if the date cannot be represented by the
     *                                  previous non-negative integer field
     */
    public static Medicine toMedicine(MedicineRecord record, LocalDate referenceDate) {
        Objects.requireNonNull(record, "Medicine record cannot be null");
        Objects.requireNonNull(referenceDate, "Reference date cannot be null");
        long expirationDays = ChronoUnit.DAYS.between(referenceDate, record.getExpiresOn());
        if (expirationDays < 0 || expirationDays > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Expiration date is outside the supported range");
        }
        int days = Math.toIntExact(expirationDays);
        return switch (record.getForm()) {
            case PILLS -> new PillsMedicine(record.getName(), record.getPrice(), days,
                    record.requiresPrescription());
            case LIQUID -> new LiquidMedicine(record.getName(), record.getPrice(), days,
                    record.requiresPrescription());
        };
    }
}
