package ua.lpnu.lendiel.vitalii.lab03;

/**
 * Medicine in pill form.
 */
public final class PillsMedicine extends Medicine {

    /**
     * Creates a pill-form medicine.
     *
     * @param name medicine name
     * @param price medicine price
     * @param expirationDays number of days until expiration
     * @param prescription whether a prescription is required
     */
    public PillsMedicine(String name, double price, int expirationDays,
            boolean prescription) {
        super(name, MedicineForm.PILLS, price, expirationDays, prescription);
    }

    /**
     * Returns the form implemented by this subtype.
     *
     * @return {@link MedicineForm#PILLS}
     */
    @Override
    public MedicineForm getForm() {
        return storedForm();
    }
}
