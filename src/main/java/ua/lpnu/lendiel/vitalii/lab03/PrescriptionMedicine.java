package ua.lpnu.lendiel.vitalii.lab03;

/**
 * Medicine that can be dispensed only when a prescription is provided.
 */
public final class PrescriptionMedicine extends Medicine {

    /**
     * Creates a prescription medicine.
     *
     * @param name medicine name
     * @param form medicine form
     * @param price medicine price
     * @param daysToExpire number of days until expiration
     */
    public PrescriptionMedicine(String name, String form, double price, int daysToExpire) {
        super(name, form, price, daysToExpire, MedicineKind.PRESCRIPTION, true);
    }

    /**
     * A prescription medicine is available only when it is not expired and a
     * prescription is provided.
     *
     * @param prescriptionProvided whether the customer has a valid prescription
     * @return {@code true} when the medicine can be dispensed
     */
    @Override
    public boolean isAvailable(boolean prescriptionProvided) {
        return prescriptionProvided && !isExpired();
    }
}
