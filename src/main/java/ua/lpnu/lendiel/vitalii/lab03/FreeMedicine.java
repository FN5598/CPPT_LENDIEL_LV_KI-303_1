package ua.lpnu.lendiel.vitalii.lab03;

/**
 * Medicine that can be dispensed without a prescription.
 */
public final class FreeMedicine extends Medicine {

    /**
     * Creates a medicine available for free sale.
     *
     * @param name medicine name
     * @param form medicine form
     * @param price medicine price
     * @param daysToExpire number of days until expiration
     */
    public FreeMedicine(String name, String form, double price, int daysToExpire) {
        super(name, form, price, daysToExpire, MedicineKind.FREE_SALE, false);
    }

    /**
     * A free-sale medicine is available when it is not expired; the
     * prescription flag does not affect it.
     *
     * @param prescriptionProvided whether the customer has a valid prescription
     * @return {@code true} when the medicine can be dispensed
     */
    @Override
    public boolean isAvailable(boolean prescriptionProvided) {
        return !isExpired();
    }
}
